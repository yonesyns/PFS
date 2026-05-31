package com.fleet.notification.service;

import com.fleet.notification.entity.Notification;
import com.twilio.Twilio;
import com.twilio.rest.api.v2010.account.Message;
import com.twilio.type.PhoneNumber;
import jakarta.annotation.PostConstruct;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

@Slf4j
@Service
public class SmsService {

    @Value("${twilio.account.sid:}")
    private String accountSid;

    @Value("${twilio.auth.token:}")
    private String authToken;

    @Value("${twilio.phone.number:}")
    private String twilioPhoneNumber;

    @PostConstruct
    public void init() {
        if (accountSid != null && !accountSid.isEmpty() && authToken != null && !authToken.isEmpty()) {
            Twilio.init(accountSid, authToken);
            log.info("Twilio SMS service initialized");
        } else {
            log.warn("Twilio credentials not configured. SMS will be logged only.");
        }
    }

    public void sendSms(Notification notification) {
        if (accountSid == null || accountSid.isEmpty()) {
            log.info("[SMS MOCK] To: {} | Message: {}", notification.getRecipientPhone(), notification.getContent());
            return;
        }

        try {
            Message message = Message.creator(
                new PhoneNumber(notification.getRecipientPhone()),
                new PhoneNumber(twilioPhoneNumber),
                notification.getSubject() + ": " + notification.getContent()
            ).create();

            log.info("SMS sent to {}. SID: {}", notification.getRecipientPhone(), message.getSid());
        } catch (Exception e) {
            log.error("Failed to send SMS to {}: {}", notification.getRecipientPhone(), e.getMessage());
            throw new RuntimeException("SMS sending failed", e);
        }
    }
}
