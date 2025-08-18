package com.starwars.app.controller;

import com.starwars.app.dto.swapi.PersonDTO;
import com.starwars.app.dto.swapi.PersonDetailResponse;
import com.starwars.app.dto.swapi.SwapiResponse;
import com.starwars.app.service.external.PeopleService;
import com.starwars.app.service.JwtService;
import com.starwars.app.service.UserService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.web.servlet.MockMvc;

import java.util.Arrays;
import java.util.Optional;

import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(PeopleController.class)
class PeopleControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private PeopleService peopleService;

    @MockBean
    private UserService userService;

    @MockBean
    private PasswordEncoder passwordEncoder;

    @MockBean
    private JwtService jwtService;

    private SwapiResponse<PersonDTO> peopleResponse;
    private PersonDetailResponse personDetailResponse;

    @BeforeEach
    void setUp() {
        PersonDTO person1 = new PersonDTO("1", "Luke Skywalker", "https://www.swapi.tech/api/people/1");
        PersonDTO person2 = new PersonDTO("2", "C-3PO", "https://www.swapi.tech/api/people/2");

        peopleResponse = new SwapiResponse<>();
        peopleResponse.setMessage("ok");
        peopleResponse.setTotal_records(82);
        peopleResponse.setTotal_pages(9);
        peopleResponse.setResults(Arrays.asList(person1, person2));

        PersonDetailResponse.PersonProperties properties = new PersonDetailResponse.PersonProperties();
        properties.setName("Luke Skywalker");
        properties.setHeight("172");
        properties.setMass("77");
        properties.setGender("male");

        PersonDetailResponse.PersonResult result = new PersonDetailResponse.PersonResult();
        result.setProperties(properties);
        result.setUid("1");

        personDetailResponse = new PersonDetailResponse();
        personDetailResponse.setMessage("ok");
        personDetailResponse.setResult(result);
    }

    @Test
    @WithMockUser(username = "user", roles = {"USER"})
    void shouldGetPeopleSuccessfully() throws Exception {
        when(peopleService.getPeople(1, 10)).thenReturn(peopleResponse);

        mockMvc.perform(get("/api/people"))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.message").value("ok"))
                .andExpect(jsonPath("$.results").isArray())
                .andExpect(jsonPath("$.results[0].name").value("Luke Skywalker"))
                .andExpect(jsonPath("$.results[1].name").value("C-3PO"));
    }

    @Test
    @WithMockUser(username = "user", roles = {"USER"})
    void shouldGetPeopleWithCustomPageAndSize() throws Exception {
        when(peopleService.getPeople(2, 5)).thenReturn(peopleResponse);

        mockMvc.perform(get("/api/people")
                        .param("page", "2")
                        .param("size", "5"))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.message").value("ok"));
    }

    @Test
    @WithMockUser(username = "user", roles = {"USER"})
    void shouldReturnNoContentWhenPeopleListIsEmpty() throws Exception {
        SwapiResponse<PersonDTO> emptyResponse = new SwapiResponse<>();
        emptyResponse.setResults(Arrays.asList());

        when(peopleService.getPeople(anyInt(), anyInt())).thenReturn(emptyResponse);

        mockMvc.perform(get("/api/people"))
                .andExpect(status().isNoContent());
    }

    @Test
    @WithMockUser(username = "user", roles = {"USER"})
    void shouldReturnNoContentWhenPeopleListIsNull() throws Exception {
        when(peopleService.getPeople(anyInt(), anyInt())).thenReturn(null);

        mockMvc.perform(get("/api/people"))
                .andExpect(status().isNoContent());
    }

    @Test
    @WithMockUser(username = "user", roles = {"USER"})
    void shouldReturnNoContentWhenPeopleResultsIsNull() throws Exception {
        SwapiResponse<PersonDTO> responseWithNullResults = new SwapiResponse<>();
        responseWithNullResults.setResults(null);

        when(peopleService.getPeople(anyInt(), anyInt())).thenReturn(responseWithNullResults);

        mockMvc.perform(get("/api/people"))
                .andExpect(status().isNoContent());
    }

    @Test
    @WithMockUser(username = "user", roles = {"USER"})
    void shouldGetPersonByIdSuccessfully() throws Exception {
        when(peopleService.getPersonById("1")).thenReturn(Optional.of(personDetailResponse));

        mockMvc.perform(get("/api/people/1"))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.name").value("Luke Skywalker"))
                .andExpect(jsonPath("$.height").value("172"))
                .andExpect(jsonPath("$.mass").value("77"))
                .andExpect(jsonPath("$.gender").value("male"));
    }

    @Test
    @WithMockUser(username = "user", roles = {"USER"})
    void shouldReturnNotFoundWhenPersonDoesNotExist() throws Exception {
        when(peopleService.getPersonById("999")).thenReturn(Optional.empty());

        mockMvc.perform(get("/api/people/999"))
                .andExpect(status().isNotFound());
    }

    @Test
    @WithMockUser(username = "user", roles = {"USER"})
    void shouldReturnHealthCheckSuccessfully() throws Exception {
        mockMvc.perform(get("/api/people/health"))
                .andExpect(status().isOk())
                .andExpect(content().contentType("text/plain;charset=UTF-8"))
                .andExpect(content().string("People service is running"));
    }

    @Test
    @WithMockUser(username = "user", roles = {"USER"})
    void shouldUseDefaultParametersWhenNotProvided() throws Exception {
        when(peopleService.getPeople(1, 10)).thenReturn(peopleResponse);

        mockMvc.perform(get("/api/people"))
                .andExpect(status().isOk());
    }

    @Test
    @WithMockUser(username = "user", roles = {"USER"})
    void shouldHandleInvalidPageParameter() throws Exception {
        mockMvc.perform(get("/api/people")
                        .param("page", "invalid"))
                .andExpect(status().isInternalServerError());
    }

    @Test
    @WithMockUser(username = "user", roles = {"USER"})
    void shouldHandleInvalidSizeParameter() throws Exception {
        mockMvc.perform(get("/api/people")
                        .param("size", "invalid"))
                .andExpect(status().isInternalServerError());
    }

    @Test
    @WithMockUser(username = "user", roles = {"USER"})
    void shouldHandleServiceExceptionInGetPeople() throws Exception {
        when(peopleService.getPeople(anyInt(), anyInt()))
                .thenThrow(new RuntimeException("SWAPI service unavailable"));

        mockMvc.perform(get("/api/people"))
                .andExpect(status().isInternalServerError());
    }

    @Test
    @WithMockUser(username = "user", roles = {"USER"})
    void shouldHandleServiceExceptionInGetPersonById() throws Exception {
        when(peopleService.getPersonById(anyString()))
                .thenThrow(new RuntimeException("SWAPI service unavailable"));

        mockMvc.perform(get("/api/people/1"))
                .andExpect(status().isInternalServerError());
    }

    @Test
    void shouldRequireAuthenticationForGetPeople() throws Exception {
        mockMvc.perform(get("/api/people"))
                .andExpect(status().isForbidden());
    }

    @Test
    void shouldRequireAuthenticationForGetPersonById() throws Exception {
        mockMvc.perform(get("/api/people/1"))
                .andExpect(status().isForbidden());
    }

    @Test
    void shouldRequireAuthenticationForHealthCheck() throws Exception {
        mockMvc.perform(get("/api/people/health"))
                .andExpect(status().isForbidden());
    }
}