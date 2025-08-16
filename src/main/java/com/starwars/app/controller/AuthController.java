package com.starwars.app.controller;

import com.starwars.app.dto.AuthResponse;
import com.starwars.app.dto.LoginRequest;
import com.starwars.app.dto.RegisterRequest;
import com.starwars.app.dto.UserResponse;
import com.starwars.app.service.AuthService;
import lombok.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import javax.validation.Valid;

@RestController
@RequestMapping("/api/auth")
@RequiredArgsConstructor
@Validated
public class AuthController {

    private final AuthService authService;
    private static final Logger logger = LoggerFactory.getLogger(AuthController.class);


    @PostMapping("/login")
    public ResponseEntity<AuthResponse> login(@Valid @RequestBody LoginRequest loginRequest) {
        logger.info("Login attempt for: {}", loginRequest.getIdentifier());

        AuthResponse response = authService.login(loginRequest);
        logger.info("Login successful for: {}", loginRequest.getIdentifier());

        return ResponseEntity.ok(response);
    }


    @PostMapping("/register")
    public ResponseEntity<AuthResponse> register(@Valid @RequestBody RegisterRequest registerRequest) {
        logger.info("Registration attempt for: {}", registerRequest.getUsername());

        AuthResponse response = authService.register(registerRequest);
        logger.info("Registration successful for: {}", registerRequest.getUsername());

        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }


    @GetMapping("/validate")
    public ResponseEntity<UserResponse> validateToken(@RequestHeader("Authorization") String authHeader) {
        if (authHeader == null || !authHeader.startsWith("Bearer ")) {
            throw new BadCredentialsException("Missing or invalid Authorization header");
        }

        String token = authHeader.substring(7);
        UserResponse userResponse = authService.validateToken(token);

        return ResponseEntity.ok(userResponse);
    }
}