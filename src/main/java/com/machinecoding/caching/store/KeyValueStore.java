package com.machinecoding.caching.store;

import java.util.Optional;
import java.util.Set;
import java.util.concurrent.TimeUnit;

/**
 * Core interface for a key-value store system.
 * Provides basic operations for storing, retrieving, and managing key-value pairs.
 * 
 * @param <K> the type of keys
 * @param <V> the type of values
 */
public interface KeyValueStore<K, V> {
    
    /**
     * Stores a key-value pair.
     * 
     * @param key the key
     * @param value the value
     */
    void put(K key, V value);
    
    /**
     * Stores a key-value pair with expiration time.
     * 
     * @param key the key
     * @param value the value
     * @param ttl time to live
     * @param unit time unit for TTL
     */
    void put(K key, V value, long ttl, TimeUnit unit);
    
    /**
     * Retrieves a value by key.
     * 
     * @param key the key
     * @return Optional containing the value if found and not expired, empty otherwise
     */
    Optional<V> get(K key);
    
    /**
     * Removes a key-value pair.
     * 
     * @param key the key to remove
     * @return true if the key was present and removed, false otherwise
     */
    boolean remove(K key);
    
    /**
     * Checks if a key exists and is not expired.
     * 
     * @param key the key to check
     * @return true if the key exists and is not expired
     */
    boolean containsKey(K key);
    
    /**
     * Returns the number of key-value pairs.
     * 
     * @return the size of the store
     */
    int size();
    
    /**
     * Checks if the store is empty.
     * 
     * @return true if the store is empty
     */
    boolean isEmpty();
    
    /**
     * Clears all key-value pairs.
     */
    void clear();
    
    /**
     * Returns all keys in the store.
     * 
     * @return set of all keys
     */
    Set<K> keySet();
    
    /**
     * Sets expiration time for an existing key.
     * 
     * @param key the key
     * @param ttl time to live
     * @param unit time unit for TTL
     * @return true if the key exists and expiration was set
     */
    boolean expire(K key, long ttl, TimeUnit unit);
    
    /**
     * Gets the remaining time to live for a key.
     * 
     * @param key the key
     * @return remaining TTL in milliseconds, -1 if key doesn't exist, -2 if no expiration
     */
    long getTTL(K key);
    
    /**
     * Removes expired keys from the store.
     * 
     * @return number of keys removed
     */
    int cleanupExpired();
    
    /**
     * Gets statistics about the store.
     * 
     * @return store statistics
     */
    StoreStats getStats();
}