package fr.example.spring.sse.config;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.validation.annotation.Validated;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;

@Validated
@ConfigurationProperties(prefix = "app")
public record ApplicationProperties(
        /**
         * Nom de la notification PostgreSQL.
         */
        @NotBlank String channelToListen,

        /**
         * Interval d'émission d'un heartbeat permettant de garder la connexion ouverte en seconde.
         */
        @Min(1) @Max(59) int heartbeatIntervalSeconds
) {
}
