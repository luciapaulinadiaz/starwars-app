package com.starwars.app.dto;

import lombok.*;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.Size;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class LoginRequest {

    @NotBlank(message = "Username or email is required")
    @Size(min = 3, max = 50, message = "Username must be between 3 and 50 characters")
    private String identifier; // x ahora lo dejo generico para que pueda ser username o email

    @NotBlank(message = "Password is required")
    @Size(min = 6, message = "Password must be at least 6 characters")
    private String password;
}
