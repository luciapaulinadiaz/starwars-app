package com.starwars.app.repository;

import com.starwars.app.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface UserRepository extends JpaRepository<User, Long> {


    Optional<User> findByUsername(String username);

    boolean existsByUsername(String username);


    boolean existsByEmail(String email);


    @Query("SELECT u FROM User u WHERE (u.username = :identifier OR u.email = :identifier) AND u.enabled = true")
    Optional<User> findActiveUserByUsernameOrEmail(@Param("identifier") String identifier);

}