package com.trading.user.service.dto;

import java.math.BigDecimal;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Request payload for user registration.
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class RegisterRequest {

    /** Email address of the new user. Required and must be a valid email. */
    @NotBlank(message = "Email is required")
    @Email(message = "Invalid email format")
    private String email;

    /** Password of the new user. Required and at least 6 characters long. */
    @NotBlank(message = "Password is required")
    @Size(min = 6, message = "Password must be at least 6 character long")
    private String password;

    /** First name of the new user. Required. */
    @NotBlank(message = "First name is required")
    private String firstName;

    /** Last name of the new user. Required. */
    @NotBlank(message = "Last name is required")
    private String lastName;

    /** Optional initial deposit; defaults to 10000 when not provided. */
    private BigDecimal initialDeposit = BigDecimal.valueOf(10000);
}
