package com.starwars.app.controller;

import com.starwars.app.dto.swapi.PersonDTO;
import com.starwars.app.dto.swapi.PersonDetailResponse;
import com.starwars.app.dto.swapi.SwapiResponse;
import com.starwars.app.service.external.PeopleService;
import lombok.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import javax.validation.constraints.Max;
import javax.validation.constraints.Min;

@RestController
@RequestMapping("/api/people")
@RequiredArgsConstructor
public class PeopleController {

    private final PeopleService peopleService;
    private static final Logger logger = LoggerFactory.getLogger(PeopleController.class);


    /**
     * Obtiene lista paginada de personas
     * GET /api/people?page=1&size=10
     */
    @GetMapping
    public ResponseEntity<SwapiResponse<PersonDTO>> getPeople(
            @RequestParam(defaultValue = "1") @Min(1) Integer page,
            @RequestParam(defaultValue = "10") @Min(1) @Max(100) Integer limit) {

        logger.info("GET /api/people - page: {}, size: {}", page, limit);

        SwapiResponse<PersonDTO> response = peopleService.getPeople(page, limit);

        if (response == null || response.getResults() == null || response.getResults().isEmpty()) {
            logger.warn("No people found for page: {}", page);
            return ResponseEntity.noContent().build();
        }

        logger.info("Successfully returned {} people", response.getResults().size());
        return ResponseEntity.ok(response);
    }


    /**
     * Obtiene detalles de un persona por ID
     * GET /api/people/{id}
     */
    @GetMapping("/{id}")
    public ResponseEntity<PersonDetailResponse.PersonProperties> getPersonById(@PathVariable String id) {
        return peopleService.getPersonById(id)
                .map(response -> response.getResult().getProperties())
                .map(properties -> {
                    logger.info("Successfully found person: {}", id);
                    return ResponseEntity.ok(properties);
                })
                .orElseGet(() -> {
                    logger.warn("Person not found with ID: {}", id);
                    return ResponseEntity.notFound().build();
                });
    }


    /**
     * Health check para el endpoint de personas
     * GET /api/people/health
     */
    @GetMapping("/health")
    public ResponseEntity<String> health() {
        logger.debug("Health check for people endpoint");
        return ResponseEntity.ok("People service is running");
    }
}