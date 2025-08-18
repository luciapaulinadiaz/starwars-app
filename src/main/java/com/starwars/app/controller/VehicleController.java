package com.starwars.app.controller;

import com.starwars.app.dto.swapi.*;
import com.starwars.app.service.external.VehicleService;
import lombok.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import javax.validation.constraints.Max;
import javax.validation.constraints.Min;
import javax.validation.constraints.NotBlank;

@RestController
@RequestMapping("/api/vehicles")
@RequiredArgsConstructor
public class VehicleController {

    private final VehicleService vehicleService;
    private static final Logger logger = LoggerFactory.getLogger(VehicleController.class);

    /**
     * Obtiene lista paginada de vehiculos
     * GET /api/vehicles?page=1&limit=10
     */
    @GetMapping
    public ResponseEntity<SwapiResponse<VehicleDTO>> getVehicles(
            @RequestParam(defaultValue = "1") @Min(1) Integer page,
            @RequestParam(defaultValue = "10") @Min(1) @Max(100) Integer limit) {

        logger.info("GET /api/vehicles - page: {}, limit: {}", page, limit);

        SwapiResponse<VehicleDTO> response = vehicleService.getVehicles(page, limit);

        if (response == null || response.getResults() == null || response.getResults().isEmpty()) {
            logger.warn("No vehicles found for page: {}", page);
            return ResponseEntity.noContent().build();
        }

        logger.info("Successfully returned {} vehicles", response.getResults().size());
        return ResponseEntity.ok(response);
    }

    /**
     * Obtiene detalles de un vehiculo por ID
     * GET /api/vehicles/{id}
     */
    @GetMapping("/{id}")
    public ResponseEntity<VehicleDetailResponse.VehicleProperties> getVehicleById(@PathVariable @NotBlank String id) {
        logger.info("GET /api/vehicles/{}", id);

        return vehicleService.getVehicleById(id)
                .map(response -> response.getResult().getProperties())
                .map(properties -> {
                    logger.info("Successfully found vehicle: {}", id);
                    return ResponseEntity.ok(properties);
                })
                .orElseGet(() -> {
                    logger.warn("Vehicle not found with ID: {}", id);
                    return ResponseEntity.notFound().build();
                });
    }

    /**
     * Health check para el endpoint de vehiculos
     * GET /api/vehicles/health
     */
    @GetMapping("/health")
    public ResponseEntity<String> health() {
        logger.debug("Health check for vehicles endpoint");
        return ResponseEntity.ok("vehicles service is running");
    }
}