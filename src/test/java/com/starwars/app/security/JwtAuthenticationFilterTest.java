package com.starwars.app.security;

import com.starwars.app.entity.User;
import com.starwars.app.service.JwtService;
import com.starwars.app.service.UserService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;

import javax.servlet.FilterChain;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.time.LocalDateTime;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class JwtAuthenticationFilterTest {

    @Mock
    private JwtService jwtService;

    @Mock
    private UserService userService;

    @Mock
    private HttpServletRequest request;

    @Mock
    private HttpServletResponse response;

    @Mock
    private FilterChain filterChain;

    @Mock
    private SecurityContext securityContext;

    @InjectMocks
    private JwtAuthenticationFilter jwtAuthenticationFilter;

    private UserDetails testUser;

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

        SecurityContextHolder.setContext(securityContext);
    }

    @Test
    void shouldAuthenticateUserWithValidJwtToken() throws ServletException, IOException {
        String validToken = "valid.jwt.token";
        String authHeader = "Bearer " + validToken;
        String username = "luke";

        when(request.getHeader("Authorization")).thenReturn(authHeader);
        when(jwtService.extractUsername(validToken)).thenReturn(username);
        when(securityContext.getAuthentication()).thenReturn(null);
        when(userService.loadUserByUsername(username)).thenReturn(testUser);
        when(jwtService.isTokenValid(validToken, testUser)).thenReturn(true);

        jwtAuthenticationFilter.doFilterInternal(request, response, filterChain);

        verify(jwtService).extractUsername(validToken);
        verify(userService).loadUserByUsername(username);
        verify(jwtService).isTokenValid(validToken, testUser);
        verify(securityContext).setAuthentication(any());
        verify(filterChain).doFilter(request, response);
    }

    @Test
    void shouldSkipAuthenticationWhenNoAuthorizationHeader() throws ServletException, IOException {
        when(request.getHeader("Authorization")).thenReturn(null);

        jwtAuthenticationFilter.doFilterInternal(request, response, filterChain);

        verify(filterChain).doFilter(request, response);
        verifyNoInteractions(jwtService, userService);
        verify(securityContext, never()).setAuthentication(any());
    }

    @Test
    void shouldSkipAuthenticationWhenAuthorizationHeaderDoesNotStartWithBearer() throws ServletException, IOException {
        when(request.getHeader("Authorization")).thenReturn("Basic someBasicToken");

        jwtAuthenticationFilter.doFilterInternal(request, response, filterChain);

        verify(filterChain).doFilter(request, response);
        verifyNoInteractions(jwtService, userService);
        verify(securityContext, never()).setAuthentication(any());
    }

    @Test
    void shouldSkipAuthenticationWhenUsernameIsNull() throws ServletException, IOException {
        String validToken = "valid.jwt.token";
        String authHeader = "Bearer " + validToken;

        when(request.getHeader("Authorization")).thenReturn(authHeader);
        when(jwtService.extractUsername(validToken)).thenReturn(null);

        jwtAuthenticationFilter.doFilterInternal(request, response, filterChain);

        verify(jwtService).extractUsername(validToken);
        verify(filterChain).doFilter(request, response);
        verifyNoInteractions(userService);
        verify(securityContext, never()).setAuthentication(any());
    }

    @Test
    void shouldSkipAuthenticationWhenUserAlreadyAuthenticated() throws ServletException, IOException {
        String validToken = "valid.jwt.token";
        String authHeader = "Bearer " + validToken;
        String username = "luke";
        Authentication existingAuth = mock(Authentication.class);

        when(request.getHeader("Authorization")).thenReturn(authHeader);
        when(jwtService.extractUsername(validToken)).thenReturn(username);
        when(securityContext.getAuthentication()).thenReturn(existingAuth);

        jwtAuthenticationFilter.doFilterInternal(request, response, filterChain);

        verify(jwtService).extractUsername(validToken);
        verify(filterChain).doFilter(request, response);
        verifyNoInteractions(userService);
        verify(securityContext, never()).setAuthentication(any());
    }

    @Test
    void shouldNotAuthenticateWhenTokenIsInvalid() throws ServletException, IOException {
        String invalidToken = "invalid.jwt.token";
        String authHeader = "Bearer " + invalidToken;
        String username = "luke";

        when(request.getHeader("Authorization")).thenReturn(authHeader);
        when(jwtService.extractUsername(invalidToken)).thenReturn(username);
        when(securityContext.getAuthentication()).thenReturn(null);
        when(userService.loadUserByUsername(username)).thenReturn(testUser);
        when(jwtService.isTokenValid(invalidToken, testUser)).thenReturn(false);

        jwtAuthenticationFilter.doFilterInternal(request, response, filterChain);

        verify(jwtService).extractUsername(invalidToken);
        verify(userService).loadUserByUsername(username);
        verify(jwtService).isTokenValid(invalidToken, testUser);
        verify(securityContext, never()).setAuthentication(any());
        verify(filterChain).doFilter(request, response);
    }

    @Test
    void shouldHandleJwtServiceExceptionGracefully() throws ServletException, IOException {
        String invalidToken = "malformed.jwt.token";
        String authHeader = "Bearer " + invalidToken;

        when(request.getHeader("Authorization")).thenReturn(authHeader);
        when(jwtService.extractUsername(invalidToken)).thenThrow(new RuntimeException("Malformed JWT"));

        jwtAuthenticationFilter.doFilterInternal(request, response, filterChain);

        verify(jwtService).extractUsername(invalidToken);
        verify(filterChain).doFilter(request, response);
        verifyNoInteractions(userService);
        verify(securityContext, never()).setAuthentication(any());
    }

    @Test
    void shouldHandleUserServiceExceptionGracefully() throws ServletException, IOException {
        String validToken = "valid.jwt.token";
        String authHeader = "Bearer " + validToken;
        String username = "nonexistent";

        when(request.getHeader("Authorization")).thenReturn(authHeader);
        when(jwtService.extractUsername(validToken)).thenReturn(username);
        when(securityContext.getAuthentication()).thenReturn(null);
        when(userService.loadUserByUsername(username)).thenThrow(new RuntimeException("User not found"));

        jwtAuthenticationFilter.doFilterInternal(request, response, filterChain);

        verify(jwtService).extractUsername(validToken);
        verify(userService).loadUserByUsername(username);
        verify(filterChain).doFilter(request, response);
        verify(securityContext, never()).setAuthentication(any());
    }

    @Test
    void shouldExtractJwtTokenCorrectly() throws ServletException, IOException {
        String token = "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9";
        String authHeader = "Bearer " + token;
        String username = "luke";

        when(request.getHeader("Authorization")).thenReturn(authHeader);
        when(jwtService.extractUsername(token)).thenReturn(username);
        when(securityContext.getAuthentication()).thenReturn(null);
        when(userService.loadUserByUsername(username)).thenReturn(testUser);
        when(jwtService.isTokenValid(token, testUser)).thenReturn(true);

        jwtAuthenticationFilter.doFilterInternal(request, response, filterChain);

        verify(jwtService).extractUsername(token);
        verify(jwtService).isTokenValid(token, testUser);
    }

    @Test
    void shouldSetAuthenticationDetailsCorrectly() throws ServletException, IOException {
        String validToken = "valid.jwt.token";
        String authHeader = "Bearer " + validToken;
        String username = "luke";

        when(request.getHeader("Authorization")).thenReturn(authHeader);
        when(jwtService.extractUsername(validToken)).thenReturn(username);
        when(securityContext.getAuthentication()).thenReturn(null);
        when(userService.loadUserByUsername(username)).thenReturn(testUser);
        when(jwtService.isTokenValid(validToken, testUser)).thenReturn(true);

        jwtAuthenticationFilter.doFilterInternal(request, response, filterChain);

        verify(securityContext).setAuthentication(argThat(auth -> {
            assertThat(auth.getPrincipal()).isEqualTo(testUser);
            assertThat(auth.getCredentials()).isNull();
            assertThat(auth.getAuthorities()).isEqualTo(testUser.getAuthorities());
            assertThat(auth.getDetails()).isNotNull();
            return true;
        }));
    }

    @Test
    void shouldHandleEmptyBearerToken() throws ServletException, IOException {
        String authHeader = "Bearer ";

        when(request.getHeader("Authorization")).thenReturn(authHeader);
        when(jwtService.extractUsername("")).thenThrow(new RuntimeException("Empty token"));

        jwtAuthenticationFilter.doFilterInternal(request, response, filterChain);

        verify(jwtService).extractUsername("");
        verify(filterChain).doFilter(request, response);
        verifyNoInteractions(userService);
        verify(securityContext, never()).setAuthentication(any());
    }

    @Test
    void shouldContinueFilterChainInAllScenarios() throws ServletException, IOException {

        when(request.getHeader("Authorization")).thenReturn(null);
        jwtAuthenticationFilter.doFilterInternal(request, response, filterChain);
        verify(filterChain, times(1)).doFilter(request, response);

        reset(filterChain, request);
        String validToken = "valid.jwt.token";
        String authHeader = "Bearer " + validToken;
        when(request.getHeader("Authorization")).thenReturn(authHeader);
        when(jwtService.extractUsername(validToken)).thenReturn("luke");
        when(securityContext.getAuthentication()).thenReturn(null);
        when(userService.loadUserByUsername("luke")).thenReturn(testUser);
        when(jwtService.isTokenValid(validToken, testUser)).thenReturn(true);

        jwtAuthenticationFilter.doFilterInternal(request, response, filterChain);
        verify(filterChain, times(1)).doFilter(request, response);

        reset(filterChain, request);
        when(request.getHeader("Authorization")).thenReturn(authHeader);
        when(jwtService.extractUsername(validToken)).thenThrow(new RuntimeException("Error"));

        jwtAuthenticationFilter.doFilterInternal(request, response, filterChain);
        verify(filterChain, times(1)).doFilter(request, response);
    }
}