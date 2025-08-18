package com.starwars.app.controller;

import com.starwars.app.dto.swapi.FilmDetailResponse;
import com.starwars.app.dto.swapi.FilmsListResponse;
import com.starwars.app.service.external.FilmService;
import lombok.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import javax.validation.constraints.Max;
import javax.validation.constraints.Min;
import javax.validation.constraints.NotBlank;

@RestController
@RequestMapping("/api/films")
@RequiredArgsConstructor
public class FilmController {

    private final FilmService filmService;
    private static final Logger logger = LoggerFactory.getLogger(FilmController.class);


    @GetMapping
    public ResponseEntity<FilmsListResponse> getFilms(
                                                       @RequestParam(defaultValue = "1") @Min(1) Integer page,
                                                       @RequestParam(defaultValue = "10") @Min(1) @Max(100) Integer limit) {

        FilmsListResponse response = filmService.getFilms(page, limit);

        if (response == null || response.getResult() == null || response.getResult().isEmpty()) {
            return ResponseEntity.noContent().build();
        }

        return ResponseEntity.ok(response);
    }

    @GetMapping("/{id}")
    public ResponseEntity<FilmDetailResponse> getFilmById(@PathVariable @NotBlank String id) {
        return filmService.getFilmById(id)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }


    @GetMapping("/health")
    public ResponseEntity<String> health() {
        logger.debug("Health check for films endpoint");
        return ResponseEntity.ok("Films service is running");
    }
}