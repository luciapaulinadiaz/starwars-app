package com.starwars.app.dto.swapi;

import lombok.*;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class StarshipDTO {
    private String uid;
    private String name;
    private String url;
}