# Contributing to Machine Coding Interview Preparation

Thank you for your interest in contributing to this project! This guide will help you get started with contributing to our comprehensive machine coding interview preparation resource.

## 🎯 Ways to Contribute

### 1. **Report Issues**
- Bug reports
- Documentation improvements
- Performance issues
- Missing test cases
- Unclear explanations

### 2. **Add New Problems**
- Implement new machine coding problems
- Add alternative solutions to existing problems
- Contribute problems from your interview experiences

### 3. **Improve Existing Code**
- Optimize performance
- Enhance thread safety
- Add better error handling
- Improve code documentation

### 4. **Enhance Documentation**
- Fix typos and grammar
- Add more detailed explanations
- Create better examples
- Improve code comments

### 5. **Add Language Support**
- Implement solutions in other languages (Python, C++, Go, etc.)
- Maintain consistency with Java implementations

## 🚀 Getting Started

### Prerequisites
- Java 11 or higher
- Git
- Basic understanding of data structures and algorithms
- Familiarity with machine coding interview patterns

### Development Setup
```bash
# Fork the repository on GitHub
# Clone your fork
git clone https://github.com/YOUR_USERNAME/machine-coding-book.git
cd machine-coding-book

# Add upstream remote
git remote add upstream https://github.com/ORIGINAL_OWNER/machine-coding-book.git

# Create a new branch for your feature
git checkout -b feature/your-feature-name
```

### Building and Testing
```bash
# Compile all code
find src/main/java -name "*.java" -exec javac -cp src/main/java {} +

# Run specific tests
java -cp src/main/java:src/test/java com.machinecoding.integration.SimpleIntegrationTest

# Run all demos to ensure they work
./scripts/run_all_demos.sh  # (if available)
```

## 📝 Contribution Guidelines

### Code Standards

#### 1. **Naming Conventions**
```java
// Classes: PascalCase
public class LRUCache<K, V> { }

// Methods and variables: camelCase
public void addElement(String elementName) { }

// Constants: UPPER_SNAKE_CASE
private static final int MAX_CAPACITY = 1000;

// Packages: lowercase with dots
package com.machinecoding.caching.lru;
```

#### 2. **Code Structure**
```java
public class ExampleService {
    // 1. Constants
    private static final int DEFAULT_CAPACITY = 100;
    
    // 2. Instance variables
    private final Map<String, Object> cache;
    private final int capacity;
    
    // 3. Constructor
    public ExampleService(int capacity) {
        this.capacity = capacity;
        this.cache = new ConcurrentHashMap<>();
    }
    
    // 4. Public methods
    public void publicMethod() { }
    
    // 5. Private methods
    private void privateMethod() { }
}
```

#### 3. **Documentation Requirements**
```java
/**
 * Implements a Least Recently Used (LRU) cache with O(1) operations.
 * 
 * This implementation uses a HashMap for O(1) access and a doubly linked list
 * for O(1) insertion/deletion to maintain the LRU order.
 * 
 * @param <K> the type of keys maintained by this cache
 * @param <V> the type of mapped values
 */
public class LRUCache<K, V> {
    
    /**
     * Retrieves a value from the cache and marks it as recently used.
     * 
     * @param key the key whose associated value is to be returned
     * @return the value associated with the key, or null if not found
     * @throws IllegalArgumentException if key is null
     */
    public V get(K key) {
        // Implementation
    }
}
```

#### 4. **Error Handling**
```java
// Good: Specific exceptions with clear messages
public void validateInput(String input) {
    if (input == null) {
        throw new IllegalArgumentException("Input cannot be null");
    }
    if (input.trim().isEmpty()) {
        throw new IllegalArgumentException("Input cannot be empty");
    }
}

// Good: Resource management
public void processFile(String filename) throws ProcessingException {
    try (BufferedReader reader = Files.newBufferedReader(Paths.get(filename))) {
        // Process file
    } catch (IOException e) {
        throw new ProcessingException("Failed to process file: " + filename, e);
    }
}
```

#### 5. **Thread Safety**
```java
// Use concurrent collections when needed
private final Map<String, Object> threadSafeMap = new ConcurrentHashMap<>();

// Use proper synchronization
private final Object lock = new Object();

public void synchronizedMethod() {
    synchronized (lock) {
        // Critical section
    }
}

// Use atomic operations
private final AtomicInteger counter = new AtomicInteger(0);
```

### Testing Standards

#### 1. **Unit Tests**
```java
@Test
public void testGet_WithValidKey_ReturnsValue() {
    // Arrange
    LRUCache<String, Integer> cache = new LRUCache<>(2);
    cache.put("key1", 1);
    
    // Act
    Integer result = cache.get("key1");
    
    // Assert
    assertEquals(Integer.valueOf(1), result);
}

@Test
public void testGet_WithNullKey_ThrowsException() {
    LRUCache<String, Integer> cache = new LRUCache<>(2);
    
    assertThrows(IllegalArgumentException.class, () -> {
        cache.get(null);
    });
}
```

#### 2. **Integration Tests**
```java
@Test
public void testCompleteWorkflow_EndToEnd_Success() {
    // Test the complete system workflow
    // Include multiple components working together
    // Verify the entire user journey
}
```

#### 3. **Performance Tests**
```java
@Test
public void testPerformance_LargeDataset_MeetsRequirements() {
    // Test with realistic data sizes
    // Measure execution time
    // Verify memory usage
    // Assert performance requirements are met
}
```

### Documentation Standards

#### 1. **README Updates**
- Update the main README.md if adding new problems
- Include usage examples for new features
- Update the table of contents

#### 2. **Chapter Documentation**
- Follow the existing chapter structure
- Include problem statement, approach analysis, implementation, and interview questions
- Provide comprehensive code examples

#### 3. **Code Comments**
```java
// Good: Explain the why, not the what
// Use binary search to find insertion point for better performance
int insertionPoint = Collections.binarySearch(sortedList, element);

// Good: Explain complex algorithms
// Implement Haversine formula to calculate distance between two points on Earth
double distance = calculateHaversineDistance(lat1, lon1, lat2, lon2);
```

## 🔄 Pull Request Process

### 1. **Before Submitting**
- [ ] Code compiles without warnings
- [ ] All tests pass
- [ ] New code has appropriate test coverage
- [ ] Documentation is updated
- [ ] Code follows the style guidelines
- [ ] No merge conflicts with main branch

### 2. **Pull Request Template**
```markdown
## Description
Brief description of changes made.

## Type of Change
- [ ] Bug fix
- [ ] New feature
- [ ] Documentation update
- [ ] Performance improvement
- [ ] Code refactoring

## Testing
- [ ] Unit tests added/updated
- [ ] Integration tests added/updated
- [ ] Manual testing completed

## Checklist
- [ ] Code follows style guidelines
- [ ] Self-review completed
- [ ] Documentation updated
- [ ] No breaking changes (or clearly documented)
```

### 3. **Review Process**
1. **Automated Checks**: CI/CD pipeline runs tests
2. **Code Review**: Maintainers review the code
3. **Feedback**: Address any requested changes
4. **Approval**: Get approval from maintainers
5. **Merge**: Changes are merged to main branch

## 🏗️ Adding New Problems

### 1. **Problem Selection Criteria**
- Commonly asked in machine coding interviews
- Demonstrates important programming concepts
- Has clear requirements and constraints
- Can be implemented in reasonable time (60-90 minutes)

### 2. **Implementation Structure**
```
src/main/java/com/machinecoding/newproblem/
├── model/                  # Data models
│   ├── Entity.java
│   └── Status.java
├── service/               # Business logic
│   ├── Service.java
│   └── ServiceImpl.java
├── Demo.java             # Demonstration class
└── README.md            # Problem-specific documentation
```

### 3. **Required Components**
- **Models**: Well-designed data structures
- **Service Interface**: Clear API definition
- **Implementation**: Thread-safe, efficient implementation
- **Demo**: Working example with sample data
- **Tests**: Comprehensive test coverage
- **Documentation**: Problem statement and solution explanation

### 4. **Chapter Documentation**
Create or update the relevant chapter file with:
- Problem statement with functional/non-functional requirements
- Approach analysis with multiple solutions
- Implementation walkthrough
- Key features and usage examples
- Performance characteristics
- Common interview questions
- Extensions and improvements

## 🐛 Reporting Issues

### Bug Reports
Include the following information:
- **Description**: Clear description of the issue
- **Steps to Reproduce**: Detailed steps to reproduce the bug
- **Expected Behavior**: What should happen
- **Actual Behavior**: What actually happens
- **Environment**: Java version, OS, etc.
- **Code Sample**: Minimal code that reproduces the issue

### Feature Requests
Include the following information:
- **Problem**: What problem does this solve?
- **Solution**: Proposed solution or feature
- **Alternatives**: Alternative solutions considered
- **Use Case**: Real-world use case for the feature

## 📋 Code Review Checklist

### For Contributors
- [ ] Code is well-documented
- [ ] Tests are comprehensive
- [ ] Performance is acceptable
- [ ] Thread safety is considered
- [ ] Error handling is appropriate
- [ ] Code follows style guidelines

### For Reviewers
- [ ] Code solves the stated problem
- [ ] Implementation is efficient
- [ ] Tests cover edge cases
- [ ] Documentation is clear
- [ ] Code is maintainable
- [ ] No security issues

## 🎉 Recognition

Contributors will be recognized in:
- **README.md**: Contributors section
- **CHANGELOG.md**: Release notes
- **GitHub**: Contributor graphs and statistics

## 📞 Getting Help

- **GitHub Issues**: For bugs and feature requests
- **GitHub Discussions**: For questions and general discussion
- **Email**: [maintainer-email@example.com] for private matters

## 📄 License

By contributing to this project, you agree that your contributions will be licensed under the MIT License.

---

Thank you for contributing to make this resource better for everyone! 🚀