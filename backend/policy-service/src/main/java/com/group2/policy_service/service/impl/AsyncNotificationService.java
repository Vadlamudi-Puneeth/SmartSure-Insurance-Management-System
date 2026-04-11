package com.group2.policy_service.service.impl;

import com.group2.policy_service.dto.NotificationEvent;
import com.group2.policy_service.dto.EmailRequest;
import com.group2.policy_service.feign.NotificationClient;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import java.time.LocalDate;

@Service
public class AsyncNotificationService {
    private static final Logger log = LoggerFactory.getLogger(AsyncNotificationService.class);
    private final RabbitTemplate rabbitTemplate;
    private final NotificationClient notificationClient;

    public AsyncNotificationService(RabbitTemplate rabbitTemplate, NotificationClient notificationClient) {
        this.rabbitTemplate = rabbitTemplate;
        this.notificationClient = notificationClient;
    }

    private void process(String email, String subject, String body) {
        if (email == null) return;
        try {
            rabbitTemplate.convertAndSend("notification.exchange", "notification.email", new NotificationEvent(email, subject, body));
        } catch (Exception e) {
            try {
                notificationClient.sendEmail(new EmailRequest(email, subject, body));
            } catch (Exception ex) {
                log.error("Fatal fail");
            }
        }
    }

    @Async
    public void sendPurchaseNotification(String userEmail, String userName, String policyName, Double premium, Double coverage, LocalDate endDate) {
        process(userEmail, "SmartSure: Policy Purchase", "Purchased " + policyName);
    }

    @Async
    public void sendPaymentNotification(String userEmail, String userName, String policyName, Double amount, Double balance) {
        process(userEmail, "SmartSure: Payment Received", "Paid ₹" + amount);
    }

    @Async
    public void sendCancellationRequestNotification(String userEmail, String userName, String policyName) {
        process(userEmail, "SmartSure: Cancellation Request", "Request for " + policyName);
    }

    @Async
    public void sendCancellationApprovalNotification(String userEmail, String userName, String policyName) {
        process(userEmail, "SmartSure: Policy Cancelled", "Cancelled " + policyName);
    }
}
