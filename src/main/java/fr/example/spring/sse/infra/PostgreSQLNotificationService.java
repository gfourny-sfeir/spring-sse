package fr.example.spring.sse.infra;

import java.sql.Connection;
import java.sql.SQLException;
import java.util.Arrays;

import org.postgresql.PGConnection;
import org.postgresql.PGNotification;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Component;

import com.zaxxer.hikari.util.DriverDataSource;

import fr.example.spring.sse.config.ApplicationProperties;
import jakarta.annotation.PreDestroy;
import reactor.core.publisher.Sinks;

import static java.util.Objects.isNull;

/**
 * Service responsable de l'écoute des notifications PostgreSQL et de leur livraison aux consommateurs enregistrés.
 * Ce service gère les aspects techniques de la connexion à PostgreSQL et de l'écoute des notifications.
 * Utilise Project Reactor pour publier les notifications sur un sink partagé.
 */
@Component
public class PostgreSQLNotificationService {

    private final Thread listenerThread;
    private final Sinks.Many<PGNotification> pgNotificationSink;
    private final ApplicationProperties applicationProperties;
    private final JdbcTemplate listenerConnection;
    private final DriverDataSource listenerConnectionSource;

    /**
     * Constructeur du service de notification PostgreSQL.
     * Initialise les connexions et démarre le thread d'écoute des notifications.
     *
     * @param pgNotificationSink       le sink pour publier les notifications PostgreSQL
     * @param applicationProperties    les propriétés de l'application contenant la configuration
     * @param listenerConnection       la connexion JDBC utilisée pour l'écoute
     * @param listenerConnectionSource la source de données pour la connexion d'écoute
     */
    public PostgreSQLNotificationService(
            Sinks.Many<PGNotification> pgNotificationSink,
            ApplicationProperties applicationProperties,
            JdbcTemplate listenerConnection,
            DriverDataSource listenerConnectionSource) {

        this.pgNotificationSink = pgNotificationSink;
        this.applicationProperties = applicationProperties;
        this.listenerConnection = listenerConnection;
        this.listenerConnectionSource = listenerConnectionSource;
        this.listenerThread = Thread.ofPlatform()
                .daemon(true)
                .name("listener-postgres")
                .start(publishNotification());
    }

    /**
     * Crée un Runnable qui écoute les notifications PostgreSQL et les publie dans le sink.
     * Cette méthode est exécutée dans un thread dédié pour éviter de bloquer le thread principal.
     *
     * @return un Runnable qui gère l'écoute et la publication des notifications
     */
    private Runnable publishNotification() {
        // Route the notification to the appropriate sink based on the channel name
        // In PostgreSQL, getName() returns the channel name
        return () -> listenerConnection.execute((Connection connection) -> {
            try {

                listenProductChange(connection);

                final var pgconn = connection.unwrap(PGConnection.class);
                while (!Thread.currentThread().isInterrupted()) {
                    routeNotifications(pgconn);
                }
            } catch (SQLException e) {
                // Only throw runtime exception if thread is not being interrupted
                if (!Thread.currentThread().isInterrupted()) {
                    throw new RuntimeException("Error while listening for notifications", e);
                }
            }
            return 0;
        });
    }

    /**
     * Configure la connexion PostgreSQL pour écouter les notifications sur le canal spécifié.
     * Exécute la commande SQL "LISTEN" avec le nom du canal défini dans les propriétés de l'application.
     *
     * @param connection la connexion JDBC à utiliser pour l'écoute
     * @throws RuntimeException si une erreur SQL se produit lors de la configuration de l'écoute
     */
    private void listenProductChange(Connection connection) {
        try {
            connection.createStatement().execute("LISTEN " + applicationProperties.channelToListen());
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }

    /**
     * Récupère les notifications PostgreSQL et les publie dans le sink.
     * Cette méthode est appelée en boucle dans le thread d'écoute pour traiter les notifications entrantes.
     *
     * @param pgconn la connexion PostgreSQL à partir de laquelle récupérer les notifications
     * @throws RuntimeException si une erreur SQL se produit lors de la récupération des notifications
     */
    private void routeNotifications(PGConnection pgconn) {
        try {
            if (!Thread.currentThread().isInterrupted()) {

                PGNotification[] notifications = pgconn.getNotifications(0);
                if (isNull(notifications)) {
                    return;
                }

                Arrays.stream(notifications)
                        .forEach(pgNotificationSink::tryEmitNext);
            }
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }

    /**
     * Nettoie les ressources utilisées par le service lors de l'arrêt de l'application.
     * Ferme la connexion à la base de données et interrompt le thread d'écoute.
     * Cette méthode est appelée automatiquement lors de la destruction du bean Spring.
     *
     * @throws SQLException si une erreur se produit lors de la fermeture de la connexion
     */
    @PreDestroy
    public void cleanup() throws SQLException {
        listenerConnectionSource.getConnection().close();
        listenerThread.interrupt();
    }
}
