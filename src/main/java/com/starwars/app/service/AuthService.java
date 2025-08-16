package com.starwars.app.service;

import com.starwars.app.dto.AuthResponse;
import com.starwars.app.dto.LoginRequest;
import com.starwars.app.dto.RegisterRequest;
import com.starwars.app.dto.UserResponse;
import com.starwars.app.entity.User;
import lombok.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class AuthService {

    private final UserService userService;
    private final JwtService jwtService;
    private final AuthenticationManager authenticationManager;
    private static final Logger logger = LoggerFactory.getLogger(AuthService.class);


    public AuthResponse login(LoginRequest loginRequest) {
        logger.info("Attempting login for identifier: {}", loginRequest.getIdentifier());

        try {
            Authentication authentication = authenticationManager.authenticate(
                    new UsernamePasswordAuthenticationToken(
                            loginRequest.getIdentifier(),
                            loginRequest.getPassword()
                    )
            );

            UserDetails userDetails = (UserDetails) authentication.getPrincipal();

            User user = userService.findByUsername(userDetails.getUsername())
                    .orElseThrow(() -> new BadCredentialsException("User not found"));

            String token = jwtService.generateToken(userDetails);

            logger.info("Login successful for user: {}", user.getUsername());

            return AuthResponse.builder()
                    .token(token)
                    .type("Bearer")
                    .user(UserResponse.fromUser(user))
                    .build();

        } catch (Exception e) {
            logger.error("Login failed for identifier: {}", loginRequest.getIdentifier());
            throw new BadCredentialsException("Invalid credentials");
        }
    }


    public AuthResponse register(RegisterRequest registerRequest) {
        logger.info("Attempting registration for username: {}", registerRequest.getUsername());

        try {
            User user = userService.registerUser(
                    registerRequest.getUsername(),
                    registerRequest.getEmail(),
                    registerRequest.getPassword(),
                    registerRequest.getFirstName(),
                    registerRequest.getLastName()
            );

            String token = jwtService.generateToken(user);

            logger.info("Registration successful for user: {}", user.getUsername());

            return AuthResponse.builder()
                    .token(token)
                    .type("Bearer")
                    .user(UserResponse.fromUser(user))
                    .build();

        } catch (IllegalArgumentException e) {
            logger.error("Registration failed: {}", e.getMessage());
            throw e;
        }
    }


    public UserResponse validateToken(String token) {
        logger.debug("Validating JWT token");

        try {
            String username = jwtService.extractUsername(token);

            User user = userService.findByUsername(username)
                    .orElseThrow(() -> new BadCredentialsException("User not found"));

            if (!jwtService.isTokenValid(token, user)) {
                throw new BadCredentialsException("Invalid token");
            }

            return UserResponse.fromUser(user);

        } catch (Exception e) {
            logger.error("Token validation failed: {}", e.getMessage());
            throw new BadCredentialsException("Invalid token");
        }
    }

    public AuthResponse refreshToken(String oldToken) {
        logger.debug("Refreshing JWT token");

        try {
            String username = jwtService.extractUsername(oldToken);
            User user = userService.findByUsername(username)
                    .orElseThrow(() -> new BadCredentialsException("User not found"));

            if (jwtService.isTokenValid(oldToken, user)) {
                String newToken = jwtService.generateToken(user);

                return AuthResponse.builder()
                        .token(newToken)
                        .type("Bearer")
                        .user(UserResponse.fromUser(user))
                        .build();
            } else {
                throw new BadCredentialsException("Invalid token for refresh");
            }

        } catch (Exception e) {
            logger.error("Token refresh failed: {}", e.getMessage());
            throw new BadCredentialsException("Token refresh failed");
        }
    }
}