package com.starwars.app.controller;

import com.starwars.app.dto.swapi.StarshipDTO;
import com.starwars.app.dto.swapi.StarshipDetailResponse;
import com.starwars.app.dto.swapi.SwapiResponse;
import com.starwars.app.service.external.StarshipService;
import lombok.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import javax.validation.constraints.Max;
import javax.validation.constraints.Min;
import javax.validation.constraints.NotBlank;

@RestController
@RequestMapping("/api/starships")
@RequiredArgsConstructor
public class StarshipController {

    private final StarshipService starshipService;
    private static final Logger logger = LoggerFactory.getLogger(StarshipController.class);

    /**
     * Obtiene lista paginada de starships
     * GET /api/starships?page=1&limit=10
     */
    @GetMapping
    public ResponseEntity<SwapiResponse<StarshipDTO>> getStarships(
            @RequestParam(defaultValue = "1") @Min(1) Integer page,
            @RequestParam(defaultValue = "10") @Min(1) @Max(100) Integer limit) {

        logger.info("GET /api/starships - page: {}, limit: {}", page, limit);

        SwapiResponse<StarshipDTO> response = starshipService.getStarships(page, limit);

        if (response == null || response.getResults() == null || response.getResults().isEmpty()) {
            logger.warn("No starships found for page: {}", page);
            return ResponseEntity.noContent().build();
        }

        logger.info("Successfully returned {} starships", response.getResults().size());
        return ResponseEntity.ok(response);
    }

    /**
     * Obtiene detalles de una starship por ID
     * GET /api/starships/{id}
     */
    @GetMapping("/{id}")
    public ResponseEntity<StarshipDetailResponse.StarshipProperties> getStarshipById(@PathVariable @NotBlank String id) {
        logger.info("GET /api/starships/{}", id);

        return starshipService.getStarshipById(id)
                .map(response -> response.getResult().getProperties())
                .map(properties -> {
                    logger.info("Successfully found starship: {}", id);
                    return ResponseEntity.ok(properties);
                })
                .orElseGet(() -> {
                    logger.warn("Starship not found with ID: {}", id);
                    return ResponseEntity.notFound().build();
                });
    }

    /**
     * Health check para el endpoint de starships
     * GET /api/starships/health
     */
    @GetMapping("/health")
    public ResponseEntity<String> health() {
        logger.debug("Health check for starships endpoint");
        return ResponseEntity.ok("Starships service is running");
    }
}