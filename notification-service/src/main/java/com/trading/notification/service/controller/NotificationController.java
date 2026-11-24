package com.trading.notification.service.controller;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.trading.notification.service.dto.NotificationTestRequest;
import com.trading.notification.service.dto.NotificationTestResponse;
import com.trading.notification.service.service.NotificationSender;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

/**
 * REST controller for manually triggering notifications (useful for testing the
 * configured channel without producing a Kafka event).
 */
@Tag(name = "Notifications", description = "Manual notification testing (email/SMS/log)")
@RestController
@RequestMapping("/api/v1/notifications")
@RequiredArgsConstructor
@Slf4j
public class NotificationController {

    private final NotificationSender notificationSender;

    @Value("${notification.channel:log}")
    private String channel;

    /**
     * Sends a manual test notification through the active channel.
     *
     * @param userId  the authenticated user id (optional, injected by the gateway).
     * @param request the notification request. Must be valid (non-blank title and message).
     * @return {@code 200 OK} with the result status and the active channel.
     */
    @Operation(summary = "Send a test notification",
            description = "Sends a manual notification through the active channel (log/email/sms).")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Notification dispatched"),
            @ApiResponse(responseCode = "400", description = "Invalid request")
    })
    @PostMapping("/test")
    public ResponseEntity<NotificationTestResponse> sendTest(
            @Parameter(description = "Authenticated user id injected by the gateway (optional)")
            @RequestHeader(value = "X-User-Id", required = false) String userId,
            @Valid @RequestBody NotificationTestRequest request) {

        String target = (userId == null || userId.isBlank()) ? "manual-test" : userId;
        log.info("Manual notification test via channel: {} for user: {}", channel, target);
        notificationSender.send(target, request.getTitle(), request.getMessage());
        return ResponseEntity.ok(new NotificationTestResponse("sent", channel));
    }
}
