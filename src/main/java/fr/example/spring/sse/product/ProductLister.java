package fr.example.spring.sse.product;

import java.util.List;

import org.springframework.stereotype.Service;

import fr.example.spring.sse.product.model.Product;
import fr.example.spring.sse.product.repositories.Produit;
import fr.example.spring.sse.product.repositories.ProduitRepository;

/**
 * Service responsable de la récupération des produits depuis le référentiel.
 */
@Service
public class ProductLister {

    private final ProduitRepository produitRepository;

    public ProductLister(ProduitRepository produitRepository) {
        this.produitRepository = produitRepository;
    }

    /**
     * Récupère la liste de tous les produits.
     *
     * @return une liste d'objets Product
     */
    public List<Product> list() {
        return produitRepository.findAll().stream()
                .map(this::mapToProduct)
                .toList();
    }

    /**
     * Convertit un objet Produit en objet Product.
     *
     * @param produit l'objet Produit à convertir
     * @return l'objet Product correspondant
     */
    private Product mapToProduct(Produit produit) {
        return Product.create(produit.getEan(), produit.getNom(), produit.getQuantite(), produit.getPrix());
    }
}
