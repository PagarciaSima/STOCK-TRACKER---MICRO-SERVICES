package com.trading.user.service.repository;

import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.trading.user.service.entity.User;

/**
 * Spring Data JPA repository for {@link User} entities.
 */
@Repository
public interface UserRepository extends JpaRepository<User, String> {

    /**
     * Checks whether a user with the given email already exists.
     *
     * @param email the email to check. Cannot be null or blank.
     * @return {@code true} if a user with the email exists, {@code false} otherwise.
     */
    boolean existsByEmail(String email);

    /**
     * Finds a user by email.
     *
     * @param email the email to search for. Cannot be null or blank.
     * @return an {@link Optional} containing the user if found, empty otherwise.
     */
    Optional<User> findByEmail(String email);
}
