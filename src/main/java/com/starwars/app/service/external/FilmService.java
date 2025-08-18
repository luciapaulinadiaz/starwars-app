package com.starwars.app.service.external;

import com.starwars.app.dto.swapi.FilmDetailResponse;
import com.starwars.app.dto.swapi.FilmsListResponse;
import lombok.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.util.Optional;

@Service
@RequiredArgsConstructor
public class FilmService {

    private final SwapiClientService swapiClient;
    private static final Logger logger = LoggerFactory.getLogger(FilmService.class);
    private static final String ENDPOINT = "films";

    public FilmsListResponse getFilms(int page, int limit) {
        String url = "films?page=" + page + "&limit=" + limit;
        return swapiClient.getById("", url, FilmsListResponse.class)
                .orElse(new FilmsListResponse());
    }

    public Optional<FilmDetailResponse> getFilmById(String id) {
        return swapiClient.getById(ENDPOINT, id, FilmDetailResponse.class);
    }
}