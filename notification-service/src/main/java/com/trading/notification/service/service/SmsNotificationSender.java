package com.trading.notification.service.service;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.client.RestClient;

import lombok.extern.slf4j.Slf4j;

/**
 * SMS implementation of {@link NotificationSender} using the Twilio REST API.
 * <p>
 * Active only when {@code notification.channel=sms}. The recipient is taken from
 * {@code notification.sms.to} (must be a Twilio-verified number in trial mode).
 * If any Twilio setting is missing, the notification is skipped and a warning is logged.
 */
@Component
@ConditionalOnProperty(name = "notification.channel", havingValue = "sms")
@Slf4j
public class SmsNotificationSender implements NotificationSender {

    private static final String TWILIO_MESSAGES_URL =
            "https://api.twilio.com/2010-04-01/Accounts/%s/Messages.json";

    private final RestClient restClient = RestClient.create();

    @Value("${twilio.account-sid:}")
    private String accountSid;

    @Value("${twilio.auth-token:}")
    private String authToken;

    @Value("${twilio.phone-number:}")
    private String fromNumber;

    @Value("${notification.sms.to:}")
    private String toNumber;

    /**
     * {@inheritDoc}
     */
    @Override
    public void send(String userId, String title, String message) {
        if (isBlank(accountSid) || isBlank(authToken) || isBlank(fromNumber) || isBlank(toNumber)) {
            log.warn("SMS channel selected but Twilio is not fully configured; "
                    + "skipping notification (user={})", userId);
            return;
        }
        try {
            MultiValueMap<String, String> form = new LinkedMultiValueMap<>();
            form.add("To", toNumber);
            form.add("From", fromNumber);
            form.add("Body", title + ": " + message);

            restClient.post()
                    .uri(String.format(TWILIO_MESSAGES_URL, accountSid))
                    .headers(headers -> headers.setBasicAuth(accountSid, authToken))
                    .contentType(MediaType.APPLICATION_FORM_URLENCODED)
                    .body(form)
                    .retrieve()
                    .toBodilessEntity();

            log.info("SMS notification sent via Twilio (user={})", userId);
        } catch (Exception e) {
            log.error("Error sending SMS notification: {}", e.getMessage());
        }
    }

    /**
     * Checks whether a configuration value is null or blank.
     *
     * @param value the value to check. May be null.
     * @return {@code true} if the value is null or blank, {@code false} otherwise.
     */
    private boolean isBlank(String value) {
        return value == null || value.isBlank();
    }
}
