package fr.example.spring.sse.product.controller.dto;

/**
 * Représentation d'une demande de mise à jour de quantité de produit.
 * Cette classe est utilisée pour recevoir les données de l'API pour la mise à jour de la quantité d'un produit.
 */
public record UpdateQuantityProduct(int quantity) {
}
