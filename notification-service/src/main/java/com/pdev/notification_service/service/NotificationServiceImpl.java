package com.pdev.notification_service.service;

import com.pdev.notification_service.client.CitizenServiceClient;
import com.pdev.notification_service.dto.ApiResponse;
import com.pdev.notification_service.dto.CitizenProfileResponse;
import com.pdev.notification_service.model.NotificationChannel;
import com.pdev.notification_service.model.NotificationLog;
import com.pdev.notification_service.model.NotificationType;
import com.pdev.notification_service.repository.NotificationLogRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import java.time.LocalDateTime;

/**
 * Dispatches notifications and records every attempt for auditability.
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class NotificationServiceImpl implements NotificationService {

    private final NotificationLogRepository notificationLogRepository;
    private final CitizenServiceClient citizenServiceClient;
    private final JavaMailSender javaMailSender;

    @Value("${notification.email.from}")
    private String emailFrom;

    @Value("${notification.email.subject}")
    private String emailSubject;

    @Override
    public void send(String citizenId, NotificationType notificationType, NotificationChannel channel, String message) {

        boolean success = false;
        String errorDetail = null;

        try {
            switch (channel) {
                case SMS -> dispatchSms(citizenId, message);
                case EMAIL -> dispatchEmail(citizenId, message);
                case PUSH -> dispatchPush(citizenId, message);
            }
            success = true;
        } catch (Exception e) {
            errorDetail = e.getMessage();
            log.error("[NOTIFICATION] Failed to dispatch {} via {}: citizenId={} error={}",
                    notificationType, channel, citizenId, e.getMessage(), e);
        }

        NotificationLog auditEntry = NotificationLog.builder()
                .citizenId(citizenId)
                .notificationType(notificationType)
                .channel(channel)
                .message(message)
                .success(success)
                .errorDetail(errorDetail)
                .sentAt(LocalDateTime.now())
                .build();

        notificationLogRepository.save(auditEntry);

        if (success) {
            log.info("[NOTIFICATION] Logged {} via {} for citizenId={}",
                    notificationType, channel, citizenId);
        }
    }

    @Override
    public void sendAll(String citizenId,
                        NotificationType notificationType,
                        String message) {
        for (NotificationChannel channel : NotificationChannel.values()) {
            send(citizenId, notificationType, channel, message);
        }
    }

    private void dispatchSms(String citizenId, String message) {
        // TODO: Twilio / AWS SNS integration
        log.info("[MOCK SMS] citizenId={} | msg=\"{}\"", citizenId, message);
    }

    private void dispatchEmail(String citizenId, String message) {
        CitizenProfileResponse profile = resolveCitizenProfile(citizenId);
        String recipient = profile.getEmail();

        if (!StringUtils.hasText(recipient)) {
            throw new IllegalStateException("No email address found for citizenId=" + citizenId);
        }
        if(!validEmail(recipient)){
            throw new IllegalArgumentException("Invalid email address: " + recipient);
        }

        SimpleMailMessage mailMessage = new SimpleMailMessage();
        mailMessage.setFrom(emailFrom);
        mailMessage.setTo(recipient);
        mailMessage.setSubject(emailSubject);
        mailMessage.setText(message);

        javaMailSender.send(mailMessage);
        log.info("[EMAIL] Sent notification to citizenId={} email={}", citizenId, recipient);
    }

    private boolean validEmail(String recipient) {
        return StringUtils.hasText(recipient) && recipient.matches("^[A-Za-z0-9+_.-]+@[A-Za-z0-9.-]+\\.[A-Za-z]{2,}$");
    }

    private void dispatchPush(String citizenId, String message) {
        // TODO: Firebase Cloud Messaging (FCM) integration
        log.info("[MOCK PUSH] citizenId={} | msg=\"{}\"", citizenId, message);
    }

    private CitizenProfileResponse resolveCitizenProfile(String citizenId) {
        ApiResponse<CitizenProfileResponse> response = citizenServiceClient.getCitizenProfile(citizenId);

        if (response == null || !response.isSuccess() || response.getData() == null) {
            throw new IllegalStateException("Unable to resolve citizen profile for citizenId=" + citizenId);
        }

        return response.getData();
    }
}
