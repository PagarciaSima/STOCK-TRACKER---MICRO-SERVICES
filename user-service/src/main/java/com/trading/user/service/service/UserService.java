package com.trading.user.service.service;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

import javax.crypto.SecretKey;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import com.trading.user.service.dto.AuthResponse;
import com.trading.user.service.dto.LoginRequest;
import com.trading.user.service.dto.RegisterRequest;
import com.trading.user.service.dto.UserResponse;
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
    private final KafkaTemplate<String, Object> kafkaTemplate;
    private static final String USER_REGISTERED_TOPIC = "user.registered";
    
    public AuthResponse register(RegisterRequest request){

        log.info("Registering user: {}", request.getEmail());

        if(userRepository.existsByEmail(request.getEmail())){
            throw new RuntimeException(
                "Email already registered "+request.getEmail());
        }

        User user = new User();
        user.setFirstName(request.getFirstName());
        user.setLastName(request.getLastName());
        user.setEmail(request.getEmail());
        user.setPassword(passwordEncoder.encode(request.getPassword()));
        user.setWalletBalance(
                request.getInitialDeposit() != null 
                ? request.getInitialDeposit() 
                : BigDecimal.valueOf(10000)
        );
        
        User savedUser = userRepository.save(user);
        
        log.info("User registered: {}", savedUser.getId());

        // Publish user.registered event
        Map<String, Object> userRegisteredEvent = new HashMap<>();
        userRegisteredEvent.put("userId", savedUser.getId());
        userRegisteredEvent.put("email", savedUser.getEmail());
        userRegisteredEvent.put("firstName", savedUser.getFirstName());
        userRegisteredEvent.put("lastName", savedUser.getLastName());
        userRegisteredEvent.put("walletBalance", savedUser.getWalletBalance());

        kafkaTemplate.send(USER_REGISTERED_TOPIC, savedUser.getId(), userRegisteredEvent);
        
        String token = generateAccessToken(savedUser.getId(), savedUser.getEmail());
        String refreshToken = generateRefreshToken(savedUser.getId());
        
        return buildAuthResponse(savedUser, token, refreshToken);
    }
    
    /**
     * Login
     */
    public AuthResponse login(LoginRequest request){

        log.info("Login attempt: {}", request.getEmail());

        User user = userRepository.findByEmail(request.getEmail())
        		.orElseThrow(() -> new RuntimeException("User not found " + request.getEmail()));
        if(!passwordEncoder.matches(
        		request.getPassword(), user.getPassword()
        )) {
        	throw new RuntimeException("Invalid credentials");
        }
        
        log.info("login successful for user: {}", user.getId());
        
        String token = generateAccessToken(user.getId(), user.getEmail());

        String refreshToken = generateRefreshToken(user.getId());

        return buildAuthResponse(user, token, refreshToken);
    }
    
    private String generateAccessToken(String userId, String email) {
        Instant now = Instant.now();
        Instant expiry = now.plusMillis(jwtExpiration);

        return Jwts.builder()
                .claim("userId", userId)
                .subject(email)
                .issuedAt(Date.from(now))
                .expiration(Date.from(expiry))
                .signWith(getSignInKey())
                .compact();
	}
    
    private SecretKey getSignInKey(){
        byte[] keyBytes = Decoders.BASE64.decode(secret);
        return Keys.hmacShaKeyFor(keyBytes);
    }

    private String generateRefreshToken(String userId){
        Instant now = Instant.now();
        Instant expiry = now.plusMillis(refreshExpiration);

        return Jwts.builder()
                .claim("userId", userId)
                .subject(userId)              
                .issuedAt(Date.from(now))     
                .expiration(Date.from(expiry))
                .signWith(getSignInKey())     
                .compact();
    }

	/**
     * Get User By Id.
     * Called by other services via Feign (internal)
     * @param userId
     * @return
     */
    public UserResponse getUserById(String userId){
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException(
                        "User not found: "+ userId));

        return mapToResponse(user);
    }
    
    public UserResponse addFunds(String userId, BigDecimal amount){
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException(
                        "User not found: " + userId));

        user.setWalletBalance(user.getWalletBalance().add(amount));
        User savedUser = userRepository.save(user);

        log.info("Funds added: {} to user: {}", amount, userId);
        return mapToResponse(savedUser);
    }
    
    public UserResponse deductFunds(String userId, BigDecimal amount){
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException(
                        "User not found: " + userId));

        if(user.getWalletBalance().compareTo(amount)<0){
            throw new RuntimeException("Insufficient wallet Balance");
        }

        user.setWalletBalance(user.getWalletBalance().subtract(amount));
        User savedUser = userRepository.save(user);

        log.info("Funds deducted: {} to user: {}", amount, userId);
        return mapToResponse(savedUser);
    }
    
    public UserResponse creditFunds(String userId, BigDecimal amount){
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException(
                        "User not found: " + userId));

        user.setWalletBalance(user.getWalletBalance().add(amount));
        User savedUser = userRepository.save(user);

        log.info("Funds credited: {} to user: {}", amount, userId);
        return mapToResponse(savedUser);
    }
    
    private AuthResponse buildAuthResponse(User user, String token, String refreshToken){
        AuthResponse response = new AuthResponse();
        response.setAccessToken(token);
        response.setUserId(user.getId());
        response.setEmail(user.getEmail());
        response.setFirstName(user.getFirstName());
        response.setLastName(user.getLastName());
        response.setRefreshToken(refreshToken);
        response.setWalletBalance(user.getWalletBalance());

        return response;
    }
    
    private UserResponse mapToResponse(User user){
        UserResponse response = new UserResponse();
        response.setId(user.getId());
        response.setEmail(user.getEmail());
        response.setFirstName(user.getFirstName());
        response.setLastName(user.getLastName());
        response.setWalletBalance(user.getWalletBalance());
        response.setStatus(user.getStatus());
        response.setCreatedAt(user.getCreatedAt());

        return response;
    }
}
