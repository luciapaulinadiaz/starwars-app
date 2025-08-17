package com.starwars.app.dto.swapi;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.*;
import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class PersonDetailResponse {

        private String message;
        private PersonResult result;

        @Data
        @NoArgsConstructor
        @AllArgsConstructor
        public static class PersonResult {
                private PersonProperties properties;
                private String _id;
                private String description;
                private String uid;
                private Integer __v;
        }

        @Data
        @NoArgsConstructor
        @AllArgsConstructor
        public static class PersonProperties {
                private String created;
                private String edited;
                private String name;
                private String gender;
                @JsonProperty("skin_color")
                private String skinColor;
                @JsonProperty("hair_color")
                private String hairColor;
                private String height;
                @JsonProperty("eye_color")
                private String eyeColor;
                private String mass;
                private String homeworld;
                @JsonProperty("birth_year")
                private String birthYear;
                private List<String> vehicles;
                private List<String> starships;
                private List<String> films;
                private String url;
        }
}