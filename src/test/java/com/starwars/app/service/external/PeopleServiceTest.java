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

import java.util.Arrays;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class PeopleServiceTest {

    @Mock
    private SwapiClientService swapiClient;

    @InjectMocks
    private PeopleService peopleService;

    private SwapiResponse<PersonDTO> mockPeopleResponse;
    private PersonDetailResponse mockPersonDetailResponse;

    @BeforeEach
    void setUp() {
        PersonDTO person1 = new PersonDTO("1", "Luke Skywalker", "https://www.swapi.tech/api/people/1");
        PersonDTO person2 = new PersonDTO("2", "C-3PO", "https://www.swapi.tech/api/people/2");

        mockPeopleResponse = new SwapiResponse<>();
        mockPeopleResponse.setMessage("ok");
        mockPeopleResponse.setTotal_records(82);
        mockPeopleResponse.setTotal_pages(9);
        mockPeopleResponse.setResults(Arrays.asList(person1, person2));

        PersonDetailResponse.PersonProperties properties = new PersonDetailResponse.PersonProperties();
        properties.setName("Luke Skywalker");
        properties.setHeight("172");
        properties.setMass("77");
        properties.setGender("male");

        PersonDetailResponse.PersonResult result = new PersonDetailResponse.PersonResult();
        result.setProperties(properties);
        result.setUid("1");

        mockPersonDetailResponse = new PersonDetailResponse();
        mockPersonDetailResponse.setMessage("ok");
        mockPersonDetailResponse.setResult(result);
    }

    @Test
    void shouldGetPeopleSuccessfully() {
        int page = 1;
        int limit = 10;
        when(swapiClient.getPage(eq("people"), eq(page), eq(limit), any(ParameterizedTypeReference.class)))
                .thenReturn(mockPeopleResponse);

        SwapiResponse<PersonDTO> result = peopleService.getPeople(page, limit);

        assertThat(result).isNotNull();
        assertThat(result.getMessage()).isEqualTo("ok");
        assertThat(result.getResults()).hasSize(2);
        assertThat(result.getResults().get(0).getName()).isEqualTo("Luke Skywalker");
        assertThat(result.getResults().get(1).getName()).isEqualTo("C-3PO");

        verify(swapiClient).getPage(eq("people"), eq(page), eq(limit), any(ParameterizedTypeReference.class));
    }

    @Test
    void shouldGetPersonByIdSuccessfully() {
        String personId = "1";
        when(swapiClient.getById("people", personId, PersonDetailResponse.class))
                .thenReturn(Optional.of(mockPersonDetailResponse));

        Optional<PersonDetailResponse> result = peopleService.getPersonById(personId);

        assertThat(result).isPresent();
        assertThat(result.get().getMessage()).isEqualTo("ok");
        assertThat(result.get().getResult().getProperties().getName()).isEqualTo("Luke Skywalker");
        assertThat(result.get().getResult().getProperties().getHeight()).isEqualTo("172");

        verify(swapiClient).getById("people", personId, PersonDetailResponse.class);
    }

    @Test
    void shouldReturnEmptyWhenPersonNotFound() {
        String personId = "999";
        when(swapiClient.getById("people", personId, PersonDetailResponse.class))
                .thenReturn(Optional.empty());

        Optional<PersonDetailResponse> result = peopleService.getPersonById(personId);

        assertThat(result).isEmpty();

        verify(swapiClient).getById("people", personId, PersonDetailResponse.class);
    }


    @Test
    void shouldHandleEmptyPeopleResponse() {
        int page = 99;
        int limit = 10;
        SwapiResponse<PersonDTO> emptyResponse = new SwapiResponse<>();
        emptyResponse.setMessage("ok");
        emptyResponse.setResults(Arrays.asList());

        when(swapiClient.getPage(eq("people"), eq(page), eq(limit), any(ParameterizedTypeReference.class)))
                .thenReturn(emptyResponse);

        SwapiResponse<PersonDTO> result = peopleService.getPeople(page, limit);

        assertThat(result).isNotNull();
        assertThat(result.getResults()).isEmpty();

        verify(swapiClient).getPage(eq("people"), eq(page), eq(limit), any(ParameterizedTypeReference.class));
    }
}