package com.starwars.app.dto.swapi;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.*;

import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class VehicleDetailResponse {

    private String message;
    private VehicleResult result;

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    public static class VehicleResult {
        private VehicleProperties properties;
        private String _id;
        private String description;
        private String uid;
        private Integer __v;
    }

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    public static class VehicleProperties {
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
        @JsonProperty("vehicle_class")
        private String vehicleClass;
        private List<String> pilots;
        private List<String> films;
        private String url;
    }
}