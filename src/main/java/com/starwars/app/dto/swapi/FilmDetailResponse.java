package com.starwars.app.dto.swapi;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.*;

import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class FilmDetailResponse {
    private String message;
    private FilmResult result;

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    public static class FilmResult {
        private FilmProperties properties;
        private String _id;
        private String description;
        private String uid;
        private Integer __v;
    }

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    public static class FilmProperties {
        private String title;
        @JsonProperty("episode_id")
        private Integer episodeId;
        @JsonProperty("opening_crawl")
        private String openingCrawl;
        private String director;
        private String producer;
        @JsonProperty("release_date")
        private String releaseDate;
        private List<String> characters;
        private List<String> planets;
        private List<String> starships;
        private List<String> vehicles;
        private List<String> species;
        private String created;
        private String edited;
        private String url;
    }
}