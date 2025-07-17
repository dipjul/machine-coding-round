package com.machinecoding.examples;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicInteger;
import java.time.LocalDateTime;

/**
 * Examples demonstrating clean code principles and best practices.
 * Each example shows a "before" (poor) and "after" (improved) version.
 */
public class CleanCodeExamples {
    
    // ========================================
    // Example 1: Meaningful Names
    // ========================================
    
    // BAD: Unclear names and purpose
    public static class BadUserManager {
        private List<String[]> d; // What is 'd'?
        
        public List<String[]> getD() {
            return d;
        }
        
        public boolean chk(String s) { // What does 'chk' do?
            for (String[] arr : d) {
                if (arr[0].equals(s)) {
                    return true;
                }
            }
            return false;
        }
    }
    
    // GOOD: Clear names and purpose
    public static class UserManager {
        private List<User> activeUsers;
        
        public List<User> getActiveUsers() {
            return new ArrayList<>(activeUsers);
        }
        
        public boolean isUserActive(String userId) {
            return activeUsers.stream()
                    .anyMatch(user -> user.getId().equals(userId));
        }
    }
    
    // ========================================
    // Example 2: Single Responsibility Principle
    // ========================================
    
    // BAD: Class doing too many things
    public static class BadOrderProcessor {
        public void processOrder(Order order) {
            // Validation
            if (order.getItems().isEmpty()) {
                throw new IllegalArgumentException("Order has no items");
            }
            
            // Price calculation
            double total = 0;
            for (OrderItem item : order.getItems()) {
                total += item.getPrice() * item.getQuantity();
            }
            
            // Payment processing
            if (order.getPaymentMethod().equals("CREDIT_CARD")) {
                // Credit card logic
                System.out.println("Processing credit card payment: $" + total);
            }
            
            // Inventory update
            for (OrderItem item : order.getItems()) {
                System.out.println("Updating inventory for: " + item.getProductId());
            }
            
            // Email notification
            System.out.println("Sending confirmation email to: " + order.getCustomerEmail());
        }
    }
    
    // GOOD: Separated responsibilities
    public static class OrderValidator {
        public void validate(Order order) {
            if (order == null) {
                throw new IllegalArgumentException("Order cannot be null");
            }
            if (order.getItems().isEmpty()) {
                throw new IllegalArgumentException("Order must contain items");
            }
        }
    }
    
    public static class PriceCalculator {
        public double calculateTotal(Order order) {
            return order.getItems().stream()
                    .mapToDouble(item -> item.getPrice() * item.getQuantity())
                    .sum();
        }
    }
    
    public static class PaymentProcessor {
        public void processPayment(Order order, double amount) {
            switch (order.getPaymentMethod()) {
                case "CREDIT_CARD":
                    processCreditCardPayment(order, amount);
                    break;
                case "PAYPAL":
                    processPayPalPayment(order, amount);
                    break;
                default:
                    throw new IllegalArgumentException("Unsupported payment method");
            }
        }
        
        private void processCreditCardPayment(Order order, double amount) {
            System.out.println("Processing credit card payment: $" + amount);
        }
        
        private void processPayPalPayment(Order order, double amount) {
            System.out.println("Processing PayPal payment: $" + amount);
        }
    }
    
    public static class GoodOrderProcessor {
        private final OrderValidator validator;
        private final PriceCalculator calculator;
        private final PaymentProcessor paymentProcessor;
        private final InventoryService inventoryService;
        private final NotificationService notificationService;
        
        public GoodOrderProcessor(OrderValidator validator, PriceCalculator calculator,
                                PaymentProcessor paymentProcessor, InventoryService inventoryService,
                                NotificationService notificationService) {
            this.validator = validator;
            this.calculator = calculator;
            this.paymentProcessor = paymentProcessor;
            this.inventoryService = inventoryService;
            this.notificationService = notificationService;
        }
        
        public void processOrder(Order order) {
            validator.validate(order);
            double total = calculator.calculateTotal(order);
            paymentProcessor.processPayment(order, total);
            inventoryService.updateInventory(order);
            notificationService.sendOrderConfirmation(order);
        }
    }
    
    // ========================================
    // Example 3: Error Handling
    // ========================================
    
    // BAD: Poor error handling
    public static class BadFileReader {
        public String readFile(String filename) {
            try {
                // File reading logic
                return "file content";
            } catch (Exception e) {
                e.printStackTrace(); // Just print and continue
                return null; // Returning null is problematic
            }
        }
        
        public void processFiles(List<String> filenames) {
            for (String filename : filenames) {
                String content = readFile(filename);
                if (content != null) { // Null check everywhere
                    System.out.println("Processing: " + content);
                }
            }
        }
    }
    
    // GOOD: Proper error handling
    public static class FileReader {
        public String readFile(String filename) throws FileReadException {
            if (filename == null || filename.trim().isEmpty()) {
                throw new IllegalArgumentException("Filename cannot be null or empty");
            }
            
            try {
                // File reading logic
                return "file content";
            } catch (Exception e) {
                throw new FileReadException("Failed to read file: " + filename, e);
            }
        }
        
        public List<String> processFiles(List<String> filenames) {
            List<String> results = new ArrayList<>();
            List<String> failedFiles = new ArrayList<>();
            
            for (String filename : filenames) {
                try {
                    String content = readFile(filename);
                    results.add(content);
                } catch (FileReadException e) {
                    failedFiles.add(filename);
                    // Log the error properly
                    System.err.println("Failed to process file: " + filename + ", Error: " + e.getMessage());
                }
            }
            
            if (!failedFiles.isEmpty()) {
                System.out.println("Warning: " + failedFiles.size() + " files failed to process");
            }
            
            return results;
        }
    }
    
    public static class FileReadException extends Exception {
        public FileReadException(String message, Throwable cause) {
            super(message, cause);
        }
    }
    
    // ========================================
    // Example 4: Thread Safety
    // ========================================
    
    // BAD: Not thread-safe
    public static class BadCounter {
        private int count = 0;
        
        public void increment() {
            count++; // Race condition!
        }
        
        public int getCount() {
            return count; // May return inconsistent value
        }
        
        public void reset() {
            count = 0; // Race condition with other operations
        }
    }
    
    // GOOD: Thread-safe implementation
    public static class ThreadSafeCounter {
        private final AtomicInteger count = new AtomicInteger(0);
        
        public void increment() {
            count.incrementAndGet();
        }
        
        public int getCount() {
            return count.get();
        }
        
        public void reset() {
            count.set(0);
        }
        
        public int incrementAndGet() {
            return count.incrementAndGet();
        }
    }
    
    // Alternative: Using synchronization
    public static class SynchronizedCounter {
        private int count = 0;
        private final Object lock = new Object();
        
        public void increment() {
            synchronized (lock) {
                count++;
            }
        }
        
        public int getCount() {
            synchronized (lock) {
                return count;
            }
        }
        
        public void reset() {
            synchronized (lock) {
                count = 0;
            }
        }
    }
    
    // ========================================
    // Example 5: Resource Management
    // ========================================
    
    // BAD: Resource leaks
    public static class BadResourceManager {
        public void processData() {
            Scanner scanner = new Scanner(System.in);
            // Process data
            System.out.println("Processing...");
            // Scanner not closed - resource leak!
        }
        
        public List<String> readLines(String filename) {
            List<String> lines = new ArrayList<>();
            try {
                Scanner fileScanner = new Scanner(new java.io.File(filename));
                while (fileScanner.hasNextLine()) {
                    lines.add(fileScanner.nextLine());
                }
                return lines;
            } catch (Exception e) {
                e.printStackTrace();
                return lines;
            }
            // Scanner not closed in finally block - resource leak!
        }
    }
    
    // GOOD: Proper resource management
    public static class GoodResourceManager {
        public void processData() {
            try (Scanner scanner = new Scanner(System.in)) {
                // Process data
                System.out.println("Processing...");
                // Scanner automatically closed
            }
        }
        
        public List<String> readLines(String filename) throws java.io.IOException {
            List<String> lines = new ArrayList<>();
            
            try (Scanner fileScanner = new Scanner(new java.io.File(filename))) {
                while (fileScanner.hasNextLine()) {
                    lines.add(fileScanner.nextLine());
                }
            } // Scanner automatically closed
            
            return lines;
        }
    }
    
    // ========================================
    // Example 6: Performance Optimization
    // ========================================
    
    // BAD: Inefficient operations
    public static class BadPerformanceExample {
        public String concatenateStrings(List<String> strings) {
            String result = "";
            for (String s : strings) {
                result += s; // Creates new String object each time
            }
            return result;
        }
        
        public boolean containsUser(List<User> users, String userId) {
            for (User user : users) { // O(n) search every time
                if (user.getId().equals(userId)) {
                    return true;
                }
            }
            return false;
        }
        
        public List<String> processItems(List<String> items) {
            List<String> result = new ArrayList<>();
            for (String item : items) {
                if (item != null && !item.isEmpty()) {
                    result.add(item.toUpperCase().trim());
                }
            }
            return result;
        }
    }
    
    // GOOD: Optimized operations
    public static class GoodPerformanceExample {
        public String concatenateStrings(List<String> strings) {
            StringBuilder sb = new StringBuilder();
            for (String s : strings) {
                sb.append(s);
            }
            return sb.toString();
        }
        
        // Use Set for O(1) lookups
        private final Set<String> userIds = new HashSet<>();
        
        public boolean containsUser(String userId) {
            return userIds.contains(userId); // O(1) lookup
        }
        
        public List<String> processItems(List<String> items) {
            return items.stream()
                    .filter(item -> item != null && !item.isEmpty())
                    .map(item -> item.toUpperCase().trim())
                    .collect(ArrayList::new, ArrayList::add, ArrayList::addAll);
        }
    }
    
    // ========================================
    // Example 7: Proper Abstraction
    // ========================================
    
    // BAD: Tight coupling and no abstraction
    public static class BadNotificationSender {
        public void sendNotification(String message, String type, String recipient) {
            if (type.equals("EMAIL")) {
                // Email sending logic
                System.out.println("Sending email to " + recipient + ": " + message);
            } else if (type.equals("SMS")) {
                // SMS sending logic
                System.out.println("Sending SMS to " + recipient + ": " + message);
            } else if (type.equals("PUSH")) {
                // Push notification logic
                System.out.println("Sending push notification to " + recipient + ": " + message);
            }
            // Adding new notification type requires modifying this method
        }
    }
    
    // GOOD: Proper abstraction with strategy pattern
    public interface NotificationChannel {
        void send(String message, String recipient);
        String getChannelType();
    }
    
    public static class EmailChannel implements NotificationChannel {
        @Override
        public void send(String message, String recipient) {
            System.out.println("Sending email to " + recipient + ": " + message);
        }
        
        @Override
        public String getChannelType() {
            return "EMAIL";
        }
    }
    
    public static class SmsChannel implements NotificationChannel {
        @Override
        public void send(String message, String recipient) {
            System.out.println("Sending SMS to " + recipient + ": " + message);
        }
        
        @Override
        public String getChannelType() {
            return "SMS";
        }
    }
    
    public static class GoodNotificationSender {
        private final Map<String, NotificationChannel> channels = new ConcurrentHashMap<>();
        
        public void registerChannel(NotificationChannel channel) {
            channels.put(channel.getChannelType(), channel);
        }
        
        public void sendNotification(String message, String type, String recipient) {
            NotificationChannel channel = channels.get(type);
            if (channel == null) {
                throw new IllegalArgumentException("Unsupported notification type: " + type);
            }
            channel.send(message, recipient);
        }
        
        public Set<String> getSupportedChannels() {
            return new HashSet<>(channels.keySet());
        }
    }
    
    // ========================================
    // Supporting Classes
    // ========================================
    
    public static class User {
        private String id;
        private String name;
        private String email;
        
        public User(String id, String name, String email) {
            this.id = id;
            this.name = name;
            this.email = email;
        }
        
        public String getId() { return id; }
        public String getName() { return name; }
        public String getEmail() { return email; }
    }
    
    public static class Order {
        private List<OrderItem> items = new ArrayList<>();
        private String paymentMethod;
        private String customerEmail;
        
        public List<OrderItem> getItems() { return items; }
        public String getPaymentMethod() { return paymentMethod; }
        public String getCustomerEmail() { return customerEmail; }
        
        public void setPaymentMethod(String paymentMethod) { this.paymentMethod = paymentMethod; }
        public void setCustomerEmail(String customerEmail) { this.customerEmail = customerEmail; }
        public void addItem(OrderItem item) { this.items.add(item); }
    }
    
    public static class OrderItem {
        private String productId;
        private double price;
        private int quantity;
        
        public OrderItem(String productId, double price, int quantity) {
            this.productId = productId;
            this.price = price;
            this.quantity = quantity;
        }
        
        public String getProductId() { return productId; }
        public double getPrice() { return price; }
        public int getQuantity() { return quantity; }
    }
    
    // Mock services
    public interface InventoryService {
        void updateInventory(Order order);
    }
    
    public interface NotificationService {
        void sendOrderConfirmation(Order order);
    }
    
    // Demo method to show examples
    public static void main(String[] args) {
        System.out.println("Clean Code Examples - Best Practices Demonstration");
        
        // Example 1: Thread-safe counter
        ThreadSafeCounter counter = new ThreadSafeCounter();
        counter.increment();
        System.out.println("Counter value: " + counter.getCount());
        
        // Example 2: Good notification sender
        GoodNotificationSender sender = new GoodNotificationSender();
        sender.registerChannel(new EmailChannel());
        sender.registerChannel(new SmsChannel());
        
        sender.sendNotification("Hello World!", "EMAIL", "user@example.com");
        sender.sendNotification("Hello World!", "SMS", "+1234567890");
        
        System.out.println("Supported channels: " + sender.getSupportedChannels());
    }
}