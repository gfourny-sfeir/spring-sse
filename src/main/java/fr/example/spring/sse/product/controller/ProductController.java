package fr.example.spring.sse.product.controller;

import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

import fr.example.spring.sse.product.ProductListener;
import fr.example.spring.sse.product.ProductLister;
import fr.example.spring.sse.product.ProductUpdater;
import fr.example.spring.sse.product.controller.dto.ListProductResponse;
import fr.example.spring.sse.product.controller.dto.QuantityProductResponse;
import fr.example.spring.sse.product.controller.dto.UpdateQuantityProduct;
import fr.example.spring.sse.product.spi.EmitProduct;
import io.vavr.Function0;
import io.vavr.Function2;

/**
 * Contrôleur REST pour la gestion des produits.
 * Fournit des endpoints pour lister les produits, mettre à jour les quantités et écouter les changements de quantité.
 */
@CrossOrigin(origins = "http://localhost:8081")
@RestController
@RequestMapping("/api/v1/products")
class ProductController {

    private final ProductUpdater productUpdater;
    private final ProductLister productLister;
    private final ProductListener productListener;
    private final EmitProduct emitProduct;

    ProductController(ProductUpdater productUpdater, ProductLister productLister, ProductListener productListener, EmitProduct emitProduct) {
        this.productUpdater = productUpdater;
        this.productLister = productLister;
        this.productListener = productListener;
        this.emitProduct = emitProduct;
    }

    /**
     * Liste tous les produits disponibles.
     *
     * @return une réponse HTTP contenant la liste des produits
     */
    @GetMapping
    ResponseEntity<ListProductResponse> listProducts() {

        return Function0.of(productLister::list)
                .andThen(ListProductResponse::mapProductsToListResponse)
                .andThen(ResponseEntity::ok)
                .apply();
    }

    /**
     * Met à jour la quantité d'un produit identifié par son code EAN.
     *
     * @param updateQuantityProduct l'objet contenant la quantité à ajouter
     * @param ean                   le code EAN du produit à mettre à jour
     * @return une réponse HTTP contenant les informations du produit mis à jour
     */
    @PutMapping("/{ean}")
    ResponseEntity<QuantityProductResponse> updateProduct(@RequestBody UpdateQuantityProduct updateQuantityProduct, @PathVariable String ean) {

        return Function2.of(productUpdater::updateQuantity)
                .andThen(QuantityProductResponse::createFromProduct)
                .andThen(ResponseEntity::ok)
                .apply(ean, updateQuantityProduct.quantity());
    }

    /**
     * Crée un émetteur SSE pour écouter les changements de quantité de produits.
     * Cette méthode permet aux clients de s'abonner aux notifications en temps réel
     * lorsque la quantité d'un produit est modifiée.
     *
     * @return un émetteur SSE configuré pour envoyer des notifications de changement de quantité
     */
    @GetMapping(value = "/listen-product-quantity-updated", produces = MediaType.TEXT_EVENT_STREAM_VALUE)
    public SseEmitter listenForProductQuantityChanges() {
        return emitProduct.createEmitterFromFlux(
                // Flux de données : s'abonner au flux de mises à jour de produits du ProductListener
                productListener.getProductUpdates(),
                // Nom de l'événement
                "product-quantity-updated",
                // Transformateur de données : convertir Product en QuantityProductResponse
                product -> new QuantityProductResponse(product.ean(), product.quantite())
        );
    }
}
