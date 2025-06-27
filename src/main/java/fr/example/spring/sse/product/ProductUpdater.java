package fr.example.spring.sse.product;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import fr.example.spring.sse.product.model.Product;
import fr.example.spring.sse.product.repositories.Produit;
import fr.example.spring.sse.product.repositories.ProduitRepository;

/**
 * Service responsable de la mise à jour des quantités de produits.
 */
@Service
public class ProductUpdater {

    private final ProduitRepository produitRepository;

    public ProductUpdater(ProduitRepository produitRepository) {
        this.produitRepository = produitRepository;
    }

    /**
     * Met à jour la quantité d'un produit identifié par son code EAN.
     *
     * @param ean      le code EAN du produit à mettre à jour
     * @param quantity la quantité à ajouter (peut être négative pour diminuer la quantité)
     * @return le produit mis à jour
     * @throws IllegalArgumentException si le produit n'est pas trouvé
     */
    @Transactional
    public Product updateQuantity(String ean, Integer quantity) {

        return produitRepository.findByEan(ean)
                .map(produit -> produit.updateQuantite(quantity))
                .map(this::mapProduitToProduct)
                .orElseThrow(() -> new IllegalArgumentException("Product not found"));
    }

    private Product mapProduitToProduct(Produit produit) {
        return Product.create(
                produit.getEan(),
                produit.getNom(),
                produit.getQuantite(),
                produit.getPrix()
        );
    }
}
