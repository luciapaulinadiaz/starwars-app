package com.starwars.app.service.external;

import com.starwars.app.dto.swapi.PersonDTO;
import com.starwars.app.dto.swapi.PersonDetailResponse;
import com.starwars.app.dto.swapi.SwapiResponse;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import org.springframework.test.util.ReflectionTestUtils;
import org.springframework.http.HttpStatus;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.RestClientException;
import org.springframework.web.client.RestTemplate;

import java.util.Arrays;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class SwapiClientServiceTest {

    @Mock
    private RestTemplate restTemplate;

    @InjectMocks
    private SwapiClientService swapiClientService;

    private final String baseUrl = "https://www.swapi.tech/api";

    @BeforeEach
    void setUp() {
        ReflectionTestUtils.setField(swapiClientService, "baseUrl", baseUrl);
    }

    @Test
    void shouldGetByIdSuccessfully() {
        String endpoint = "people";
        String id = "1";
        PersonDetailResponse mockResponse = new PersonDetailResponse();
        mockResponse.setMessage("ok");

        PersonDetailResponse.PersonResult result = new PersonDetailResponse.PersonResult();
        PersonDetailResponse.PersonProperties properties = new PersonDetailResponse.PersonProperties();
        properties.setName("Luke Skywalker");
        result.setProperties(properties);
        result.setUid("1");

        mockResponse.setResult(result);

        String expectedUrl = baseUrl + "/" + endpoint + "/" + id;

        when(restTemplate.getForEntity(expectedUrl, PersonDetailResponse.class))
                .thenReturn(ResponseEntity.ok(mockResponse));

        Optional<PersonDetailResponse> response = swapiClientService.getById(endpoint, id, PersonDetailResponse.class);

        assertThat(response).isPresent();
        assertThat(response.get().getMessage()).isEqualTo("ok");
        assertThat(response.get().getResult().getProperties().getName()).isEqualTo("Luke Skywalker");

        verify(restTemplate).getForEntity(expectedUrl, PersonDetailResponse.class);
    }

    @Test
    void shouldReturnEmptyWhenGetByIdNotFound() {
        String endpoint = "people";
        String id = "999";
        String expectedUrl = baseUrl + "/" + endpoint + "/" + id;

        when(restTemplate.getForEntity(expectedUrl, PersonDetailResponse.class))
                .thenThrow(new HttpClientErrorException(HttpStatus.NOT_FOUND));

        Optional<PersonDetailResponse> response = swapiClientService.getById(endpoint, id, PersonDetailResponse.class);

        assertThat(response).isEmpty();

        verify(restTemplate).getForEntity(expectedUrl, PersonDetailResponse.class);
    }

    @Test
    void shouldReturnEmptyWhenGetByIdRestClientException() {
        String endpoint = "people";
        String id = "1";
        String expectedUrl = baseUrl + "/" + endpoint + "/" + id;

        when(restTemplate.getForEntity(expectedUrl, PersonDetailResponse.class))
                .thenThrow(new RestClientException("Connection failed"));

        Optional<PersonDetailResponse> response = swapiClientService.getById(endpoint, id, PersonDetailResponse.class);

        assertThat(response).isEmpty();

        verify(restTemplate).getForEntity(expectedUrl, PersonDetailResponse.class);
    }


    @Test
    void shouldGetPageSuccessfully() {
        String endpoint = "people";
        int page = 1;
        int limit = 10;

        PersonDTO person1 = new PersonDTO("1", "Luke Skywalker", "https://www.swapi.tech/api/people/1");
        PersonDTO person2 = new PersonDTO("2", "C-3PO", "https://www.swapi.tech/api/people/2");

        SwapiResponse<PersonDTO> mockResponse = new SwapiResponse<>();
        mockResponse.setMessage("ok");
        mockResponse.setTotal_records(82);
        mockResponse.setTotal_pages(9);
        mockResponse.setResults(Arrays.asList(person1, person2));

        String expectedUrl = baseUrl + "/" + endpoint + "?page=" + page + "&limit=" + limit;

        when(restTemplate.exchange(
                eq(expectedUrl),
                eq(HttpMethod.GET),
                eq(null),
                any(ParameterizedTypeReference.class)
        )).thenReturn(ResponseEntity.ok(mockResponse));

        SwapiResponse<PersonDTO> response = swapiClientService.getPage(
                endpoint,
                page,
                limit,
                new ParameterizedTypeReference<SwapiResponse<PersonDTO>>() {}
        );

        assertThat(response).isNotNull();
        assertThat(response.getMessage()).isEqualTo("ok");
        assertThat(response.getResults()).hasSize(2);
        assertThat(response.getResults().get(0).getName()).isEqualTo("Luke Skywalker");
        assertThat(response.getResults().get(1).getName()).isEqualTo("C-3PO");

        verify(restTemplate).exchange(
                eq(expectedUrl),
                eq(HttpMethod.GET),
                eq(null),
                any(ParameterizedTypeReference.class)
        );
    }

    @Test
    void shouldThrowRuntimeExceptionWhenGetPageFails() {
        String endpoint = "people";
        int page = 1;
        int limit = 10;
        String expectedUrl = baseUrl + "/" + endpoint + "?page=" + page + "&limit=" + limit;

        when(restTemplate.exchange(
                eq(expectedUrl),
                eq(HttpMethod.GET),
                eq(null),
                any(ParameterizedTypeReference.class)
        )).thenThrow(new RestClientException("Connection failed"));

        assertThatThrownBy(() -> swapiClientService.getPage(
                endpoint,
                page,
                limit,
                new ParameterizedTypeReference<SwapiResponse<PersonDTO>>() {}
        ))
                .isInstanceOf(RuntimeException.class)
                .hasMessage("Failed to fetch " + endpoint + " from Star Wars API")
                .hasCauseInstanceOf(RestClientException.class);

        verify(restTemplate).exchange(
                eq(expectedUrl),
                eq(HttpMethod.GET),
                eq(null),
                any(ParameterizedTypeReference.class)
        );
    }

    @Test
    void shouldHandleEmptyGetPageResponse() {
        String endpoint = "people";
        int page = 99;
        int limit = 10;

        SwapiResponse<PersonDTO> emptyResponse = new SwapiResponse<>();
        emptyResponse.setMessage("ok");
        emptyResponse.setResults(Arrays.asList());

        String expectedUrl = baseUrl + "/" + endpoint + "?page=" + page + "&limit=" + limit;

        when(restTemplate.exchange(
                eq(expectedUrl),
                eq(HttpMethod.GET),
                eq(null),
                any(ParameterizedTypeReference.class)
        )).thenReturn(ResponseEntity.ok(emptyResponse));

        SwapiResponse<PersonDTO> response = swapiClientService.getPage(
                endpoint,
                page,
                limit,
                new ParameterizedTypeReference<SwapiResponse<PersonDTO>>() {}
        );

        assertThat(response).isNotNull();
        assertThat(response.getResults()).isEmpty();

        verify(restTemplate).exchange(
                eq(expectedUrl),
                eq(HttpMethod.GET),
                eq(null),
                any(ParameterizedTypeReference.class)
        );
    }

    @Test
    void shouldConstructCorrectUrlForGetById() {
        String endpoint = "films";
        String id = "4";
        PersonDetailResponse mockResponse = new PersonDetailResponse();

        String expectedUrl = baseUrl + "/" + endpoint + "/" + id;

        when(restTemplate.getForEntity(expectedUrl, PersonDetailResponse.class))
                .thenReturn(ResponseEntity.ok(mockResponse));

        swapiClientService.getById(endpoint, id, PersonDetailResponse.class);

        verify(restTemplate).getForEntity(expectedUrl, PersonDetailResponse.class);
    }

    @Test
    void shouldConstructCorrectUrlForGetPage() {
        String endpoint = "starships";
        int page = 3;
        int limit = 5;

        SwapiResponse<PersonDTO> mockResponse = new SwapiResponse<>();
        String expectedUrl = baseUrl + "/" + endpoint + "?page=" + page + "&limit=" + limit;

        when(restTemplate.exchange(
                eq(expectedUrl),
                eq(HttpMethod.GET),
                eq(null),
                any(ParameterizedTypeReference.class)
        )).thenReturn(ResponseEntity.ok(mockResponse));

        swapiClientService.getPage(
                endpoint,
                page,
                limit,
                new ParameterizedTypeReference<SwapiResponse<PersonDTO>>() {}
        );

        verify(restTemplate).exchange(
                eq(expectedUrl),
                eq(HttpMethod.GET),
                eq(null),
                any(ParameterizedTypeReference.class)
        );
    }

    @Test
    void shouldHandleNullResponseBodyInGetById() {
        String endpoint = "people";
        String id = "1";
        String expectedUrl = baseUrl + "/" + endpoint + "/" + id;

        when(restTemplate.getForEntity(expectedUrl, PersonDetailResponse.class))
                .thenReturn(ResponseEntity.ok(null));

        Optional<PersonDetailResponse> response = swapiClientService.getById(endpoint, id, PersonDetailResponse.class);

        assertThat(response).isEmpty();

        verify(restTemplate).getForEntity(expectedUrl, PersonDetailResponse.class);
    }
}