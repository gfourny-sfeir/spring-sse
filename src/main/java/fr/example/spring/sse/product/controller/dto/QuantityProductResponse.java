package fr.example.spring.sse.product.controller.dto;

import fr.example.spring.sse.product.model.Product;

/**
 * Représentation de la quantité d'un produit pour la réponse de l'API de mise à jour de quantité.
 * Cette classe est utilisée pour transformer les données du domaine en format de réponse API.
 */
public record QuantityProductResponse(String ean, int totalQuantity) {

    public static QuantityProductResponse createFromProduct(Product product) {
        return new QuantityProductResponse(product.ean(), product.quantite());
    }
}
