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

import io.swagger.v3.oas.annotations.Hidden;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;

/**
 * REST controller for user registration, authentication, profile and wallet
 * operations.
 * <p>
 * The authenticated user id and role are received from the API gateway through
 * the {@code X-User-Id} and {@code X-User-Role} headers.
 */
@Tag(name = "Users", description = "Registration, authentication, profiles and wallet operations")
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
    @Operation(summary = "Register a new user", description = "Creates a user, publishes a Kafka event and returns JWT tokens.")
    @ApiResponses({
            @ApiResponse(responseCode = "201", description = "User registered"),
            @ApiResponse(responseCode = "400", description = "Invalid registration request")
    })
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
    @Operation(summary = "Login", description = "Authenticates a user and returns JWT access and refresh tokens.")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Authenticated"),
            @ApiResponse(responseCode = "400", description = "Invalid login request"),
            @ApiResponse(responseCode = "401", description = "Invalid credentials")
    })
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
     */
    @Operation(summary = "List all users (admin)", description = "Returns all registered users. Requires ADMIN role.")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Users returned"),
            @ApiResponse(responseCode = "403", description = "Admin privileges required")
    })
    @GetMapping("/all")
    public ResponseEntity<List<UserResponse>> getAllUsers(
            @Parameter(description = "Authenticated user role injected by the gateway", required = true)
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
    @Operation(summary = "Get my profile", description = "Returns the authenticated user's profile.")
    @ApiResponse(responseCode = "200", description = "Profile returned")
    @GetMapping("/me")
    public ResponseEntity<UserResponse> getMyProfile(
            @Parameter(description = "Authenticated user id injected by the gateway", required = true)
            @RequestHeader("X-User-Id") String userId){

        return ResponseEntity.ok(userService.getUserById(userId));
    }

    /**
     * Returns the profile of an arbitrary user by id (admin or owner).
     *
     * @param userId      the identifier of the target user. Cannot be null or blank.
     * @param requesterId the authenticated user id (from the gateway). Cannot be null or blank.
     * @param role        the authenticated user's role (from the gateway). Cannot be null.
     * @return {@code 200 OK} with the {@link UserResponse}.
     */
    @Operation(summary = "Get a user by id",
            description = "Returns a user profile. Admins can access any user; users only their own.")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "User returned"),
            @ApiResponse(responseCode = "403", description = "Access denied")
    })
    @GetMapping("/{userId}")
    public ResponseEntity<UserResponse> getUserById(
            @Parameter(description = "Target user identifier", required = true) @PathVariable String userId,
            @Parameter(description = "Authenticated user id injected by the gateway", required = true)
            @RequestHeader("X-User-Id") String requesterId,
            @Parameter(description = "Authenticated user role injected by the gateway", required = true)
            @RequestHeader("X-User-Role") String role){

        authorizationGuard.requireOwnerOrAdmin(requesterId, role, userId);
        return ResponseEntity.ok(userService.getUserById(userId));
    }

    /**
     * Add funds to a user's wallet (admin or owner).
     *
     * @param userId      the identifier of the target user. Cannot be null or blank.
     * @param amount      the amount to add. Cannot be null or negative.
     * @param requesterId the authenticated user id (from the gateway). Cannot be null or blank.
     * @param role        the authenticated user's role (from the gateway). Cannot be null.
     * @return a {@link FundsOperationResponse} with the new balance.
     */
    @Operation(summary = "Add funds", description = "Adds funds to a wallet. Admins can target any user; users only their own.")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Funds added"),
            @ApiResponse(responseCode = "403", description = "Access denied")
    })
    @PostMapping("/{userId}/funds/add")
    public ResponseEntity<FundsOperationResponse> addFunds(
            @Parameter(description = "Target user identifier", required = true) @PathVariable String userId,
            @Parameter(description = "Amount to add", required = true) @RequestParam BigDecimal amount,
            @Parameter(description = "Authenticated user id injected by the gateway", required = true)
            @RequestHeader("X-User-Id") String requesterId,
            @Parameter(description = "Authenticated user role injected by the gateway", required = true)
            @RequestHeader("X-User-Role") String role){

        authorizationGuard.requireOwnerOrAdmin(requesterId, role, userId);
        return ResponseEntity.ok(
                userService.addFunds(userId, amount));
    }

    /**
     * Deduct funds from a user's wallet (admin or owner).
     *
     * @param userId      the identifier of the target user. Cannot be null or blank.
     * @param amount      the amount to deduct. Cannot be null or negative.
     * @param requesterId the authenticated user id (from the gateway). Cannot be null or blank.
     * @param role        the authenticated user's role (from the gateway). Cannot be null.
     * @return a {@link FundsOperationResponse} with the new balance.
     */
    @Operation(summary = "Deduct funds", description = "Deducts funds from a wallet. Admins can target any user; users only their own.")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Funds deducted"),
            @ApiResponse(responseCode = "403", description = "Access denied")
    })
    @PostMapping("/{userId}/funds/deduct")
    public ResponseEntity<FundsOperationResponse> deductFunds(
            @Parameter(description = "Target user identifier", required = true) @PathVariable String userId,
            @Parameter(description = "Amount to deduct", required = true) @RequestParam BigDecimal amount,
            @Parameter(description = "Authenticated user id injected by the gateway", required = true)
            @RequestHeader("X-User-Id") String requesterId,
            @Parameter(description = "Authenticated user role injected by the gateway", required = true)
            @RequestHeader("X-User-Role") String role){

        authorizationGuard.requireOwnerOrAdmin(requesterId, role, userId);
        return ResponseEntity.ok(
                userService.deductFunds(userId, amount));
    }

    /**
     * Credit funds to a user's wallet (admin or owner).
     *
     * @param userId      the identifier of the target user. Cannot be null or blank.
     * @param amount      the amount to credit. Cannot be null or negative.
     * @param requesterId the authenticated user id (from the gateway). Cannot be null or blank.
     * @param role        the authenticated user's role (from the gateway). Cannot be null.
     * @return a {@link FundsOperationResponse} with the new balance.
     */
    @Operation(summary = "Credit funds", description = "Credits funds to a wallet. Admins can target any user; users only their own.")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Funds credited"),
            @ApiResponse(responseCode = "403", description = "Access denied")
    })
    @PostMapping("/{userId}/funds/credit")
    public ResponseEntity<FundsOperationResponse> creditFunds(
            @Parameter(description = "Target user identifier", required = true) @PathVariable String userId,
            @Parameter(description = "Amount to credit", required = true) @RequestParam BigDecimal amount,
            @Parameter(description = "Authenticated user id injected by the gateway", required = true)
            @RequestHeader("X-User-Id") String requesterId,
            @Parameter(description = "Authenticated user role injected by the gateway", required = true)
            @RequestHeader("X-User-Role") String role){

        authorizationGuard.requireOwnerOrAdmin(requesterId, role, userId);
        return ResponseEntity.ok(
                userService.creditFunds(userId, amount));
    }

    // ---------------------------------------------------------------------
    // Internal (service-to-service) endpoints. Not intended for external
    // clients; blocked at the API gateway (/internal/**). Hidden from Swagger.
    // ---------------------------------------------------------------------

    /**
     * Internal: deduct funds (called by order-service on BUY).
     *
     * @param userId the identifier of the user. Cannot be null or blank.
     * @param amount the amount to deduct. Cannot be null or negative.
     * @return a {@link FundsOperationResponse} with the new balance.
     */
    @Hidden
    @PostMapping("/internal/{userId}/funds/deduct")
    public ResponseEntity<FundsOperationResponse> internalDeductFunds(
            @PathVariable String userId,
            @RequestParam BigDecimal amount){

        return ResponseEntity.ok(
                userService.deductFunds(userId, amount));
    }

    /**
     * Internal: credit funds (called by order-service on SELL).
     *
     * @param userId the identifier of the user. Cannot be null or blank.
     * @param amount the amount to credit. Cannot be null or negative.
     * @return a {@link FundsOperationResponse} with the new balance.
     */
    @Hidden
    @PostMapping("/internal/{userId}/funds/credit")
    public ResponseEntity<FundsOperationResponse> internalCreditFunds(
            @PathVariable String userId,
            @RequestParam BigDecimal amount){

        return ResponseEntity.ok(
                userService.creditFunds(userId, amount));
    }
}
