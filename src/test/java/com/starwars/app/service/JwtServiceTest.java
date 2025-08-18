package com.starwars.app.service;

import com.starwars.app.entity.User;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.ExpiredJwtException;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.MalformedJwtException;
import io.jsonwebtoken.security.Keys;
import io.jsonwebtoken.security.SignatureException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.test.util.ReflectionTestUtils;

import javax.crypto.SecretKey;
import java.nio.charset.StandardCharsets;
import java.time.LocalDateTime;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@ExtendWith(MockitoExtension.class)
class JwtServiceTest {

    @InjectMocks
    private JwtService jwtService;

    private final String secretKey = "mySecretKeyForTestingPurposesItNeedsToBeAtLeast32Characters";
    private final Long jwtExpiration = 86400000L;

    private UserDetails testUser;

    @BeforeEach
    void setUp() {
        ReflectionTestUtils.setField(jwtService, "secretKey", secretKey);
        ReflectionTestUtils.setField(jwtService, "jwtExpiration", jwtExpiration);

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
    }

    @Test
    void shouldGenerateTokenSuccessfully() {
        String token = jwtService.generateToken(testUser);

        assertThat(token).isNotNull();
        assertThat(token).isNotEmpty();
        assertThat(token.split("\\.")).hasSize(3);
    }

    @Test
    void shouldGenerateTokenWithExtraClaimsSuccessfully() {
        Map<String, Object> extraClaims = new HashMap<>();
        extraClaims.put("role", "JEDI");
        extraClaims.put("force", true);

        String token = jwtService.generateToken(extraClaims, testUser);

        assertThat(token).isNotNull();
        assertThat(token).isNotEmpty();
        assertThat(token.split("\\.")).hasSize(3);

        Claims claims = Jwts.parserBuilder()
                .setSigningKey(Keys.hmacShaKeyFor(secretKey.getBytes(StandardCharsets.UTF_8)))
                .build()
                .parseClaimsJws(token)
                .getBody();

        assertThat(claims.get("role")).isEqualTo("JEDI");
        assertThat(claims.get("force")).isEqualTo(true);
    }

    @Test
    void shouldExtractUsernameFromToken() {
        String token = jwtService.generateToken(testUser);

        String extractedUsername = jwtService.extractUsername(token);

        assertThat(extractedUsername).isEqualTo("luke");
    }

    @Test
    void shouldExtractExpirationFromToken() {
        String token = jwtService.generateToken(testUser);

        Date extractedExpiration = jwtService.extractExpiration(token);

        assertThat(extractedExpiration).isNotNull();
        assertThat(extractedExpiration).isAfter(new Date());

        long expectedExpiration = System.currentTimeMillis() + jwtExpiration;
        long actualExpiration = extractedExpiration.getTime();
        long tolerance = 5000; // 5 seconds tolerance
        assertThat(Math.abs(actualExpiration - expectedExpiration)).isLessThan(tolerance);
    }

    @Test
    void shouldExtractCustomClaimFromToken() {
        Map<String, Object> extraClaims = new HashMap<>();
        extraClaims.put("customClaim", "customValue");
        String token = jwtService.generateToken(extraClaims, testUser);

        String customClaim = jwtService.extractClaim(token, claims -> claims.get("customClaim", String.class));

        assertThat(customClaim).isEqualTo("customValue");
    }

    @Test
    void shouldValidateTokenSuccessfully() {
        String token = jwtService.generateToken(testUser);

        boolean isValid = jwtService.isTokenValid(token, testUser);

        assertThat(isValid).isTrue();
    }

    @Test
    void shouldInvalidateTokenWithWrongUsername() {
        String token = jwtService.generateToken(testUser);

        UserDetails anotherUser = User.builder()
                .username("vader")
                .email("vader@sith.com")
                .password("encodedPassword")
                .firstName("Darth")
                .lastName("Vader")
                .role(User.Role.USER)
                .enabled(true)
                .build();

        boolean isValid = jwtService.isTokenValid(token, anotherUser);

        assertThat(isValid).isFalse();
    }

    @Test
    void shouldInvalidateExpiredToken() {
        ReflectionTestUtils.setField(jwtService, "jwtExpiration", 1L);
        String token = jwtService.generateToken(testUser);

        try {
            Thread.sleep(10);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }

        assertThatThrownBy(() -> jwtService.isTokenValid(token, testUser))
                .isInstanceOf(ExpiredJwtException.class);
    }

    @Test
    void shouldThrowExceptionForMalformedToken() {
        String malformedToken = "this.is.not.a.valid.jwt.token";

        assertThatThrownBy(() -> jwtService.extractUsername(malformedToken))
                .isInstanceOf(MalformedJwtException.class);
    }

    @Test
    void shouldThrowExceptionForTokenWithInvalidSignature() {
        String token = jwtService.generateToken(testUser);
        String tokenWithTamperedSignature = token.substring(0, token.length() - 5) + "wrong";

        assertThatThrownBy(() -> jwtService.extractUsername(tokenWithTamperedSignature))
                .isInstanceOf(SignatureException.class);
    }

    @Test
    void shouldThrowExceptionForExpiredTokenWhenExtracting() {
        ReflectionTestUtils.setField(jwtService, "jwtExpiration", 1L);
        String token = jwtService.generateToken(testUser);

        try {
            Thread.sleep(10);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }

        assertThatThrownBy(() -> jwtService.extractUsername(token))
                .isInstanceOf(ExpiredJwtException.class);
    }

    @Test
    void shouldExtractIssuedAtDateFromToken() {
        long beforeTokenGeneration = System.currentTimeMillis() - 1000;
        String token = jwtService.generateToken(testUser);
        long afterTokenGeneration = System.currentTimeMillis() + 1000;

        Date issuedAt = jwtService.extractClaim(token, Claims::getIssuedAt);

        assertThat(issuedAt).isNotNull();
        assertThat(issuedAt.getTime()).isBetween(beforeTokenGeneration, afterTokenGeneration);
    }

    @Test
    void shouldHandleEmptyExtraClaimsMap() {
        Map<String, Object> emptyClaims = new HashMap<>();

        String token = jwtService.generateToken(emptyClaims, testUser);

        assertThat(token).isNotNull();
        String extractedUsername = jwtService.extractUsername(token);
        assertThat(extractedUsername).isEqualTo("luke");
    }

}