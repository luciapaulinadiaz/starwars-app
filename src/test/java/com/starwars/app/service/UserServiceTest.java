package com.starwars.app.service;

import com.starwars.app.entity.User;
import com.starwars.app.repository.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.time.LocalDateTime;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class UserServiceTest {

    @Mock
    private UserRepository userRepository;

    @Mock
    private PasswordEncoder passwordEncoder;

    @InjectMocks
    private UserService userService;

    private User testUser;

    @BeforeEach
    void setUp() {
        testUser = User.builder()
                .id(1L)
                .username("obiwan")
                .email("obiwan@jedi.com")
                .password("encodedPassword")
                .firstName("Obi-Wan")
                .lastName("Kenobi")
                .role(User.Role.USER)
                .enabled(true)
                .createdAt(LocalDateTime.now())
                .build();
    }

    @Test
    void shouldLoadUserByUsernameSuccessfully() {
        String identifier = "obiwan";
        when(userRepository.findActiveUserByUsernameOrEmail(identifier))
                .thenReturn(Optional.of(testUser));

        UserDetails result = userService.loadUserByUsername(identifier);

        assertThat(result).isNotNull();
        assertThat(result.getUsername()).isEqualTo("obiwan");
        assertThat(result.isEnabled()).isTrue();

        verify(userRepository).findActiveUserByUsernameOrEmail(identifier);
    }

    @Test
    void shouldLoadUserByEmailSuccessfully() {
        String identifier = "obiwan@jedi.com";
        when(userRepository.findActiveUserByUsernameOrEmail(identifier))
                .thenReturn(Optional.of(testUser));

        UserDetails result = userService.loadUserByUsername(identifier);

        assertThat(result).isNotNull();
        assertThat(result.getUsername()).isEqualTo("obiwan");
        assertThat(result.isEnabled()).isTrue();

        verify(userRepository).findActiveUserByUsernameOrEmail(identifier);
    }

    @Test
    void shouldThrowExceptionWhenLoadUserByUsernameNotFound() {
        String identifier = "nonexistent";
        when(userRepository.findActiveUserByUsernameOrEmail(identifier))
                .thenReturn(Optional.empty());

        assertThatThrownBy(() -> userService.loadUserByUsername(identifier))
                .isInstanceOf(UsernameNotFoundException.class)
                .hasMessage("User not found with identifier: " + identifier);

        verify(userRepository).findActiveUserByUsernameOrEmail(identifier);
    }

    @Test
    void shouldRegisterUserSuccessfully() {
        String username = "yoda";
        String email = "yoda@jedi.com";
        String password = "force900";
        String firstName = "Master";
        String lastName = "Yoda";
        String encodedPassword = "encodedForce900";

        User expectedUser = User.builder()
                .id(2L)
                .username(username)
                .email(email)
                .password(encodedPassword)
                .firstName(firstName)
                .lastName(lastName)
                .role(User.Role.USER)
                .enabled(true)
                .createdAt(LocalDateTime.now())
                .build();

        when(userRepository.existsByUsername(username)).thenReturn(false);
        when(userRepository.existsByEmail(email)).thenReturn(false);
        when(passwordEncoder.encode(password)).thenReturn(encodedPassword);
        when(userRepository.save(any(User.class))).thenReturn(expectedUser);

        User result = userService.registerUser(username, email, password, firstName, lastName);

        assertThat(result).isNotNull();
        assertThat(result.getUsername()).isEqualTo(username);
        assertThat(result.getEmail()).isEqualTo(email);
        assertThat(result.getPassword()).isEqualTo(encodedPassword);
        assertThat(result.getFirstName()).isEqualTo(firstName);
        assertThat(result.getLastName()).isEqualTo(lastName);
        assertThat(result.getRole()).isEqualTo(User.Role.USER);
        assertThat(result.isEnabled()).isTrue();

        verify(userRepository).existsByUsername(username);
        verify(userRepository).existsByEmail(email);
        verify(passwordEncoder).encode(password);
        verify(userRepository).save(any(User.class));
    }

    @Test
    void shouldThrowExceptionWhenRegisterUserWithExistingUsername() {
        String username = "vader";
        String email = "vader@sith.com";
        String password = "darkside";
        String firstName = "Darth";
        String lastName = "Vader";

        when(userRepository.existsByUsername(username)).thenReturn(true);

        assertThatThrownBy(() -> userService.registerUser(username, email, password, firstName, lastName))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("Username already exists: " + username);

        verify(userRepository).existsByUsername(username);
        verifyNoMoreInteractions(userRepository);
        verifyNoInteractions(passwordEncoder);
    }

    @Test
    void shouldThrowExceptionWhenRegisterUserWithExistingEmail() {
        String username = "palpatine";
        String email = "obiwan@jedi.com";
        String password = "emperor";
        String firstName = "Emperor";
        String lastName = "Palpatine";

        when(userRepository.existsByUsername(username)).thenReturn(false);
        when(userRepository.existsByEmail(email)).thenReturn(true);

        assertThatThrownBy(() -> userService.registerUser(username, email, password, firstName, lastName))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("Email already exists: " + email);

        verify(userRepository).existsByUsername(username);
        verify(userRepository).existsByEmail(email);
        verifyNoMoreInteractions(userRepository);
        verifyNoInteractions(passwordEncoder);
    }

    @Test
    void shouldFindByUsernameSuccessfully() {
        String username = "obiwan";
        when(userRepository.findByUsername(username)).thenReturn(Optional.of(testUser));

        Optional<User> result = userService.findByUsername(username);

        assertThat(result).isPresent();
        assertThat(result.get().getUsername()).isEqualTo(username);
        assertThat(result.get().getEmail()).isEqualTo("obiwan@jedi.com");

        verify(userRepository).findByUsername(username);
    }

    @Test
    void shouldReturnEmptyWhenFindByUsernameNotFound() {
        String username = "nonexistent";
        when(userRepository.findByUsername(username)).thenReturn(Optional.empty());

        Optional<User> result = userService.findByUsername(username);

        assertThat(result).isEmpty();

        verify(userRepository).findByUsername(username);
    }

}