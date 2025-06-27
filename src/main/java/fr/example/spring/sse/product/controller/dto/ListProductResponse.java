package fr.example.spring.sse.product.controller.dto;

import java.util.List;

import fr.example.spring.sse.product.model.Product;

import static java.util.stream.Collectors.collectingAndThen;
import static java.util.stream.Collectors.toUnmodifiableList;

/**
 * Représentation d'une liste de produit pour la réponse de l'API de liste des produits.
 * Cette classe est utilisée pour transformer les données du domaine en format de réponse API.
 */
public record ListProductResponse(
        List<ProductResponse> produits
) {
    public static ListProductResponse mapProductsToListResponse(List<Product> products) {
        return products.stream()
                .map(ProductResponse::mapToProductResponse)
                .collect(collectingAndThen(
                        toUnmodifiableList(),
                        ListProductResponse::new
                ));
    }
}
