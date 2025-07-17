# Chapter 4: Rate Limiting Systems

## Table of Contents
1. [Introduction to Rate Limiting](#introduction-to-rate-limiting)
2. [Rate Limiting Algorithms](#rate-limiting-algorithms)
3. [Token Bucket Implementation](#token-bucket-implementation)
4. [Distributed Locking System](#distributed-locking-system)
5. [Advanced Rate Limiting Patterns](#advanced-rate-limiting-patterns)
6. [Performance and Scalability](#performance-and-scalability)
7. [Interview Questions and Solutions](#interview-questions-and-solutions)

## Introduction to Rate Limiting

Rate limiting is a critical technique used to control the rate of requests sent or received by a system. It protects services from being overwhelmed by too many requests, ensures fair usage among users, and maintains system stability under high load.

### Why Rate Limiting?

1. **Prevent Abuse**: Protect against malicious attacks (DDoS, brute force)
2. **Ensure Fair Usage**: Prevent any single user from consuming all resources
3. **Maintain Performance**: Keep response times consistent under load
4. **Cost Control**: Limit expensive operations (API calls, database queries)
5. **SLA Compliance**: Ensure service level agreements are met

### Common Use Cases

1. **API Rate Limiting**: Limit requests per user/API key
2. **Login Attempts**: Prevent brute force attacks
3. **Resource Access**: Limit database connections, file uploads
4. **Billing Control**: Enforce usage limits for paid services
5. **Quality of Service**: Prioritize different user tiers

### Rate Limiting Strategies

1. **Per-User Limiting**: Individual limits for each user
2. **Per-IP Limiting**: Limits based on source IP address
3. **Global Limiting**: System-wide request limits
4. **Hierarchical Limiting**: Multiple levels of limits
5. **Dynamic Limiting**: Adaptive limits based on system load

## Rate Limiting Algorithms

### 1. Token Bucket Algorithm

**Concept**: Tokens are added to a bucket at a fixed rate. Each request consumes a token. If no tokens are available, the request is rejected.

**Characteristics:**
- Allows burst traffic up to bucket capacity
- Smooth rate limiting over time
- Memory efficient
- Easy to implement

### 2. Leaky Bucket Algorithm

**Concept**: Requests are added to a queue and processed at a fixed rate, like water leaking from a bucket.

**Characteristics:**
- Enforces strict output rate
- Smooths out burst traffic
- Can cause delays for legitimate requests
- Requires queue management

### 3. Fixed Window Counter

**Concept**: Count requests in fixed time windows (e.g., per minute).

**Characteristics:**
- Simple to implement
- Memory efficient
- Can allow burst at window boundaries
- Less accurate rate limiting

### 4. Sliding Window Log

**Concept**: Keep a log of request timestamps and count requests in a sliding time window.

**Characteristics:**
- Most accurate rate limiting
- High memory usage
- Complex cleanup logic
- Expensive for high-traffic systems

### 5. Sliding Window Counter

**Concept**: Hybrid approach combining fixed windows with sliding calculation.

**Characteristics:**
- Good balance of accuracy and efficiency
- Moderate memory usage
- Smooth rate limiting
- More complex implementation

## Token Bucket Implementation

### Architecture Overview

Our token bucket implementation provides flexible, high-performance rate limiting with support for different time units and burst handling.

**File Location:** `src/main/java/com/machinecoding/ratelimiting/TokenBucketRateLimiter.java`

### Core Implementation

```java
public class TokenBucketRateLimiter implements RateLimiter {
    private final long capacity;
    private final long refillRate;
    private final TimeUnit timeUnit;
    private final Object lock = new Object();
    
    private long tokens;
    private long lastRefillTime;
    
    public TokenBucketRateLimiter(long capacity, long refillRate, TimeUnit timeUnit) {
        if (capacity <= 0 || refillRate <= 0) {
            throw new IllegalArgumentException("Capacity and refill rate must be positive");
        }
        
        this.capacity = capacity;
        this.refillRate = refillRate;
        this.timeUnit = timeUnit;
        this.tokens = capacity; // Start with full bucket
        this.lastRefillTime = System.nanoTime();
    }
    
    @Override
    public boolean tryAcquire() {
        return tryAcquire(1);
    }
    
    @Override
    public boolean tryAcquire(long permits) {
        if (permits <= 0) {
            throw new IllegalArgumentException("Permits must be positive");
        }
        
        synchronized (lock) {
            refillTokens();
            
            if (tokens >= permits) {
                tokens -= permits;
                return true;
            }
            
            return false;
        }
    }
    
    private void refillTokens() {
        long currentTime = System.nanoTime();
        long elapsedTime = currentTime - lastRefillTime;
        
        if (elapsedTime > 0) {
            long tokensToAdd = (elapsedTime * refillRate) / timeUnit.toNanos(1);
            tokens = Math.min(capacity, tokens + tokensToAdd);
            lastRefillTime = currentTime;
        }
    }
    
    @Override
    public long getAvailableTokens() {
        synchronized (lock) {
            refillTokens();
            return tokens;
        }
    }
}
```

### Advanced Features

#### 1. Multi-User Rate Limiting
```java
public class MultiUserTokenBucketRateLimiter {
    private final Map<String, TokenBucketRateLimiter> userLimiters;
    private final long capacity;
    private final long refillRate;
    private final TimeUnit timeUnit;
    
    public MultiUserTokenBucketRateLimiter(long capacity, long refillRate, TimeUnit timeUnit) {
        this.userLimiters = new ConcurrentHashMap<>();
        this.capacity = capacity;
        this.refillRate = refillRate;
        this.timeUnit = timeUnit;
    }
    
    public boolean tryAcquire(String userId) {
        TokenBucketRateLimiter limiter = userLimiters.computeIfAbsent(userId, 
            k -> new TokenBucketRateLimiter(capacity, refillRate, timeUnit));
        
        return limiter.tryAcquire();
    }
    
    // Cleanup inactive users periodically
    public void cleanup() {
        long cutoffTime = System.currentTimeMillis() - TimeUnit.HOURS.toMillis(1);
        userLimiters.entrySet().removeIf(entry -> 
            entry.getValue().getLastAccessTime() < cutoffTime);
    }
}
```

#### 2. Hierarchical Rate Limiting
```java
public class HierarchicalRateLimiter {
    private final TokenBucketRateLimiter globalLimiter;
    private final Map<String, TokenBucketRateLimiter> userLimiters;
    
    public boolean tryAcquire(String userId) {
        // Check global limit first
        if (!globalLimiter.tryAcquire()) {
            return false;
        }
        
        // Check user-specific limit
        TokenBucketRateLimiter userLimiter = getUserLimiter(userId);
        if (!userLimiter.tryAcquire()) {
            // Return token to global limiter
            globalLimiter.returnToken();
            return false;
        }
        
        return true;
    }
}
```

#### 3. Weighted Rate Limiting
```java
public class WeightedRateLimiter {
    private final TokenBucketRateLimiter limiter;
    private final Map<String, Integer> userWeights;
    
    public boolean tryAcquire(String userId, String operation) {
        int weight = calculateWeight(userId, operation);
        return limiter.tryAcquire(weight);
    }
    
    private int calculateWeight(String userId, String operation) {
        int baseWeight = getOperationWeight(operation);
        int userMultiplier = userWeights.getOrDefault(userId, 1);
        return baseWeight * userMultiplier;
    }
}
```

### Usage Examples

#### Basic Usage
```java
// Create rate limiter: 100 requests per second
TokenBucketRateLimiter limiter = new TokenBucketRateLimiter(100, 100, TimeUnit.SECONDS);

// Check if request is allowed
if (limiter.tryAcquire()) {
    // Process request
    processRequest();
} else {
    // Reject request
    return "Rate limit exceeded";
}
```

#### Burst Handling
```java
// Allow bursts up to 1000 requests, refill at 100/second
TokenBucketRateLimiter burstLimiter = new TokenBucketRateLimiter(1000, 100, TimeUnit.SECONDS);

// Handle burst of requests
for (int i = 0; i < 500; i++) {
    if (burstLimiter.tryAcquire()) {
        processBurstRequest(i);
    }
}
```

## Distributed Locking System

### Architecture Overview

Distributed locking ensures mutual exclusion across multiple nodes in a distributed system. Our implementation provides consensus-based locking with deadlock prevention.

**File Location:** `src/main/java/com/machinecoding/ratelimiting/InMemoryDistributedLock.java`

### Core Components

#### 1. Lock Interface
```java
public interface DistributedLock {
    boolean tryLock(String lockKey, String ownerId, long timeoutMs);
    boolean unlock(String lockKey, String ownerId);
    boolean renewLock(String lockKey, String ownerId, long extensionMs);
    boolean isLocked(String lockKey);
    String getLockOwner(String lockKey);
}
```

#### 2. Lock Entry Model
```java
private static class LockEntry {
    private final String ownerId;
    private final long acquisitionTime;
    private volatile long expirationTime;
    private final AtomicInteger renewalCount;
    
    public LockEntry(String ownerId, long timeoutMs) {
        this.ownerId = ownerId;
        this.acquisitionTime = System.currentTimeMillis();
        this.expirationTime = acquisitionTime + timeoutMs;
        this.renewalCount = new AtomicInteger(0);
    }
    
    public boolean isExpired() {
        return System.currentTimeMillis() > expirationTime;
    }
    
    public boolean isOwnedBy(String ownerId) {
        return this.ownerId.equals(ownerId);
    }
}
```

#### 3. Main Implementation
```java
public class InMemoryDistributedLock implements DistributedLock {
    private final ConcurrentHashMap<String, LockEntry> locks;
    private final ScheduledExecutorService cleanupExecutor;
    private final LockStats stats;
    
    public InMemoryDistributedLock() {
        this.locks = new ConcurrentHashMap<>();
        this.stats = new LockStats();
        this.cleanupExecutor = Executors.newSingleThreadScheduledExecutor();
        
        // Schedule periodic cleanup of expired locks
        cleanupExecutor.scheduleAtFixedRate(this::cleanupExpiredLocks, 
            30, 30, TimeUnit.SECONDS);
    }
    
    @Override
    public boolean tryLock(String lockKey, String ownerId, long timeoutMs) {
        if (lockKey == null || ownerId == null || timeoutMs <= 0) {
            throw new IllegalArgumentException("Invalid lock parameters");
        }
        
        LockEntry newEntry = new LockEntry(ownerId, timeoutMs);
        LockEntry existingEntry = locks.putIfAbsent(lockKey, newEntry);
        
        if (existingEntry == null) {
            // Successfully acquired lock
            stats.incrementAcquisitions();
            return true;
        }
        
        // Check if existing lock is expired
        if (existingEntry.isExpired()) {
            if (locks.replace(lockKey, existingEntry, newEntry)) {
                stats.incrementAcquisitions();
                return true;
            }
        }
        
        // Check if same owner is trying to re-acquire (reentrant)
        if (existingEntry.isOwnedBy(ownerId)) {
            existingEntry.expirationTime = System.currentTimeMillis() + timeoutMs;
            stats.incrementReentrantAcquisitions();
            return true;
        }
        
        stats.incrementFailedAcquisitions();
        return false;
    }
    
    @Override
    public boolean unlock(String lockKey, String ownerId) {
        if (lockKey == null || ownerId == null) {
            return false;
        }
        
        LockEntry entry = locks.get(lockKey);
        if (entry != null && entry.isOwnedBy(ownerId)) {
            if (locks.remove(lockKey, entry)) {
                stats.incrementReleases();
                return true;
            }
        }
        
        return false;
    }
    
    @Override
    public boolean renewLock(String lockKey, String ownerId, long extensionMs) {
        LockEntry entry = locks.get(lockKey);
        if (entry != null && entry.isOwnedBy(ownerId) && !entry.isExpired()) {
            entry.expirationTime = System.currentTimeMillis() + extensionMs;
            entry.renewalCount.incrementAndGet();
            stats.incrementRenewals();
            return true;
        }
        
        return false;
    }
    
    private void cleanupExpiredLocks() {
        long currentTime = System.currentTimeMillis();
        locks.entrySet().removeIf(entry -> entry.getValue().isExpired());
    }
}
```

### Advanced Features

#### 1. Deadlock Detection
```java
public class DeadlockDetector {
    private final Map<String, Set<String>> waitGraph;
    
    public boolean wouldCauseDeadlock(String requesterId, String lockKey) {
        String currentOwner = getLockOwner(lockKey);
        if (currentOwner == null) {
            return false;
        }
        
        return hasPath(currentOwner, requesterId);
    }
    
    private boolean hasPath(String from, String to) {
        if (from.equals(to)) {
            return true;
        }
        
        Set<String> neighbors = waitGraph.get(from);
        if (neighbors == null) {
            return false;
        }
        
        for (String neighbor : neighbors) {
            if (hasPath(neighbor, to)) {
                return true;
            }
        }
        
        return false;
    }
}
```

#### 2. Lock Ordering
```java
public class OrderedLockManager {
    private final DistributedLock distributedLock;
    
    public boolean acquireMultipleLocks(String ownerId, long timeoutMs, String... lockKeys) {
        // Sort lock keys to prevent deadlock
        String[] sortedKeys = Arrays.stream(lockKeys)
            .sorted()
            .toArray(String[]::new);
        
        List<String> acquiredLocks = new ArrayList<>();
        
        try {
            for (String lockKey : sortedKeys) {
                if (!distributedLock.tryLock(lockKey, ownerId, timeoutMs)) {
                    // Failed to acquire lock, release all acquired locks
                    releaseAllLocks(ownerId, acquiredLocks);
                    return false;
                }
                acquiredLocks.add(lockKey);
            }
            
            return true;
            
        } catch (Exception e) {
            releaseAllLocks(ownerId, acquiredLocks);
            throw e;
        }
    }
}
```

### Usage Examples

#### Basic Locking
```java
DistributedLock lock = new InMemoryDistributedLock();
String lockKey = "resource:123";
String ownerId = "worker-1";

if (lock.tryLock(lockKey, ownerId, 30000)) { // 30 second timeout
    try {
        // Critical section
        processResource();
    } finally {
        lock.unlock(lockKey, ownerId);
    }
} else {
    // Could not acquire lock
    handleLockFailure();
}
```

#### Lock Renewal
```java
// Long-running task with lock renewal
if (lock.tryLock(lockKey, ownerId, 10000)) {
    try {
        while (hasMoreWork()) {
            doWork();
            
            // Renew lock every 5 seconds
            if (!lock.renewLock(lockKey, ownerId, 10000)) {
                throw new RuntimeException("Failed to renew lock");
            }
        }
    } finally {
        lock.unlock(lockKey, ownerId);
    }
}
```

## Advanced Rate Limiting Patterns

### 1. Adaptive Rate Limiting

Adjust limits based on system performance:

```java
public class AdaptiveRateLimiter {
    private final TokenBucketRateLimiter baseLimiter;
    private final SystemMetrics systemMetrics;
    private volatile double adaptationFactor = 1.0;
    
    public boolean tryAcquire() {
        updateAdaptationFactor();
        
        long adjustedPermits = (long) (1 * adaptationFactor);
        return baseLimiter.tryAcquire(adjustedPermits);
    }
    
    private void updateAdaptationFactor() {
        double cpuUsage = systemMetrics.getCpuUsage();
        double memoryUsage = systemMetrics.getMemoryUsage();
        
        if (cpuUsage > 0.8 || memoryUsage > 0.8) {
            adaptationFactor = Math.max(0.1, adaptationFactor * 0.9);
        } else if (cpuUsage < 0.5 && memoryUsage < 0.5) {
            adaptationFactor = Math.min(2.0, adaptationFactor * 1.1);
        }
    }
}
```

### 2. Circuit Breaker Integration

Combine rate limiting with circuit breaker pattern:

```java
public class RateLimitedCircuitBreaker {
    private final TokenBucketRateLimiter rateLimiter;
    private final CircuitBreaker circuitBreaker;
    
    public <T> T execute(Supplier<T> operation) {
        if (!rateLimiter.tryAcquire()) {
            throw new RateLimitExceededException("Rate limit exceeded");
        }
        
        return circuitBreaker.execute(operation);
    }
}
```

### 3. Priority-Based Rate Limiting

Different limits for different priority levels:

```java
public class PriorityRateLimiter {
    private final Map<Priority, TokenBucketRateLimiter> limiters;
    
    public enum Priority {
        HIGH(1000, 100),
        MEDIUM(500, 50),
        LOW(100, 10);
        
        private final long capacity;
        private final long refillRate;
    }
    
    public boolean tryAcquire(Priority priority) {
        TokenBucketRateLimiter limiter = limiters.get(priority);
        return limiter.tryAcquire();
    }
}
```

## Performance and Scalability

### Performance Characteristics

| Algorithm | Memory Usage | CPU Usage | Accuracy | Burst Handling |
|-----------|-------------|-----------|----------|----------------|
| Token Bucket | O(1) | O(1) | Good | Excellent |
| Leaky Bucket | O(n) | O(1) | Excellent | Poor |
| Fixed Window | O(1) | O(1) | Poor | Poor |
| Sliding Window | O(n) | O(n) | Excellent | Good |

### Optimization Techniques

#### 1. Lock-Free Implementation
```java
public class LockFreeTokenBucket {
    private final AtomicLong tokens;
    private final AtomicLong lastRefillTime;
    
    public boolean tryAcquire() {
        while (true) {
            long currentTokens = tokens.get();
            long currentTime = System.nanoTime();
            
            // Calculate new token count
            long newTokens = Math.min(capacity, 
                currentTokens + calculateRefill(currentTime));
            
            if (newTokens >= 1) {
                if (tokens.compareAndSet(currentTokens, newTokens - 1)) {
                    lastRefillTime.set(currentTime);
                    return true;
                }
            } else {
                return false;
            }
        }
    }
}
```

#### 2. Batch Processing
```java
public class BatchRateLimiter {
    private final TokenBucketRateLimiter limiter;
    
    public List<Boolean> tryAcquireBatch(List<String> requests) {
        return requests.stream()
            .map(request -> limiter.tryAcquire())
            .collect(Collectors.toList());
    }
}
```

### Monitoring and Metrics

```java
public class RateLimiterMetrics {
    private final AtomicLong totalRequests = new AtomicLong();
    private final AtomicLong allowedRequests = new AtomicLong();
    private final AtomicLong deniedRequests = new AtomicLong();
    
    public void recordRequest(boolean allowed) {
        totalRequests.incrementAndGet();
        if (allowed) {
            allowedRequests.incrementAndGet();
        } else {
            deniedRequests.incrementAndGet();
        }
    }
    
    public double getAllowanceRate() {
        long total = totalRequests.get();
        return total == 0 ? 0.0 : (double) allowedRequests.get() / total;
    }
}
```

## Interview Questions and Solutions

### Question 1: Design a Rate Limiter

**Problem:** Design a rate limiter that can handle 1000 requests per second with burst capability.

**Key Points to Cover:**
1. Algorithm choice (Token Bucket recommended)
2. Thread safety
3. Memory efficiency
4. Burst handling

**Solution:**
```java
public class RateLimiter {
    private final long capacity;
    private final long refillRate;
    private long tokens;
    private long lastRefillTime;
    private final Object lock = new Object();
    
    public boolean allowRequest() {
        synchronized (lock) {
            refillTokens();
            
            if (tokens > 0) {
                tokens--;
                return true;
            }
            return false;
        }
    }
    
    private void refillTokens() {
        long now = System.currentTimeMillis();
        long tokensToAdd = (now - lastRefillTime) * refillRate / 1000;
        tokens = Math.min(capacity, tokens + tokensToAdd);
        lastRefillTime = now;
    }
}
```

### Question 2: Distributed Rate Limiting

**Problem:** How would you implement rate limiting across multiple servers?

**Key Points to Cover:**
1. Centralized vs distributed approaches
2. Consistency vs availability trade-offs
3. Network latency considerations
4. Data synchronization

**Solution Approaches:**
- Redis-based centralized rate limiting
- Gossip protocol for distributed state
- Consistent hashing for data distribution
- Local rate limiting with global coordination

### Question 3: Multi-Tenant Rate Limiting

**Problem:** Design a rate limiter that supports different limits for different users/tenants.

**Key Points to Cover:**
1. Per-user state management
2. Memory cleanup strategies
3. Configuration management
4. Fair resource allocation

**Solution:**
```java
public class MultiTenantRateLimiter {
    private final Map<String, TokenBucketRateLimiter> userLimiters;
    private final RateLimitConfig config;
    
    public boolean allowRequest(String userId) {
        TokenBucketRateLimiter limiter = userLimiters.computeIfAbsent(userId, 
            this::createLimiterForUser);
        
        return limiter.tryAcquire();
    }
    
    private TokenBucketRateLimiter createLimiterForUser(String userId) {
        RateLimitSettings settings = config.getSettingsForUser(userId);
        return new TokenBucketRateLimiter(
            settings.getCapacity(), 
            settings.getRefillRate(), 
            TimeUnit.SECONDS
        );
    }
}
```

## Summary

Rate limiting is essential for building robust, scalable systems. Key takeaways:

1. **Algorithm Selection**: Token bucket provides the best balance of features and performance
2. **Thread Safety**: Always consider concurrent access in multi-threaded environments
3. **Distributed Systems**: Design for consistency vs availability trade-offs
4. **Monitoring**: Track metrics to understand system behavior and tune limits
5. **Flexibility**: Support different limits for different users/operations
6. **Performance**: Optimize for low latency and high throughput

The implementations in this chapter provide production-ready rate limiting solutions that can handle high-traffic scenarios while maintaining system stability and fairness.