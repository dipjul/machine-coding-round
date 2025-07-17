package com.machinecoding.messagequeues.queue;

/**
 * Core interface for a message queue system.
 * Provides basic operations for producing and consuming messages.
 * 
 * @param <T> the type of messages stored in the queue
 */
public interface MessageQueue<T> {
    
    /**
     * Adds a message to the queue.
     * This operation should be thread-safe.
     * 
     * @param message the message to add
     * @throws InterruptedException if the thread is interrupted while waiting
     */
    void produce(T message) throws InterruptedException;
    
    /**
     * Retrieves and removes a message from the queue.
     * Blocks if the queue is empty until a message becomes available.
     * 
     * @return the next message from the queue
     * @throws InterruptedException if the thread is interrupted while waiting
     */
    T consume() throws InterruptedException;
    
    /**
     * Retrieves and removes a message from the queue with a timeout.
     * 
     * @param timeoutMs maximum time to wait in milliseconds
     * @return the next message from the queue, or null if timeout occurs
     * @throws InterruptedException if the thread is interrupted while waiting
     */
    T consume(long timeoutMs) throws InterruptedException;
    
    /**
     * Returns the current size of the queue.
     * 
     * @return number of messages currently in the queue
     */
    int size();
    
    /**
     * Checks if the queue is empty.
     * 
     * @return true if the queue contains no messages
     */
    boolean isEmpty();
    
    /**
     * Shuts down the queue and releases resources.
     * After shutdown, no new messages can be produced or consumed.
     */
    void shutdown();
}