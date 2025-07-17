package com.machinecoding.messagequeues.queue;

import java.util.LinkedList;
import java.util.Queue;
import java.util.concurrent.locks.Condition;
import java.util.concurrent.locks.ReentrantLock;

/**
 * Custom implementation of MessageQueue using manual synchronization.
 * This implementation demonstrates low-level concurrency control using locks and conditions.
 * 
 * @param <T> the type of messages stored in the queue
 */
public class CustomMessageQueue<T> implements MessageQueue<T> {
    
    private final Queue<T> queue;
    private final int capacity;
    private final ReentrantLock lock;
    private final Condition notEmpty;
    private final Condition notFull;
    private volatile boolean isShutdown;
    
    /**
     * Creates an unbounded message queue.
     */
    public CustomMessageQueue() {
        this(Integer.MAX_VALUE);
    }
    
    /**
     * Creates a bounded message queue with specified capacity.
     * 
     * @param capacity maximum number of messages the queue can hold
     */
    public CustomMessageQueue(int capacity) {
        if (capacity <= 0) {
            throw new IllegalArgumentException("Capacity must be positive");
        }
        
        this.queue = new LinkedList<>();
        this.capacity = capacity;
        this.lock = new ReentrantLock();
        this.notEmpty = lock.newCondition();
        this.notFull = lock.newCondition();
        this.isShutdown = false;
    }
    
    @Override
    public void produce(T message) throws InterruptedException {
        if (message == null) {
            throw new IllegalArgumentException("Message cannot be null");
        }
        
        lock.lock();
        try {
            if (isShutdown) {
                throw new IllegalStateException("Queue is shutdown");
            }
            
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
            
            // If shutdown and empty, return null
            if (isShutdown && queue.isEmpty()) {
                return null;
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
    
    @Override
    public T consume(long timeoutMs) throws InterruptedException {
        lock.lock();
        try {
            long remainingTime = timeoutMs * 1_000_000; // Convert to nanoseconds
            
            // Wait while queue is empty and not shutdown
            while (queue.isEmpty() && !isShutdown && remainingTime > 0) {
                remainingTime = notEmpty.awaitNanos(remainingTime);
            }
            
            // If shutdown and empty, return null
            if (isShutdown && queue.isEmpty()) {
                return null;
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
    
    @Override
    public int size() {
        lock.lock();
        try {
            return queue.size();
        } finally {
            lock.unlock();
        }
    }
    
    @Override
    public boolean isEmpty() {
        lock.lock();
        try {
            return queue.isEmpty();
        } finally {
            lock.unlock();
        }
    }
    
    @Override
    public void shutdown() {
        lock.lock();
        try {
            isShutdown = true;
            notEmpty.signalAll(); // Wake up all waiting consumers
            notFull.signalAll();  // Wake up all waiting producers
        } finally {
            lock.unlock();
        }
    }
    
    /**
     * Checks if the queue has been shutdown.
     * 
     * @return true if the queue is shutdown
     */
    public boolean isShutdown() {
        return isShutdown;
    }
}