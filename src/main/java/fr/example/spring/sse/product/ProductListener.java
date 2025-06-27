package fr.example.spring.sse.product;

import org.postgresql.PGNotification;
import org.springframework.stereotype.Service;

import fr.example.spring.sse.product.model.Product;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Sinks;

/**
 * Service responsable de l'écoute des notifications de changement de quantité de produits et
 * de leur conversion en objets Product.
 */
@Service
public class ProductListener {

    private final Sinks.Many<PGNotification> pgNotificationSink;
    private final ProductNotificationMapper productNotificationMapper;

    public ProductListener(Sinks.Many<PGNotification> pgNotificationSink, ProductNotificationMapper productNotificationMapper) {
        this.pgNotificationSink = pgNotificationSink;
        this.productNotificationMapper = productNotificationMapper;
    }

    /**
     * Retourne un Flux de produits mis à jour à partir des notifications PostgreSQL.
     * Cette méthode démarre l'écouteur de notification s'il n'est pas déjà en cours d'exécution.
     *
     * @return un Flux de produits mis à jour
     */
    public Flux<Product> getProductUpdates() {
        return pgNotificationSink.asFlux()
                .map(productNotificationMapper::map);
    }
}
