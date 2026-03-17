package com.codapt.quizapp.controller;

import io.swagger.v3.oas.annotations.OpenAPIDefinition;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.annotation.PreDestroy;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.health.actuate.endpoint.HealthEndpoint;
import org.springframework.boot.health.contributor.Status;
import org.springframework.boot.info.BuildProperties;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;
import java.util.Objects;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.atomic.AtomicReference;


/**
 * HealthController exposes application liveness/readiness and build metadata for monitoring and diagnostics.
 */
@RestController
@OpenAPIDefinition(tags = { @Tag(name = "Health Controller", description = "Exposes application liveness/readiness and build metadata for monitoring and diagnostics.") })
public class HealthController {

    private static final Logger logger = LoggerFactory.getLogger(HealthController.class);
    private static final String UNKNOWN = "unknown";

    private final HealthEndpoint healthEndpoint;
    private final BuildProperties buildProperties;
    // We keep the scheduler as a field so we can shut it down in the future if needed.
    private final ScheduledExecutorService scheduledExecutorService = Executors.newSingleThreadScheduledExecutor();
    // Use AtomicReference to make updates to the status thread-safe.
    private final AtomicReference<Status> healthStatus = new AtomicReference<>(Status.UNKNOWN);

    public HealthController(HealthEndpoint healthEndpoint, BuildProperties buildProperties) {
        this.healthEndpoint = healthEndpoint;
        this.buildProperties = buildProperties;
        scheduledExecutorService.scheduleAtFixedRate(this::refreshHealth, 0, 30, java.util.concurrent.TimeUnit.SECONDS);
    }

    @PreDestroy
    public void shutdown() {
        scheduledExecutorService.shutdownNow();
    }

    /**
     * GET /health
     *
     * Returns a concise JSON object containing:
     * - component: the application name (from build properties)
     * - status: cached overall health status (UP/DOWN/UNKNOWN) retrieved periodically from Spring Boot Actuator
     * - version: application version (from build properties)
     * - buildTime: build timestamp
     * - buildBy: build group/organization
     * - buildUrl: artifact id (kept for compatibility)
     * - buildNumber: repeated version field included for consumers expecting this key
     *
     * Notes:
     * - The controller maintains a cached health status refreshed every 30 seconds to avoid calling the health
     *   endpoint on every request (useful under load and for faster responses).
     * - The health value is a Spring Boot Actuator Status object; when serialized it appears as a string like "UP" or "DOWN".
     *
     * Responses:
     * - 200 OK: JSON object described above.
     */
    @GetMapping({"/health", "/health/"})
    @Operation(summary = "Get Health Status",
            description = "Returns a small JSON object with the application's current health and build metadata.",
            tags = "Health Controller"
    )
    public Map<String, Object> getHealth() {
        return Map.of(
                "component", Objects.toString(buildProperties.getName(), UNKNOWN),
                "status", healthStatus.get(),
                "version", Objects.toString(buildProperties.getVersion(), UNKNOWN),
                "buildTime", Objects.toString(buildProperties.getTime(), UNKNOWN),
                "buildBy", Objects.toString(buildProperties.getGroup(), UNKNOWN),
                "buildUrl", Objects.toString(buildProperties.getArtifact(), UNKNOWN),
                "buildNumber", Objects.toString(buildProperties.getVersion(), UNKNOWN)
        );
    }

    @GetMapping("/favicon.ico")
    public ResponseEntity<Void> favicon() {
        return ResponseEntity.status(HttpStatus.NO_CONTENT).build();
    }

    /**
     * Refresh the cached health status from the Actuator HealthEndpoint.
     * This method is scheduled to run periodically by the constructor. Any exception while querying
     * the endpoint will set the status to UNKNOWN so consumers can treat it as a degraded state.
     */
    public void refreshHealth() {
        try {
            healthStatus.set(healthEndpoint.health().getStatus());
        } catch (Exception e) {
            logger.warn("Unable to refresh actuator health status, using UNKNOWN", e);
            healthStatus.set(Status.UNKNOWN);
        }
    }
}
