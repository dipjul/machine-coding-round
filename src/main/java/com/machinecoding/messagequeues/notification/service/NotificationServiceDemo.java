package com.machinecoding.messagequeues.notification.service;

import com.machinecoding.messagequeues.notification.channels.EmailChannel;
import com.machinecoding.messagequeues.notification.channels.SmsChannel;
import com.machinecoding.messagequeues.notification.model.Notification;
import com.machinecoding.messagequeues.notification.model.NotificationType;
import com.machinecoding.messagequeues.notification.model.Priority;

import java.util.List;
import java.util.concurrent.TimeUnit;

/**
 * Demonstration of the NotificationService with multiple channels,
 * priority handling, retry mechanisms, and observer pattern.
 */
public class NotificationServiceDemo {
    
    public static void main(String[] args) throws InterruptedException {
        System.out.println("=== Notification Service Demo ===\n");
        
        // Create notification service
        NotificationService service = new NotificationService(3, 500);
        
        // Add observer to track events
        service.addObserver(new LoggingObserver());
        
        // Register channels
        EmailChannel emailChannel = new EmailChannel("smtp.gmail.com", 587, "demo@example.com", true, 0.8);
        SmsChannel smsChannel = new SmsChannel("https://api.twilio.com", "demo-key", true, 0.9);
        
        service.registerChannel(emailChannel);
        service.registerChannel(smsChannel);
        
        System.out.println("Registered channels: EMAIL, SMS\n");
        
        // Demo 1: Basic notifications
        System.out.println("=== Demo 1: Basic Notifications ===");
        sendBasicNotifications(service);
        Thread.sleep(2000);
        
        // Demo 2: Priority handling
        System.out.println("\n=== Demo 2: Priority Handling ===");
        sendPriorityNotifications(service);
        Thread.sleep(3000);
        
        // Demo 3: Retry mechanism
        System.out.println("\n=== Demo 3: Retry Mechanism ===");
        demonstrateRetries(service);
        Thread.sleep(4000);
        
        // Demo 4: Bulk notifications
        System.out.println("\n=== Demo 4: Bulk Notifications ===");
        sendBulkNotifications(service);
        Thread.sleep(3000);
        
        // Cleanup
        service.stop();
        System.out.println("\n=== Demo Complete ===");
    }
    
    private static void sendBasicNotifications(NotificationService service) {
        // Email notification
        Notification emailNotification = Notification.builder()
            .id("email-001")
            .recipient("user@example.com")
            .subject("Welcome to our service!")
            .content("Thank you for signing up. We're excited to have you on board.")
            .type(NotificationType.EMAIL)
            .priority(Priority.NORMAL)
            .metadata("campaign", "welcome")
            .build();
        
        // SMS notification
        Notification smsNotification = Notification.builder()
            .id("sms-001")
            .recipient("+1234567890")
            .content("Your verification code is: 123456")
            .type(NotificationType.SMS)
            .priority(Priority.HIGH)
            .build();
        
        service.sendNotification(emailNotification);
        service.sendNotification(smsNotification);
        
        System.out.println("Sent basic email and SMS notifications");
    }
    
    private static void sendPriorityNotifications(NotificationService service) {
        // Send notifications in reverse priority order to demonstrate priority queue
        List<Notification> notifications = List.of(
            createNotification("low-priority", "Low priority message", Priority.LOW),
            createNotification("normal-priority", "Normal priority message", Priority.NORMAL),
            createNotification("high-priority", "High priority message", Priority.HIGH),
            createNotification("urgent-priority", "URGENT: System maintenance in 5 minutes!", Priority.URGENT)
        );
        
        System.out.println("Sending notifications in reverse priority order...");
        service.sendNotifications(notifications);
        System.out.println("Notice how URGENT notifications are processed first!");
    }
    
    private static void demonstrateRetries(NotificationService service) {
        // Register a channel with low success rate to trigger retries
        EmailChannel unreliableChannel = new EmailChannel("unreliable.smtp.com", 587, "test", true, 0.3);
        service.registerChannel(unreliableChannel);
        
        Notification retryNotification = Notification.builder()
            .id("retry-demo")
            .recipient("retry@example.com")
            .subject("Retry Demonstration")
            .content("This notification will likely need retries due to low success rate")
            .type(NotificationType.EMAIL)
            .priority(Priority.NORMAL)
            .build();
        
        service.sendNotification(retryNotification);
        System.out.println("Sent notification with unreliable channel - watch for retries!");
    }
    
    private static void sendBulkNotifications(NotificationService service) {
        System.out.println("Sending 20 bulk notifications...");
        
        for (int i = 1; i <= 20; i++) {
            NotificationType type = (i % 2 == 0) ? NotificationType.EMAIL : NotificationType.SMS;
            String recipient = type == NotificationType.EMAIL ? 
                "user" + i + "@example.com" : "+123456789" + (i % 10);
            
            Notification notification = Notification.builder()
                .id("bulk-" + i)
                .recipient(recipient)
                .subject("Bulk notification " + i)
                .content("This is bulk notification number " + i)
                .type(type)
                .priority(i <= 5 ? Priority.HIGH : Priority.NORMAL)
                .build();
            
            service.sendNotification(notification);
        }
        
        System.out.println("Queued 20 notifications for processing");
        System.out.println("Current queue size: " + service.getQueueSize());
    }
    
    private static Notification createNotification(String id, String content, Priority priority) {
        return Notification.builder()
            .id(id)
            .recipient("demo@example.com")
            .subject("Priority Demo")
            .content(content)
            .type(NotificationType.EMAIL)
            .priority(priority)
            .build();
    }
    
    /**
     * Observer that logs notification events to console.
     */
    private static class LoggingObserver implements NotificationService.NotificationObserver {
        @Override
        public void onNotificationEvent(NotificationService.NotificationEvent event, 
                                      Notification notification, String message) {
            String timestamp = java.time.LocalTime.now().toString().substring(0, 8);
            String priorityBadge = getPriorityBadge(notification.getPriority());
            
            switch (event) {
                case QUEUED:
                    System.out.printf("[%s] %s QUEUED: %s (%s)%n", 
                        timestamp, priorityBadge, notification.getId(), notification.getType());
                    break;
                case SENDING:
                    System.out.printf("[%s] %s SENDING: %s to %s%n", 
                        timestamp, priorityBadge, notification.getId(), notification.getRecipient());
                    break;
                case SENT:
                    System.out.printf("[%s] %s ✅ SENT: %s%n", 
                        timestamp, priorityBadge, notification.getId());
                    break;
                case RETRY:
                    System.out.printf("[%s] %s 🔄 RETRY: %s - %s%n", 
                        timestamp, priorityBadge, notification.getId(), message);
                    break;
                case FAILED:
                    System.out.printf("[%s] %s ❌ FAILED: %s - %s%n", 
                        timestamp, priorityBadge, notification.getId(), message);
                    break;
            }
        }
        
        private String getPriorityBadge(Priority priority) {
            switch (priority) {
                case URGENT: return "🚨";
                case HIGH: return "⚡";
                case NORMAL: return "📧";
                case LOW: return "📝";
                default: return "📧";
            }
        }
    }
}