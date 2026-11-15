package com.erp.brazil.hr.presentation.rest;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.info.BuildProperties;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.time.Instant;
import java.util.Map;

@RestController
@RequestMapping("/api/v1/health")
@Tag(name = "Health", description = "Health check endpoints")
public class HealthController {

    @Value("${spring.application.name:hr-audit-service}")
    private String appName;

    @Value("${app.openapi.version:1.0.0}")
    private String version;

    @GetMapping
    @Operation(summary = "Basic health check")
    public ResponseEntity<Map<String, Object>> health() {
        return ResponseEntity.ok(Map.of(
            "status", "UP",
            "service", appName,
            "version", version,
            "timestamp", Instant.now()
        ));
    }

    @GetMapping("/ready")
    @Operation(summary = "Readiness check")
    public ResponseEntity<Map<String, Object>> ready() {
        return ResponseEntity.ok(Map.of(
            "status", "READY",
            "service", appName,
            "timestamp", Instant.now()
        ));
    }

    @GetMapping("/live")
    @Operation(summary = "Liveness check")
    public ResponseEntity<Map<String, Object>> live() {
        return ResponseEntity.ok(Map.of(
            "status", "ALIVE",
            "service", appName,
            "timestamp", Instant.now()
        ));
    }

    @GetMapping("/info")
    @Operation(summary = "Application info")
    public ResponseEntity<Map<String, Object>> info() {
        return ResponseEntity.ok(Map.of(
            "service", appName,
            "version", version,
            "description", "ERP Brazil - HR, Audit & Notifications Service",
            "timestamp", Instant.now()
        ));
    }
}