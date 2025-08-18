package com.starwars.app.service;

import com.starwars.app.dto.AuthResponse;
import com.starwars.app.dto.LoginRequest;
import com.starwars.app.dto.RegisterRequest;
import com.starwars.app.dto.UserResponse;
import com.starwars.app.entity.User;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;

import java.time.LocalDateTime;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class AuthServiceTest {

    @Mock
    private UserService userService;

    @Mock
    private JwtService jwtService;

    @Mock
    private AuthenticationManager authenticationManager;

    @Mock
    private Authentication authentication;

    @InjectMocks
    private AuthService authService;

    private User testUser;
    private LoginRequest loginRequest;
    private RegisterRequest registerRequest;

    @BeforeEach
    void setUp() {
        testUser = User.builder()
                .id(1L)
                .username("luke")
                .email("luke@starwars.com")
                .password("encodedPassword")
                .firstName("Luke")
                .lastName("Skywalker")
                .role(User.Role.USER)
                .enabled(true)
                .createdAt(LocalDateTime.now())
                .build();

        loginRequest = new LoginRequest("luke", "force123");

        registerRequest = new RegisterRequest();
        registerRequest.setUsername("anakin");
        registerRequest.setEmail("anakin@starwars.com");
        registerRequest.setPassword("darkside123");
        registerRequest.setFirstName("Anakin");
        registerRequest.setLastName("Skywalker");
    }

    @Test
    void shouldLoginSuccessfully() {
        String expectedToken = "simulated token";

        when(authenticationManager.authenticate(any(UsernamePasswordAuthenticationToken.class)))
                .thenReturn(authentication);
        when(authentication.getPrincipal()).thenReturn(testUser);
        when(userService.findByUsername("luke")).thenReturn(Optional.of(testUser));
        when(jwtService.generateToken(testUser)).thenReturn(expectedToken);

        AuthResponse response = authService.login(loginRequest);

        assertThat(response).isNotNull();
        assertThat(response.getToken()).isEqualTo(expectedToken);
        assertThat(response.getType()).isEqualTo("Bearer");
        assertThat(response.getUser()).isNotNull();
        assertThat(response.getUser().getUsername()).isEqualTo("luke");
        assertThat(response.getUser().getEmail()).isEqualTo("luke@starwars.com");

        verify(authenticationManager).authenticate(any(UsernamePasswordAuthenticationToken.class));
        verify(userService).findByUsername("luke");
        verify(jwtService).generateToken(testUser);
    }

    @Test
    void shouldThrowExceptionWhenLoginWithInvalidCredentials() {
        // Given
        when(authenticationManager.authenticate(any(UsernamePasswordAuthenticationToken.class)))
                .thenThrow(new BadCredentialsException("Invalid credentials"));

        // When & Then
        assertThatThrownBy(() -> authService.login(loginRequest))
                .isInstanceOf(BadCredentialsException.class)
                .hasMessage("Invalid credentials");

        verify(authenticationManager).authenticate(any(UsernamePasswordAuthenticationToken.class));
        verifyNoInteractions(userService, jwtService);
    }

    @Test
    void shouldRegisterSuccessfully() {
        String expectedToken = "simulated token";
        User newUser = User.builder()
                .id(2L)
                .username("anakin")
                .email("anakin@starwars.com")
                .firstName("Anakin")
                .lastName("Skywalker")
                .role(User.Role.USER)
                .enabled(true)
                .createdAt(LocalDateTime.now())
                .build();

        when(userService.registerUser(
                "anakin",
                "anakin@starwars.com",
                "darkside123",
                "Anakin",
                "Skywalker"
        )).thenReturn(newUser);
        when(jwtService.generateToken(newUser)).thenReturn(expectedToken);

        AuthResponse response = authService.register(registerRequest);

        assertThat(response).isNotNull();
        assertThat(response.getToken()).isEqualTo(expectedToken);
        assertThat(response.getType()).isEqualTo("Bearer");
        assertThat(response.getUser()).isNotNull();
        assertThat(response.getUser().getUsername()).isEqualTo("anakin");
        assertThat(response.getUser().getEmail()).isEqualTo("anakin@starwars.com");

        verify(userService).registerUser("anakin", "anakin@starwars.com", "darkside123", "Anakin", "Skywalker");
        verify(jwtService).generateToken(newUser);
    }

    @Test
    void shouldThrowExceptionWhenRegisterWithExistingUsername() {
        when(userService.registerUser(anyString(), anyString(), anyString(), anyString(), anyString()))
                .thenThrow(new IllegalArgumentException("Username already exists: anakin"));

        assertThatThrownBy(() -> authService.register(registerRequest))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("Username already exists: anakin");

        verify(userService).registerUser("anakin", "anakin@starwars.com", "darkside123", "Anakin", "Skywalker");
        verifyNoInteractions(jwtService);
    }

    @Test
    void shouldValidateTokenSuccessfully() {
        String token = "valid token";

        when(jwtService.extractUsername(token)).thenReturn("luke");
        when(userService.findByUsername("luke")).thenReturn(Optional.of(testUser));
        when(jwtService.isTokenValid(token, testUser)).thenReturn(true);

        UserResponse response = authService.validateToken(token);

        assertThat(response).isNotNull();
        assertThat(response.getUsername()).isEqualTo("luke");
        assertThat(response.getEmail()).isEqualTo("luke@starwars.com");

        verify(jwtService).extractUsername(token);
        verify(userService).findByUsername("luke");
        verify(jwtService).isTokenValid(token, testUser);
    }

    @Test
    void shouldThrowExceptionWhenValidatingInvalidToken() {
        String token = "invalid token";

        when(jwtService.extractUsername(token)).thenReturn("luke");
        when(userService.findByUsername("luke")).thenReturn(Optional.of(testUser));
        when(jwtService.isTokenValid(token, testUser)).thenReturn(false);

        assertThatThrownBy(() -> authService.validateToken(token))
                .isInstanceOf(BadCredentialsException.class)
                .hasMessage("Invalid token");

        verify(jwtService).extractUsername(token);
        verify(userService).findByUsername("luke");
        verify(jwtService).isTokenValid(token, testUser);
    }

    @Test
    void shouldRefreshTokenSuccessfully() {
        String oldToken = "old token";
        String newToken = "new token";

        when(jwtService.extractUsername(oldToken)).thenReturn("luke");
        when(userService.findByUsername("luke")).thenReturn(Optional.of(testUser));
        when(jwtService.isTokenValid(oldToken, testUser)).thenReturn(true);
        when(jwtService.generateToken(testUser)).thenReturn(newToken);

        AuthResponse response = authService.refreshToken(oldToken);

        assertThat(response).isNotNull();
        assertThat(response.getToken()).isEqualTo(newToken);
        assertThat(response.getType()).isEqualTo("Bearer");
        assertThat(response.getUser().getUsername()).isEqualTo("luke");

        verify(jwtService).extractUsername(oldToken);
        verify(userService).findByUsername("luke");
        verify(jwtService).isTokenValid(oldToken, testUser);
        verify(jwtService).generateToken(testUser);
    }

    @Test
    void shouldThrowBadCredentialsInRefreshToken() {
        String oldToken = "old token";

        when(jwtService.extractUsername(oldToken)).thenReturn("luke");
        when(userService.findByUsername("luke")).thenReturn(Optional.of(testUser));
        when(jwtService.isTokenValid(oldToken, testUser)).thenReturn(false);

        assertThatThrownBy(() -> authService.refreshToken(oldToken))
                .isInstanceOf(BadCredentialsException.class)
                .hasMessage("Token refresh failed");
    }
}