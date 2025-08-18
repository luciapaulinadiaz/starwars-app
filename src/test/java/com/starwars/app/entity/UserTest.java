package com.starwars.app.entity;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.security.core.GrantedAuthority;

import javax.validation.ConstraintViolation;
import javax.validation.Validation;
import javax.validation.Validator;
import javax.validation.ValidatorFactory;
import java.time.LocalDateTime;
import java.util.Collection;
import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;

class UserTest {

    private Validator validator;
    private User validUser;

    @BeforeEach
    void setUp() {
        ValidatorFactory factory = Validation.buildDefaultValidatorFactory();
        validator = factory.getValidator();

        validUser = User.builder()
                .id(1L)
                .username("luke")
                .email("luke@starwars.com")
                .password("force123")
                .firstName("Luke")
                .lastName("Skywalker")
                .role(User.Role.USER)
                .enabled(true)
                .createdAt(LocalDateTime.now())
                .updatedAt(LocalDateTime.now())
                .build();
    }

    @Test
    void shouldCreateValidUser() {
        Set<ConstraintViolation<User>> violations = validator.validate(validUser);

        assertThat(violations).isEmpty();
        assertThat(validUser.getUsername()).isEqualTo("luke");
        assertThat(validUser.getEmail()).isEqualTo("luke@starwars.com");
        assertThat(validUser.getPassword()).isEqualTo("force123");
        assertThat(validUser.getFirstName()).isEqualTo("Luke");
        assertThat(validUser.getLastName()).isEqualTo("Skywalker");
        assertThat(validUser.getRole()).isEqualTo(User.Role.USER);
        assertThat(validUser.isEnabled()).isTrue();
    }

    @Test
    void shouldHaveDefaultValues() {
        User userWithDefaults = User.builder()
                .username("anakin")
                .email("anakin@starwars.com")
                .password("darkside123")
                .build();

        assertThat(userWithDefaults.getRole()).isEqualTo(User.Role.USER);
        assertThat(userWithDefaults.isEnabled()).isTrue();
    }

    @Test
    void shouldValidateEmailNotBlank() {
        User user = User.builder()
                .id(1L)
                .username("luke")
                .email("")
                .password("force123")
                .build();

        Set<ConstraintViolation<User>> violations = validator.validate(user);

        assertThat(violations).hasSize(1);
        assertThat(violations.iterator().next().getMessage()).isEqualTo("Email is required");
    }

    @Test
    void shouldValidateEmailFormat() {
        User user = User.builder()
                .id(1L)
                .username("luke")
                .email("invalid-email")
                .password("force123")
                .build();

        Set<ConstraintViolation<User>> violations = validator.validate(user);

        assertThat(violations).hasSize(1);
        assertThat(violations.iterator().next().getMessage()).isEqualTo("Invalid email format");
    }

    @Test
    void shouldValidatePasswordMinSize() {
        User user = User.builder()
                .id(1L)
                .username("luke")
                .email("luke@starwars.com")
                .password("12345")
                .build();

        Set<ConstraintViolation<User>> violations = validator.validate(user);

        assertThat(violations).hasSize(1);
        assertThat(violations.iterator().next().getPropertyPath().toString()).isEqualTo("password");
    }

    @Test
    void shouldImplementUserDetailsCorrectly() {
        Collection<? extends GrantedAuthority> authorities = validUser.getAuthorities();

        assertThat(authorities).hasSize(1);
        assertThat(authorities.iterator().next().getAuthority()).isEqualTo("ROLE_USER");

        assertThat(validUser.getUsername()).isEqualTo("luke");
        assertThat(validUser.getPassword()).isEqualTo("force123");
        assertThat(validUser.isAccountNonExpired()).isTrue();
        assertThat(validUser.isAccountNonLocked()).isTrue();
        assertThat(validUser.isCredentialsNonExpired()).isTrue();
        assertThat(validUser.isEnabled()).isTrue();
    }

    @Test
    void shouldReturnCorrectAuthoritiesForAdmin() {
        User adminUser = User.builder()
                .id(1L)
                .username("luke")
                .email("luke@starwars.com")
                .password("force123")
                .role(User.Role.ADMIN)
                .build();

        Collection<? extends GrantedAuthority> authorities = adminUser.getAuthorities();

        assertThat(authorities).hasSize(1);
        assertThat(authorities.iterator().next().getAuthority()).isEqualTo("ROLE_ADMIN");
    }

    @Test
    void shouldReturnFalseForIsEnabledWhenDisabled() {
        User disabledUser = User.builder()
                .id(1L)
                .username("luke")
                .email("luke@starwars.com")
                .password("force123")
                .enabled(false)
                .build();

        assertThat(disabledUser.isEnabled()).isFalse();
    }

    @Test
    void shouldTestBuilderPattern() {
        User user = User.builder()
                .id(2L)
                .username("vader")
                .email("vader@sith.com")
                .password("darkside123")
                .firstName("Darth")
                .lastName("Vader")
                .role(User.Role.ADMIN)
                .enabled(false)
                .build();

        assertThat(user.getId()).isEqualTo(2L);
        assertThat(user.getUsername()).isEqualTo("vader");
        assertThat(user.getEmail()).isEqualTo("vader@sith.com");
        assertThat(user.getPassword()).isEqualTo("darkside123");
        assertThat(user.getFirstName()).isEqualTo("Darth");
        assertThat(user.getLastName()).isEqualTo("Vader");
        assertThat(user.getRole()).isEqualTo(User.Role.ADMIN);
        assertThat(user.isEnabled()).isFalse();
    }

    @Test
    void shouldTestEqualsAndHashCode() {
        User user1 = User.builder()
                .id(1L)
                .username("luke")
                .email("luke@starwars.com")
                .password("force123")
                .build();

        User user2 = User.builder()
                .id(1L)
                .username("luke")
                .email("luke@starwars.com")
                .password("force123")
                .build();

        User user3 = User.builder()
                .id(2L)
                .username("vader")
                .email("vader@sith.com")
                .password("darkside123")
                .build();

        assertThat(user1).isEqualTo(user2);
        assertThat(user1).isNotEqualTo(user3);
        assertThat(user1.hashCode()).isEqualTo(user2.hashCode());
        assertThat(user1.hashCode()).isNotEqualTo(user3.hashCode());
    }

    @Test
    void shouldTestToString() {
        String userString = validUser.toString();

        assertThat(userString).contains("luke");
        assertThat(userString).contains("luke@starwars.com");
        assertThat(userString).contains("Luke");
        assertThat(userString).contains("Skywalker");
        assertThat(userString).contains("USER");
    }

    @Test
    void shouldTestNoArgsConstructor() {
        User user = new User();

        assertThat(user).isNotNull();
        assertThat(user.getId()).isNull();
        assertThat(user.getUsername()).isNull();
        assertThat(user.getEmail()).isNull();
        assertThat(user.getPassword()).isNull();
    }

    @Test
    void shouldTestAllArgsConstructor() {
        LocalDateTime now = LocalDateTime.now();

        User user = new User(1L, "obi-wan", "obi@jedi.com", "highground123",
                "Obi-Wan", "Kenobi", User.Role.USER, true, now, now);

        assertThat(user.getId()).isEqualTo(1L);
        assertThat(user.getUsername()).isEqualTo("obi-wan");
        assertThat(user.getEmail()).isEqualTo("obi@jedi.com");
        assertThat(user.getPassword()).isEqualTo("highground123");
        assertThat(user.getFirstName()).isEqualTo("Obi-Wan");
        assertThat(user.getLastName()).isEqualTo("Kenobi");
        assertThat(user.getRole()).isEqualTo(User.Role.USER);
        assertThat(user.isEnabled()).isTrue();
        assertThat(user.getCreatedAt()).isEqualTo(now);
        assertThat(user.getUpdatedAt()).isEqualTo(now);
    }

    @Test
    void shouldTestEnumValues() {
        assertThat(User.Role.values()).containsExactly(User.Role.USER, User.Role.ADMIN);
        assertThat(User.Role.valueOf("USER")).isEqualTo(User.Role.USER);
        assertThat(User.Role.valueOf("ADMIN")).isEqualTo(User.Role.ADMIN);
    }
}