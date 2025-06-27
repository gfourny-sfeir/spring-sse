package fr.example.spring.sse.product.spi;

import java.util.function.Function;

import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

import reactor.core.publisher.Flux;

public interface EmitProduct {

    /**
     * Crée un émetteur SSE qui recevra des événements du flux fourni.
     *
     * @param <T>        le type de données qui sera envoyé comme événements
     * @param dataFlux   un flux qui émettra les données à envoyer comme événements
     * @param eventName  le nom des événements SSE
     * @param dataMapper une fonction qui transforme les données au format attendu par le client
     * @return un SseEmitter configuré pour envoyer des événements
     */
    <T, R> SseEmitter createEmitterFromFlux(
            Flux<T> dataFlux,
            String eventName,
            Function<T, R> dataMapper);

    /**
     * Crée un émetteur SSE qui recevra des événements du flux fourni.
     *
     * @param <T>        le type de données qui sera envoyé comme événements
     * @param dataFlux   un flux qui émettra les données à envoyer comme événements
     * @param eventName  le nom des événements SSE
     * @param dataMapper une fonction qui transforme les données au format attendu par le client
     * @param timeout    le délai d'expiration de la connexion SSE en millisecondes
     * @return un SseEmitter configuré pour envoyer des événements
     */
    <T, R> SseEmitter createEmitterFromFlux(
            Flux<T> dataFlux,
            String eventName,
            Function<T, R> dataMapper,
            long timeout);
}
