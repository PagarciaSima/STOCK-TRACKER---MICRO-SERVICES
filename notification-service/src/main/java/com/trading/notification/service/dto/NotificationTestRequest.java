package com.trading.notification.service.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Request payload for sending a manual test notification.
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class NotificationTestRequest {

    /** Notification title/subject. Required. */
    @NotBlank(message = "Title is required")
    private String title;

    /** Notification body message. Required. */
    @NotBlank(message = "Message is required")
    private String message;
}
