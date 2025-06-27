package fr.example.spring.sse.product.repositories;

import java.util.Optional;

import org.springframework.data.repository.ListCrudRepository;

/**
 * Référentiel pour l'entité Produit.
 * Fournit des méthodes pour accéder et manipuler les données des produits dans la base de données.
 */
public interface ProduitRepository extends ListCrudRepository<Produit, Integer> {

    /**
     * Trouve un produit par son code EAN.
     *
     * @param ean le code EAN du produit à rechercher
     * @return un Optional contenant le produit s'il existe, ou vide sinon
     */
    Optional<Produit> findByEan(String ean);
}
