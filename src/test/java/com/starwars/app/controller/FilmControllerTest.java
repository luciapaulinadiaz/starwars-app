package com.starwars.app.controller;

import com.starwars.app.dto.swapi.FilmDTO;
import com.starwars.app.dto.swapi.FilmDetailResponse;
import com.starwars.app.dto.swapi.FilmsListResponse;
import com.starwars.app.service.external.FilmService;
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

@WebMvcTest(FilmController.class)
class FilmControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private FilmService filmService;

    @MockBean
    private UserService userService;

    @MockBean
    private PasswordEncoder passwordEncoder;

    @MockBean
    private JwtService jwtService;

    private FilmsListResponse filmsListResponse;
    private FilmDetailResponse filmDetailResponse;

    @BeforeEach
    void setUp() {
        FilmDTO film1 = new FilmDTO();
        film1.setUid("1");
        film1.setProperties(createFilmProperties("A New Hope", 4));

        FilmDTO film2 = new FilmDTO();
        film2.setUid("2");
        film2.setProperties(createFilmProperties("The Empire Strikes Back", 5));

        filmsListResponse = new FilmsListResponse();
        filmsListResponse.setMessage("ok");
        filmsListResponse.setResult(Arrays.asList(film1, film2));

        FilmDetailResponse.FilmProperties detailProperties = new FilmDetailResponse.FilmProperties();
        detailProperties.setTitle("A New Hope");
        detailProperties.setEpisodeId(4);
        detailProperties.setDirector("George Lucas");
        detailProperties.setReleaseDate("1977-05-25");

        FilmDetailResponse.FilmResult detailResult = new FilmDetailResponse.FilmResult();
        detailResult.setProperties(detailProperties);
        detailResult.setUid("1");

        filmDetailResponse = new FilmDetailResponse();
        filmDetailResponse.setMessage("ok");
        filmDetailResponse.setResult(detailResult);
    }

    private FilmDTO.FilmProperties createFilmProperties(String title, int episodeId) {
        FilmDTO.FilmProperties properties = new FilmDTO.FilmProperties();
        properties.setTitle(title);
        properties.setEpisodeId(episodeId);
        properties.setDirector("George Lucas");
        properties.setReleaseDate(title.equals("A New Hope") ? "1977-05-25" : "1980-05-17");
        return properties;
    }

    @Test
    @WithMockUser(username = "user", roles = {"USER"})
    void shouldGetFilmsSuccessfully() throws Exception {
        when(filmService.getFilms(1, 10)).thenReturn(filmsListResponse);

        mockMvc.perform(get("/api/films"))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.message").value("ok"))
                .andExpect(jsonPath("$.result").isArray())
                .andExpect(jsonPath("$.result[0].properties.title").value("A New Hope"))
                .andExpect(jsonPath("$.result[1].properties.title").value("The Empire Strikes Back"));
    }

    @Test
    @WithMockUser(username = "user", roles = {"USER"})
    void shouldGetFilmsWithCustomPageAndLimit() throws Exception {
        when(filmService.getFilms(2, 5)).thenReturn(filmsListResponse);

        mockMvc.perform(get("/api/films")
                        .param("page", "2")
                        .param("limit", "5"))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.message").value("ok"));
    }

    @Test
    @WithMockUser(username = "user", roles = {"USER"})
    void shouldReturnNoContentWhenFilmsListIsEmpty() throws Exception {
        FilmsListResponse emptyResponse = new FilmsListResponse();
        emptyResponse.setResult(Arrays.asList());

        when(filmService.getFilms(anyInt(), anyInt())).thenReturn(emptyResponse);

        mockMvc.perform(get("/api/films"))
                .andExpect(status().isNoContent());
    }

    @Test
    @WithMockUser(username = "user", roles = {"USER"})
    void shouldReturnNoContentWhenFilmsListIsNull() throws Exception {
        when(filmService.getFilms(anyInt(), anyInt())).thenReturn(null);

        mockMvc.perform(get("/api/films"))
                .andExpect(status().isNoContent());
    }

    @Test
    @WithMockUser(username = "user", roles = {"USER"})
    void shouldReturnNoContentWhenFilmsResultIsNull() throws Exception {
        FilmsListResponse responseWithNullResult = new FilmsListResponse();
        responseWithNullResult.setResult(null);

        when(filmService.getFilms(anyInt(), anyInt())).thenReturn(responseWithNullResult);

        mockMvc.perform(get("/api/films"))
                .andExpect(status().isNoContent());
    }

    @Test
    @WithMockUser(username = "user", roles = {"USER"})
    void shouldGetFilmByIdSuccessfully() throws Exception {
        when(filmService.getFilmById("1")).thenReturn(Optional.of(filmDetailResponse));

        mockMvc.perform(get("/api/films/1"))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.message").value("ok"))
                .andExpect(jsonPath("$.result.properties.title").value("A New Hope"))
                .andExpect(jsonPath("$.result.properties.director").value("George Lucas"));
    }

    @Test
    @WithMockUser(username = "user", roles = {"USER"})
    void shouldReturnNotFoundWhenFilmDoesNotExist() throws Exception {
        when(filmService.getFilmById("999")).thenReturn(Optional.empty());

        mockMvc.perform(get("/api/films/999"))
                .andExpect(status().isNotFound());
    }

    @Test
    @WithMockUser(username = "user", roles = {"USER"})
    void shouldReturnHealthCheckSuccessfully() throws Exception {
        mockMvc.perform(get("/api/films/health"))
                .andExpect(status().isOk())
                .andExpect(content().contentType("text/plain;charset=UTF-8"))
                .andExpect(content().string("Films service is running"));
    }


    @Test
    @WithMockUser(username = "user", roles = {"USER"})
    void shouldUseDefaultParametersWhenNotProvided() throws Exception {
        when(filmService.getFilms(1, 10)).thenReturn(filmsListResponse);

        mockMvc.perform(get("/api/films"))
                .andExpect(status().isOk());
    }

    @Test
    @WithMockUser(username = "user", roles = {"USER"})
    void shouldHandleInvalidPageParameter() throws Exception {
        mockMvc.perform(get("/api/films")
                        .param("page", "invalid"))
                .andExpect(status().isInternalServerError());
    }

    @Test
    @WithMockUser(username = "user", roles = {"USER"})
    void shouldHandleInvalidLimitParameter() throws Exception {
        mockMvc.perform(get("/api/films")
                        .param("limit", "invalid"))
                .andExpect(status().isInternalServerError());
    }


    @Test
    @WithMockUser(username = "user", roles = {"USER"})
    void shouldHandleServiceExceptionInGetFilms() throws Exception {
        when(filmService.getFilms(anyInt(), anyInt()))
                .thenThrow(new RuntimeException("SWAPI service unavailable"));

        mockMvc.perform(get("/api/films"))
                .andExpect(status().isInternalServerError());
    }

    @Test
    @WithMockUser(username = "user", roles = {"USER"})
    void shouldHandleServiceExceptionInGetFilmById() throws Exception {
        when(filmService.getFilmById(anyString()))
                .thenThrow(new RuntimeException("SWAPI service unavailable"));

        mockMvc.perform(get("/api/films/1"))
                .andExpect(status().isInternalServerError());
    }

    @Test
    void shouldRequireAuthenticationForGetFilms() throws Exception {
        mockMvc.perform(get("/api/films"))
                .andExpect(status().isForbidden());
    }

    @Test
    void shouldRequireAuthenticationForGetFilmById() throws Exception {
        mockMvc.perform(get("/api/films/1"))
                .andExpect(status().isForbidden());
    }

    @Test
    void shouldRequireAuthenticationForHealthCheck() throws Exception {
        mockMvc.perform(get("/api/films/health"))
                .andExpect(status().isForbidden());
    }
}