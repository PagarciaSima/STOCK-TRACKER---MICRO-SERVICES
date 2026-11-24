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

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

/**
 * REST controller for manually triggering notifications (useful for testing the
 * configured channel without producing a Kafka event).
 */
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
     * <p>
     * When called through the API gateway, the authenticated user id is available
     * in the {@code X-User-Id} header; when called directly on the service port it
     * falls back to {@code manual-test}.
     *
     * @param userId  the authenticated user id (optional, injected by the gateway).
     * @param request the notification request. Must be valid (non-blank title and message).
     * @return {@code 200 OK} with the result status and the active channel.
     */
    @PostMapping("/test")
    public ResponseEntity<NotificationTestResponse> sendTest(
            @RequestHeader(value = "X-User-Id", required = false) String userId,
            @Valid @RequestBody NotificationTestRequest request) {

        String target = (userId == null || userId.isBlank()) ? "manual-test" : userId;
        log.info("Manual notification test via channel: {} for user: {}", channel, target);
        notificationSender.send(target, request.getTitle(), request.getMessage());
        return ResponseEntity.ok(new NotificationTestResponse("sent", channel));
    }
}
