package com.starwars.app.dto.swapi;

import lombok.*;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class PersonDTO {
    private String uid;
    private String name;
    private String url;
}