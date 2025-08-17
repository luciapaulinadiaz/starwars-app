package com.starwars.app.service.external;

import com.starwars.app.dto.swapi.PersonDTO;
import com.starwars.app.dto.swapi.PersonDetailResponse;
import com.starwars.app.dto.swapi.SwapiResponse;
import lombok.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.stereotype.Service;

import java.util.Optional;

@Service
@RequiredArgsConstructor
public class PeopleService {

    private final SwapiClientService swapiClient;
    private static final Logger logger = LoggerFactory.getLogger(PeopleService.class);

    private static final String ENDPOINT = "people";


    public SwapiResponse<PersonDTO> getPeople(int page, int limit) {
        return swapiClient.getPage(
                ENDPOINT,
                page,
                limit,
                new ParameterizedTypeReference<SwapiResponse<PersonDTO>>() {}
        );
    }

    public Optional<PersonDetailResponse> getPersonById(String id) {
        return swapiClient.getById(ENDPOINT, id, PersonDetailResponse.class);
    }
}