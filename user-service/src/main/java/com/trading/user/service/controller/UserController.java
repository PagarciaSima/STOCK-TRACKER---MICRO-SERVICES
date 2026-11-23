package com.trading.user.service.controller;

import java.math.BigDecimal;
import java.util.List;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.trading.user.service.dto.AuthResponse;
import com.trading.user.service.dto.FundsOperationResponse;
import com.trading.user.service.dto.LoginRequest;
import com.trading.user.service.dto.RegisterRequest;
import com.trading.user.service.dto.UserResponse;
import com.trading.user.service.security.AuthorizationGuard;
import com.trading.user.service.service.UserService;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;

/**
 * REST controller for user registration, authentication, profile and wallet
 * operations.
 * <p>
 * The authenticated user id and role are received from the API gateway through
 * the {@code X-User-Id} and {@code X-User-Role} headers.
 */
@RestController
@RequestMapping("/api/v1/users")
@RequiredArgsConstructor
public class UserController {

    private final UserService userService;
    private final AuthorizationGuard authorizationGuard;

    /**
     * Registers a new user and returns JWT tokens plus the user profile.
     *
     * @param request the registration request. Must be valid (valid email, non-blank password).
     * @return {@code 201 Created} with the {@link AuthResponse}.
     */
    @PostMapping("/register")
    public ResponseEntity<AuthResponse> register(
            @Valid @RequestBody RegisterRequest request){

        return ResponseEntity.status(HttpStatus.CREATED)
                .body(userService.register(request));
    }

    /**
     * Authenticates a user and returns JWT tokens plus the user profile.
     *
     * @param request the login request. Must be valid (valid email, non-blank password).
     * @return {@code 200 OK} with the {@link AuthResponse}.
     */
    @PostMapping("/login")
    public ResponseEntity<AuthResponse> login(
            @Valid @RequestBody LoginRequest request){

        return ResponseEntity.ok(userService.login(request));
    }

    /**
     * Returns all registered users. Admin-only endpoint.
     *
     * @param role the authenticated user's role (from the gateway). Cannot be null.
     * @return {@code 200 OK} with the list of users.
     * @throws com.trading.user.service.exception.AccessDeniedException if the caller is not an admin.
     */
    @GetMapping("/all")
    public ResponseEntity<List<UserResponse>> getAllUsers(
            @RequestHeader("X-User-Role") String role){

        authorizationGuard.requireAdmin(role);
        return ResponseEntity.ok(userService.getAllUsers());
    }

    /**
     * Returns the profile of the authenticated user.
     *
     * @param userId the authenticated user id (from the gateway). Cannot be null or blank.
     * @return {@code 200 OK} with the {@link UserResponse}.
     */
    @GetMapping("/me")
    public ResponseEntity<UserResponse> getMyProfile(
            @RequestHeader("X-User-Id") String userId){

        return ResponseEntity.ok(userService.getUserById(userId));
    }

    /**
     * Returns the profile of an arbitrary user by id. Admins can access any user;
     * a standard user can only access their own profile.
     *
     * @param userId the identifier of the target user. Cannot be null or blank.
     * @param requesterId the authenticated user id (from the gateway). Cannot be null or blank.
     * @param role the authenticated user's role (from the gateway). Cannot be null.
     * @return {@code 200 OK} with the {@link UserResponse}.
     * @throws com.trading.user.service.exception.AccessDeniedException if the caller is not an admin nor the owner.
     */
    @GetMapping("/{userId}")
    public ResponseEntity<UserResponse> getUserById(
            @PathVariable String userId,
            @RequestHeader("X-User-Id") String requesterId,
            @RequestHeader("X-User-Role") String role){

        authorizationGuard.requireOwnerOrAdmin(requesterId, role, userId);
        return ResponseEntity.ok(userService.getUserById(userId));
    }

    /**
     * Add funds to a user's wallet. Admins can target any user; a standard user
     * can only target their own wallet.
     *
     * @param userId the identifier of the target user. Cannot be null or blank.
     * @param amount the amount to add. Cannot be null or negative.
     * @param requesterId the authenticated user id (from the gateway). Cannot be null or blank.
     * @param role the authenticated user's role (from the gateway). Cannot be null.
     * @return a {@link FundsOperationResponse} with the new balance.
     * @throws com.trading.user.service.exception.AccessDeniedException if the caller is not an admin nor the owner.
     */
    @PostMapping("/{userId}/funds/add")
    public ResponseEntity<FundsOperationResponse> addFunds(
            @PathVariable String userId,
            @RequestParam BigDecimal amount,
            @RequestHeader("X-User-Id") String requesterId,
            @RequestHeader("X-User-Role") String role){

        authorizationGuard.requireOwnerOrAdmin(requesterId, role, userId);
        return ResponseEntity.ok(
                userService.addFunds(userId, amount));
    }

    /**
     * Deduct funds from a user's wallet. Admins can target any user; a standard
     * user can only target their own wallet.
     *
     * @param userId the identifier of the target user. Cannot be null or blank.
     * @param amount the amount to deduct. Cannot be null or negative.
     * @param requesterId the authenticated user id (from the gateway). Cannot be null or blank.
     * @param role the authenticated user's role (from the gateway). Cannot be null.
     * @return a {@link FundsOperationResponse} with the new balance.
     * @throws com.trading.user.service.exception.AccessDeniedException if the caller is not an admin nor the owner.
     */
    @PostMapping("/{userId}/funds/deduct")
    public ResponseEntity<FundsOperationResponse> deductFunds(
            @PathVariable String userId,
            @RequestParam BigDecimal amount,
            @RequestHeader("X-User-Id") String requesterId,
            @RequestHeader("X-User-Role") String role){

        authorizationGuard.requireOwnerOrAdmin(requesterId, role, userId);
        return ResponseEntity.ok(
                userService.deductFunds(userId, amount));
    }

    /**
     * Credit funds to a user's wallet (used for SELL orders). Admins can target
     * any user; a standard user can only target their own wallet.
     *
     * @param userId the identifier of the target user. Cannot be null or blank.
     * @param amount the amount to credit. Cannot be null or negative.
     * @param requesterId the authenticated user id (from the gateway). Cannot be null or blank.
     * @param role the authenticated user's role (from the gateway). Cannot be null.
     * @return a {@link FundsOperationResponse} with the new balance.
     * @throws com.trading.user.service.exception.AccessDeniedException if the caller is not an admin nor the owner.
     */
    @PostMapping("/{userId}/funds/credit")
    public ResponseEntity<FundsOperationResponse> creditFunds(
            @PathVariable String userId,
            @RequestParam BigDecimal amount,
            @RequestHeader("X-User-Id") String requesterId,
            @RequestHeader("X-User-Role") String role){

        authorizationGuard.requireOwnerOrAdmin(requesterId, role, userId);
        return ResponseEntity.ok(
                userService.creditFunds(userId, amount));
    }

    // ---------------------------------------------------------------------
    // Internal (service-to-service) endpoints. Not intended for external
    // clients; blocked at the API gateway (/internal/**). Used by order-service
    // via Feign to move funds when placing orders.
    // ---------------------------------------------------------------------

    /**
     * Internal: deduct funds from a user's wallet (called by order-service on BUY).
     *
     * @param userId the identifier of the user. Cannot be null or blank.
     * @param amount the amount to deduct. Cannot be null or negative.
     * @return a {@link FundsOperationResponse} with the new balance.
     */
    @PostMapping("/internal/{userId}/funds/deduct")
    public ResponseEntity<FundsOperationResponse> internalDeductFunds(
            @PathVariable String userId,
            @RequestParam BigDecimal amount){

        return ResponseEntity.ok(
                userService.deductFunds(userId, amount));
    }

    /**
     * Internal: credit funds to a user's wallet (called by order-service on SELL).
     *
     * @param userId the identifier of the user. Cannot be null or blank.
     * @param amount the amount to credit. Cannot be null or negative.
     * @return a {@link FundsOperationResponse} with the new balance.
     */
    @PostMapping("/internal/{userId}/funds/credit")
    public ResponseEntity<FundsOperationResponse> internalCreditFunds(
            @PathVariable String userId,
            @RequestParam BigDecimal amount){

        return ResponseEntity.ok(
                userService.creditFunds(userId, amount));
    }
}
