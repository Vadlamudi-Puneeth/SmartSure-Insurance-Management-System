package com.group2.policy_service.service.impl;

import com.group2.policy_service.dto.NotificationEvent;
import com.group2.policy_service.feign.AuthClient;
import com.group2.policy_service.feign.UserDTO;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

import java.time.LocalDate;

@Service
public class AsyncNotificationService {

    private static final Logger log = LoggerFactory.getLogger(AsyncNotificationService.class);
    private final AuthClient authClient;
    private final RabbitTemplate rabbitTemplate;

    public AsyncNotificationService(AuthClient authClient, RabbitTemplate rabbitTemplate) {
        this.authClient = authClient;
        this.rabbitTemplate = rabbitTemplate;
    }

    @Async
    public void sendPurchaseNotification(Long userId, String policyName, Double premium, Double coverage, LocalDate endDate) {
        try {
            UserDTO user = authClient.getUserById(userId);
            if (user == null || user.getEmail() == null) return;

            String subject = "SmartSure: Policy Purchase Successful";
            String htmlBody = String.format(
                "<html><body style='font-family: Arial, sans-serif; color: #333; line-height: 1.6;'>" +
                "<div style='max-width: 600px; margin: auto; border: 1px solid #ddd; border-radius: 10px; overflow: hidden;'>" +
                "<div style='background: #1e3c72; color: white; padding: 20px; text-align: center;'>" +
                "<h1 style='margin: 0;'>🛡️ SmartSure</h1>" +
                "</div>" +
                "<div style='padding: 20px;'>" +
                "<h2>Congratulations %s!</h2>" +
                "<p>You have successfully purchased a new insurance policy.</p>" +
                "<div style='background: #f4f7f6; padding: 15px; border-radius: 5px; border-left: 5px solid #1e3c72;'>" +
                "<strong>Policy Name:</strong> %s<br/>" +
                "<strong>Premium Amount:</strong> ₹%.2f<br/>" +
                "<strong>Coverage:</strong> ₹%.2f<br/>" +
                "<strong>Expiry Date:</strong> %s" +
                "</div>" +
                "<p style='margin-top: 20px;'>Thank you for choosing SmartSure for your protection.</p>" +
                "</div>" +
                "<div style='background: #f9f9f9; padding: 10px; text-align: center; font-size: 12px; color: #777;'>" +
                "&copy; 2026 SmartSure Insurance Management System" +
                "</div></div></body></html>",
                user.getName(), policyName, premium, coverage, endDate
            );
            queueNotification(user.getEmail(), subject, htmlBody);
        } catch (Exception e) {
            log.error("Failed to process purchase notification: {}", e.getMessage());
        }
    }

    @Async
    public void sendPaymentNotification(Long userId, String policyName, Double amount, Double balance) {
        try {
            UserDTO user = authClient.getUserById(userId);
            if (user == null || user.getEmail() == null) return;

            String subject = "SmartSure: Premium Payment Successful";
            String htmlBody = String.format(
                "<html><body style='font-family: Arial, sans-serif; color: #333;'>" +
                "<div style='max-width: 600px; margin: auto; border: 1px solid #ddd; border-radius: 10px; overflow: hidden;'>" +
                "<div style='background: #27ae60; color: white; padding: 20px; text-align: center;'>" +
                "<h1 style='margin: 0;'>💰 Payment Received</h1>" +
                "</div>" +
                "<div style='padding: 20px;'>" +
                "<p>Hello %s,</p>" +
                "<p>A premium payment has been successfully processed for your policy: <strong>%s</strong>.</p>" +
                "<div style='background: #f4f7f6; padding: 15px; border-radius: 5px; border-left: 5px solid #27ae60;'>" +
                "<strong>Amount Paid:</strong> ₹%.2f<br/>" +
                "<strong>Remaining Balance:</strong> ₹%.2f" +
                "</div>" +
                "<p style='margin-top: 20px;'>Thank you for keeping your policy active!</p>" +
                "</div>" +
                "<div style='background: #f9f9f9; padding: 10px; text-align: center; font-size: 12px; color: #777;'>" +
                "&copy; 2026 SmartSure Insurance Management System" +
                "</div></div></body></html>",
                user.getName(), policyName, amount, balance
            );
            queueNotification(user.getEmail(), subject, htmlBody);
        } catch (Exception e) {
            log.error("Failed to process payment notification: {}", e.getMessage());
        }
    }

    @Async
    public void sendCancellationRequestNotification(Long userId, String policyName) {
        try {
            UserDTO user = authClient.getUserById(userId);
            if (user == null || user.getEmail() == null) return;

            String subject = "SmartSure: Cancellation Request Received";
            String htmlBody = String.format(
                "<html><body style='font-family: Arial, sans-serif; color: #333;'>" +
                "<div style='max-width: 600px; margin: auto; border: 1px solid #ddd; border-radius: 10px; overflow: hidden;'>" +
                "<div style='background: #e67e22; color: white; padding: 20px; text-align: center;'>" +
                "<h1 style='margin: 0;'>⚠️ Cancellation Request</h1>" +
                "</div>" +
                "<div style='padding: 20px;'>" +
                "<p>Hello %s,</p>" +
                "<p>We have received your request to cancel your policy: <strong>%s</strong>.</p>" +
                "<p>Our administration team is currently reviewing your request. You will be notified once the review is complete.</p>" +
                "<p>No further action is required from your side at this moment.</p>" +
                "</div>" +
                "<div style='background: #f9f9f9; padding: 10px; text-align: center; font-size: 12px; color: #777;'>" +
                "&copy; 2026 SmartSure Insurance Management System" +
                "</div></div></body></html>",
                user.getName(), policyName
            );
            queueNotification(user.getEmail(), subject, htmlBody);
        } catch (Exception e) {
            log.error("Failed to process cancellation request notification: {}", e.getMessage());
        }
    }

    @Async
    public void sendCancellationApprovalNotification(Long userId, String policyName) {
        try {
            UserDTO user = authClient.getUserById(userId);
            if (user == null || user.getEmail() == null) return;

            String subject = "SmartSure: Policy Cancellation Approved";
            String htmlBody = String.format(
                "<html><body style='font-family: Arial, sans-serif; color: #333;'>" +
                "<div style='max-width: 600px; margin: auto; border: 1px solid #ddd; border-radius: 10px; overflow: hidden;'>" +
                "<div style='background: #c0392b; color: white; padding: 20px; text-align: center;'>" +
                "<h1 style='margin: 0;'>🚫 Policy Cancelled</h1>" +
                "</div>" +
                "<div style='padding: 20px;'>" +
                "<p>Hello %s,</p>" +
                "<p>Your cancellation request for policy <strong>%s</strong> has been <strong>Approved</strong>.</p>" +
                "<p>The policy has been successfully terminated. Any associated benefits will no longer be available.</p>" +
                "</div>" +
                "<div style='background: #f9f9f9; padding: 10px; text-align: center; font-size: 12px; color: #777;'>" +
                "&copy; 2026 SmartSure Insurance Management System" +
                "</div></div></body></html>",
                user.getName(), policyName
            );
            queueNotification(user.getEmail(), subject, htmlBody);
        } catch (Exception e) {
            log.error("Failed to process cancellation approval notification: {}", e.getMessage());
        }
    }

    private void queueNotification(String email, String subject, String htmlBody) {
        try {
            NotificationEvent event = new NotificationEvent(email, subject, htmlBody);
            rabbitTemplate.convertAndSend("notification.exchange", "notification.email", event);
            log.info("📧 Notification event queued for: {}", email);
        } catch (Exception e) {
            log.error("Failed to queue notification to RabbitMQ: {}", e.getMessage());
        }
    }
}
