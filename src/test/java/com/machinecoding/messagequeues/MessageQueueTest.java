package com.machinecoding.messagequeues;

import com.machinecoding.messagequeues.queue.MessageQueue;
import com.machinecoding.messagequeues.queue.BlockingMessageQueue;
import com.machinecoding.messagequeues.queue.CustomMessageQueue;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Timeout;
import static org.junit.jupiter.api.Assertions.*;

import java.util.concurrent.*;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.List;
import java.util.ArrayList;

/**
 * Comprehensive test suite for MessageQueue implementations.
 * Tests both BlockingMessageQueue and CustomMessageQueue implementations.
 */
public class MessageQueueTest {
    
    @Nested
    @DisplayName("BlockingMessageQueue Tests")
    class BlockingMessageQueueTests {
        
        private MessageQueue<String> queue;
        
        @BeforeEach
        void setUp() {
            queue = new BlockingMessageQueue<>();
        }
        
        @Test
        @DisplayName("Should produce and consume messages in FIFO order")
        void testBasicProduceConsume() throws InterruptedException {
            queue.produce("message1");
            queue.produce("message2");
            queue.produce("message3");
            
            assertEquals("message1", queue.consume());
            assertEquals("message2", queue.consume());
            assertEquals("message3", queue.consume());
        }
        
        @Test
        @DisplayName("Should handle concurrent producers and consumers")
        @Timeout(5)
        void testConcurrentProducerConsumer() throws InterruptedException {
            final int messageCount = 1000;
            final AtomicInteger producedCount = new AtomicInteger(0);
            final AtomicInteger consumedCount = new AtomicInteger(0);
            final List<String> consumedMessages = new CopyOnWriteArrayList<>();
            
            // Create producer threads
            ExecutorService producers = Executors.newFixedThreadPool(3);
            for (int i = 0; i < 3; i++) {
                final int producerId = i;
                producers.submit(() -> {
                    try {
                        for (int j = 0; j < messageCount / 3; j++) {
                            String message = "producer-" + producerId + "-message-" + j;
                            queue.produce(message);
                            producedCount.incrementAndGet();
                        }
                    } catch (InterruptedException e) {
                        Thread.currentThread().interrupt();
                    }
                });
            }
            
            // Create consumer threads
            ExecutorService consumers = Executors.newFixedThreadPool(2);
            for (int i = 0; i < 2; i++) {
                consumers.submit(() -> {
                    try {
                        while (consumedCount.get() < messageCount) {
                            String message = queue.consume(100);
                            if (message != null) {
                                consumedMessages.add(message);
                                consumedCount.incrementAndGet();
                            }
                        }
                    } catch (InterruptedException e) {
                        Thread.currentThread().interrupt();
                    }
                });
            }
            
            producers.shutdown();
            consumers.shutdown();
            
            assertTrue(producers.awaitTermination(3, TimeUnit.SECONDS));
            assertTrue(consumers.awaitTermination(3, TimeUnit.SECONDS));
            
            assertEquals(messageCount, producedCount.get());
            assertEquals(messageCount, consumedCount.get());
            assertEquals(messageCount, consumedMessages.size());
        }
        
        @Test
        @DisplayName("Should handle bounded queue capacity")
        void testBoundedQueue() throws InterruptedException {
            MessageQueue<String> boundedQueue = new BlockingMessageQueue<>(2);
            
            boundedQueue.produce("msg1");
            boundedQueue.produce("msg2");
            
            assertEquals(2, boundedQueue.size());
            
            // This should not block since we're testing bounded behavior
            CompletableFuture<Void> producer = CompletableFuture.runAsync(() -> {
                try {
                    boundedQueue.produce("msg3");
                } catch (InterruptedException e) {
                    Thread.currentThread().interrupt();
                }
            });
            
            // Give producer time to potentially block
            Thread.sleep(100);
            assertFalse(producer.isDone());
            
            // Consume one message to make space
            assertEquals("msg1", boundedQueue.consume());
            
            // Now producer should complete
            assertDoesNotThrow(() -> producer.get(1, TimeUnit.SECONDS));
            assertEquals(2, boundedQueue.size());
        }
        
        @Test
        @DisplayName("Should handle timeout on consume")
        void testConsumeTimeout() throws InterruptedException {
            long startTime = System.currentTimeMillis();
            String result = queue.consume(500);
            long endTime = System.currentTimeMillis();
            
            assertNull(result);
            assertTrue(endTime - startTime >= 450); // Allow some variance
        }
        
        @Test
        @DisplayName("Should handle shutdown gracefully")
        void testShutdown() throws InterruptedException {
            queue.produce("message1");
            queue.shutdown();
            
            // Should still be able to consume existing messages
            assertEquals("message1", queue.consume());
            
            // Should return null for subsequent consumes
            assertNull(queue.consume(100));
            
            // Should throw exception for new produces
            assertThrows(IllegalStateException.class, () -> queue.produce("new message"));
        }
        
        @Test
        @DisplayName("Should reject null messages")
        void testNullMessageRejection() {
            assertThrows(IllegalArgumentException.class, () -> queue.produce(null));
        }
    }
    
    @Nested
    @DisplayName("CustomMessageQueue Tests")
    class CustomMessageQueueTests {
        
        private MessageQueue<String> queue;
        
        @BeforeEach
        void setUp() {
            queue = new CustomMessageQueue<>();
        }
        
        @Test
        @DisplayName("Should produce and consume messages in FIFO order")
        void testBasicProduceConsume() throws InterruptedException {
            queue.produce("message1");
            queue.produce("message2");
            queue.produce("message3");
            
            assertEquals("message1", queue.consume());
            assertEquals("message2", queue.consume());
            assertEquals("message3", queue.consume());
        }
        
        @Test
        @DisplayName("Should handle concurrent access correctly")
        @Timeout(5)
        void testConcurrentAccess() throws InterruptedException {
            final int messageCount = 500;
            final CountDownLatch producerLatch = new CountDownLatch(messageCount);
            final CountDownLatch consumerLatch = new CountDownLatch(messageCount);
            
            // Producer thread
            Thread producer = new Thread(() -> {
                try {
                    for (int i = 0; i < messageCount; i++) {
                        queue.produce("message-" + i);
                        producerLatch.countDown();
                    }
                } catch (InterruptedException e) {
                    Thread.currentThread().interrupt();
                }
            });
            
            // Consumer thread
            Thread consumer = new Thread(() -> {
                try {
                    for (int i = 0; i < messageCount; i++) {
                        String message = queue.consume();
                        assertNotNull(message);
                        assertTrue(message.startsWith("message-"));
                        consumerLatch.countDown();
                    }
                } catch (InterruptedException e) {
                    Thread.currentThread().interrupt();
                }
            });
            
            producer.start();
            consumer.start();
            
            assertTrue(producerLatch.await(3, TimeUnit.SECONDS));
            assertTrue(consumerLatch.await(3, TimeUnit.SECONDS));
            
            producer.join();
            consumer.join();
            
            assertTrue(queue.isEmpty());
        }
        
        @Test
        @DisplayName("Should respect capacity limits")
        void testCapacityLimits() throws InterruptedException {
            MessageQueue<String> boundedQueue = new CustomMessageQueue<>(3);
            
            boundedQueue.produce("msg1");
            boundedQueue.produce("msg2");
            boundedQueue.produce("msg3");
            
            assertEquals(3, boundedQueue.size());
            
            // Test that producer blocks when queue is full
            CompletableFuture<Void> blockedProducer = CompletableFuture.runAsync(() -> {
                try {
                    boundedQueue.produce("msg4");
                } catch (InterruptedException e) {
                    Thread.currentThread().interrupt();
                }
            });
            
            Thread.sleep(100);
            assertFalse(blockedProducer.isDone());
            
            // Consume one message to unblock producer
            assertEquals("msg1", boundedQueue.consume());
            
            assertDoesNotThrow(() -> blockedProducer.get(1, TimeUnit.SECONDS));
            assertEquals(3, boundedQueue.size());
        }
        
        @Test
        @DisplayName("Should handle invalid capacity")
        void testInvalidCapacity() {
            assertThrows(IllegalArgumentException.class, () -> new CustomMessageQueue<>(0));
            assertThrows(IllegalArgumentException.class, () -> new CustomMessageQueue<>(-1));
        }
    }
    
    @Nested
    @DisplayName("Performance Comparison Tests")
    class PerformanceTests {
        
        @Test
        @DisplayName("Should demonstrate performance characteristics")
        @Timeout(10)
        void testPerformanceComparison() throws InterruptedException {
            final int messageCount = 10000;
            
            // Test BlockingMessageQueue
            long blockingStart = System.currentTimeMillis();
            testQueuePerformance(new BlockingMessageQueue<>(), messageCount);
            long blockingTime = System.currentTimeMillis() - blockingStart;
            
            // Test CustomMessageQueue
            long customStart = System.currentTimeMillis();
            testQueuePerformance(new CustomMessageQueue<>(), messageCount);
            long customTime = System.currentTimeMillis() - customStart;
            
            System.out.println("BlockingMessageQueue time: " + blockingTime + "ms");
            System.out.println("CustomMessageQueue time: " + customTime + "ms");
            
            // Both should complete within reasonable time
            assertTrue(blockingTime < 5000);
            assertTrue(customTime < 5000);
        }
        
        private void testQueuePerformance(MessageQueue<String> queue, int messageCount) 
                throws InterruptedException {
            CountDownLatch latch = new CountDownLatch(2);
            
            Thread producer = new Thread(() -> {
                try {
                    for (int i = 0; i < messageCount; i++) {
                        queue.produce("message-" + i);
                    }
                } catch (InterruptedException e) {
                    Thread.currentThread().interrupt();
                } finally {
                    latch.countDown();
                }
            });
            
            Thread consumer = new Thread(() -> {
                try {
                    for (int i = 0; i < messageCount; i++) {
                        queue.consume();
                    }
                } catch (InterruptedException e) {
                    Thread.currentThread().interrupt();
                } finally {
                    latch.countDown();
                }
            });
            
            producer.start();
            consumer.start();
            
            assertTrue(latch.await(5, TimeUnit.SECONDS));
        }
    }
}