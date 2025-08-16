package com.starwars.app.dto;

import lombok.*;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class AuthResponse {

    private String token;
    private String type = "Bearer";
    private UserResponse user;

    public AuthResponse(String token, UserResponse user) {
        this.token = token;
        this.user = user;
    }
}