package fr.example.spring.sse.product.repositories;

import java.math.BigDecimal;

import org.hibernate.annotations.ColumnDefault;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

/**
 * Entité JPA représentant un produit dans la base de données.
 * Cette classe est utilisée pour la persistance des données de produits.
 */
@Entity
@Table(name = "produit")
public class Produit {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id", nullable = false)
    private Integer id;

    @Column(name = "ean", nullable = false, length = 13)
    private String ean;

    @Column(name = "nom", nullable = false)
    private String nom;

    @ColumnDefault("0")
    @Column(name = "quantite", nullable = false)
    private Integer quantite;

    @Column(name = "prix", nullable = false, precision = 10, scale = 2)
    private BigDecimal prix;

    public Integer getId() {
        return id;
    }

    public void setId(Integer id) {
        this.id = id;
    }

    public String getEan() {
        return ean;
    }

    public void setEan(String ean) {
        this.ean = ean;
    }

    public String getNom() {
        return nom;
    }

    public void setNom(String nom) {
        this.nom = nom;
    }

    public Integer getQuantite() {
        return quantite;
    }

    public void setQuantite(Integer quantite) {
        this.quantite = quantite;
    }

    public BigDecimal getPrix() {
        return prix;
    }

    public void setPrix(BigDecimal prix) {
        this.prix = prix;
    }

    /**
     * Met à jour la quantité du produit en ajoutant la valeur spécifiée.
     * La valeur peut être négative pour diminuer la quantité.
     *
     * @param quantite la quantité à ajouter (ou soustraire si négative)
     * @return l'instance courante pour permettre le chaînage de méthodes
     */
    public Produit updateQuantite(Integer quantite) {
        this.quantite += quantite;
        return this;
    }

}
