package com.trading.notification.service.dto;

/**
 * Response for a manual test notification.
 *
 * @param status  the result status (e.g. "sent"). Cannot be null or blank.
 * @param channel the active notification channel (log, email, sms). Cannot be null or blank.
 */
public record NotificationTestResponse(String status, String channel) {
}
