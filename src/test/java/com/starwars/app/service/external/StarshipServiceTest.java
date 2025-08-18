package com.starwars.app.service.external;

import com.starwars.app.dto.swapi.StarshipDTO;
import com.starwars.app.dto.swapi.StarshipDetailResponse;
import com.starwars.app.dto.swapi.SwapiResponse;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.core.ParameterizedTypeReference;

import java.util.Arrays;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class StarshipServiceTest {

    @Mock
    private SwapiClientService swapiClient;

    @InjectMocks
    private StarshipService starshipService;

    private SwapiResponse<StarshipDTO> mockStarshipsResponse;
    private StarshipDetailResponse mockStarshipDetailResponse;

    @BeforeEach
    void setUp() {
        StarshipDTO starship1 = new StarshipDTO("1", "Death Star", "https://www.swapi.tech/api/starships/1");
        StarshipDTO starship2 = new StarshipDTO("2", "Millennium Falcon", "https://www.swapi.tech/api/starships/2");

        mockStarshipsResponse = new SwapiResponse<>();
        mockStarshipsResponse.setMessage("ok");
        mockStarshipsResponse.setTotal_records(36);
        mockStarshipsResponse.setTotal_pages(4);
        mockStarshipsResponse.setResults(Arrays.asList(starship1, starship2));

        StarshipDetailResponse.StarshipProperties properties = new StarshipDetailResponse.StarshipProperties();
        properties.setName("Death Star");
        properties.setModel("DS-1 Orbital Battle Station");
        properties.setManufacturer("Imperial Department of Military Research");
        properties.setStarshipClass("Deep Space Mobile Battlestation");

        StarshipDetailResponse.StarshipResult result = new StarshipDetailResponse.StarshipResult();
        result.setProperties(properties);
        result.setUid("1");

        mockStarshipDetailResponse = new StarshipDetailResponse();
        mockStarshipDetailResponse.setMessage("ok");
        mockStarshipDetailResponse.setResult(result);
    }

    @Test
    void shouldGetStarshipsSuccessfully() {
        int page = 1;
        int limit = 10;
        when(swapiClient.getPage(eq("starships"), eq(page), eq(limit), any(ParameterizedTypeReference.class)))
                .thenReturn(mockStarshipsResponse);

        SwapiResponse<StarshipDTO> result = starshipService.getStarships(page, limit);

        assertThat(result).isNotNull();
        assertThat(result.getMessage()).isEqualTo("ok");
        assertThat(result.getResults()).hasSize(2);
        assertThat(result.getResults().get(0).getName()).isEqualTo("Death Star");
        assertThat(result.getResults().get(1).getName()).isEqualTo("Millennium Falcon");

        verify(swapiClient).getPage(eq("starships"), eq(page), eq(limit), any(ParameterizedTypeReference.class));
    }

    @Test
    void shouldGetStarshipByIdSuccessfully() {
        String starshipId = "1";
        when(swapiClient.getById("starships", starshipId, StarshipDetailResponse.class))
                .thenReturn(Optional.of(mockStarshipDetailResponse));

        Optional<StarshipDetailResponse> result = starshipService.getStarshipById(starshipId);

        assertThat(result).isPresent();
        assertThat(result.get().getMessage()).isEqualTo("ok");
        assertThat(result.get().getResult().getProperties().getName()).isEqualTo("Death Star");
        assertThat(result.get().getResult().getProperties().getModel()).isEqualTo("DS-1 Orbital Battle Station");

        verify(swapiClient).getById("starships", starshipId, StarshipDetailResponse.class);
    }

    @Test
    void shouldReturnEmptyWhenStarshipNotFound() {
        String starshipId = "999";
        when(swapiClient.getById("starships", starshipId, StarshipDetailResponse.class))
                .thenReturn(Optional.empty());

        Optional<StarshipDetailResponse> result = starshipService.getStarshipById(starshipId);

        assertThat(result).isEmpty();

        verify(swapiClient).getById("starships", starshipId, StarshipDetailResponse.class);
    }

    @Test
    void shouldHandleEmptyStarshipsResponse() {
        int page = 99;
        int limit = 10;
        SwapiResponse<StarshipDTO> emptyResponse = new SwapiResponse<>();
        emptyResponse.setMessage("ok");
        emptyResponse.setResults(Arrays.asList());

        when(swapiClient.getPage(eq("starships"), eq(page), eq(limit), any(ParameterizedTypeReference.class)))
                .thenReturn(emptyResponse);

        SwapiResponse<StarshipDTO> result = starshipService.getStarships(page, limit);

        assertThat(result).isNotNull();
        assertThat(result.getResults()).isEmpty();

        verify(swapiClient).getPage(eq("starships"), eq(page), eq(limit), any(ParameterizedTypeReference.class));
    }
}