package com.machinecoding.caching.lru;

import java.util.HashMap;
import java.util.Map;

/**
 * Basic LRU Cache implementation using HashMap + Doubly Linked List.
 * This is the classic implementation commonly asked in interviews.
 * 
 * Time Complexity: O(1) for all operations
 * Space Complexity: O(capacity)
 * 
 * @param <K> the type of keys
 * @param <V> the type of values
 */
public class BasicLRUCache<K, V> implements LRUCache<K, V> {
    
    private final int capacity;
    private final Map<K, Node<K, V>> cache;
    private final Node<K, V> head;
    private final Node<K, V> tail;
    
    // Statistics
    private long totalGets = 0;
    private long totalPuts = 0;
    private long totalRemoves = 0;
    private long hits = 0;
    private long misses = 0;
    private long evictions = 0;
    
    public BasicLRUCache(int capacity) {
        if (capacity <= 0) {
            throw new IllegalArgumentException("Capacity must be positive");
        }
        
        this.capacity = capacity;
        this.cache = new HashMap<>(capacity);
        
        // Create dummy head and tail nodes
        this.head = new Node<>(null, null);
        this.tail = new Node<>(null, null);
        head.next = tail;
        tail.prev = head;
    }
    
    @Override
    public V get(K key) {
        totalGets++;
        
        Node<K, V> node = cache.get(key);
        if (node == null) {
            misses++;
            return null;
        }
        
        hits++;
        // Move to head (mark as recently used)
        moveToHead(node);
        return node.value;
    }
    
    @Override
    public void put(K key, V value) {
        totalPuts++;
        
        Node<K, V> existing = cache.get(key);
        if (existing != null) {
            // Update existing node
            existing.value = value;
            moveToHead(existing);
            return;
        }
        
        // Create new node
        Node<K, V> newNode = new Node<>(key, value);
        
        if (cache.size() >= capacity) {
            // Remove least recently used (tail)
            Node<K, V> lru = removeTail();
            cache.remove(lru.key);
            evictions++;
        }
        
        // Add new node to head
        addToHead(newNode);
        cache.put(key, newNode);
    }
    
    @Override
    public V remove(K key) {
        totalRemoves++;
        
        Node<K, V> node = cache.remove(key);
        if (node == null) {
            return null;
        }
        
        removeNode(node);
        return node.value;
    }
    
    @Override
    public boolean containsKey(K key) {
        return cache.containsKey(key);
    }
    
    @Override
    public int size() {
        return cache.size();
    }
    
    @Override
    public int capacity() {
        return capacity;
    }
    
    @Override
    public boolean isEmpty() {
        return cache.isEmpty();
    }
    
    @Override
    public boolean isFull() {
        return cache.size() >= capacity;
    }
    
    @Override
    public void clear() {
        cache.clear();
        head.next = tail;
        tail.prev = head;
    }
    
    @Override
    public CacheStats getStats() {
        return new CacheStats(
            totalGets, totalPuts, totalRemoves,
            hits, misses, evictions,
            cache.size(), capacity
        );
    }
    
    // Doubly Linked List operations
    
    private void addToHead(Node<K, V> node) {
        node.prev = head;
        node.next = head.next;
        head.next.prev = node;
        head.next = node;
    }
    
    private void removeNode(Node<K, V> node) {
        node.prev.next = node.next;
        node.next.prev = node.prev;
    }
    
    private void moveToHead(Node<K, V> node) {
        removeNode(node);
        addToHead(node);
    }
    
    private Node<K, V> removeTail() {
        Node<K, V> lru = tail.prev;
        removeNode(lru);
        return lru;
    }
    
    /**
     * Node class for doubly linked list.
     */
    private static class Node<K, V> {
        K key;
        V value;
        Node<K, V> prev;
        Node<K, V> next;
        
        Node(K key, V value) {
            this.key = key;
            this.value = value;
        }
    }
    
    /**
     * Returns a string representation of the cache for debugging.
     * Shows the order from most recently used to least recently used.
     */
    public String toOrderedString() {
        StringBuilder sb = new StringBuilder();
        sb.append("[");
        
        Node<K, V> current = head.next;
        boolean first = true;
        while (current != tail) {
            if (!first) {
                sb.append(", ");
            }
            sb.append(current.key).append("=").append(current.value);
            current = current.next;
            first = false;
        }
        
        sb.append("]");
        return sb.toString();
    }
}