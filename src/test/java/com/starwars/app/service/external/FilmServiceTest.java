package com.starwars.app.service.external;

import com.starwars.app.dto.swapi.FilmDTO;
import com.starwars.app.dto.swapi.FilmDetailResponse;
import com.starwars.app.dto.swapi.FilmsListResponse;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Arrays;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class FilmServiceTest {

    @Mock
    private SwapiClientService swapiClient;

    @InjectMocks
    private FilmService filmService;

    private FilmsListResponse mockFilmsListResponse;
    private FilmDetailResponse mockFilmDetailResponse;

    @BeforeEach
    void setUp() {
        FilmDTO film1 = new FilmDTO();
        film1.setUid("1");
        film1.setProperties(createFilmDTOProperties("A New Hope", 4, "1977-05-25"));

        FilmDTO film2 = new FilmDTO();
        film2.setUid("2");
        film2.setProperties(createFilmDTOProperties("The Empire Strikes Back", 5, "1980-05-17"));

        mockFilmsListResponse = new FilmsListResponse();
        mockFilmsListResponse.setMessage("ok");
        mockFilmsListResponse.setResult(Arrays.asList(film1, film2));
        mockFilmsListResponse.setApiVersion("1.0");
        mockFilmsListResponse.setTimestamp("2025-08-17T10:00:00Z");

        FilmDetailResponse.FilmProperties detailProperties = new FilmDetailResponse.FilmProperties();
        detailProperties.setTitle("A New Hope");
        detailProperties.setEpisodeId(4);
        detailProperties.setOpeningCrawl("It is a period of civil war...");
        detailProperties.setDirector("George Lucas");
        detailProperties.setProducer("Gary Kurtz, Rick McCallum");
        detailProperties.setReleaseDate("1977-05-25");

        FilmDetailResponse.FilmResult detailResult = new FilmDetailResponse.FilmResult();
        detailResult.setProperties(detailProperties);
        detailResult.setUid("1");
        detailResult.setDescription("A film within the Star Wars universe");
        detailResult.set_id("64f1c2e3b8d9a7f4e5c6d7e8");
        detailResult.set__v(0);

        mockFilmDetailResponse = new FilmDetailResponse();
        mockFilmDetailResponse.setMessage("ok");
        mockFilmDetailResponse.setResult(detailResult);
    }

    private FilmDTO.FilmProperties createFilmDTOProperties(String title, int episodeId, String releaseDate) {
        FilmDTO.FilmProperties properties = new FilmDTO.FilmProperties();
        properties.setTitle(title);
        properties.setEpisodeId(episodeId);
        properties.setDirector("George Lucas");
        properties.setReleaseDate(releaseDate);
        return properties;
    }

    @Test
    void shouldGetFilmsSuccessfully() {
        int page = 1;
        int limit = 10;
        String expectedUrl = "films?page=" + page + "&limit=" + limit;

        when(swapiClient.getById("", expectedUrl, FilmsListResponse.class))
                .thenReturn(Optional.of(mockFilmsListResponse));

        FilmsListResponse result = filmService.getFilms(page, limit);

        assertThat(result).isNotNull();
        assertThat(result.getMessage()).isEqualTo("ok");
        assertThat(result.getResult()).hasSize(2);
        assertThat(result.getResult().get(0).getProperties().getTitle()).isEqualTo("A New Hope");
        assertThat(result.getResult().get(1).getProperties().getTitle()).isEqualTo("The Empire Strikes Back");
        assertThat(result.getResult().get(0).getUid()).isEqualTo("1");
        assertThat(result.getResult().get(1).getUid()).isEqualTo("2");

        verify(swapiClient).getById("", expectedUrl, FilmsListResponse.class);
    }

    @Test
    void shouldReturnEmptyFilmsListWhenNotFound() {
        int page = 99;
        int limit = 10;
        String expectedUrl = "films?page=" + page + "&limit=" + limit;

        when(swapiClient.getById("", expectedUrl, FilmsListResponse.class))
                .thenReturn(Optional.empty());

        FilmsListResponse result = filmService.getFilms(page, limit);

        assertThat(result).isNotNull();
        assertThat(result).isInstanceOf(FilmsListResponse.class);

        verify(swapiClient).getById("", expectedUrl, FilmsListResponse.class);
    }

    @Test
    void shouldGetFilmByIdSuccessfully() {
        String filmId = "1";
        when(swapiClient.getById("films", filmId, FilmDetailResponse.class))
                .thenReturn(Optional.of(mockFilmDetailResponse));

        Optional<FilmDetailResponse> result = filmService.getFilmById(filmId);

        assertThat(result).isPresent();
        assertThat(result.get().getMessage()).isEqualTo("ok");
        assertThat(result.get().getResult().getProperties().getTitle()).isEqualTo("A New Hope");
        assertThat(result.get().getResult().getProperties().getEpisodeId()).isEqualTo(4);
        assertThat(result.get().getResult().getProperties().getDirector()).isEqualTo("George Lucas");
        assertThat(result.get().getResult().getProperties().getReleaseDate()).isEqualTo("1977-05-25");
        assertThat(result.get().getResult().getUid()).isEqualTo("1");

        verify(swapiClient).getById("films", filmId, FilmDetailResponse.class);
    }

    @Test
    void shouldReturnEmptyWhenFilmNotFound() {
        String filmId = "999";
        when(swapiClient.getById("films", filmId, FilmDetailResponse.class))
                .thenReturn(Optional.empty());

        Optional<FilmDetailResponse> result = filmService.getFilmById(filmId);

        assertThat(result).isEmpty();

        verify(swapiClient).getById("films", filmId, FilmDetailResponse.class);
    }

    @Test
    void shouldConstructCorrectUrlForGetFilms() {
        int page = 3;
        int limit = 5;
        String expectedUrl = "films?page=3&limit=5";

        when(swapiClient.getById("", expectedUrl, FilmsListResponse.class))
                .thenReturn(Optional.of(mockFilmsListResponse));

        filmService.getFilms(page, limit);

        verify(swapiClient).getById("", expectedUrl, FilmsListResponse.class);
    }

    @Test
    void shouldHandleEmptyFilmsListResponse() {
        int page = 1;
        int limit = 10;
        String expectedUrl = "films?page=" + page + "&limit=" + limit;

        FilmsListResponse emptyResponse = new FilmsListResponse();
        emptyResponse.setMessage("ok");
        emptyResponse.setResult(Arrays.asList());

        when(swapiClient.getById("", expectedUrl, FilmsListResponse.class))
                .thenReturn(Optional.of(emptyResponse));

        FilmsListResponse result = filmService.getFilms(page, limit);

        assertThat(result).isNotNull();
        assertThat(result.getResult()).isEmpty();

        verify(swapiClient).getById("", expectedUrl, FilmsListResponse.class);
    }

    @Test
    void shouldUseCorrectEndpointForGetFilmById() {
        String filmId = "4";
        when(swapiClient.getById("films", filmId, FilmDetailResponse.class))
                .thenReturn(Optional.of(mockFilmDetailResponse));

        filmService.getFilmById(filmId);

        verify(swapiClient).getById(eq("films"), eq(filmId), eq(FilmDetailResponse.class));
    }
}