# Chapter 10: Best Practices and Interview Preparation

## Table of Contents
1. [Clean Code Principles](#clean-code-principles)
2. [Design Patterns in Machine Coding](#design-patterns-in-machine-coding)
3. [Common Mistakes and How to Avoid Them](#common-mistakes-and-how-to-avoid-them)
4. [Interview Preparation Strategies](#interview-preparation-strategies)
5. [Time Management Techniques](#time-management-techniques)
6. [Code Review Checklist](#code-review-checklist)
7. [Performance Optimization Tips](#performance-optimization-tips)
8. [Testing Best Practices](#testing-best-practices)

## Clean Code Principles

### 1. Meaningful Names

**Bad Example:**
```java
public class D {
    private List<int[]> l;
    
    public List<int[]> getL() {
        List<int[]> l1 = new ArrayList<>();
        for (int[] x : l) {
            if (x[0] == 4) {
                l1.add(x);
            }
        }
        return l1;
    }
}
```

**Good Example:**
```java
public class GameBoard {
    private List<Cell> cells;
    
    public List<Cell> getFlaggedCells() {
        List<Cell> flaggedCells = new ArrayList<>();
        for (Cell cell : cells) {
            if (cell.isFlagged()) {
                flaggedCells.add(cell);
            }
        }
        return flaggedCells;
    }
}
```

### 2. Single Responsibility Principle

**Bad Example:**
```java
public class User {
    private String name;
    private String email;
    
    // User data management
    public void setName(String name) { this.name = name; }
    public void setEmail(String email) { this.email = email; }
    
    // Email functionality - violates SRP
    public void sendEmail(String message) {
        // Email sending logic
        System.out.println("Sending email to " + email + ": " + message);
    }
    
    // Database operations - violates SRP
    public void saveToDatabase() {
        // Database saving logic
        System.out.println("Saving user to database");
    }
}
```

**Good Example:**
```java
public class User {
    private String name;
    private String email;
    
    // Only user data management
    public void setName(String name) { this.name = name; }
    public void setEmail(String email) { this.email = email; }
    public String getName() { return name; }
    public String getEmail() { return email; }
}

public class EmailService {
    public void sendEmail(User user, String message) {
        System.out.println("Sending email to " + user.getEmail() + ": " + message);
    }
}

public class UserRepository {
    public void save(User user) {
        System.out.println("Saving user to database: " + user.getName());
    }
}
```

### 3. Function Size and Complexity

**Bad Example:**
```java
public void processOrder(Order order) {
    // Validation
    if (order == null) throw new IllegalArgumentException("Order cannot be null");
    if (order.getItems().isEmpty()) throw new IllegalArgumentException("Order must have items");
    
    // Calculate total
    double total = 0;
    for (OrderItem item : order.getItems()) {
        if (item.getQuantity() <= 0) throw new IllegalArgumentException("Invalid quantity");
        total += item.getPrice() * item.getQuantity();
        if (item.getDiscount() > 0) {
            total -= (item.getPrice() * item.getQuantity() * item.getDiscount() / 100);
        }
    }
    
    // Apply order-level discount
    if (order.getCustomer().isPremium()) {
        total *= 0.9; // 10% discount
    }
    
    // Process payment
    if (order.getPaymentMethod().equals("CREDIT_CARD")) {
        // Credit card processing
        if (!validateCreditCard(order.getCreditCard())) {
            throw new RuntimeException("Invalid credit card");
        }
        chargeCreditCard(order.getCreditCard(), total);
    } else if (order.getPaymentMethod().equals("PAYPAL")) {
        // PayPal processing
        processPayPalPayment(order.getPayPalAccount(), total);
    }
    
    // Update inventory
    for (OrderItem item : order.getItems()) {
        updateInventory(item.getProductId(), item.getQuantity());
    }
    
    // Send notifications
    sendOrderConfirmation(order.getCustomer().getEmail(), order);
    if (order.getCustomer().isSmsEnabled()) {
        sendSmsNotification(order.getCustomer().getPhone(), order);
    }
}
```

**Good Example:**
```java
public void processOrder(Order order) {
    validateOrder(order);
    double total = calculateTotal(order);
    processPayment(order, total);
    updateInventory(order);
    sendNotifications(order);
}

private void validateOrder(Order order) {
    if (order == null) {
        throw new IllegalArgumentException("Order cannot be null");
    }
    if (order.getItems().isEmpty()) {
        throw new IllegalArgumentException("Order must have items");
    }
    validateOrderItems(order.getItems());
}

private double calculateTotal(Order order) {
    double subtotal = calculateSubtotal(order.getItems());
    return applyDiscounts(subtotal, order.getCustomer());
}

private void processPayment(Order order, double total) {
    PaymentProcessor processor = paymentProcessorFactory.getProcessor(order.getPaymentMethod());
    processor.processPayment(order, total);
}

private void updateInventory(Order order) {
    for (OrderItem item : order.getItems()) {
        inventoryService.updateStock(item.getProductId(), item.getQuantity());
    }
}

private void sendNotifications(Order order) {
    notificationService.sendOrderConfirmation(order);
}
```

## Design Patterns in Machine Coding

### 1. Factory Pattern

**Use Case:** Creating different types of objects based on input

```java
// Payment processor factory
public class PaymentProcessorFactory {
    public static PaymentProcessor createProcessor(PaymentMethod method) {
        switch (method) {
            case CREDIT_CARD:
                return new CreditCardProcessor();
            case PAYPAL:
                return new PayPalProcessor();
            case BANK_TRANSFER:
                return new BankTransferProcessor();
            default:
                throw new IllegalArgumentException("Unsupported payment method: " + method);
        }
    }
}

// Usage in booking system
public class BookingService {
    public void processPayment(Booking booking, PaymentMethod method, double amount) {
        PaymentProcessor processor = PaymentProcessorFactory.createProcessor(method);
        processor.processPayment(booking.getCustomerId(), amount);
    }
}
```

### 2. Observer Pattern

**Use Case:** Notification systems, event handling

```java
// Event notification system
public interface GameEventListener {
    void onPlayerMove(Player player, int newPosition);
    void onGameEnd(Player winner);
}

public class SnakeAndLadderGame {
    private List<GameEventListener> listeners = new ArrayList<>();
    
    public void addListener(GameEventListener listener) {
        listeners.add(listener);
    }
    
    private void notifyPlayerMove(Player player, int newPosition) {
        for (GameEventListener listener : listeners) {
            listener.onPlayerMove(player, newPosition);
        }
    }
    
    private void notifyGameEnd(Player winner) {
        for (GameEventListener listener : listeners) {
            listener.onGameEnd(winner);
        }
    }
}

// Concrete listener implementations
public class GameLogger implements GameEventListener {
    @Override
    public void onPlayerMove(Player player, int newPosition) {
        System.out.println(player.getName() + " moved to position " + newPosition);
    }
    
    @Override
    public void onGameEnd(Player winner) {
        System.out.println("Game ended. Winner: " + winner.getName());
    }
}
```

### 3. Strategy Pattern

**Use Case:** Different algorithms for the same operation

```java
// Pricing strategies for hotel booking
public interface PricingStrategy {
    double calculatePrice(Room room, LocalDate checkIn, LocalDate checkOut);
}

public class StandardPricingStrategy implements PricingStrategy {
    @Override
    public double calculatePrice(Room room, LocalDate checkIn, LocalDate checkOut) {
        long nights = ChronoUnit.DAYS.between(checkIn, checkOut);
        return room.getBasePrice() * nights;
    }
}

public class SeasonalPricingStrategy implements PricingStrategy {
    @Override
    public double calculatePrice(Room room, LocalDate checkIn, LocalDate checkOut) {
        long nights = ChronoUnit.DAYS.between(checkIn, checkOut);
        double basePrice = room.getBasePrice() * nights;
        
        // Apply seasonal multiplier
        if (isHighSeason(checkIn, checkOut)) {
            return basePrice * 1.5;
        }
        return basePrice;
    }
    
    private boolean isHighSeason(LocalDate checkIn, LocalDate checkOut) {
        // Implementation for high season detection
        return false;
    }
}

public class HotelBookingService {
    private PricingStrategy pricingStrategy;
    
    public void setPricingStrategy(PricingStrategy strategy) {
        this.pricingStrategy = strategy;
    }
    
    public double calculateBookingPrice(Room room, LocalDate checkIn, LocalDate checkOut) {
        return pricingStrategy.calculatePrice(room, checkIn, checkOut);
    }
}
```

### 4. Command Pattern

**Use Case:** Undo/Redo operations, request queuing

```java
// Chess game with move history
public interface Command {
    void execute();
    void undo();
}

public class MoveCommand implements Command {
    private ChessBoard board;
    private Move move;
    private Piece capturedPiece;
    
    public MoveCommand(ChessBoard board, Move move) {
        this.board = board;
        this.move = move;
    }
    
    @Override
    public void execute() {
        capturedPiece = board.getPiece(move.getTo());
        board.movePiece(move.getFrom(), move.getTo());
    }
    
    @Override
    public void undo() {
        board.movePiece(move.getTo(), move.getFrom());
        if (capturedPiece != null) {
            board.setPiece(move.getTo(), capturedPiece);
        }
    }
}

public class ChessGame {
    private Stack<Command> moveHistory = new Stack<>();
    
    public void makeMove(Move move) {
        Command command = new MoveCommand(board, move);
        command.execute();
        moveHistory.push(command);
    }
    
    public void undoLastMove() {
        if (!moveHistory.isEmpty()) {
            Command lastCommand = moveHistory.pop();
            lastCommand.undo();
        }
    }
}
```

## Common Mistakes and How to Avoid Them

### 1. Not Handling Edge Cases

**Common Mistake:**
```java
public class Calculator {
    public double divide(double a, double b) {
        return a / b; // What if b is 0?
    }
    
    public String getFirstWord(String sentence) {
        return sentence.split(" ")[0]; // What if sentence is null or empty?
    }
}
```

**Better Approach:**
```java
public class Calculator {
    public double divide(double a, double b) {
        if (b == 0) {
            throw new IllegalArgumentException("Division by zero is not allowed");
        }
        return a / b;
    }
    
    public String getFirstWord(String sentence) {
        if (sentence == null || sentence.trim().isEmpty()) {
            return "";
        }
        String[] words = sentence.trim().split("\\s+");
        return words.length > 0 ? words[0] : "";
    }
}
```

### 2. Ignoring Thread Safety

**Common Mistake:**
```java
public class Counter {
    private int count = 0;
    
    public void increment() {
        count++; // Not thread-safe
    }
    
    public int getCount() {
        return count; // Not thread-safe
    }
}
```

**Better Approach:**
```java
public class Counter {
    private final AtomicInteger count = new AtomicInteger(0);
    
    public void increment() {
        count.incrementAndGet();
    }
    
    public int getCount() {
        return count.get();
    }
}

// Or using synchronization
public class SynchronizedCounter {
    private int count = 0;
    
    public synchronized void increment() {
        count++;
    }
    
    public synchronized int getCount() {
        return count;
    }
}
```

### 3. Poor Error Handling

**Common Mistake:**
```java
public class FileProcessor {
    public String readFile(String filename) {
        try {
            return Files.readString(Paths.get(filename));
        } catch (Exception e) {
            e.printStackTrace(); // Poor error handling
            return null;
        }
    }
}
```

**Better Approach:**
```java
public class FileProcessor {
    public String readFile(String filename) throws FileProcessingException {
        if (filename == null || filename.trim().isEmpty()) {
            throw new IllegalArgumentException("Filename cannot be null or empty");
        }
        
        try {
            Path path = Paths.get(filename);
            if (!Files.exists(path)) {
                throw new FileProcessingException("File does not exist: " + filename);
            }
            return Files.readString(path);
        } catch (IOException e) {
            throw new FileProcessingException("Failed to read file: " + filename, e);
        }
    }
}

public class FileProcessingException extends Exception {
    public FileProcessingException(String message) {
        super(message);
    }
    
    public FileProcessingException(String message, Throwable cause) {
        super(message, cause);
    }
}
```

### 4. Not Validating Input

**Common Mistake:**
```java
public class UserService {
    public User createUser(String name, String email, int age) {
        User user = new User();
        user.setName(name);
        user.setEmail(email);
        user.setAge(age);
        return user;
    }
}
```

**Better Approach:**
```java
public class UserService {
    public User createUser(String name, String email, int age) {
        validateUserInput(name, email, age);
        
        User user = new User();
        user.setName(name.trim());
        user.setEmail(email.toLowerCase().trim());
        user.setAge(age);
        return user;
    }
    
    private void validateUserInput(String name, String email, int age) {
        if (name == null || name.trim().isEmpty()) {
            throw new IllegalArgumentException("Name cannot be null or empty");
        }
        
        if (email == null || !isValidEmail(email)) {
            throw new IllegalArgumentException("Invalid email address");
        }
        
        if (age < 0 || age > 150) {
            throw new IllegalArgumentException("Age must be between 0 and 150");
        }
    }
    
    private boolean isValidEmail(String email) {
        return email.contains("@") && email.contains(".");
    }
}
```

### 5. Memory Leaks and Resource Management

**Common Mistake:**
```java
public class DatabaseConnection {
    public List<User> getUsers() {
        Connection conn = null;
        PreparedStatement stmt = null;
        ResultSet rs = null;
        
        try {
            conn = DriverManager.getConnection(url, username, password);
            stmt = conn.prepareStatement("SELECT * FROM users");
            rs = stmt.executeQuery();
            
            List<User> users = new ArrayList<>();
            while (rs.next()) {
                users.add(mapResultSetToUser(rs));
            }
            return users;
        } catch (SQLException e) {
            throw new RuntimeException("Database error", e);
        }
        // Resources not closed - memory leak!
    }
}
```

**Better Approach:**
```java
public class DatabaseConnection {
    public List<User> getUsers() {
        String sql = "SELECT * FROM users";
        
        try (Connection conn = DriverManager.getConnection(url, username, password);
             PreparedStatement stmt = conn.prepareStatement(sql);
             ResultSet rs = stmt.executeQuery()) {
            
            List<User> users = new ArrayList<>();
            while (rs.next()) {
                users.add(mapResultSetToUser(rs));
            }
            return users;
            
        } catch (SQLException e) {
            throw new RuntimeException("Database error", e);
        }
        // Resources automatically closed with try-with-resources
    }
}
```

## Interview Preparation Strategies

### 1. Problem Understanding Phase (5-10 minutes)

**Steps to Follow:**
1. **Read the problem carefully** - Don't rush to code immediately
2. **Ask clarifying questions:**
   - What are the expected inputs and outputs?
   - What are the constraints (time, space, data size)?
   - Are there any edge cases to consider?
   - What are the performance requirements?

**Example Questions for a Chat System:**
- How many users can be in a chat room?
- Do we need to persist chat history?
- Should we support file sharing?
- What about user authentication?
- Do we need real-time notifications?

### 2. High-Level Design Phase (10-15 minutes)

**Steps to Follow:**
1. **Identify main components** and their responsibilities
2. **Draw a simple architecture diagram**
3. **Define key interfaces** and data structures
4. **Discuss design patterns** you'll use
5. **Get approval** from interviewer before coding

**Example for LRU Cache:**
```
Components:
1. Cache Interface - get(), put(), remove()
2. Node - doubly linked list node
3. HashMap - for O(1) access
4. Capacity management - eviction policy

Data Structures:
- HashMap<Key, Node> for fast access
- Doubly linked list for LRU ordering
```

### 3. Implementation Phase (30-40 minutes)

**Best Practices:**
1. **Start with core functionality** - implement the happy path first
2. **Write clean, readable code** - use meaningful names
3. **Add comments** for complex logic
4. **Handle edge cases** as you go
5. **Test your code** with examples

**Implementation Order:**
1. Define classes and interfaces
2. Implement core methods
3. Add error handling
4. Add edge case handling
5. Add any additional features

### 4. Testing and Validation Phase (5-10 minutes)

**Testing Strategy:**
1. **Walk through your code** with the interviewer
2. **Test with provided examples**
3. **Think of edge cases:**
   - Null inputs
   - Empty collections
   - Boundary conditions
   - Concurrent access (if applicable)
4. **Discuss time and space complexity**

## Time Management Techniques

### 1. The 60-Minute Breakdown

| Phase | Time | Activities |
|-------|------|------------|
| Understanding | 5-10 min | Read problem, ask questions, clarify requirements |
| Design | 10-15 min | High-level design, identify components, get approval |
| Implementation | 30-40 min | Code core functionality, handle edge cases |
| Testing | 5-10 min | Walk through code, test examples, discuss complexity |

### 2. Prioritization Strategy

**Must-Have (P0):**
- Core functionality working
- Basic error handling
- Clean, readable code

**Should-Have (P1):**
- Edge case handling
- Input validation
- Performance optimization

**Nice-to-Have (P2):**
- Additional features
- Advanced optimizations
- Comprehensive logging

### 3. When You're Running Out of Time

**Strategies:**
1. **Communicate with interviewer** - "I'm running short on time, should I focus on X or Y?"
2. **Implement core functionality first** - get something working
3. **Stub out remaining methods** - show you know what needs to be done
4. **Explain your approach** - even if you can't code it all

## Code Review Checklist

### 1. Functionality
- [ ] Does the code solve the problem correctly?
- [ ] Are all requirements implemented?
- [ ] Are edge cases handled?
- [ ] Is error handling appropriate?

### 2. Code Quality
- [ ] Are variable and method names meaningful?
- [ ] Is the code well-structured and organized?
- [ ] Are functions/methods of appropriate size?
- [ ] Is there any code duplication?

### 3. Performance
- [ ] What is the time complexity?
- [ ] What is the space complexity?
- [ ] Are there any obvious performance bottlenecks?
- [ ] Is the solution scalable?

### 4. Thread Safety
- [ ] Is the code thread-safe if needed?
- [ ] Are shared resources properly synchronized?
- [ ] Are there any potential race conditions?

### 5. Testing
- [ ] Can the code be easily tested?
- [ ] Are there any obvious test cases missing?
- [ ] How would you test edge cases?

## Performance Optimization Tips

### 1. Choose the Right Data Structure

**Example: Frequent Lookups**
```java
// Bad: O(n) lookup time
List<User> users = new ArrayList<>();
public User findUser(String id) {
    for (User user : users) {
        if (user.getId().equals(id)) {
            return user;
        }
    }
    return null;
}

// Good: O(1) lookup time
Map<String, User> users = new HashMap<>();
public User findUser(String id) {
    return users.get(id);
}
```

### 2. Avoid Unnecessary Object Creation

**Example: String Concatenation**
```java
// Bad: Creates many intermediate String objects
public String buildMessage(List<String> parts) {
    String result = "";
    for (String part : parts) {
        result += part + " ";
    }
    return result.trim();
}

// Good: Uses StringBuilder
public String buildMessage(List<String> parts) {
    StringBuilder sb = new StringBuilder();
    for (String part : parts) {
        sb.append(part).append(" ");
    }
    return sb.toString().trim();
}
```

### 3. Use Appropriate Collection Operations

**Example: Bulk Operations**
```java
// Bad: Multiple individual operations
public void removeInactiveUsers(List<User> users) {
    for (User user : users) {
        if (!user.isActive()) {
            users.remove(user); // ConcurrentModificationException!
        }
    }
}

// Good: Use iterator or stream
public void removeInactiveUsers(List<User> users) {
    users.removeIf(user -> !user.isActive());
}
```

### 4. Cache Expensive Operations

**Example: Expensive Calculations**
```java
public class PrimeChecker {
    private final Map<Integer, Boolean> cache = new HashMap<>();
    
    public boolean isPrime(int n) {
        return cache.computeIfAbsent(n, this::calculateIsPrime);
    }
    
    private boolean calculateIsPrime(int n) {
        if (n < 2) return false;
        for (int i = 2; i <= Math.sqrt(n); i++) {
            if (n % i == 0) return false;
        }
        return true;
    }
}
```

## Testing Best Practices

### 1. Unit Testing Structure

**Follow the AAA Pattern:**
- **Arrange:** Set up test data and conditions
- **Act:** Execute the method being tested
- **Assert:** Verify the results

```java
@Test
public void testCalculateTotal_WithValidItems_ReturnsCorrectTotal() {
    // Arrange
    Order order = new Order();
    order.addItem(new OrderItem("item1", 10.0, 2)); // $20
    order.addItem(new OrderItem("item2", 15.0, 1)); // $15
    OrderService service = new OrderService();
    
    // Act
    double total = service.calculateTotal(order);
    
    // Assert
    assertEquals(35.0, total, 0.01);
}
```

### 2. Test Edge Cases

```java
@Test
public void testDivide_WithZeroDivisor_ThrowsException() {
    Calculator calc = new Calculator();
    
    assertThrows(IllegalArgumentException.class, () -> {
        calc.divide(10, 0);
    });
}

@Test
public void testGetUser_WithNullId_ReturnsNull() {
    UserService service = new UserService();
    
    User result = service.getUser(null);
    
    assertNull(result);
}
```

### 3. Integration Testing

```java
@Test
public void testBookingWorkflow_EndToEnd_Success() {
    // Test the complete booking workflow
    BookingService bookingService = new BookingService();
    PaymentService paymentService = new PaymentService();
    NotificationService notificationService = new NotificationService();
    
    // Create booking
    Booking booking = bookingService.createBooking("user123", "hotel456", 
                                                  LocalDate.now(), LocalDate.now().plusDays(2));
    assertNotNull(booking);
    
    // Process payment
    boolean paymentSuccess = paymentService.processPayment(booking.getId(), 100.0);
    assertTrue(paymentSuccess);
    
    // Verify notification sent
    verify(notificationService).sendBookingConfirmation(booking);
}
```

## Summary

This chapter covered essential best practices for machine coding interviews:

1. **Clean Code Principles** - Write readable, maintainable code
2. **Design Patterns** - Use appropriate patterns for common problems
3. **Common Mistakes** - Learn from typical pitfalls and how to avoid them
4. **Interview Strategies** - Manage time effectively and communicate clearly
5. **Performance Tips** - Optimize for the right metrics
6. **Testing Practices** - Ensure code quality through proper testing

Remember: The goal is not just to solve the problem, but to demonstrate your ability to write production-quality code that is maintainable, scalable, and robust.

**Key Takeaways:**
- Always clarify requirements before coding
- Design before implementing
- Write clean, readable code
- Handle edge cases and errors
- Test your solution
- Communicate throughout the process

Practice these principles with the examples in this book, and you'll be well-prepared for your machine coding interviews!