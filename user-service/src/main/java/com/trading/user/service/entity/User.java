package com.trading.user.service.entity;

import java.math.BigDecimal;
import java.time.LocalDateTime;

import org.hibernate.annotations.CreationTimestamp;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * JPA entity representing a registered user of the platform, including their
 * credentials, profile and wallet balance.
 */
@Entity
@Table(name = "users")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class User {

    /** Unique identifier (UUID) of the user. */
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private String id;

    /** Unique email address, used as the login identifier. */
    @Column(nullable = false, unique = true)
    private String email;

    /** BCrypt-hashed password. Never stored in plain text. */
    @Column(nullable = false)
    private String password;

    /** First name of the user. */
    private String firstName;

    /** Last name of the user. */
    @Column(nullable = false)
    private String lastName;

    /** Current wallet balance, used for trading funds. */
    @Column(nullable = false, precision = 15, scale = 2)
    private BigDecimal walletBalance = BigDecimal.ZERO;

    /** Account status (active, suspended, blocked). */
    @Enumerated(EnumType.STRING)
    private UserStatus status = UserStatus.ACTIVE;

    /** Timestamp when the user was created. */
    @CreationTimestamp
    private LocalDateTime createdAt;
}
