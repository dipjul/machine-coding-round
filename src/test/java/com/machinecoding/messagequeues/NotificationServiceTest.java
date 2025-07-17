package com.machinecoding.messagequeues;

import com.machinecoding.messagequeues.notification.service.NotificationService;
import com.machinecoding.messagequeues.notification.channels.EmailChannel;
import com.machinecoding.messagequeues.notification.channels.SmsChannel;
import com.machinecoding.messagequeues.notification.service.NotificationChannel;
import com.machinecoding.messagequeues.notification.model.Notification;
import com.machinecoding.messagequeues.notification.model.NotificationType;
import com.machinecoding.messagequeues.notification.model.Priority;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Timeout;
import static org.junit.jupiter.api.Assertions.*;

import java.util.concurrent.*;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.List;
import java.util.ArrayList;

/**
 * Comprehensive test suite for NotificationService.
 */
public class NotificationServiceTest {
    
    private NotificationService notificationService;
    private TestObserver testObserver;
    
    @BeforeEach
    void setUp() {
        notificationService = new NotificationService(2, 100);
        testObserver = new TestObserver();
        notificationService.addObserver(testObserver);
    }
    
    @AfterEach
    void tearDown() {
        notificationService.stop();
    }
    
    @Nested
    @DisplayName("Basic Functionality Tests")
    class BasicFunctionalityTests {
        
        @Test
        @DisplayName("Should register and unregister channels")
        void testChannelRegistration() {
            EmailChannel emailChannel = new EmailChannel("smtp.test.com", 587, "test@example.com");
            SmsChannel smsChannel = new SmsChannel("https://sms-api.test.com", "test-key");
            
            assertFalse(notificationService.hasChannel(NotificationType.EMAIL));
            assertFalse(notificationService.hasChannel(NotificationType.SMS));
            
            notificationService.registerChannel(emailChannel);
            notificationService.registerChannel(smsChannel);
            
            assertTrue(notificationService.hasChannel(NotificationType.EMAIL));
            assertTrue(notificationService.hasChannel(NotificationType.SMS));
            
            notificationService.unregisterChannel(NotificationType.EMAIL);
            assertFalse(notificationService.hasChannel(NotificationType.EMAIL));
            assertTrue(notificationService.hasChannel(NotificationType.SMS));
        }
        
        @Test
        @DisplayName("Should send notification successfully")
        @Timeout(5)
        void testSuccessfulNotification() throws Exception {
            // Register a mock channel that always succeeds
            notificationService.registerChannel(new MockChannel(NotificationType.EMAIL, true, true));
            
            Notification notification = Notification.builder()
                .id("test-1")
                .recipient("test@example.com")
                .subject("Test Subject")
                .content("Test content")
                .type(NotificationType.EMAIL)
                .priority(Priority.NORMAL)
                .build();
            
            CompletableFuture<Boolean> result = notificationService.sendNotification(notification);
            assertTrue(result.get(3, TimeUnit.SECONDS));
            
            // Wait for processing
            Thread.sleep(500);
            
            assertTrue(testObserver.hasEvent(NotificationService.NotificationEvent.QUEUED));
            assertTrue(testObserver.hasEvent(NotificationService.NotificationEvent.SENDING));
            assertTrue(testObserver.hasEvent(NotificationService.NotificationEvent.SENT));
        }
        
        @Test
        @DisplayName("Should handle missing channel")
        @Timeout(5)
        void testMissingChannel() throws Exception {
            Notification notification = Notification.builder()
                .id("test-2")
                .recipient("test@example.com")
                .content("Test content")
                .type(NotificationType.EMAIL)
                .build();
            
            notificationService.sendNotification(notification);
            
            // Wait for processing
            Thread.sleep(500);
            
            assertTrue(testObserver.hasEvent(NotificationService.NotificationEvent.QUEUED));
            assertTrue(testObserver.hasEvent(NotificationService.NotificationEvent.FAILED));
            assertTrue(testObserver.getLastMessage().contains("No channel registered"));
        }
    }
    
    @Nested
    @DisplayName("Retry Mechanism Tests")
    class RetryMechanismTests {
        
        @Test
        @DisplayName("Should retry failed notifications")
        @Timeout(10)
        void testRetryMechanism() throws Exception {
            // Register a channel that fails first time, succeeds second time
            MockChannel channel = new MockChannel(NotificationType.EMAIL, true, false);
            channel.setSucceedAfterAttempts(2);
            notificationService.registerChannel(channel);
            
            Notification notification = Notification.builder()
                .id("retry-test")
                .recipient("test@example.com")
                .content("Retry test")
                .type(NotificationType.EMAIL)
                .build();
            
            notificationService.sendNotification(notification);
            
            // Wait for retries to complete
            Thread.sleep(2000);
            
            assertTrue(testObserver.hasEvent(NotificationService.NotificationEvent.RETRY));
            assertTrue(testObserver.hasEvent(NotificationService.NotificationEvent.SENT));
            assertEquals(2, channel.getAttemptCount());
        }
        
        @Test
        @DisplayName("Should fail after max retries")
        @Timeout(10)
        void testMaxRetriesExceeded() throws Exception {
            // Register a channel that always fails
            MockChannel channel = new MockChannel(NotificationType.EMAIL, true, false);
            notificationService.registerChannel(channel);
            
            Notification notification = Notification.builder()
                .id("max-retry-test")
                .recipient("test@example.com")
                .content("Max retry test")
                .type(NotificationType.EMAIL)
                .build();
            
            notificationService.sendNotification(notification);
            
            // Wait for all retries to complete
            Thread.sleep(3000);
            
            assertTrue(testObserver.hasEvent(NotificationService.NotificationEvent.FAILED));
            assertTrue(testObserver.getLastMessage().contains("after 2 retries"));
            assertEquals(3, channel.getAttemptCount()); // Initial + 2 retries
        }
    }
    
    @Nested
    @DisplayName("Priority and Concurrency Tests")
    class PriorityAndConcurrencyTests {
        
        @Test
        @DisplayName("Should process high priority notifications first")
        @Timeout(10)
        void testPriorityProcessing() throws Exception {
            MockChannel channel = new MockChannel(NotificationType.EMAIL, true, true);
            channel.setProcessingDelay(200); // Add delay to see ordering
            notificationService.registerChannel(channel);
            
            // Send notifications in reverse priority order
            List<Notification> notifications = List.of(
                createNotification("low", Priority.LOW),
                createNotification("normal", Priority.NORMAL),
                createNotification("high", Priority.HIGH),
                createNotification("urgent", Priority.URGENT)
            );
            
            notificationService.sendNotifications(notifications);
            
            // Wait for processing
            Thread.sleep(2000);
            
            List<String> processedOrder = channel.getProcessedNotifications();
            assertEquals(4, processedOrder.size());
            
            // Verify urgent was processed first, low was processed last
            assertEquals("urgent", processedOrder.get(0));
            assertEquals("low", processedOrder.get(3));
        }
        
        @Test
        @DisplayName("Should handle concurrent notifications")
        @Timeout(15)
        void testConcurrentNotifications() throws Exception {
            MockChannel channel = new MockChannel(NotificationType.EMAIL, true, true);
            notificationService.registerChannel(channel);
            
            final int notificationCount = 100;
            final AtomicInteger sentCount = new AtomicInteger(0);
            
            ExecutorService executor = Executors.newFixedThreadPool(10);
            CountDownLatch latch = new CountDownLatch(notificationCount);
            
            for (int i = 0; i < notificationCount; i++) {
                final int id = i;
                executor.submit(() -> {
                    try {
                        Notification notification = Notification.builder()
                            .id("concurrent-" + id)
                            .recipient("user" + id + "@example.com")
                            .content("Concurrent test " + id)
                            .type(NotificationType.EMAIL)
                            .build();
                        
                        notificationService.sendNotification(notification);
                        sentCount.incrementAndGet();
                    } finally {
                        latch.countDown();
                    }
                });
            }
            
            assertTrue(latch.await(5, TimeUnit.SECONDS));
            assertEquals(notificationCount, sentCount.get());
            
            // Wait for processing
            Thread.sleep(3000);
            
            assertEquals(notificationCount, channel.getProcessedNotifications().size());
        }
    }
    
    @Nested
    @DisplayName("Observer Pattern Tests")
    class ObserverPatternTests {
        
        @Test
        @DisplayName("Should notify multiple observers")
        @Timeout(5)
        void testMultipleObservers() throws Exception {
            TestObserver observer2 = new TestObserver();
            notificationService.addObserver(observer2);
            
            MockChannel channel = new MockChannel(NotificationType.EMAIL, true, true);
            notificationService.registerChannel(channel);
            
            Notification notification = createNotification("observer-test", Priority.NORMAL);
            notificationService.sendNotification(notification);
            
            Thread.sleep(500);
            
            assertTrue(testObserver.hasEvent(NotificationService.NotificationEvent.SENT));
            assertTrue(observer2.hasEvent(NotificationService.NotificationEvent.SENT));
        }
        
        @Test
        @DisplayName("Should handle observer removal")
        @Timeout(5)
        void testObserverRemoval() throws Exception {
            TestObserver observer2 = new TestObserver();
            notificationService.addObserver(observer2);
            notificationService.removeObserver(testObserver);
            
            MockChannel channel = new MockChannel(NotificationType.EMAIL, true, true);
            notificationService.registerChannel(channel);
            
            Notification notification = createNotification("removal-test", Priority.NORMAL);
            notificationService.sendNotification(notification);
            
            Thread.sleep(500);
            
            assertFalse(testObserver.hasEvent(NotificationService.NotificationEvent.SENT));
            assertTrue(observer2.hasEvent(NotificationService.NotificationEvent.SENT));
        }
    }
    
    private Notification createNotification(String id, Priority priority) {
        return Notification.builder()
            .id(id)
            .recipient("test@example.com")
            .content("Test content for " + id)
            .type(NotificationType.EMAIL)
            .priority(priority)
            .build();
    }
    
    /**
     * Test observer implementation for tracking events.
     */
    private static class TestObserver implements NotificationService.NotificationObserver {
        private final List<NotificationService.NotificationEvent> events = new CopyOnWriteArrayList<>();
        private volatile String lastMessage;
        
        @Override
        public void onNotificationEvent(NotificationService.NotificationEvent event, 
                                      Notification notification, String message) {
            events.add(event);
            lastMessage = message;
        }
        
        public boolean hasEvent(NotificationService.NotificationEvent event) {
            return events.contains(event);
        }
        
        public String getLastMessage() {
            return lastMessage;
        }
        
        public List<NotificationService.NotificationEvent> getEvents() {
            return new ArrayList<>(events);
        }
    }
    
    /**
     * Mock channel for testing.
     */
    private static class MockChannel implements NotificationChannel {
        private final NotificationType type;
        private final boolean available;
        private boolean shouldSucceed;
        private int succeedAfterAttempts = 1;
        private int attemptCount = 0;
        private int processingDelay = 0;
        private final List<String> processedNotifications = new CopyOnWriteArrayList<>();
        
        public MockChannel(NotificationType type, boolean available, boolean shouldSucceed) {
            this.type = type;
            this.available = available;
            this.shouldSucceed = shouldSucceed;
        }
        
        @Override
        public NotificationType getType() {
            return type;
        }
        
        @Override
        public boolean send(Notification notification) {
            attemptCount++;
            
            if (processingDelay > 0) {
                try {
                    Thread.sleep(processingDelay);
                } catch (InterruptedException e) {
                    Thread.currentThread().interrupt();
                    return false;
                }
            }
            
            boolean success = shouldSucceed || attemptCount >= succeedAfterAttempts;
            if (success) {
                processedNotifications.add(notification.getId());
            }
            
            return success;
        }
        
        @Override
        public boolean isAvailable() {
            return available;
        }
        
        @Override
        public String getName() {
            return "MockChannel(" + type + ")";
        }
        
        public void setSucceedAfterAttempts(int attempts) {
            this.succeedAfterAttempts = attempts;
        }
        
        public void setProcessingDelay(int delayMs) {
            this.processingDelay = delayMs;
        }
        
        public int getAttemptCount() {
            return attemptCount;
        }
        
        public List<String> getProcessedNotifications() {
            return new ArrayList<>(processedNotifications);
        }
    }
}