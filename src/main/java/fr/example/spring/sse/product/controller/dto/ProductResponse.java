package fr.example.spring.sse.product.controller.dto;

import java.math.BigDecimal;

import fr.example.spring.sse.product.model.Product;

/**
 * Représentation d'un produit pour la réponse de l'API de liste des produits.
 * Cette classe est utilisée pour transformer les données du domaine en format de réponse API.
 */
public record ProductResponse(
        String ean,
        String nom,
        int quantite,
        BigDecimal prix
) {
    static ProductResponse mapToProductResponse(Product product) {
        return new ProductResponse(product.ean(), product.nom(), product.quantite(), product.prix());
    }
}
