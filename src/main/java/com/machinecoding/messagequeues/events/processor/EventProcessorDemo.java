package com.machinecoding.messagequeues.events.processor;

import com.machinecoding.messagequeues.events.model.Event;
import com.machinecoding.messagequeues.events.model.EventPriority;
import com.machinecoding.messagequeues.events.UserRegisteredEvent;
import com.machinecoding.messagequeues.events.OrderCreatedEvent;

import java.math.BigDecimal;
import java.util.List;
import java.util.ArrayList;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * Demonstration of the Event Processing System with multiple handlers,
 * filters, priority processing, and asynchronous execution.
 */
public class EventProcessorDemo {
    
    public static void main(String[] args) throws InterruptedException {
        System.out.println("=== Event Processing System Demo ===\n");
        
        EventProcessor processor = new EventProcessor();
        
        // Register event handlers
        registerHandlers(processor);
        
        // Add global filters
        addFilters(processor);
        
        System.out.println("Registered handlers for: " + processor.getRegisteredEventTypes());
        System.out.println("Initial stats: " + processor.getStats() + "\n");
        
        // Demo 1: Basic event processing
        System.out.println("=== Demo 1: Basic Event Processing ===");
        processBasicEvents(processor);
        Thread.sleep(2000);
        
        // Demo 2: Priority-based processing
        System.out.println("\n=== Demo 2: Priority-Based Processing ===");
        processPriorityEvents(processor);
        Thread.sleep(3000);
        
        // Demo 3: Bulk event processing
        System.out.println("\n=== Demo 3: Bulk Event Processing ===");
        processBulkEvents(processor);
        Thread.sleep(4000);
        
        // Demo 4: Event filtering
        System.out.println("\n=== Demo 4: Event Filtering ===");
        demonstrateFiltering(processor);
        Thread.sleep(2000);
        
        // Final stats
        System.out.println("\n=== Final Statistics ===");
        System.out.println("Final stats: " + processor.getStats());
        
        processor.stop();
        System.out.println("\n=== Demo Complete ===");
    }
    
    private static void registerHandlers(EventProcessor processor) {
        // User registration handlers
        processor.registerHandler("USER_REGISTERED", new WelcomeEmailHandler());
        processor.registerHandler("USER_REGISTERED", new UserAnalyticsHandler());
        processor.registerHandler("USER_REGISTERED", new AccountSetupHandler());
        
        // Order creation handlers
        processor.registerHandler("ORDER_CREATED", new OrderConfirmationHandler());
        processor.registerHandler("ORDER_CREATED", new InventoryUpdateHandler());
        processor.registerHandler("ORDER_CREATED", new PaymentProcessingHandler());
        processor.registerHandler("ORDER_CREATED", new ShippingHandler());
        
        System.out.println("Registered handlers for user registration and order creation events");
    }
    
    private static void addFilters(EventProcessor processor) {
        // Filter to skip events from test sources
        processor.addGlobalFilter(event -> !event.getSource().contains("Test"));
        
        // Filter to process only recent events (within last hour)
        processor.addGlobalFilter(event -> 
            event.getTimestamp().isAfter(java.time.LocalDateTime.now().minusHours(1))
        );
        
        System.out.println("Added global filters for source and timestamp validation");
    }
    
    private static void processBasicEvents(EventProcessor processor) {
        // Create user registration event
        UserRegisteredEvent userEvent = new UserRegisteredEvent(
            "user-123", "john.doe@example.com", "johndoe"
        );
        userEvent.addMetadata("registrationSource", "web");
        userEvent.addMetadata("referralCode", "FRIEND2024");
        
        // Create order event
        OrderCreatedEvent orderEvent = new OrderCreatedEvent(
            "order-456", "user-123", new BigDecimal("299.99"), "USD"
        );
        orderEvent.addMetadata("paymentMethod", "credit_card");
        orderEvent.addMetadata("shippingAddress", "123 Main St, City, State");
        
        processor.publishEvent(userEvent);
        processor.publishEvent(orderEvent);
        
        System.out.println("Published basic user registration and order events");
    }
    
    private static void processPriorityEvents(EventProcessor processor) {
        List<Event> events = new ArrayList<>();
        
        // Create events with different priorities
        events.add(new UserRegisteredEvent("user-low", "low@example.com", "lowuser"));
        
        OrderCreatedEvent normalOrder = new OrderCreatedEvent(
            "order-normal", "user-normal", new BigDecimal("50.00"), "USD"
        );
        events.add(normalOrder);
        
        OrderCreatedEvent highValueOrder = new OrderCreatedEvent(
            "order-high", "user-vip", new BigDecimal("2500.00"), "USD"
        );
        highValueOrder.addMetadata("customerTier", "VIP");
        events.add(highValueOrder);
        
        // Add a critical system event
        Event criticalEvent = new Event("SYSTEM_ALERT", "SystemMonitor") {
            @Override
            public Object getPayload() {
                return "Database connection pool exhausted";
            }
            
            @Override
            public EventPriority getPriority() {
                return EventPriority.CRITICAL;
            }
        };
        events.add(criticalEvent);
        
        processor.registerHandler("SYSTEM_ALERT", new SystemAlertHandler());
        
        System.out.println("Publishing events with different priorities...");
        processor.publishEvents(events);
        System.out.println("Notice how CRITICAL and HIGH priority events are processed first!");
    }
    
    private static void processBulkEvents(EventProcessor processor) {
        List<Event> bulkEvents = new ArrayList<>();
        
        // Generate 50 user registration events
        for (int i = 1; i <= 25; i++) {
            UserRegisteredEvent event = new UserRegisteredEvent(
                "bulk-user-" + i, 
                "user" + i + "@example.com", 
                "user" + i
            );
            event.addMetadata("batch", "bulk-registration");
            bulkEvents.add(event);
        }
        
        // Generate 25 order events
        for (int i = 1; i <= 25; i++) {
            BigDecimal amount = new BigDecimal(Math.random() * 1000 + 10);
            OrderCreatedEvent event = new OrderCreatedEvent(
                "bulk-order-" + i,
                "bulk-user-" + (i % 25 + 1),
                amount,
                "USD"
            );
            event.addMetadata("batch", "bulk-orders");
            bulkEvents.add(event);
        }
        
        System.out.println("Processing 50 bulk events (25 users + 25 orders)...");
        processor.publishEvents(bulkEvents);
        System.out.println("Current queue size: " + processor.getQueueSize());
    }
    
    private static void demonstrateFiltering(EventProcessor processor) {
        // This event should be filtered out (test source)
        UserRegisteredEvent testEvent = new UserRegisteredEvent(
            "test-user", "test@example.com", "testuser"
        ) {
            @Override
            public String getSource() {
                return "TestService";
            }
        };
        
        // This event should be processed (normal source)
        UserRegisteredEvent normalEvent = new UserRegisteredEvent(
            "normal-user", "normal@example.com", "normaluser"
        );
        
        processor.publishEvent(testEvent);
        processor.publishEvent(normalEvent);
        
        System.out.println("Published test event (should be filtered) and normal event");
        System.out.println("Only the normal event should be processed due to filtering");
    }
    
    // Event Handler Implementations
    
    private static class WelcomeEmailHandler implements EventProcessor.EventHandler {
        @Override
        public void handle(Event event) throws Exception {
            if (event instanceof UserRegisteredEvent) {
                UserRegisteredEvent userEvent = (UserRegisteredEvent) event;
                Thread.sleep(100); // Simulate email sending delay
                System.out.println("📧 Sent welcome email to: " + userEvent.getEmail());
            }
        }
    }
    
    private static class UserAnalyticsHandler implements EventProcessor.EventHandler {
        @Override
        public void handle(Event event) throws Exception {
            if (event instanceof UserRegisteredEvent) {
                UserRegisteredEvent userEvent = (UserRegisteredEvent) event;
                Thread.sleep(50); // Simulate analytics processing
                System.out.println("📊 Updated user analytics for: " + userEvent.getUserId());
            }
        }
    }
    
    private static class AccountSetupHandler implements EventProcessor.EventHandler {
        @Override
        public void handle(Event event) throws Exception {
            if (event instanceof UserRegisteredEvent) {
                UserRegisteredEvent userEvent = (UserRegisteredEvent) event;
                Thread.sleep(150); // Simulate account setup
                System.out.println("⚙️ Set up account for: " + userEvent.getUsername());
            }
        }
    }
    
    private static class OrderConfirmationHandler implements EventProcessor.EventHandler {
        @Override
        public void handle(Event event) throws Exception {
            if (event instanceof OrderCreatedEvent) {
                OrderCreatedEvent orderEvent = (OrderCreatedEvent) event;
                Thread.sleep(80); // Simulate confirmation processing
                System.out.println("✅ Order confirmation sent for: " + orderEvent.getOrderId() + 
                                 " ($" + orderEvent.getAmount() + ")");
            }
        }
    }
    
    private static class InventoryUpdateHandler implements EventProcessor.EventHandler {
        @Override
        public void handle(Event event) throws Exception {
            if (event instanceof OrderCreatedEvent) {
                OrderCreatedEvent orderEvent = (OrderCreatedEvent) event;
                Thread.sleep(120); // Simulate inventory update
                System.out.println("📦 Updated inventory for order: " + orderEvent.getOrderId());
            }
        }
    }
    
    private static class PaymentProcessingHandler implements EventProcessor.EventHandler {
        @Override
        public void handle(Event event) throws Exception {
            if (event instanceof OrderCreatedEvent) {
                OrderCreatedEvent orderEvent = (OrderCreatedEvent) event;
                Thread.sleep(200); // Simulate payment processing
                System.out.println("💳 Processed payment for order: " + orderEvent.getOrderId() + 
                                 " ($" + orderEvent.getAmount() + ")");
            }
        }
    }
    
    private static class ShippingHandler implements EventProcessor.EventHandler {
        @Override
        public void handle(Event event) throws Exception {
            if (event instanceof OrderCreatedEvent) {
                OrderCreatedEvent orderEvent = (OrderCreatedEvent) event;
                Thread.sleep(100); // Simulate shipping setup
                System.out.println("🚚 Shipping arranged for order: " + orderEvent.getOrderId());
            }
        }
    }
    
    private static class SystemAlertHandler implements EventProcessor.EventHandler {
        @Override
        public void handle(Event event) throws Exception {
            Thread.sleep(50); // Simulate alert processing
            System.out.println("🚨 CRITICAL ALERT: " + event.getPayload());
        }
    }
}