package fr.example.spring.sse.product;

import java.math.BigDecimal;

import org.postgresql.PGNotification;
import org.springframework.stereotype.Service;

import com.fasterxml.jackson.databind.ObjectMapper;

import fr.example.spring.sse.product.model.Product;
import io.vavr.control.Try;

@Service
public class ProductNotificationMapper {

    private final ObjectMapper objectMapper;

    ProductNotificationMapper(ObjectMapper objectMapper) {
        this.objectMapper = objectMapper;
    }

    /**
     * Transforme la notification PostgreSQL en un objet Product.
     *
     * @param notification la notification PostgreSQL
     * @return l'objet Product
     */
    Product map(PGNotification notification) {

        final var payload = notification.getParameter();
        final var jsonNode = Try.of(() -> objectMapper.readTree(payload))
                .getOrElseThrow(e -> new RuntimeException("Failed to parse notification payload", e));

        final var ean = jsonNode.get("ean").asText();
        final var nom = jsonNode.get("nom").asText();
        final var quantite = jsonNode.get("quantite").asInt();

        // Note: prix is not included in the notification payload according to schema.sql
        // We're setting it to zero here, but in a real application you might want to
        // fetch the complete product from the database
        final var prix = BigDecimal.ZERO;

        return Product.create(ean, nom, quantite, prix);
    }
}
