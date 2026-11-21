package com.trading.user.service.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

/**
 * Request payload for user login.
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class LoginRequest {

    /** Email address of the user. Required and must be a valid email. */
    @NotBlank(message = "Email is required")
    @Email(message = "Invalid email format")
    private String email;

    /** Password of the user. Required and at least 6 characters long. */
    @NotBlank(message = "Password is required")
    @Size(min = 6, message = "Password must be at least 6 character long")
    private String password;
}
