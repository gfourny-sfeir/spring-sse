package fr.example.spring.sse;

import java.util.Properties;

import org.postgresql.PGNotification;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.domain.EntityScan;
import org.springframework.boot.autoconfigure.jdbc.DataSourceProperties;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.boot.web.servlet.support.SpringBootServletInitializer;
import org.springframework.cache.annotation.EnableCaching;
import org.springframework.context.annotation.Bean;
import org.springframework.jdbc.core.JdbcTemplate;

import com.zaxxer.hikari.util.DriverDataSource;

import fr.example.spring.sse.config.ApplicationProperties;
import lombok.Generated;
import reactor.core.publisher.Sinks;

@SpringBootApplication
@EntityScan(basePackageClasses = { SpringSSE.class })
@EnableCaching
@EnableConfigurationProperties(ApplicationProperties.class)
public class SpringSSE extends SpringBootServletInitializer {

    // la méthode main n'a pas besoin d'être couverte par des tests
    // l'annotation lombok @Generated indique à JaCoCo (Java Code Coverage Library) de l'ignorer
    @Generated
    public static void main(String[] args) {
        SpringApplication.run(SpringSSE.class, args);
    }

    /**
     * Bean permettant de broadcaster les notifications PostgreSQL
     *
     * @return {@link Sinks.Many} de {@link PGNotification}
     */
    @Bean
    Sinks.Many<PGNotification> notificationSink() {
        return Sinks.many().multicast().directAllOrNothing();
    }

    /**
     * Configure un driver datasource spécifique pour la connexion écoutant les notifications.</br>
     * Cela évite de garder une connexion ouverte dans le pool de connexion standard HikariCP.
     *
     * @param dataSourceProperties {@link DataSourceProperties}
     * @return {@link DriverDataSource}
     */
    @Bean("listenerConnectionSource")
    DriverDataSource listenerConnectionSource(DataSourceProperties dataSourceProperties) {
        return new DriverDataSource(
                dataSourceProperties.determineUrl(),
                dataSourceProperties.determineDriverClassName(),
                new Properties(),
                dataSourceProperties.determineUsername(),
                dataSourceProperties.determinePassword());
    }

    /**
     * Déclare un {@link JdbcTemplate} spécifique pour la requête de LISTEN.
     *
     * @param listenerConnectionSource {@link DriverDataSource}
     * @return {@link JdbcTemplate}
     */
    @Bean("listenerConnection")
    JdbcTemplate listenerConnection(DriverDataSource listenerConnectionSource) {
        return new JdbcTemplate(listenerConnectionSource);
    }
}
