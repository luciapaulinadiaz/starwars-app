package com.starwars.app.dto.swapi;

import lombok.*;

import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class SwapiResponse<T> {
    private String message;
    private Integer total_records;
    private Integer total_pages;
    private String previous;
    private String next;
    private List<T> results;
}