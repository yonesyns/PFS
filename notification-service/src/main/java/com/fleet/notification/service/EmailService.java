package com.fleet.notification.service;

import com.fleet.notification.entity.Notification;
import jakarta.mail.MessagingException;
import jakarta.mail.internet.MimeMessage;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.stereotype.Service;
import org.thymeleaf.TemplateEngine;
import org.thymeleaf.context.Context;

import java.nio.charset.StandardCharsets;

@Slf4j
@Service
@RequiredArgsConstructor
public class EmailService {

    private final JavaMailSender mailSender;
    private final TemplateEngine templateEngine;

    @Value("${spring.mail.username:noreply@fleet.com}")
    private String fromEmail;

    public void sendSimpleEmail(Notification notification) {
        try {
            SimpleMailMessage message = new SimpleMailMessage();
            message.setFrom(fromEmail);
            message.setTo(notification.getRecipientEmail());
            message.setSubject(notification.getSubject());
            message.setText(notification.getContent());

            mailSender.send(message);
            log.info("Simple email sent to {}", notification.getRecipientEmail());
        } catch (Exception e) {
            log.error("Failed to send simple email to {}: {}", notification.getRecipientEmail(), e.getMessage());
            throw new RuntimeException("Email sending failed", e);
        }
    }

    public void sendHtmlEmail(Notification notification) {
        try {
            MimeMessage message = mailSender.createMimeMessage();
            MimeMessageHelper helper = new MimeMessageHelper(
                message, 
                MimeMessageHelper.MULTIPART_MODE_MIXED, 
                StandardCharsets.UTF_8.name()
            );

            helper.setFrom(fromEmail);
            helper.setTo(notification.getRecipientEmail());
            helper.setSubject(notification.getSubject());

            // Si template spécifié, utiliser Thymeleaf
            if (notification.getTemplateName() != null) {
                Context context = new Context();
                // Parse templateData JSON et ajouter au contexte
                context.setVariable("notification", notification);
                String html = templateEngine.process(notification.getTemplateName(), context);
                helper.setText(html, true);
            } else {
                helper.setText(notification.getContent(), true);
            }

            mailSender.send(message);
            log.info("HTML email sent to {}", notification.getRecipientEmail());
        } catch (MessagingException e) {
            log.error("Failed to send HTML email to {}: {}", notification.getRecipientEmail(), e.getMessage());
            throw new RuntimeException("HTML email sending failed", e);
        }
    }
}
