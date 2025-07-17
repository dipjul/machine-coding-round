package com.machinecoding.messagequeues.queue;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * Demonstration class for MessageQueue implementations.
 * Shows basic usage and concurrent behavior.
 */
public class MessageQueueDemo {
    
    public static void main(String[] args) throws InterruptedException {
        System.out.println("=== Message Queue Demo ===\n");
        
        // Test BlockingMessageQueue
        System.out.println("Testing BlockingMessageQueue:");
        testMessageQueue(new BlockingMessageQueue<>());
        
        System.out.println("\n" + "=".repeat(50) + "\n");
        
        // Test CustomMessageQueue
        System.out.println("Testing CustomMessageQueue:");
        testMessageQueue(new CustomMessageQueue<>());
        
        System.out.println("\n=== Demo Complete ===");
    }
    
    private static void testMessageQueue(MessageQueue<String> queue) throws InterruptedException {
        // Test 1: Basic produce/consume
        System.out.println("1. Basic produce/consume test:");
        queue.produce("Hello");
        queue.produce("World");
        queue.produce("!");
        
        System.out.println("   Consumed: " + queue.consume());
        System.out.println("   Consumed: " + queue.consume());
        System.out.println("   Consumed: " + queue.consume());
        System.out.println("   Queue size: " + queue.size());
        System.out.println("   Is empty: " + queue.isEmpty());
        
        // Test 2: Concurrent producers and consumers
        System.out.println("\n2. Concurrent test:");
        final int messageCount = 100;
        final AtomicInteger producedCount = new AtomicInteger(0);
        final AtomicInteger consumedCount = new AtomicInteger(0);
        
        ExecutorService executor = Executors.newFixedThreadPool(4);
        
        // Start 2 producers
        for (int i = 0; i < 2; i++) {
            final int producerId = i;
            executor.submit(() -> {
                try {
                    for (int j = 0; j < messageCount / 2; j++) {
                        String message = "Producer-" + producerId + "-Msg-" + j;
                        queue.produce(message);
                        producedCount.incrementAndGet();
                    }
                } catch (InterruptedException e) {
                    Thread.currentThread().interrupt();
                }
            });
        }
        
        // Start 2 consumers
        for (int i = 0; i < 2; i++) {
            final int consumerId = i;
            executor.submit(() -> {
                try {
                    while (consumedCount.get() < messageCount) {
                        String message = queue.consume(100);
                        if (message != null) {
                            consumedCount.incrementAndGet();
                            if (consumedCount.get() % 20 == 0) {
                                System.out.println("   Consumer-" + consumerId + 
                                    " processed message: " + message);
                            }
                        }
                    }
                } catch (InterruptedException e) {
                    Thread.currentThread().interrupt();
                }
            });
        }
        
        executor.shutdown();
        executor.awaitTermination(5, TimeUnit.SECONDS);
        
        System.out.println("   Total produced: " + producedCount.get());
        System.out.println("   Total consumed: " + consumedCount.get());
        System.out.println("   Final queue size: " + queue.size());
        
        // Test 3: Timeout behavior
        System.out.println("\n3. Timeout test:");
        long startTime = System.currentTimeMillis();
        String result = queue.consume(200);
        long endTime = System.currentTimeMillis();
        System.out.println("   Timeout result: " + result);
        System.out.println("   Time taken: " + (endTime - startTime) + "ms");
        
        // Test 4: Shutdown behavior
        System.out.println("\n4. Shutdown test:");
        queue.produce("Final message");
        queue.shutdown();
        System.out.println("   Message after shutdown: " + queue.consume());
        System.out.println("   Queue shutdown complete");
    }
}