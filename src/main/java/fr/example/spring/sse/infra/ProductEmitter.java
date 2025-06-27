package fr.example.spring.sse.infra;

import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.function.Function;

import org.postgresql.PGNotification;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

import fr.example.spring.sse.config.ApplicationProperties;
import fr.example.spring.sse.product.spi.EmitProduct;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Sinks;
import reactor.core.scheduler.Schedulers;

/**
 * Service responsable de la gestion des événements envoyés par le serveur (SSE).
 * Ce service abstrait les détails techniques de création et de gestion des émetteurs SSE.
 */
@Component
public class ProductEmitter implements EmitProduct {

    private static final long DEFAULT_TIMEOUT = Long.MAX_VALUE;
    private static final Logger log = LoggerFactory.getLogger(ProductEmitter.class);
    private static final String HEARTBEAT_EVENT_NAME = "heartbeat";

    // Track active executors for cleanup
    private final Set<ExecutorService> activeExecutors = ConcurrentHashMap.newKeySet();
    private final Sinks.Many<PGNotification> pgNotificationSink;
    private final ApplicationProperties applicationProperties;

    public ProductEmitter(Sinks.Many<PGNotification> pgNotificationSink, ApplicationProperties applicationProperties) {
        this.pgNotificationSink = pgNotificationSink;
        this.applicationProperties = applicationProperties;
    }

    /**
     * Crée un émetteur SSE qui recevra des événements du flux fourni.
     *
     * @param <T>        le type de données qui sera envoyé comme événements
     * @param dataFlux   un flux qui émettra les données à envoyer comme événements
     * @param eventName  le nom des événements SSE
     * @param dataMapper une fonction qui transforme les données au format attendu par le client
     * @return un SseEmitter configuré pour envoyer des événements
     */
    @Override
    public <T, R> SseEmitter createEmitterFromFlux(
            Flux<T> dataFlux,
            String eventName,
            Function<T, R> dataMapper) {

        return createEmitterFromFlux(dataFlux, eventName, dataMapper, DEFAULT_TIMEOUT);
    }

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
    @Override
    public <T, R> SseEmitter createEmitterFromFlux(
            Flux<T> dataFlux,
            String eventName,
            Function<T, R> dataMapper,
            long timeout) {

        final var emitter = new SseEmitter(timeout);
        log.info("SSE connection opened {}", emitter);
        final var heartbeatExecutor = Executors.newSingleThreadScheduledExecutor();
        activeExecutors.add(heartbeatExecutor);

        // Set up completion callbacks
        emitter.onCompletion(() -> {
            log.info("SSE connection closed {}", emitter);
            heartbeatExecutor.shutdown();
            activeExecutors.remove(heartbeatExecutor);
        });
        emitter.onTimeout(() -> {
            pgNotificationSink.tryEmitComplete();
            log.info("SSE connection timed out");
            heartbeatExecutor.shutdown();
            activeExecutors.remove(heartbeatExecutor);
        });
        emitter.onError(ex -> {
            pgNotificationSink.tryEmitError(ex);
            heartbeatExecutor.shutdown();
            activeExecutors.remove(heartbeatExecutor);
        });

        // Schedule heartbeat events every HEARTBEAT_INTERVAL seconds
        AtomicInteger heartbeatCounter = new AtomicInteger(0);
        heartbeatExecutor.scheduleAtFixedRate(() -> {
            try {
                SseEmitter.SseEventBuilder event = SseEmitter.event()
                        .data("")
                        .id("heartbeat-" + heartbeatCounter.incrementAndGet())
                        .name(HEARTBEAT_EVENT_NAME);
                emitter.send(event);
                log.debug("Sent heartbeat event");
            } catch (Exception ex) {
                emitter.completeWithError(ex);
            }
        }, 0, applicationProperties.heartbeatIntervalSeconds(), TimeUnit.SECONDS);

        // Subscribe to the flux
        AtomicInteger eventId = new AtomicInteger(0);
        dataFlux.publishOn(Schedulers.boundedElastic())
                .subscribe(
                        data -> {
                            try {
                                // Map the data to the format expected by the client
                                R mappedData = dataMapper.apply(data);

                                // Create and send an SSE event
                                SseEmitter.SseEventBuilder event = SseEmitter.event()
                                        .data(mappedData)
                                        .id(String.valueOf(eventId.getAndIncrement()))
                                        .name(eventName);

                                emitter.send(event);
                            } catch (Exception ex) {
                                emitter.completeWithError(ex);
                            }
                        },
                        emitter::completeWithError,
                        emitter::complete
                );

        return emitter;
    }
}
