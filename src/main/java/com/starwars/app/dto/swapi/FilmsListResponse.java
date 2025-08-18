package com.starwars.app.dto.swapi;

import lombok.*;

import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class FilmsListResponse {

    private String message;
    private List<FilmDTO> result;
    private String apiVersion;
    private String timestamp;

}