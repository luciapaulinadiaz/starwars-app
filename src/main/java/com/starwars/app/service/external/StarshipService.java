package com.starwars.app.service.external;

import com.starwars.app.dto.swapi.StarshipDTO;
import com.starwars.app.dto.swapi.StarshipDetailResponse;
import com.starwars.app.dto.swapi.SwapiResponse;
import lombok.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.stereotype.Service;

import java.util.Optional;

@Service
@RequiredArgsConstructor
public class StarshipService {

    private final SwapiClientService swapiClient;
    private static final Logger logger = LoggerFactory.getLogger(StarshipService.class);

    private static final String ENDPOINT = "starships";


    public SwapiResponse<StarshipDTO> getStarships(int page, int limit) {
        return swapiClient.getPage(
                ENDPOINT,
                page,
                limit,
                new ParameterizedTypeReference<SwapiResponse<StarshipDTO>>() {}
        );
    }


    public Optional<StarshipDetailResponse> getStarshipById(String id) {
        return swapiClient.getById(ENDPOINT, id, StarshipDetailResponse.class);
    }

}