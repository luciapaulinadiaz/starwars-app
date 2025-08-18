package com.starwars.app.dto.swapi;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.*;

import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class StarshipDetailResponse {

    private String message;
    private StarshipResult result;

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    public static class StarshipResult {
        private StarshipProperties properties;
        private String _id;
        private String description;
        private String uid;
        private Integer __v;
    }

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    public static class StarshipProperties {
        private String created;
        private String edited;
        private String consumables;
        private String name;
        @JsonProperty("cargo_capacity")
        private String cargoCapacity;
        private String passengers;
        @JsonProperty("max_atmosphering_speed")
        private String maxAtmospheringSpeed;
        private String crew;
        private String length;
        private String model;
        @JsonProperty("cost_in_credits")
        private String costInCredits;
        private String manufacturer;
        private List<String> pilots;
        @JsonProperty("MGLT")
        private String mglt;
        @JsonProperty("starship_class")
        private String starshipClass;
        @JsonProperty("hyperdrive_rating")
        private String hyperdriveRating;
        private List<String> films;
        private String url;
    }
}