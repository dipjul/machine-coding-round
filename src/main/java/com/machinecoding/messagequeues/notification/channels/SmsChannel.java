package com.machinecoding.messagequeues.notification.channels;

import com.machinecoding.messagequeues.notification.service.NotificationChannel;
import com.machinecoding.messagequeues.notification.model.Notification;
import com.machinecoding.messagequeues.notification.model.NotificationType;

import java.util.Random;

/**
 * SMS notification channel implementation.
 * Simulates SMS delivery through a gateway service.
 */
public class SmsChannel implements NotificationChannel {
    
    private final String gatewayUrl;
    private final String apiKey;
    private final boolean isAvailable;
    private final double successRate;
    private final Random random;
    
    public SmsChannel(String gatewayUrl, String apiKey) {
        this(gatewayUrl, apiKey, true, 0.95);
    }
    
    public SmsChannel(String gatewayUrl, String apiKey, boolean isAvailable, double successRate) {
        this.gatewayUrl = gatewayUrl;
        this.apiKey = apiKey;
        this.isAvailable = isAvailable;
        this.successRate = successRate;
        this.random = new Random();
    }
    
    @Override
    public NotificationType getType() {
        return NotificationType.SMS;
    }
    
    @Override
    public boolean send(Notification notification) {
        // Simulate API call delay
        try {
            Thread.sleep(50 + random.nextInt(100));
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            return false;
        }
        
        // Validate phone number format (simple validation)
        String recipient = notification.getRecipient();
        if (!recipient.matches("\\+?[1-9]\\d{1,14}")) {
            System.out.println("[SMS] Invalid phone number format: " + recipient);
            return false;
        }
        
        // Simulate success/failure
        boolean success = random.nextDouble() < successRate;
        
        if (success) {
            System.out.println(String.format(
                "[SMS] Sent to %s via %s - Content: %.50s%s", 
                recipient, gatewayUrl, notification.getContent(),
                notification.getContent().length() > 50 ? "..." : ""
            ));
        } else {
            System.out.println(String.format(
                "[SMS] Failed to send to %s - Gateway error", 
                recipient
            ));
        }
        
        return success;
    }
    
    @Override
    public boolean isAvailable() {
        return isAvailable;
    }
    
    @Override
    public String getName() {
        return "SmsChannel(" + gatewayUrl + ")";
    }
}