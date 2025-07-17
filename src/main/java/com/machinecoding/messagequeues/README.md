# Message Queues & Event Driven Systems

This package contains implementations for Chapter 2 of the Machine Coding Interview Book, covering three fundamental problems in event-driven architectures.

## Package Structure

```
messagequeues/
├── queue/                          # Problem 1: Message Queue System
│   ├── MessageQueue.java           # Core interface
│   ├── BlockingMessageQueue.java   # Java concurrent collections implementation
│   ├── CustomMessageQueue.java     # Manual synchronization implementation
│   └── MessageQueueDemo.java       # Demonstration and testing
├── notification/                   # Problem 2: Notification Service
│   ├── service/
│   │   ├── NotificationService.java    # Core service with retry logic
│   │   └── NotificationChannel.java    # Channel interface
│   ├── channels/
│   │   ├── EmailChannel.java           # Email delivery implementation
│   │   └── SmsChannel.java             # SMS delivery implementation
│   └── model/
│       ├── Notification.java           # Rich notification model
│       ├── NotificationType.java       # Supported notification types
│       └── Priority.java               # Priority levels
└── events/                         # Problem 3: Event Processing System
    ├── processor/
    │   └── EventProcessor.java         # Core event bus with priority processing
    └── model/
        ├── Event.java                  # Base event class
        ├── EventPriority.java          # Event priority levels
        ├── UserRegisteredEvent.java    # Sample user event
        └── OrderCreatedEvent.java      # Sample order event
```

## Problem 1: Message Queue System

**Location**: `com.machinecoding.messagequeues.queue`

### Key Features
- **Thread-safe producer-consumer pattern**
- **Two implementations**: BlockingQueue-based and manual synchronization
- **Bounded and unbounded queue support**
- **Timeout handling and graceful shutdown**

### Usage Example
```java
MessageQueue<String> queue = new BlockingMessageQueue<>();
queue.produce("Hello World");
String message = queue.consume();
```

### Demo
```bash
java -cp src/main/java com.machinecoding.messagequeues.queue.MessageQueueDemo
```

## Problem 2: Notification Service

**Location**: `com.machinecoding.messagequeues.notification`

### Key Features
- **Multi-channel delivery** (Email, SMS, Push, Slack, Webhook)
- **Priority-based processing** (LOW, NORMAL, HIGH, URGENT)
- **Retry mechanism** with configurable attempts and delays
- **Observer pattern** for event tracking and monitoring
- **Asynchronous processing** with thread pools

### Usage Example
```java
NotificationService service = new NotificationService();
service.registerChannel(new EmailChannel("smtp.gmail.com", 587, "user"));

Notification notification = Notification.builder()
    .id("notif-001")
    .recipient("user@example.com")
    .subject("Welcome!")
    .content("Thank you for signing up")
    .type(NotificationType.EMAIL)
    .priority(Priority.HIGH)
    .build();

service.sendNotification(notification);
```

### Demo
```bash
java -cp src/main/java com.machinecoding.messagequeues.NotificationServiceDemo
```

## Problem 3: Event Processing System

**Location**: `com.machinecoding.messagequeues.events`

### Key Features
- **Event bus architecture** with priority-based processing
- **Multiple handlers per event type**
- **Global and event-specific filtering**
- **Asynchronous processing** with separate thread pools
- **Retry mechanism** for failed event processing
- **Statistics and monitoring**

### Usage Example
```java
EventProcessor processor = new EventProcessor();

// Register handlers
processor.registerHandler("USER_REGISTERED", event -> {
    System.out.println("Sending welcome email to: " + event.getPayload());
});

// Add filters
processor.addGlobalFilter(event -> !event.getSource().contains("Test"));

// Publish events
UserRegisteredEvent event = new UserRegisteredEvent("user-123", "john@example.com", "john");
processor.publishEvent(event);
```

### Demo
```bash
java -cp src/main/java com.machinecoding.messagequeues.EventProcessorDemo
```

## Design Patterns Demonstrated

### 1. Producer-Consumer Pattern
- **Location**: Message Queue implementations
- **Purpose**: Decoupling producers from consumers with thread-safe queuing

### 2. Observer Pattern
- **Location**: Notification Service event tracking
- **Purpose**: Real-time monitoring and logging of notification events

### 3. Strategy Pattern
- **Location**: Notification channels, Event handlers
- **Purpose**: Runtime selection of delivery/processing strategies

### 4. Builder Pattern
- **Location**: Notification construction
- **Purpose**: Flexible object creation with optional parameters

### 5. Command Pattern
- **Location**: Event encapsulation
- **Purpose**: Events as command objects with payload and metadata

### 6. Chain of Responsibility
- **Location**: Event filtering
- **Purpose**: Sequential filtering with short-circuit evaluation

## Concurrency Features

### Thread Safety Mechanisms
- **ConcurrentHashMap**: Handler and channel registries
- **CopyOnWriteArrayList**: Observer and filter lists
- **PriorityBlockingQueue**: Priority-based task ordering
- **AtomicBoolean/AtomicLong**: Thread-safe state and counters
- **ReentrantLock**: Manual synchronization with conditions

### Performance Optimizations
- **Separate thread pools**: Processing vs. handler execution
- **Priority queues**: Critical events processed first
- **Lock-free data structures**: Where possible for better performance
- **Concurrent handler execution**: Maximizes throughput

## Testing

### Unit Tests
- **MessageQueueTest.java**: Comprehensive queue testing
- **NotificationServiceTest.java**: Service functionality and concurrency

### Integration Tests
- **Demo applications**: Real-world usage scenarios
- **Performance testing**: Concurrent load testing
- **Error handling**: Failure scenarios and recovery

## Interview Preparation

### Common Questions
1. **"How do you ensure thread safety?"**
   - Discuss concurrent collections vs. manual synchronization
   - Explain trade-offs and performance implications

2. **"How do you handle failures and retries?"**
   - Describe exponential backoff and circuit breaker patterns
   - Explain dead letter queues and error isolation

3. **"How would you scale these systems?"**
   - Mention distributed message queues (Kafka, RabbitMQ)
   - Discuss horizontal scaling and load balancing

### Key Concepts Covered
- **Concurrent Programming**: Thread pools, locks, atomic operations
- **Event-Driven Architecture**: Decoupling, asynchronous processing
- **System Design**: Scalability, fault tolerance, monitoring
- **Design Patterns**: Multiple patterns in practical context

## Extensions and Improvements

### Possible Enhancements
1. **Persistence**: Database storage for durability
2. **Distributed Processing**: Multi-node event processing
3. **Circuit Breaker**: Automatic failure detection
4. **Metrics Dashboard**: Real-time monitoring
5. **Event Sourcing**: Historical event replay
6. **Schema Evolution**: Event versioning support

This organized structure provides a clean separation of concerns while maintaining the educational value and practical applicability for machine coding interviews.