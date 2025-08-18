package com.starwars.app.dto.swapi;

import lombok.*;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class VehicleDTO {
    private String uid;
    private String name;
    private String url;
}