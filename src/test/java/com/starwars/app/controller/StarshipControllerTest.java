package com.starwars.app.controller;

import com.starwars.app.dto.swapi.StarshipDTO;
import com.starwars.app.dto.swapi.StarshipDetailResponse;
import com.starwars.app.dto.swapi.SwapiResponse;
import com.starwars.app.service.external.StarshipService;
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

@WebMvcTest(StarshipController.class)
class StarshipControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private StarshipService starshipService;

    @MockBean
    private UserService userService;

    @MockBean
    private PasswordEncoder passwordEncoder;

    @MockBean
    private JwtService jwtService;

    private SwapiResponse<StarshipDTO> starshipsResponse;
    private StarshipDetailResponse starshipDetailResponse;

    @BeforeEach
    void setUp() {
        StarshipDTO starship1 = new StarshipDTO("1", "Death Star", "https://www.swapi.tech/api/starships/1");
        StarshipDTO starship2 = new StarshipDTO("2", "Millennium Falcon", "https://www.swapi.tech/api/starships/2");

        starshipsResponse = new SwapiResponse<>();
        starshipsResponse.setMessage("ok");
        starshipsResponse.setTotal_records(36);
        starshipsResponse.setTotal_pages(4);
        starshipsResponse.setResults(Arrays.asList(starship1, starship2));

        StarshipDetailResponse.StarshipProperties properties = new StarshipDetailResponse.StarshipProperties();
        properties.setName("Death Star");
        properties.setModel("DS-1 Orbital Battle Station");
        properties.setManufacturer("Imperial Department of Military Research");
        properties.setStarshipClass("Deep Space Mobile Battlestation");

        StarshipDetailResponse.StarshipResult result = new StarshipDetailResponse.StarshipResult();
        result.setProperties(properties);
        result.setUid("1");

        starshipDetailResponse = new StarshipDetailResponse();
        starshipDetailResponse.setMessage("ok");
        starshipDetailResponse.setResult(result);
    }

    @Test
    @WithMockUser(username = "user", roles = {"USER"})
    void shouldGetStarshipsSuccessfully() throws Exception {
        when(starshipService.getStarships(1, 10)).thenReturn(starshipsResponse);

        mockMvc.perform(get("/api/starships"))
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.message").value("ok"))
                .andExpect(jsonPath("$.results").isArray())
                .andExpect(jsonPath("$.results[0].name").value("Death Star"))
                .andExpect(jsonPath("$.results[1].name").value("Millennium Falcon"));
    }

    @Test
    @WithMockUser(username = "user", roles = {"USER"})
    void shouldGetStarshipsWithCustomPageAndLimit() throws Exception {
        when(starshipService.getStarships(2, 5)).thenReturn(starshipsResponse);

        mockMvc.perform(get("/api/starships")
                        .param("page", "2")
                        .param("limit", "5"))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.message").value("ok"));
    }

    @Test
    @WithMockUser(username = "user", roles = {"USER"})
    void shouldReturnNoContentWhenStarshipsListIsEmpty() throws Exception {
        SwapiResponse<StarshipDTO> emptyResponse = new SwapiResponse<>();
        emptyResponse.setResults(Arrays.asList());

        when(starshipService.getStarships(anyInt(), anyInt())).thenReturn(emptyResponse);

        mockMvc.perform(get("/api/starships"))
                .andExpect(status().isNoContent());
    }

    @Test
    @WithMockUser(username = "user", roles = {"USER"})
    void shouldReturnNoContentWhenStarshipsListIsNull() throws Exception {
        when(starshipService.getStarships(anyInt(), anyInt())).thenReturn(null);

        mockMvc.perform(get("/api/starships"))
                .andExpect(status().isNoContent());
    }

    @Test
    @WithMockUser(username = "user", roles = {"USER"})
    void shouldReturnNoContentWhenStarshipsResultsIsNull() throws Exception {
        SwapiResponse<StarshipDTO> responseWithNullResults = new SwapiResponse<>();
        responseWithNullResults.setResults(null);

        when(starshipService.getStarships(anyInt(), anyInt())).thenReturn(responseWithNullResults);

        mockMvc.perform(get("/api/starships"))
                .andExpect(status().isNoContent());
    }

    @Test
    @WithMockUser(username = "user", roles = {"USER"})
    void shouldGetStarshipByIdSuccessfully() throws Exception {
        when(starshipService.getStarshipById("1")).thenReturn(Optional.of(starshipDetailResponse));

        mockMvc.perform(get("/api/starships/1"))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.name").value("Death Star"))
                .andExpect(jsonPath("$.model").value("DS-1 Orbital Battle Station"))
                .andExpect(jsonPath("$.manufacturer").value("Imperial Department of Military Research"));
    }

    @Test
    @WithMockUser(username = "user", roles = {"USER"})
    void shouldReturnNotFoundWhenStarshipDoesNotExist() throws Exception {
        when(starshipService.getStarshipById("999")).thenReturn(Optional.empty());

        mockMvc.perform(get("/api/starships/999"))
                .andExpect(status().isNotFound());
    }

    @Test
    @WithMockUser(username = "user", roles = {"USER"})
    void shouldReturnHealthCheckSuccessfully() throws Exception {
        mockMvc.perform(get("/api/starships/health"))
                .andExpect(status().isOk())
                .andExpect(content().contentType("text/plain;charset=UTF-8"))
                .andExpect(content().string("Starships service is running"));
    }

    @Test
    @WithMockUser(username = "user", roles = {"USER"})
    void shouldUseDefaultParametersWhenNotProvided() throws Exception {
        when(starshipService.getStarships(1, 10)).thenReturn(starshipsResponse);

        mockMvc.perform(get("/api/starships"))
                .andExpect(status().isOk());
    }

    @Test
    @WithMockUser(username = "user", roles = {"USER"})
    void shouldHandleInvalidPageParameter() throws Exception {
        mockMvc.perform(get("/api/starships")
                        .param("page", "invalid"))
                .andExpect(status().isInternalServerError());
    }

    @Test
    @WithMockUser(username = "user", roles = {"USER"})
    void shouldHandleInvalidLimitParameter() throws Exception {
        mockMvc.perform(get("/api/starships")
                        .param("limit", "invalid"))
                .andExpect(status().isInternalServerError());
    }

    @Test
    @WithMockUser(username = "user", roles = {"USER"})
    void shouldHandleServiceExceptionInGetStarships() throws Exception {
        when(starshipService.getStarships(anyInt(), anyInt()))
                .thenThrow(new RuntimeException("SWAPI service unavailable"));

        mockMvc.perform(get("/api/starships"))
                .andExpect(status().isInternalServerError());
    }

    @Test
    @WithMockUser(username = "user", roles = {"USER"})
    void shouldHandleServiceExceptionInGetStarshipById() throws Exception {
        when(starshipService.getStarshipById(anyString()))
                .thenThrow(new RuntimeException("SWAPI service unavailable"));

        mockMvc.perform(get("/api/starships/1"))
                .andExpect(status().isInternalServerError());
    }

    @Test
    void shouldRequireAuthenticationForGetStarships() throws Exception {
        mockMvc.perform(get("/api/starships"))
                .andExpect(status().isForbidden());
    }

    @Test
    void shouldRequireAuthenticationForGetStarshipById() throws Exception {
        mockMvc.perform(get("/api/starships/1"))
                .andExpect(status().isForbidden());
    }

    @Test
    void shouldRequireAuthenticationForHealthCheck() throws Exception {
        mockMvc.perform(get("/api/starships/health"))
                .andExpect(status().isForbidden());
    }
}