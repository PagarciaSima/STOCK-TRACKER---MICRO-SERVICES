package com.trading.user.service.dto;

import java.math.BigDecimal;
import java.time.LocalDateTime;

import com.trading.user.service.entity.UserStatus;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Public representation of a user, without sensitive fields (e.g. password).
 */
@Data
@AllArgsConstructor
@NoArgsConstructor
public class UserResponse {

    /** Identifier of the user. */
    private String id;

    /** Email of the user. */
    private String email;

    /** First name of the user. */
    private String firstName;

    /** Last name of the user. */
    private String lastName;

    /** Current wallet balance. */
    private BigDecimal walletBalance;

    /** Account status. */
    private UserStatus status;

    /** Timestamp when the user was created. */
    private LocalDateTime createdAt;
}
