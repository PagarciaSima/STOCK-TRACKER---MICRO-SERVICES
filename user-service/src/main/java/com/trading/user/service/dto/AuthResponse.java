package com.trading.user.service.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.math.BigDecimal;

/**
 * Authentication response returned after register or login, containing the
 * authenticated user's profile and JWT tokens.
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class AuthResponse {

    /** Identifier of the authenticated user. */
    private String userId;

    /** Email of the authenticated user. */
    private String email;

    /** First name of the authenticated user. */
    private String firstName;

    /** Last name of the authenticated user. */
    private String lastName;

    /** Current wallet balance. */
    private BigDecimal walletBalance;

    /** Signed JWT access token. */
    private String accessToken;

    /** Signed JWT refresh token. */
    private String refreshToken;

    /** Token type, always "Bearer". */
    private String tokenType = "Bearer";
}
