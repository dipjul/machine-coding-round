package com.machinecoding.caching.lru;

/**
 * Interface for LRU (Least Recently Used) Cache.
 * Provides basic cache operations with automatic eviction of least recently used items.
 * 
 * @param <K> the type of keys
 * @param <V> the type of values
 */
public interface LRUCache<K, V> {
    
    /**
     * Retrieves a value by key and marks it as recently used.
     * 
     * @param key the key
     * @return the value associated with the key, or null if not found
     */
    V get(K key);
    
    /**
     * Stores a key-value pair and marks it as recently used.
     * If the cache is at capacity, evicts the least recently used item.
     * 
     * @param key the key
     * @param value the value
     */
    void put(K key, V value);
    
    /**
     * Removes a key-value pair from the cache.
     * 
     * @param key the key to remove
     * @return the removed value, or null if key was not found
     */
    V remove(K key);
    
    /**
     * Checks if the cache contains the specified key.
     * 
     * @param key the key to check
     * @return true if the key exists in the cache
     */
    boolean containsKey(K key);
    
    /**
     * Returns the current size of the cache.
     * 
     * @return the number of key-value pairs in the cache
     */
    int size();
    
    /**
     * Returns the maximum capacity of the cache.
     * 
     * @return the maximum number of key-value pairs the cache can hold
     */
    int capacity();
    
    /**
     * Checks if the cache is empty.
     * 
     * @return true if the cache is empty
     */
    boolean isEmpty();
    
    /**
     * Checks if the cache is at full capacity.
     * 
     * @return true if the cache is at full capacity
     */
    boolean isFull();
    
    /**
     * Clears all entries from the cache.
     */
    void clear();
    
    /**
     * Gets cache statistics.
     * 
     * @return cache statistics
     */
    CacheStats getStats();
}