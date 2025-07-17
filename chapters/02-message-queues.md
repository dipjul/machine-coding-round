# Chapter 2: Message Queues & Event Driven Systems

## Table of Contents
1. [Introduction to Message Queues](#introduction-to-message-queues)
2. [Message Queue System Implementation](#message-queue-system-implementation)
3. [Notification Service Implementation](#notification-service-implementation)
4. [Event Processing System Implementation](#event-processing-system-implementation)
5. [Design Patterns and Best Practices](#design-patterns-and-best-practices)
6. [Performance Considerations](#performance-considerations)
7. [Interview Questions and Solutions](#interview-questions-and-solutions)

## Introduction to Message Queues

Message queues are fundamental components in distributed systems that enable asynchronous communication between different parts of an application. They provide a reliable way to decouple producers and consumers, allowing systems to scale and handle varying loads effectively.

### Key Concepts

**Producer-Consumer Pattern:**
- **Producers** create and send messages to the queue
- **Consumers** receive and process messages from the queue
- **Queue** acts as a buffer between producers and consumers

**Benefits:**
- **Decoupling**: Producers and consumers don't need to know about each other
- **Scalability**: Multiple producers and consumers can work independently
- **Reliability**: Messages are persisted until successfully processed
- **Load Balancing**: Work can be distributed across multiple consumers

### Common Use Cases

1. **Task Processing**: Background job processing
2. **Event Notification**: System-wide event broadcasting
3. **Data Pipeline**: ETL operations and data streaming
4. **Microservices Communication**: Inter-service messaging
5. **Load Leveling**: Handling traffic spikes

## Message Queue System Implementation

### Core Components

Our message queue implementation consists of several key components:

```java
// Core interface
public interface MessageQueue<T> {
    void produce(T message) throws InterruptedException;
    T consume() throws InterruptedException;
    T consume(long timeoutMs) throws InterruptedException;
    int size();
    boolean isEmpty();
    void shutdown();
}
```

### Implementation Details

**File Location:** `src/main/java/com/machinecoding/messagequeues/queue/CustomMessageQueue.java`

#### Key Features:
1. **Thread Safety**: Uses ReentrantLock and Conditions for safe concurrent access
2. **Bounded Capacity**: Configurable maximum queue size
3. **Blocking Operations**: Producers block when queue is full, consumers block when empty
4. **Graceful Shutdown**: Proper resource cleanup and thread interruption handling

#### Core Implementation:

```java
public class CustomMessageQueue<T> implements MessageQueue<T> {
    private final Queue<T> queue;
    private final int capacity;
    private final ReentrantLock lock;
    private final Condition notEmpty;
    private final Condition notFull;
    private volatile boolean isShutdown;
    
    @Override
    public void produce(T message) throws InterruptedException {
        if (message == null) {
            throw new IllegalArgumentException("Message cannot be null");
        }
        
        lock.lock();
        try {
            // Wait while queue is full
            while (queue.size() >= capacity && !isShutdown) {
                notFull.await();
            }
            
            if (isShutdown) {
                throw new IllegalStateException("Queue is shutdown");
            }
            
            queue.offer(message);
            notEmpty.signal(); // Notify waiting consumers
            
        } finally {
            lock.unlock();
        }
    }
    
    @Override
    public T consume() throws InterruptedException {
        lock.lock();
        try {
            // Wait while queue is empty and not shutdown
            while (queue.isEmpty() && !isShutdown) {
                notEmpty.await();
            }
            
            T message = queue.poll();
            if (message != null) {
                notFull.signal(); // Notify waiting producers
            }
            
            return message;
            
        } finally {
            lock.unlock();
        }
    }
}
```

### Usage Example

```java
// Create a bounded message queue
CustomMessageQueue<String> messageQueue = new CustomMessageQueue<>(1000);

// Producer thread
Thread producer = new Thread(() -> {
    try {
        for (int i = 0; i < 100; i++) {
            messageQueue.produce("Message " + i);
            Thread.sleep(10); // Simulate processing time
        }
    } catch (InterruptedException e) {
        Thread.currentThread().interrupt();
    }
});

// Consumer thread
Thread consumer = new Thread(() -> {
    try {
        while (!Thread.currentThread().isInterrupted()) {
            String message = messageQueue.consume();
            if (message != null) {
                System.out.println("Processed: " + message);
            }
        }
    } catch (InterruptedException e) {
        Thread.currentThread().interrupt();
    }
});

producer.start();
consumer.start();
```

## Notification Service Implementation

### Architecture Overview

The notification service implements the Observer pattern to provide a flexible, multi-channel notification system.

**File Location:** `src/main/java/com/machinecoding/messagequeues/notification/service/NotificationService.java`

### Key Components

#### 1. Notification Model
```java
public class Notification {
    private final String id;
    private final String recipient;
    private final String title;
    private final String message;
    private final NotificationType type;
    private final Priority priority;
    private final long timestamp;
    
    // Builder pattern for flexible construction
    public static class Builder {
        // Builder implementation
    }
}
```

#### 2. Notification Channels
```java
public interface NotificationChannel {
    void send(String message, String recipient);
    String getChannelType();
}

// Email channel implementation
public class EmailChannel implements NotificationChannel {
    @Override
    public void send(String message, String recipient) {
        // Email sending logic
        System.out.println("Sending email to " + recipient + ": " + message);
    }
}

// SMS channel implementation
public class SmsChannel implements NotificationChannel {
    @Override
    public void send(String message, String recipient) {
        // SMS sending logic
        System.out.println("Sending SMS to " + recipient + ": " + message);
    }
}
```

#### 3. Main Service
```java
public class NotificationService {
    private final Map<String, NotificationChannel> channels;
    private final PriorityQueue<NotificationTask> taskQueue;
    private final ExecutorService executorService;
    private final List<NotificationObserver> observers;
    
    public void sendNotification(Notification notification) {
        NotificationTask task = new NotificationTask(notification);
        taskQueue.offer(task);
        
        // Process asynchronously
        executorService.submit(() -> processNotification(task));
    }
    
    private void processNotification(NotificationTask task) {
        try {
            NotificationChannel channel = getChannel(task.getNotification().getType());
            channel.send(task.getNotification().getMessage(), 
                        task.getNotification().getRecipient());
            
            // Notify observers of successful delivery
            notifyObservers(new NotificationEvent(task.getNotification(), "SENT"));
            
        } catch (Exception e) {
            // Handle retry logic
            handleFailure(task, e);
        }
    }
}
```

### Features

1. **Multi-Channel Support**: Email, SMS, Push notifications
2. **Priority Handling**: High-priority notifications processed first
3. **Retry Mechanism**: Automatic retry with exponential backoff
4. **Observer Pattern**: Event notifications for delivery status
5. **Asynchronous Processing**: Non-blocking notification sending

### Usage Example

```java
NotificationService service = new NotificationService();

// Register channels
service.registerChannel(new EmailChannel());
service.registerChannel(new SmsChannel());

// Create notification
Notification notification = new Notification.Builder()
    .recipient("user@example.com")
    .title("Welcome!")
    .message("Welcome to our platform")
    .type(NotificationType.EMAIL)
    .priority(Priority.HIGH)
    .build();

// Send notification
service.sendNotification(notification);
```

## Event Processing System Implementation

### Architecture

The event processing system provides a scalable, asynchronous event handling mechanism with filtering and routing capabilities.

**File Location:** `src/main/java/com/machinecoding/messagequeues/events/processor/EventProcessor.java`

### Core Components

#### 1. Event Model
```java
public class Event {
    private final String id;
    private final String type;
    private final Object payload;
    private final long timestamp;
    private final EventPriority priority;
    private final Map<String, String> metadata;
}
```

#### 2. Event Handlers
```java
public interface EventHandler {
    void handle(Event event);
    boolean canHandle(Event event);
    String getHandlerType();
}

// Example handler implementation
public class UserRegistrationHandler implements EventHandler {
    @Override
    public void handle(Event event) {
        // Process user registration event
        System.out.println("Processing user registration: " + event.getPayload());
    }
    
    @Override
    public boolean canHandle(Event event) {
        return "USER_REGISTERED".equals(event.getType());
    }
}
```

#### 3. Event Processor
```java
public class EventProcessor {
    private final Map<String, List<EventHandler>> handlers;
    private final ExecutorService executorService;
    private final BlockingQueue<EventTask> eventQueue;
    private final List<EventFilter> filters;
    
    public void publishEvent(Event event) {
        // Apply filters
        if (shouldProcessEvent(event)) {
            EventTask task = new EventTask(event);
            eventQueue.offer(task);
        }
    }
    
    public void registerHandler(EventHandler handler) {
        handlers.computeIfAbsent(handler.getHandlerType(), k -> new ArrayList<>())
                .add(handler);
    }
    
    private void processEvent(EventTask task) {
        Event event = task.getEvent();
        List<EventHandler> eventHandlers = getHandlersForEvent(event);
        
        // Process handlers in parallel for better performance
        eventHandlers.parallelStream().forEach(handler -> {
            try {
                if (handler.canHandle(event)) {
                    handler.handle(event);
                }
            } catch (Exception e) {
                handleProcessingError(event, handler, e);
            }
        });
    }
}
```

### Features

1. **Asynchronous Processing**: Events processed in background threads
2. **Event Filtering**: Configurable filters to control event processing
3. **Handler Registration**: Dynamic handler registration and management
4. **Parallel Processing**: Multiple handlers can process events concurrently
5. **Error Handling**: Robust error handling with retry mechanisms
6. **Priority Support**: High-priority events processed first

### Usage Example

```java
EventProcessor processor = new EventProcessor();

// Register handlers
processor.registerHandler(new UserRegistrationHandler());
processor.registerHandler(new EmailNotificationHandler());
processor.registerHandler(new AnalyticsHandler());

// Publish events
Event userRegistered = new Event.Builder()
    .type("USER_REGISTERED")
    .payload(new UserData("john@example.com", "John Doe"))
    .priority(EventPriority.HIGH)
    .build();

processor.publishEvent(userRegistered);
```

## Design Patterns and Best Practices

### 1. Producer-Consumer Pattern
- **Decoupling**: Producers and consumers operate independently
- **Buffering**: Queue acts as a buffer to handle load variations
- **Scalability**: Multiple producers and consumers can be added

### 2. Observer Pattern
- **Event Notification**: Observers are notified of state changes
- **Loose Coupling**: Subjects don't need to know about specific observers
- **Dynamic Subscription**: Observers can be added/removed at runtime

### 3. Strategy Pattern
- **Channel Selection**: Different notification channels implement the same interface
- **Algorithm Variation**: Different processing strategies can be plugged in
- **Runtime Selection**: Strategy can be chosen at runtime

### Best Practices

#### Thread Safety
```java
// Use proper synchronization
private final ReentrantLock lock = new ReentrantLock();
private final Condition notEmpty = lock.newCondition();

// Always use try-finally with locks
lock.lock();
try {
    // Critical section
} finally {
    lock.unlock();
}
```

#### Resource Management
```java
// Proper shutdown handling
public void shutdown() {
    isShutdown = true;
    lock.lock();
    try {
        notEmpty.signalAll();
        notFull.signalAll();
    } finally {
        lock.unlock();
    }
    
    executorService.shutdown();
    try {
        if (!executorService.awaitTermination(5, TimeUnit.SECONDS)) {
            executorService.shutdownNow();
        }
    } catch (InterruptedException e) {
        executorService.shutdownNow();
        Thread.currentThread().interrupt();
    }
}
```

#### Error Handling
```java
// Robust error handling with retry
private void handleFailure(NotificationTask task, Exception e) {
    if (task.getRetryCount() < maxRetries) {
        task.incrementRetryCount();
        
        // Exponential backoff
        long delay = (long) Math.pow(2, task.getRetryCount()) * 1000;
        
        scheduledExecutor.schedule(() -> {
            processNotification(task);
        }, delay, TimeUnit.MILLISECONDS);
    } else {
        // Send to dead letter queue or log failure
        handlePermanentFailure(task, e);
    }
}
```

## Performance Considerations

### 1. Queue Sizing
- **Bounded Queues**: Prevent memory exhaustion
- **Capacity Planning**: Size based on expected load
- **Monitoring**: Track queue depth and processing rates

### 2. Thread Pool Management
```java
// Configure thread pools appropriately
ExecutorService executorService = new ThreadPoolExecutor(
    corePoolSize,           // Core threads
    maximumPoolSize,        // Maximum threads
    keepAliveTime,          // Thread timeout
    TimeUnit.SECONDS,
    new LinkedBlockingQueue<>(queueCapacity),
    new ThreadPoolExecutor.CallerRunsPolicy() // Backpressure handling
);
```

### 3. Batching
```java
// Process messages in batches for better throughput
public void processBatch(List<Message> messages) {
    // Batch processing logic
    messages.parallelStream().forEach(this::processMessage);
}
```

### 4. Memory Management
- **Message Size**: Keep messages reasonably sized
- **Cleanup**: Remove processed messages promptly
- **Monitoring**: Track memory usage and GC patterns

## Interview Questions and Solutions

### Question 1: Design a Message Queue System

**Problem:** Design a thread-safe message queue that supports multiple producers and consumers.

**Key Points to Cover:**
1. Thread safety mechanisms
2. Blocking vs non-blocking operations
3. Capacity management
4. Graceful shutdown

**Solution Approach:**
```java
public class MessageQueue<T> {
    private final Queue<T> queue = new LinkedList<>();
    private final int capacity;
    private final ReentrantLock lock = new ReentrantLock();
    private final Condition notEmpty = lock.newCondition();
    private final Condition notFull = lock.newCondition();
    
    // Implementation as shown above
}
```

### Question 2: Implement a Notification System

**Problem:** Design a notification system that can send notifications through multiple channels (email, SMS, push).

**Key Points to Cover:**
1. Strategy pattern for different channels
2. Priority handling
3. Retry mechanisms
4. Observer pattern for status updates

**Solution Approach:**
- Use Strategy pattern for notification channels
- Implement priority queue for message ordering
- Add retry logic with exponential backoff
- Use Observer pattern for delivery status notifications

### Question 3: Event Processing System

**Problem:** Design an event processing system that can handle high-throughput events with multiple handlers.

**Key Points to Cover:**
1. Asynchronous processing
2. Event filtering
3. Handler registration
4. Error handling and recovery

**Solution Approach:**
- Use thread pools for asynchronous processing
- Implement event filters for selective processing
- Support dynamic handler registration
- Add comprehensive error handling with dead letter queues

## Summary

Message queues and event-driven systems are fundamental building blocks for scalable, distributed applications. Key takeaways:

1. **Decoupling**: Message queues decouple producers from consumers
2. **Scalability**: Asynchronous processing enables better scalability
3. **Reliability**: Proper error handling and retry mechanisms ensure reliability
4. **Performance**: Thread pools and batching improve performance
5. **Flexibility**: Design patterns enable flexible, extensible architectures

The implementations in this chapter demonstrate production-ready patterns that can be adapted for various use cases in machine coding interviews and real-world applications.