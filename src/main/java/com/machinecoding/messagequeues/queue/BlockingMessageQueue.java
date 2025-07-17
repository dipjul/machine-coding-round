package com.machinecoding.messagequeues.queue;

import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;

/**
 * Thread-safe implementation of MessageQueue using BlockingQueue.
 * This implementation uses Java's built-in concurrent collections for thread safety.
 * 
 * @param <T> the type of messages stored in the queue
 */
public class BlockingMessageQueue<T> implements MessageQueue<T> {
    
    private final BlockingQueue<T> queue;
    private final AtomicBoolean isShutdown;
    
    /**
     * Creates an unbounded message queue.
     */
    public BlockingMessageQueue() {
        this.queue = new LinkedBlockingQueue<>();
        this.isShutdown = new AtomicBoolean(false);
    }
    
    /**
     * Creates a bounded message queue with specified capacity.
     * 
     * @param capacity maximum number of messages the queue can hold
     */
    public BlockingMessageQueue(int capacity) {
        this.queue = new LinkedBlockingQueue<>(capacity);
        this.isShutdown = new AtomicBoolean(false);
    }
    
    @Override
    public void produce(T message) throws InterruptedException {
        if (isShutdown.get()) {
            throw new IllegalStateException("Queue is shutdown");
        }
        
        if (message == null) {
            throw new IllegalArgumentException("Message cannot be null");
        }
        
        queue.put(message);
    }
    
    @Override
    public T consume() throws InterruptedException {
        if (isShutdown.get() && queue.isEmpty()) {
            return null;
        }
        
        return queue.take();
    }
    
    @Override
    public T consume(long timeoutMs) throws InterruptedException {
        if (isShutdown.get() && queue.isEmpty()) {
            return null;
        }
        
        return queue.poll(timeoutMs, TimeUnit.MILLISECONDS);
    }
    
    @Override
    public int size() {
        return queue.size();
    }
    
    @Override
    public boolean isEmpty() {
        return queue.isEmpty();
    }
    
    @Override
    public void shutdown() {
        isShutdown.set(true);
    }
    
    /**
     * Checks if the queue has been shutdown.
     * 
     * @return true if the queue is shutdown
     */
    public boolean isShutdown() {
        return isShutdown.get();
    }
}