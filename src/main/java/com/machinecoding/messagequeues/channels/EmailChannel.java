package com.machinecoding.messagequeues.channels;

import com.machinecoding.messagequeues.NotificationChannel;
import com.machinecoding.messagequeues.notification.Notification;
import com.machinecoding.messagequeues.notification.NotificationType;

import java.util.Random;

/**
 * Email notification channel implementation.
 * Simulates email delivery with configurable success rate.
 */
public class EmailChannel implements NotificationChannel {
    
    private final String smtpServer;
    private final int port;
    private final String username;
    private final boolean isAvailable;
    private final double successRate;
    private final Random random;
    
    public EmailChannel(String smtpServer, int port, String username) {
        this(smtpServer, port, username, true, 0.9);
    }
    
    public EmailChannel(String smtpServer, int port, String username, 
                       boolean isAvailable, double successRate) {
        this.smtpServer = smtpServer;
        this.port = port;
        this.username = username;
        this.isAvailable = isAvailable;
        this.successRate = successRate;
        this.random = new Random();
    }
    
    @Override
    public NotificationType getType() {
        return NotificationType.EMAIL;
    }
    
    @Override
    public boolean send(Notification notification) {
        // Simulate network delay
        try {
            Thread.sleep(100 + random.nextInt(200));
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            return false;
        }
        
        // Simulate success/failure based on success rate
        boolean success = random.nextDouble() < successRate;
        
        if (success) {
            System.out.println(String.format(
                "[EMAIL] Sent to %s via %s:%d - Subject: %s", 
                notification.getRecipient(), smtpServer, port, notification.getSubject()
            ));
        } else {
            System.out.println(String.format(
                "[EMAIL] Failed to send to %s - SMTP error", 
                notification.getRecipient()
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
        return "EmailChannel(" + smtpServer + ":" + port + ")";
    }
}