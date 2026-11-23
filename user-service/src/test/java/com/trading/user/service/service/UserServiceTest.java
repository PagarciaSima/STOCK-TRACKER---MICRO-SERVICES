package com.trading.user.service.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.when;

import java.util.Optional;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.util.ReflectionTestUtils;

import com.trading.user.service.dto.AuthResponse;
import com.trading.user.service.dto.LoginRequest;
import com.trading.user.service.dto.UserRegisteredEvent;
import com.trading.user.service.entity.Role;
import com.trading.user.service.entity.User;
import com.trading.user.service.repository.UserRepository;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.io.Decoders;
import io.jsonwebtoken.security.Keys;

/**
 * Unit tests for {@link UserService} focused on the JWT role claim.
 */
@ExtendWith(MockitoExtension.class)
class UserServiceTest {

    private static final String SECRET = "qNiwobQFrXWDou6EIWAhman5n771RqZlCltkMgbYptc=";
    private static final String EMAIL = "admin@example.com";
    private static final String RAW_PASSWORD = "secret123";

    @Mock
    private UserRepository userRepository;

    @Mock
    private PasswordEncoder passwordEncoder;

    @Mock
    private KafkaTemplate<String, UserRegisteredEvent> kafkaTemplate;

    @InjectMocks
    private UserService userService;

    @BeforeEach
    void setUp() {
        ReflectionTestUtils.setField(userService, "secret", SECRET);
        ReflectionTestUtils.setField(userService, "jwtExpiration", 3600000L);
        ReflectionTestUtils.setField(userService, "refreshExpiration", 86400000L);
    }

    @Test
    void shouldIncludeRoleClaimInAccessTokenOnLogin() {
        User user = new User();
        user.setId("user-1");
        user.setEmail(EMAIL);
        user.setPassword("hashed");
        user.setRole(Role.ADMIN);

        when(userRepository.findByEmail(EMAIL)).thenReturn(Optional.of(user));
        when(passwordEncoder.matches(RAW_PASSWORD, "hashed")).thenReturn(true);

        LoginRequest request = new LoginRequest();
        request.setEmail(EMAIL);
        request.setPassword(RAW_PASSWORD);

        AuthResponse response = userService.login(request);

        assertThat(response.getRole()).isEqualTo(Role.ADMIN);

        Claims claims = Jwts.parser()
                .verifyWith(Keys.hmacShaKeyFor(Decoders.BASE64.decode(SECRET)))
                .build()
                .parseSignedClaims(response.getAccessToken())
                .getPayload();

        assertThat(claims.get("role", String.class)).isEqualTo("ADMIN");
        assertThat(claims.get("userId", String.class)).isEqualTo("user-1");
    }
}
