package com.starwars.app.service;

import com.starwars.app.entity.User;
import com.starwars.app.repository.UserRepository;
import lombok.*;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Optional;

@Service
@RequiredArgsConstructor
@Transactional

public class UserService implements UserDetailsService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;

    private static final Logger logger = LoggerFactory.getLogger(UserService.class);


    @Override
    public UserDetails loadUserByUsername(String identifier) throws UsernameNotFoundException {
        logger.debug("Attempting to load user by identifier: {}", identifier);

        return userRepository.findActiveUserByUsernameOrEmail(identifier)
                .orElseThrow(() -> new UsernameNotFoundException(
                        "User not found with identifier: " + identifier));
    }


    public User registerUser(String username, String email, String password,
                             String firstName, String lastName) {
        logger.info("Registering new user with username: {}", username);


        if (userRepository.existsByUsername(username)) {
            throw new IllegalArgumentException("Username already exists: " + username);
        }

        if (userRepository.existsByEmail(email)) {
            throw new IllegalArgumentException("Email already exists: " + email);
        }

        User user = User.builder()
                .username(username)
                .email(email)
                .password(passwordEncoder.encode(password))
                .firstName(firstName)
                .lastName(lastName)
                .role(User.Role.USER)
                .enabled(true)
                .build();

        User savedUser = userRepository.save(user);
        logger.info("User registered successfully with ID: {}", savedUser.getId());

        return savedUser;
    }


    @Transactional(readOnly = true)
    public Optional<User> findByUsername(String username) {
        return userRepository.findByUsername(username);
    }

}