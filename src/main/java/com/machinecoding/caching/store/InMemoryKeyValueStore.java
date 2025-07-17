package com.machinecoding.caching.store;

import java.util.*;
import java.util.concurrent.*;
import java.util.concurrent.atomic.AtomicLong;
import java.util.concurrent.locks.ReadWriteLock;
import java.util.concurrent.locks.ReentrantReadWriteLock;
import java.util.stream.Collectors;

/**
 * Thread-safe in-memory implementation of KeyValueStore.
 * Features:
 * - Thread-safe operations using ReadWriteLock
 * - TTL support with automatic expiration
 * - Memory management and statistics
 * - Background cleanup of expired keys
 */
public class InMemoryKeyValueStore<K, V> implements KeyValueStore<K, V> {
    
    private final ConcurrentHashMap<K, StoreEntry<V>> store;
    private final ReadWriteLock lock;
    private final ScheduledExecutorService cleanupExecutor;
    private final AtomicLong totalGets;
    private final AtomicLong totalPuts;
    private final AtomicLong totalRemoves;
    private final AtomicLong hits;
    private final AtomicLong misses;
    private final AtomicLong expiredKeys;
    private final boolean enableAutoCleanup;
    
    public InMemoryKeyValueStore() {
        this(true, 60); // Auto cleanup every 60 seconds
    }
    
    public InMemoryKeyValueStore(boolean enableAutoCleanup, long cleanupIntervalSeconds) {
        this.store = new ConcurrentHashMap<>();
        this.lock = new ReentrantReadWriteLock();
        this.totalGets = new AtomicLong(0);
        this.totalPuts = new AtomicLong(0);
        this.totalRemoves = new AtomicLong(0);
        this.hits = new AtomicLong(0);
        this.misses = new AtomicLong(0);
        this.expiredKeys = new AtomicLong(0);
        this.enableAutoCleanup = enableAutoCleanup;
        
        if (enableAutoCleanup) {
            this.cleanupExecutor = Executors.newSingleThreadScheduledExecutor(r -> {
                Thread t = new Thread(r, "KeyValueStore-Cleanup");
                t.setDaemon(true);
                return t;
            });
            
            // Schedule periodic cleanup
            cleanupExecutor.scheduleAtFixedRate(
                this::cleanupExpired, 
                cleanupIntervalSeconds, 
                cleanupIntervalSeconds, 
                TimeUnit.SECONDS
            );
        } else {
            this.cleanupExecutor = null;
        }
    }
    
    @Override
    public void put(K key, V value) {
        if (key == null) {
            throw new IllegalArgumentException("Key cannot be null");
        }
        
        lock.writeLock().lock();
        try {
            store.put(key, new StoreEntry<>(value));
            totalPuts.incrementAndGet();
        } finally {
            lock.writeLock().unlock();
        }
    }
    
    @Override
    public void put(K key, V value, long ttl, TimeUnit unit) {
        if (key == null) {
            throw new IllegalArgumentException("Key cannot be null");
        }
        if (ttl <= 0) {
            throw new IllegalArgumentException("TTL must be positive");
        }
        
        lock.writeLock().lock();
        try {
            long expirationTime = System.currentTimeMillis() + unit.toMillis(ttl);
            store.put(key, new StoreEntry<>(value, expirationTime));
            totalPuts.incrementAndGet();
        } finally {
            lock.writeLock().unlock();
        }
    }
    
    @Override
    public Optional<V> get(K key) {
        if (key == null) {
            return Optional.empty();
        }
        
        lock.readLock().lock();
        try {
            totalGets.incrementAndGet();
            StoreEntry<V> entry = store.get(key);
            
            if (entry == null) {
                misses.incrementAndGet();
                return Optional.empty();
            }
            
            if (entry.isExpired()) {
                misses.incrementAndGet();
                // Remove expired entry asynchronously to avoid blocking reads
                CompletableFuture.runAsync(() -> {
                    lock.writeLock().lock();
                    try {
                        // Double-check the entry is still there and expired
                        StoreEntry<V> currentEntry = store.get(key);
                        if (currentEntry != null && currentEntry.isExpired()) {
                            store.remove(key);
                            expiredKeys.incrementAndGet();
                        }
                    } finally {
                        lock.writeLock().unlock();
                    }
                });
                return Optional.empty();
            }
            
            hits.incrementAndGet();
            return Optional.of(entry.getValue());
        } finally {
            lock.readLock().unlock();
        }
    }
    
    @Override
    public boolean remove(K key) {
        if (key == null) {
            return false;
        }
        
        lock.writeLock().lock();
        try {
            totalRemoves.incrementAndGet();
            return store.remove(key) != null;
        } finally {
            lock.writeLock().unlock();
        }
    }
    
    @Override
    public boolean containsKey(K key) {
        if (key == null) {
            return false;
        }
        
        lock.readLock().lock();
        try {
            StoreEntry<V> entry = store.get(key);
            return entry != null && !entry.isExpired();
        } finally {
            lock.readLock().unlock();
        }
    }
    
    @Override
    public int size() {
        lock.readLock().lock();
        try {
            // Count only non-expired entries
            return (int) store.values().stream()
                    .filter(entry -> !entry.isExpired())
                    .count();
        } finally {
            lock.readLock().unlock();
        }
    }
    
    @Override
    public boolean isEmpty() {
        return size() == 0;
    }
    
    @Override
    public void clear() {
        lock.writeLock().lock();
        try {
            store.clear();
        } finally {
            lock.writeLock().unlock();
        }
    }
    
    @Override
    public Set<K> keySet() {
        lock.readLock().lock();
        try {
            return store.entrySet().stream()
                    .filter(entry -> !entry.getValue().isExpired())
                    .map(Map.Entry::getKey)
                    .collect(Collectors.toSet());
        } finally {
            lock.readLock().unlock();
        }
    }
    
    @Override
    public boolean expire(K key, long ttl, TimeUnit unit) {
        if (key == null || ttl <= 0) {
            return false;
        }
        
        lock.writeLock().lock();
        try {
            StoreEntry<V> entry = store.get(key);
            if (entry == null || entry.isExpired()) {
                return false;
            }
            
            long expirationTime = System.currentTimeMillis() + unit.toMillis(ttl);
            store.put(key, new StoreEntry<>(entry.getValue(), expirationTime));
            return true;
        } finally {
            lock.writeLock().unlock();
        }
    }
    
    @Override
    public long getTTL(K key) {
        if (key == null) {
            return -1;
        }
        
        lock.readLock().lock();
        try {
            StoreEntry<V> entry = store.get(key);
            if (entry == null) {
                return -1; // Key doesn't exist
            }
            
            if (!entry.hasExpiration()) {
                return -2; // No expiration set
            }
            
            long remaining = entry.getExpirationTime() - System.currentTimeMillis();
            return Math.max(0, remaining);
        } finally {
            lock.readLock().unlock();
        }
    }
    
    @Override
    public int cleanupExpired() {
        lock.writeLock().lock();
        try {
            List<K> expiredKeys = new ArrayList<>();
            
            for (Map.Entry<K, StoreEntry<V>> entry : store.entrySet()) {
                if (entry.getValue().isExpired()) {
                    expiredKeys.add(entry.getKey());
                }
            }
            
            for (K key : expiredKeys) {
                store.remove(key);
            }
            
            this.expiredKeys.addAndGet(expiredKeys.size());
            return expiredKeys.size();
        } finally {
            lock.writeLock().unlock();
        }
    }
    
    @Override
    public StoreStats getStats() {
        lock.readLock().lock();
        try {
            long memoryUsage = estimateMemoryUsage();
            return new StoreStats(
                totalGets.get(),
                totalPuts.get(),
                totalRemoves.get(),
                hits.get(),
                misses.get(),
                expiredKeys.get(),
                store.size(),
                memoryUsage
            );
        } finally {
            lock.readLock().unlock();
        }
    }
    
    /**
     * Estimates memory usage of the store.
     * This is a rough estimation for demonstration purposes.
     */
    private long estimateMemoryUsage() {
        // Rough estimation: 
        // - Each entry: ~100 bytes overhead
        // - String keys: ~40 bytes + 2 * length
        // - Object values: ~50 bytes (rough estimate)
        return store.size() * 200L; // Simplified estimation
    }
    
    /**
     * Shuts down the store and cleanup resources.
     */
    public void shutdown() {
        if (cleanupExecutor != null && !cleanupExecutor.isShutdown()) {
            cleanupExecutor.shutdown();
            try {
                if (!cleanupExecutor.awaitTermination(5, TimeUnit.SECONDS)) {
                    cleanupExecutor.shutdownNow();
                }
            } catch (InterruptedException e) {
                cleanupExecutor.shutdownNow();
                Thread.currentThread().interrupt();
            }
        }
    }
    
    /**
     * Internal class to represent a store entry with optional expiration.
     */
    private static class StoreEntry<V> {
        private final V value;
        private final long expirationTime;
        private final boolean hasExpiration;
        
        public StoreEntry(V value) {
            this.value = value;
            this.expirationTime = -1;
            this.hasExpiration = false;
        }
        
        public StoreEntry(V value, long expirationTime) {
            this.value = value;
            this.expirationTime = expirationTime;
            this.hasExpiration = true;
        }
        
        public V getValue() {
            return value;
        }
        
        public long getExpirationTime() {
            return expirationTime;
        }
        
        public boolean hasExpiration() {
            return hasExpiration;
        }
        
        public boolean isExpired() {
            return hasExpiration && System.currentTimeMillis() > expirationTime;
        }
    }
}