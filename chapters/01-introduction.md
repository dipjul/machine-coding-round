# Chapter 1: Introduction & Foundations

## Purpose of the Book

Welcome to the comprehensive guide for machine coding interview preparation. This book is designed to help software engineers systematically prepare for machine coding rounds at top technology companies. Unlike system design interviews that focus on high-level architecture, machine coding interviews require you to implement working code that demonstrates your programming skills, design thinking, and problem-solving abilities.

Machine coding interviews typically last 60-90 minutes and require you to:
- Understand and clarify requirements
- Design clean, extensible code architecture
- Implement working solutions with proper error handling
- Write unit tests and demonstrate code quality
- Handle edge cases and discuss trade-offs

## How to Approach Machine Coding Rounds

### 1. Requirements Clarification (5-10 minutes)
- Ask clarifying questions about functional requirements
- Understand the scope and constraints
- Identify core vs. nice-to-have features
- Clarify input/output formats and data types

### 2. High-Level Design (10-15 minutes)
- Sketch the overall architecture
- Identify main classes and their responsibilities
- Define interfaces and relationships
- Discuss design patterns that might be applicable

### 3. Implementation (40-60 minutes)
- Start with core functionality
- Write clean, readable code
- Handle edge cases and error scenarios
- Demonstrate good coding practices

### 4. Testing & Validation (10-15 minutes)
- Write unit tests for critical functionality
- Test edge cases and error conditions
- Validate the solution with sample inputs
- Discuss potential improvements

## Key Design Principles

### Object-Oriented Programming (OOP)

#### Encapsulation
Encapsulation involves bundling data and methods that operate on that data within a single unit (class) and restricting access to internal implementation details.

```java
public class BankAccount {
    private double balance;  // Private data
    private String accountNumber;
    
    public BankAccount(String accountNumber, double initialBalance) {
        this.accountNumber = accountNumber;
        this.balance = initialBalance;
    }
    
    // Controlled access through public methods
    public double getBalance() {
        return balance;
    }
    
    public boolean withdraw(double amount) {
        if (amount > 0 && amount <= balance) {
            balance -= amount;
            return true;
        }
        return false;
    }
    
    public void deposit(double amount) {
        if (amount > 0) {
            balance += amount;
        }
    }
}
```

#### Inheritance
Inheritance allows classes to inherit properties and methods from parent classes, promoting code reuse and establishing "is-a" relationships.

```java
public abstract class Vehicle {
    protected String brand;
    protected int year;
    
    public Vehicle(String brand, int year) {
        this.brand = brand;
        this.year = year;
    }
    
    public abstract void start();
    
    public void displayInfo() {
        System.out.println(brand + " " + year);
    }
}

public class Car extends Vehicle {
    private int doors;
    
    public Car(String brand, int year, int doors) {
        super(brand, year);
        this.doors = doors;
    }
    
    @Override
    public void start() {
        System.out.println("Car engine started");
    }
}
```

#### Polymorphism
Polymorphism allows objects of different types to be treated as instances of the same type through a common interface.

```java
public interface PaymentProcessor {
    boolean processPayment(double amount);
}

public class CreditCardProcessor implements PaymentProcessor {
    @Override
    public boolean processPayment(double amount) {
        // Credit card processing logic
        return true;
    }
}

public class PayPalProcessor implements PaymentProcessor {
    @Override
    public boolean processPayment(double amount) {
        // PayPal processing logic
        return true;
    }
}

// Polymorphic usage
public class PaymentService {
    public void processOrder(PaymentProcessor processor, double amount) {
        if (processor.processPayment(amount)) {
            System.out.println("Payment successful");
        }
    }
}
```

### SOLID Principles

#### Single Responsibility Principle (SRP)
A class should have only one reason to change, meaning it should have only one job or responsibility.

```java
// Violates SRP - handles both user data and email sending
public class UserManager {
    public void saveUser(User user) { /* save logic */ }
    public void sendWelcomeEmail(User user) { /* email logic */ }
}

// Follows SRP - separate responsibilities
public class UserRepository {
    public void saveUser(User user) { /* save logic */ }
}

public class EmailService {
    public void sendWelcomeEmail(User user) { /* email logic */ }
}
```

#### Open/Closed Principle (OCP)
Software entities should be open for extension but closed for modification.

```java
public abstract class Shape {
    public abstract double calculateArea();
}

public class Rectangle extends Shape {
    private double width, height;
    
    public Rectangle(double width, double height) {
        this.width = width;
        this.height = height;
    }
    
    @Override
    public double calculateArea() {
        return width * height;
    }
}

public class Circle extends Shape {
    private double radius;
    
    public Circle(double radius) {
        this.radius = radius;
    }
    
    @Override
    public double calculateArea() {
        return Math.PI * radius * radius;
    }
}

// Can add new shapes without modifying existing code
public class AreaCalculator {
    public double calculateTotalArea(List<Shape> shapes) {
        return shapes.stream()
                    .mapToDouble(Shape::calculateArea)
                    .sum();
    }
}
```

#### Liskov Substitution Principle (LSP)
Objects of a superclass should be replaceable with objects of its subclasses without breaking the application.

#### Interface Segregation Principle (ISP)
Clients should not be forced to depend on interfaces they don't use.

```java
// Violates ISP - forces all implementations to implement unused methods
public interface Worker {
    void work();
    void eat();
    void sleep();
}

// Follows ISP - segregated interfaces
public interface Workable {
    void work();
}

public interface Eatable {
    void eat();
}

public interface Sleepable {
    void sleep();
}

public class Human implements Workable, Eatable, Sleepable {
    @Override
    public void work() { /* implementation */ }
    
    @Override
    public void eat() { /* implementation */ }
    
    @Override
    public void sleep() { /* implementation */ }
}

public class Robot implements Workable {
    @Override
    public void work() { /* implementation */ }
    // Robot doesn't need to eat or sleep
}
```

#### Dependency Inversion Principle (DIP)
High-level modules should not depend on low-level modules. Both should depend on abstractions.

```java
// Violates DIP - high-level class depends on low-level class
public class OrderService {
    private MySQLDatabase database = new MySQLDatabase();
    
    public void saveOrder(Order order) {
        database.save(order);
    }
}

// Follows DIP - depends on abstraction
public interface Database {
    void save(Order order);
}

public class OrderService {
    private Database database;
    
    public OrderService(Database database) {
        this.database = database;
    }
    
    public void saveOrder(Order order) {
        database.save(order);
    }
}
```

## Essential Design Patterns

### Creational Patterns

#### Singleton Pattern
Ensures a class has only one instance and provides global access to it.

```java
public class DatabaseConnection {
    private static volatile DatabaseConnection instance;
    private static final Object lock = new Object();
    
    private DatabaseConnection() {
        // Private constructor prevents instantiation
    }
    
    public static DatabaseConnection getInstance() {
        if (instance == null) {
            synchronized (lock) {
                if (instance == null) {
                    instance = new DatabaseConnection();
                }
            }
        }
        return instance;
    }
    
    public void connect() {
        // Connection logic
    }
}
```

#### Factory Pattern
Creates objects without specifying their exact classes.

```java
public interface Animal {
    void makeSound();
}

public class Dog implements Animal {
    @Override
    public void makeSound() {
        System.out.println("Woof!");
    }
}

public class Cat implements Animal {
    @Override
    public void makeSound() {
        System.out.println("Meow!");
    }
}

public class AnimalFactory {
    public static Animal createAnimal(String type) {
        switch (type.toLowerCase()) {
            case "dog":
                return new Dog();
            case "cat":
                return new Cat();
            default:
                throw new IllegalArgumentException("Unknown animal type");
        }
    }
}
```

### Behavioral Patterns

#### Observer Pattern
Defines a one-to-many dependency between objects so that when one object changes state, all dependents are notified.

```java
public interface Observer {
    void update(String message);
}

public interface Subject {
    void addObserver(Observer observer);
    void removeObserver(Observer observer);
    void notifyObservers(String message);
}

public class NewsAgency implements Subject {
    private List<Observer> observers = new ArrayList<>();
    private String news;
    
    @Override
    public void addObserver(Observer observer) {
        observers.add(observer);
    }
    
    @Override
    public void removeObserver(Observer observer) {
        observers.remove(observer);
    }
    
    @Override
    public void notifyObservers(String message) {
        for (Observer observer : observers) {
            observer.update(message);
        }
    }
    
    public void setNews(String news) {
        this.news = news;
        notifyObservers(news);
    }
}
```

#### Strategy Pattern
Defines a family of algorithms, encapsulates each one, and makes them interchangeable.

```java
public interface SortingStrategy {
    void sort(int[] array);
}

public class BubbleSort implements SortingStrategy {
    @Override
    public void sort(int[] array) {
        // Bubble sort implementation
    }
}

public class QuickSort implements SortingStrategy {
    @Override
    public void sort(int[] array) {
        // Quick sort implementation
    }
}

public class SortContext {
    private SortingStrategy strategy;
    
    public SortContext(SortingStrategy strategy) {
        this.strategy = strategy;
    }
    
    public void setStrategy(SortingStrategy strategy) {
        this.strategy = strategy;
    }
    
    public void executeSort(int[] array) {
        strategy.sort(array);
    }
}
```

## Concurrency Fundamentals

### Thread Safety
When multiple threads access shared resources, ensure data consistency and prevent race conditions.

```java
public class Counter {
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
}

// Using concurrent collections
public class ThreadSafeCache {
    private final ConcurrentHashMap<String, String> cache = new ConcurrentHashMap<>();
    
    public void put(String key, String value) {
        cache.put(key, value);
    }
    
    public String get(String key) {
        return cache.get(key);
    }
}
```

### Producer-Consumer Pattern
```java
public class ProducerConsumerExample {
    private final BlockingQueue<String> queue = new ArrayBlockingQueue<>(10);
    
    public void produce(String item) throws InterruptedException {
        queue.put(item);  // Blocks if queue is full
    }
    
    public String consume() throws InterruptedException {
        return queue.take();  // Blocks if queue is empty
    }
}
```

## Best Practices for Machine Coding Interviews

1. **Start Simple**: Begin with basic functionality and iterate
2. **Think Out Loud**: Explain your thought process
3. **Write Clean Code**: Use meaningful names and proper formatting
4. **Handle Edge Cases**: Consider null inputs, empty collections, etc.
5. **Test Your Code**: Write unit tests and validate with examples
6. **Be Prepared to Extend**: Design for future requirements
7. **Manage Time**: Allocate time for design, implementation, and testing

This foundation will serve as the basis for all the machine coding problems we'll explore in the following chapters. Each problem will demonstrate these principles in action and show how to apply them effectively in interview scenarios.