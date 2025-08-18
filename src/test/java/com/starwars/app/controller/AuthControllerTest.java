package com.starwars.app.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.starwars.app.dto.AuthResponse;
import com.starwars.app.dto.LoginRequest;
import com.starwars.app.dto.RegisterRequest;
import com.starwars.app.dto.UserResponse;
import com.starwars.app.service.AuthService;
import com.starwars.app.service.JwtService;
import com.starwars.app.service.UserService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.web.servlet.MockMvc;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(AuthController.class)
class AuthControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockBean
    private AuthService authService;

    @MockBean
    private UserService userService;

    @MockBean
    private PasswordEncoder passwordEncoder;

    @MockBean
    private JwtService jwtService;

    @Test
    void shouldLoginSuccessfully() throws Exception {
        LoginRequest loginRequest = new LoginRequest("user@example.com", "password123");
        UserResponse userResponse = new UserResponse(1L,"username","email", "firstName", "lastName", "USER", true, null);

        AuthResponse mockResponse = new AuthResponse("mock-jwt-token", "Bearer", userResponse);
        when(authService.login(any(LoginRequest.class))).thenReturn(mockResponse);

        mockMvc.perform(post("/api/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(loginRequest))
                        .with(csrf()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.token").value("mock-jwt-token"))
                .andExpect(jsonPath("$.type").value("Bearer"));
    }

    @Test
    void shouldRegisterSuccessfully() throws Exception {
        RegisterRequest registerRequest = new RegisterRequest("username", "user@example.com", "password123", "firstName", "lastName");
        UserResponse userResponse = new UserResponse(1L,"username","user@example.com", "firstName", "lastName", "USER", true, null);

        AuthResponse mockResponse = new AuthResponse("new-user-token", "Bearer", userResponse);
        when(authService.register(any(RegisterRequest.class))).thenReturn(mockResponse);

        mockMvc.perform(post("/api/auth/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(registerRequest))
                        .with(csrf()))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.token").value("new-user-token"))
                .andExpect(jsonPath("$.type").value("Bearer"));
    }

    @Test
    @WithMockUser(username = "user", roles = { "USER" })
    void shouldValidateTokenSuccessfully() throws Exception {

        UserResponse mockUser = new UserResponse(1L,"username","user@example.com", "firstName", "lastName", "USER", true, null);
        when(authService.validateToken("token123")).thenReturn(mockUser);

        mockMvc.perform(get("/api/auth/validate")
                        .header("Authorization", "Bearer token123"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.username").value("username"))
                .andExpect(jsonPath("$.email").value("user@example.com"))
                .andExpect(jsonPath("$.role").value("USER"));
    }

}
