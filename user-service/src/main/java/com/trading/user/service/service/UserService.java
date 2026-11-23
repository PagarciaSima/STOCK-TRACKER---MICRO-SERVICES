package com.trading.user.service.service;


import java.math.BigDecimal;
import java.time.Instant;
import java.util.Date;
import java.util.List;

import javax.crypto.SecretKey;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import com.trading.user.service.dto.AuthResponse;
import com.trading.user.service.dto.FundsOperationResponse;
import com.trading.user.service.dto.LoginRequest;
import com.trading.user.service.dto.RegisterRequest;
import com.trading.user.service.dto.UserRegisteredEvent;
import com.trading.user.service.dto.UserResponse;
import com.trading.user.service.entity.Role;
import com.trading.user.service.entity.User;
import com.trading.user.service.repository.UserRepository;

import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.io.Decoders;
import io.jsonwebtoken.security.Keys;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Service
@Slf4j
@RequiredArgsConstructor
public class UserService {

    @Value("${jwt.secret}")
    private String secret;

    @Value("${jwt.expiration}")
    private long jwtExpiration;

    @Value("${jwt.refresh-expiration}")
    private long refreshExpiration;

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final KafkaTemplate<String, UserRegisteredEvent> kafkaTemplate;
    private static final String USER_REGISTERED_TOPIC = "user.registered";

    /**
     * Register a new user.
     * <p>
     * Flow:
     * <ol>
     *     <li>Validate that the email is unique.</li>
     *     <li>Create and persist the user entity.</li>
     *     <li>Publish a {@code user.registered} Kafka event.</li>
     *     <li>Generate access and refresh JWT tokens.</li>
     *     <li>Build the authentication response.</li>
     * </ol>
     *
     * @param request the registration request containing email, password, firstName,
     *                lastName and optional initialDeposit.
     *                {@code email} must be valid and unique; {@code password} must not be blank.
     * @return the authentication response with tokens and user data.
     * @throws RuntimeException if the email is already registered.
     */
    public AuthResponse register(RegisterRequest request) {
        log.info("Registering user: {}", request.getEmail());

        validateEmailUnique(request.getEmail());
        User user = createUserEntity(request);
        User savedUser = userRepository.save(user);
        log.info("User registered: {}", savedUser.getId());

        publishUserRegisteredEvent(savedUser);

        String[] tokens = generateTokens(savedUser.getId(), savedUser.getEmail(), resolveRole(savedUser));
        return buildAuthResponse(savedUser, tokens[0], tokens[1]);
    }

    /**
     * Validate that the email is not already registered.
     *
     * @param email the email to validate. Cannot be null or blank.
     * @throws RuntimeException if the email is already registered.
     */
    private void validateEmailUnique(String email) {
        if (userRepository.existsByEmail(email)) {
            throw new RuntimeException("Email already registered " + email);
        }
    }

    /**
     * Create a {@link User} entity from the registration request.
     *
     * @param request the registration request. Cannot be null.
     * @return a new (unsaved) {@link User} entity.
     */
    private User createUserEntity(RegisterRequest request) {
        User user = new User();
        user.setFirstName(request.getFirstName());
        user.setLastName(request.getLastName());
        user.setEmail(request.getEmail());
        user.setPassword(passwordEncoder.encode(request.getPassword()));
        user.setWalletBalance(
                request.getInitialDeposit() != null
                        ? request.getInitialDeposit()
                        // initial fictitious balance
                        : BigDecimal.valueOf(10000)
        );
        user.setRole(Role.USER);
        return user;
    }

    /**
     * Publish a typed {@link UserRegisteredEvent} to the {@code user.registered} Kafka topic.
     *
     * @param savedUser the saved user. Cannot be null.
     */
    private void publishUserRegisteredEvent(User savedUser) {
        UserRegisteredEvent event = new UserRegisteredEvent(
                savedUser.getId(),
                savedUser.getEmail(),
                savedUser.getFirstName(),
                savedUser.getLastName(),
                savedUser.getWalletBalance()
        );
        kafkaTemplate.send(USER_REGISTERED_TOPIC, savedUser.getId(), event);
        log.debug("Published user.registered event for user: {}", savedUser.getId());
    }

    /**
     * Generate access and refresh JWT tokens for a user.
     *
     * @param userId  the identifier of the user. Cannot be null or blank.
     * @param email   the email of the user (used as JWT subject). Cannot be null or blank.
     * @param role    the authorization role of the user. Cannot be null.
     * @return an array where [0] is the access token and [1] is the refresh token.
     */
    private String[] generateTokens(String userId, String email, Role role) {
        String accessToken = generateAccessToken(userId, email, role);
        String refreshToken = generateRefreshToken(userId, role);
        return new String[]{accessToken, refreshToken};
    }

    /**
     * Login a user with email and password.
     *
     * @param request the login request containing email and password.
     *                {@code email} must be valid; {@code password} must not be blank.
     * @return the authentication response with tokens and user data.
     * @throws RuntimeException if the user is not found or the password is invalid.
     */
    public AuthResponse login(LoginRequest request) {
        log.info("Login attempt: {}", request.getEmail());

        User user = userRepository.findByEmail(request.getEmail())
                .orElseThrow(() -> new RuntimeException("User not found " + request.getEmail()));
        if (!passwordEncoder.matches(request.getPassword(), user.getPassword())) {
            throw new RuntimeException("Invalid password for " + request.getEmail());
        }

        String[] tokens = generateTokens(user.getId(), user.getEmail(), resolveRole(user));
        return buildAuthResponse(user, tokens[0], tokens[1]);
    }

    /**
     * Generate a signed JWT access token.
     *
     * @param userId  the identifier of the user (stored in the "userId" claim).
     *                Cannot be null or blank.
     * @param email   the email of the user (used as JWT subject).
     *                Cannot be null or blank.
     * @param role    the authorization role (stored in the "role" claim). Cannot be null.
     * @return the signed access token string.
     */
    private String generateAccessToken(String userId, String email, Role role) {
        Instant now = Instant.now();
        Instant expiry = now.plusMillis(jwtExpiration);

        return Jwts.builder()
                .claim("userId", userId)
                .claim("role", role.name())
                .subject(email)
                .issuedAt(Date.from(now))
                .expiration(Date.from(expiry))
                .signWith(getSignInKey())
                .compact();
    }

    /**
     * Generate a signed JWT refresh token.
     *
     * @param userId the identifier of the user (stored in the "userId" claim and subject).
     *               Cannot be null or blank.
     * @param role   the authorization role (stored in the "role" claim). Cannot be null.
     * @return the signed refresh token string.
     */
    private String generateRefreshToken(String userId, Role role) {
        Instant now = Instant.now();
        Instant expiry = now.plusMillis(refreshExpiration);

        return Jwts.builder()
                .claim("userId", userId)
                .claim("role", role.name())
                .subject(userId)
                .issuedAt(Date.from(now))
                .expiration(Date.from(expiry))
                .signWith(getSignInKey())
                .compact();
    }

    /**
     * Builds the signing key used to sign and verify JWT tokens.
     *
     * @return a {@link SecretKey} derived from the configured JWT secret. Never null.
     */
    private SecretKey getSignInKey() {
        byte[] keyBytes = Decoders.BASE64.decode(secret);
        return Keys.hmacShaKeyFor(keyBytes);
    }

    /**
     * Get user by identifier. Called by other services via Feign (internal).
     *
     * @param userId the identifier of the user. Cannot be null or blank.
     * @return the mapped {@link UserResponse} for the user.
     * @throws RuntimeException if the user is not found.
     */
    public UserResponse getUserById(String userId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("User not found: " + userId));

        return mapToResponse(user);
    }
    
    /**
     * Add funds to a user's wallet.
     *
     * @param userId the identifier of the user. Cannot be null or blank.
     * @param amount the amount to add. Cannot be null or negative.
     * @return a {@link FundsOperationResponse} with the new balance.
     * @throws RuntimeException if the user is not found.
     */
    public FundsOperationResponse addFunds(String userId, BigDecimal amount) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("User not found: " + userId));

        user.setWalletBalance(user.getWalletBalance().add(amount));
        User savedUser = userRepository.save(user);

        log.info("Funds added: {} to user: {}", amount, userId);
        return new FundsOperationResponse(savedUser.getId(), savedUser.getWalletBalance(), true);
    }

    /**
     * Deduct funds from a user's wallet.
     *
     * @param userId the identifier of the user. Cannot be null or blank.
     * @param amount the amount to deduct. Cannot be null or negative.
     * @return a {@link FundsOperationResponse} with the new balance.
     * @throws RuntimeException if the user is not found or there are insufficient funds.
     */
    public FundsOperationResponse deductFunds(String userId, BigDecimal amount) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("User not found: " + userId));

        if (user.getWalletBalance().compareTo(amount) < 0) {
            throw new RuntimeException("Insufficient wallet Balance");
        }

        user.setWalletBalance(user.getWalletBalance().subtract(amount));
        User savedUser = userRepository.save(user);

        log.info("Funds deducted: {} from user: {}", amount, userId);
        return new FundsOperationResponse(savedUser.getId(), savedUser.getWalletBalance(), true);
    }

    /**
     * Credit funds to a user's wallet (used for SELL orders).
     *
     * @param userId the identifier of the user. Cannot be null or blank.
     * @param amount the amount to credit. Cannot be null or negative.
     * @return a {@link FundsOperationResponse} with the new balance.
     * @throws RuntimeException if the user is not found.
     */
    public FundsOperationResponse creditFunds(String userId, BigDecimal amount) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("User not found: " + userId));

        user.setWalletBalance(user.getWalletBalance().add(amount));
        User savedUser = userRepository.save(user);

        log.info("Funds credited: {} to user: {}", amount, userId);
        return new FundsOperationResponse(savedUser.getId(), savedUser.getWalletBalance(), true);
    }

    /**
     * Returns all registered users. Intended for administrative use only.
     *
     * @return the list of all users mapped to {@link UserResponse} (possibly empty). Never null.
     */
    public List<UserResponse> getAllUsers() {
        return userRepository.findAll().stream()
                .map(this::mapToResponse)
                .toList();
    }

    /**
     * Resolves the effective role of a user, defaulting to {@code USER} when the
     * stored role is null (e.g. legacy rows created before roles were introduced).
     *
     * @param user the user entity. Cannot be null.
     * @return the effective {@link Role}. Never null.
     */
    private Role resolveRole(User user) {
        return user.getRole() != null ? user.getRole() : Role.USER;
    }

    /**
     * Builds an {@link AuthResponse} from a user entity and JWT tokens.
     *
     * @param user        the authenticated user. Cannot be null.
     * @param token       the access token. Cannot be null or blank.
     * @param refreshToken the refresh token. Cannot be null or blank.
     * @return a populated {@link AuthResponse} with tokens and user data.
     */
    private AuthResponse buildAuthResponse(User user, String token, String refreshToken) {
        AuthResponse response = new AuthResponse();
        response.setAccessToken(token);
        response.setUserId(user.getId());
        response.setEmail(user.getEmail());
        response.setFirstName(user.getFirstName());
        response.setLastName(user.getLastName());
        response.setRefreshToken(refreshToken);
        response.setWalletBalance(user.getWalletBalance());
        response.setRole(resolveRole(user));

        return response;
    }

    /**
     * Maps a {@link User} entity to a {@link UserResponse} DTO.
     *
     * @param user the user entity. Cannot be null.
     * @return a {@link UserResponse} with the user's public data.
     */
    private UserResponse mapToResponse(User user) {
        UserResponse response = new UserResponse();
        response.setId(user.getId());
        response.setEmail(user.getEmail());
        response.setFirstName(user.getFirstName());
        response.setLastName(user.getLastName());
        response.setWalletBalance(user.getWalletBalance());
        response.setStatus(user.getStatus());
        response.setRole(resolveRole(user));
        response.setCreatedAt(user.getCreatedAt());

        return response;
    }
}
