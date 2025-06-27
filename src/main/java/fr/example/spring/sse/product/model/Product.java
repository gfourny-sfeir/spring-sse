package fr.example.spring.sse.product.model;

import java.math.BigDecimal;

import jakarta.annotation.Nonnull;

/**
 * Représentation d'un produit dans le domaine métier.
 * Cette classe est immuable et contient les informations essentielles d'un produit.
 */
public record Product(
        @Nonnull String ean,
        @Nonnull String nom,
        @Nonnull Integer quantite,
        @Nonnull BigDecimal prix
) {
    /**
     * Crée une nouvelle instance de Product avec les valeurs spécifiées.
     *
     * @param ean      le code EAN du produit
     * @param nom      le nom du produit
     * @param quantite la quantité disponible du produit
     * @param prix     le prix du produit
     * @return une nouvelle instance de Product
     */
    public static Product create(String ean, String nom, Integer quantite, BigDecimal prix) {
        return new Product(ean, nom, quantite, prix);
    }
}
