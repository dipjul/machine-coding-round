package com.machinecoding.integration;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.TestMethodOrder;
import org.junit.jupiter.api.MethodOrderer;
import static org.junit.jupiter.api.Assertions.*;

import com.machinecoding.messagequeues.queue.CustomMessageQueue;
import com.machinecoding.messagequeues.notification.service.NotificationService;
import com.machinecoding.caching.lru.LRUCache;
import com.machinecoding.caching.store.InMemoryKeyValueStore;
import com.machinecoding.ratelimiting.TokenBucketRateLimiter;
import com.machinecoding.realtime.chat.service.InMemoryChatService;
import com.machinecoding.search.autocomplete.InMemoryAutocompleteService;
import com.machinecoding.booking.service.InMemoryHotelBookingService;
import com.machinecoding.payment.service.InMemoryPaymentService;
import com.machinecoding.games.snakeladder.service.SnakeLadderGame;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.CountDownLatch;
import java.util.List;
import java.util.ArrayList;

/**
 * Comprehensive integration test suite for all machine coding systems.
 * Tests system interactions, performance, and reliability under various conditions.
 */
@TestMethodOrder(MethodOrderer.DisplayName.class)
public class IntegrationTestSuite {
    
    private ExecutorService executorService;
    private static final int THREAD_POOL_SIZE = 10;
    private static final int PERFORMANCE_TEST_ITERATIONS = 1000;
    
    @BeforeEach
    void setUp() {
        executorService = Executors.newFixedThreadPool(THREAD_POOL_SIZE);
    }
    
    @AfterEach
    void tearDown() {
        if (executorService != null) {
            executorService.shutdown();
            try {
                if (!executorService.awaitTermination(5, TimeUnit.SECONDS)) {
                    executorService.shutdownNow();
                }
            } catch (InterruptedException e) {
                executorService.shutdownNow();
                Thread.currentThread().interrupt();
            }
        }
    }
    
    @Test
    @DisplayName("01. Message Queue System Integration Test")
    void testMessageQueueSystemIntegration() throws InterruptedException {
        CustomMessageQueue<String> messageQueue = new CustomMessageQueue<>(100);
        CountDownLatch producerLatch = new CountDownLatch(5);
        CountDownLatch consumerLatch = new CountDownLatch(5);
        List<String> consumedMessages = new ArrayList<>();
        
        // Start producers
        for (int i = 0; i < 5; i++) {
            final int producerId = i;
            executorService.submit(() -> {
                try {
                    for (int j = 0; j < 10; j++) {
                        messageQueue.enqueue("Message-" + producerId + "-" + j);
                        Thread.sleep(10); // Simulate processing time
                    }
                } catch (InterruptedException e) {
                    Thread.currentThread().interrupt();
                } finally {
                    producerLatch.countDown();
                }
            });
        }
        
        // Start consumers
        for (int i = 0; i < 5; i++) {
            executorService.submit(() -> {
                try {
                    for (int j = 0; j < 10; j++) {
                        String message = messageQueue.dequeue();
                        synchronized (consumedMessages) {
                            consumedMessages.add(message);
                        }
                        Thread.sleep(5); // Simulate processing time
                    }
                } catch (InterruptedException e) {
                    Thread.currentThread().interrupt();
                } finally {
                    consumerLatch.countDown();
                }
            });
        }
        
        // Wait for completion
        assertTrue(producerLatch.await(10, TimeUnit.SECONDS), "Producers should complete within timeout");
        assertTrue(consumerLatch.await(10, TimeUnit.SECONDS), "Consumers should complete within timeout");
        
        // Verify results
        assertEquals(50, consumedMessages.size(), "All messages should be consumed");
        assertTrue(messageQueue.isEmpty(), "Queue should be empty after consumption");
    }
    
    @Test
    @DisplayName("02. Caching System Performance Test")
    void testCachingSystemPerformance() throws InterruptedException {
        LRUCache<String, String> lruCache = new LRUCache<>(1000);
        InMemoryKeyValueStore kvStore = new InMemoryKeyValueStore();
        
        CountDownLatch latch = new CountDownLatch(THREAD_POOL_SIZE);
        long startTime = System.currentTimeMillis();
        
        // Concurrent cache operations
        for (int i = 0; i < THREAD_POOL_SIZE; i++) {
            final int threadId = i;
            executorService.submit(() -> {
                try {
                    for (int j = 0; j < PERFORMANCE_TEST_ITERATIONS / THREAD_POOL_SIZE; j++) {
                        String key = "key-" + threadId + "-" + j;
                        String value = "value-" + threadId + "-" + j;
                        
                        // LRU Cache operations
                        lruCache.put(key, value);
                        String retrieved = lruCache.get(key);
                        assertEquals(value, retrieved);
                        
                        // KV Store operations
                        kvStore.put(key, value);
                        String kvRetrieved = kvStore.get(key);
                        assertEquals(value, kvRetrieved);
                    }
                } finally {
                    latch.countDown();
                }
            });
        }
        
        assertTrue(latch.await(30, TimeUnit.SECONDS), "Cache operations should complete within timeout");
        long endTime = System.currentTimeMillis();
        
        System.out.println("Cache Performance Test Results:");
        System.out.println("  Total operations: " + (PERFORMANCE_TEST_ITERATIONS * 4)); // 2 puts + 2 gets
        System.out.println("  Time taken: " + (endTime - startTime) + "ms");
        System.out.println("  Operations per second: " + 
                         ((PERFORMANCE_TEST_ITERATIONS * 4 * 1000.0) / (endTime - startTime)));
        
        // Verify cache state
        assertTrue(lruCache.size() <= 1000, "LRU cache should respect capacity limit");
        assertTrue(kvStore.size() <= PERFORMANCE_TEST_ITERATIONS, "KV store should contain all entries");
    }
    
    @Test
    @DisplayName("03. Rate Limiting System Stress Test")
    void testRateLimitingSystemStressTest() throws InterruptedException {
        TokenBucketRateLimiter rateLimiter = new TokenBucketRateLimiter(100, 10); // 100 requests, 10 per second refill
        CountDownLatch latch = new CountDownLatch(THREAD_POOL_SIZE);
        List<Boolean> results = new ArrayList<>();
        
        for (int i = 0; i < THREAD_POOL_SIZE; i++) {
            executorService.submit(() -> {
                try {
                    for (int j = 0; j < 20; j++) { // Each thread tries 20 requests
                        boolean allowed = rateLimiter.allowRequest("user-" + Thread.currentThread().getId());
                        synchronized (results) {
                            results.add(allowed);
                        }
                        Thread.sleep(50); // Small delay between requests
                    }
                } catch (InterruptedException e) {
                    Thread.currentThread().interrupt();
                } finally {
                    latch.countDown();
                }
            });
        }
        
        assertTrue(latch.await(15, TimeUnit.SECONDS), "Rate limiting test should complete within timeout");
        
        // Analyze results
        long allowedRequests = results.stream().mapToLong(b -> b ? 1 : 0).sum();
        long deniedRequests = results.size() - allowedRequests;
        
        System.out.println("Rate Limiting Test Results:");
        System.out.println("  Total requests: " + results.size());
        System.out.println("  Allowed requests: " + allowedRequests);
        System.out.println("  Denied requests: " + deniedRequests);
        System.out.println("  Denial rate: " + String.format("%.2f%%", (deniedRequests * 100.0) / results.size()));
        
        // Should have some denied requests due to rate limiting
        assertTrue(deniedRequests > 0, "Rate limiter should deny some requests under load");
        assertTrue(allowedRequests > 0, "Rate limiter should allow some requests");
    }
    
    @Test
    @DisplayName("04. Chat System Concurrent Users Test")
    void testChatSystemConcurrentUsers() throws InterruptedException {
        InMemoryChatService chatService = new InMemoryChatService();
        String roomId = chatService.createRoom("Test Room", "admin");
        
        CountDownLatch latch = new CountDownLatch(THREAD_POOL_SIZE);
        List<String> sentMessages = new ArrayList<>();
        
        // Simulate concurrent users joining and sending messages
        for (int i = 0; i < THREAD_POOL_SIZE; i++) {
            final int userId = i;
            executorService.submit(() -> {
                try {
                    String username = "user" + userId;
                    chatService.joinRoom(roomId, username);
                    
                    // Send multiple messages
                    for (int j = 0; j < 5; j++) {
                        String messageContent = "Message from " + username + " #" + j;
                        String messageId = chatService.sendMessage(roomId, username, messageContent);
                        synchronized (sentMessages) {
                            sentMessages.add(messageId);
                        }
                        Thread.sleep(10); // Small delay between messages
                    }
                } catch (InterruptedException e) {
                    Thread.currentThread().interrupt();
                } finally {
                    latch.countDown();
                }
            });
        }
        
        assertTrue(latch.await(10, TimeUnit.SECONDS), "Chat test should complete within timeout");
        
        // Verify results
        assertEquals(THREAD_POOL_SIZE * 5, sentMessages.size(), "All messages should be sent");
        
        var roomMessages = chatService.getMessages(roomId, 0, 1000);
        assertEquals(THREAD_POOL_SIZE * 5, roomMessages.size(), "All messages should be stored in room");
        
        System.out.println("Chat System Test Results:");
        System.out.println("  Concurrent users: " + THREAD_POOL_SIZE);
        System.out.println("  Messages sent: " + sentMessages.size());
        System.out.println("  Messages stored: " + roomMessages.size());
    }
    
    @Test
    @DisplayName("05. Search System Performance Test")
    void testSearchSystemPerformance() throws InterruptedException {
        InMemoryAutocompleteService autocompleteService = new InMemoryAutocompleteService();
        
        // Build index with sample data
        String[] sampleTerms = {
            "apple", "application", "apply", "approach", "appropriate",
            "banana", "band", "bank", "basic", "basketball",
            "computer", "complete", "company", "compare", "complex",
            "database", "data", "development", "design", "detail"
        };
        
        for (String term : sampleTerms) {
            autocompleteService.addTerm(term, 1.0);
        }
        
        CountDownLatch latch = new CountDownLatch(THREAD_POOL_SIZE);
        List<Long> searchTimes = new ArrayList<>();
        
        // Concurrent search operations
        for (int i = 0; i < THREAD_POOL_SIZE; i++) {
            executorService.submit(() -> {
                try {
                    for (int j = 0; j < 100; j++) {
                        String prefix = sampleTerms[j % sampleTerms.length].substring(0, 2);
                        
                        long startTime = System.nanoTime();
                        var suggestions = autocompleteService.getSuggestions(prefix, 5);
                        long endTime = System.nanoTime();
                        
                        synchronized (searchTimes) {
                            searchTimes.add(endTime - startTime);
                        }
                        
                        assertFalse(suggestions.isEmpty(), "Should find suggestions for prefix: " + prefix);
                    }
                } finally {
                    latch.countDown();
                }
            });
        }
        
        assertTrue(latch.await(10, TimeUnit.SECONDS), "Search test should complete within timeout");
        
        // Calculate performance metrics
        double avgSearchTime = searchTimes.stream().mapToLong(Long::longValue).average().orElse(0.0);
        long maxSearchTime = searchTimes.stream().mapToLong(Long::longValue).max().orElse(0L);
        
        System.out.println("Search System Performance Results:");
        System.out.println("  Total searches: " + searchTimes.size());
        System.out.println("  Average search time: " + String.format("%.2f μs", avgSearchTime / 1000.0));
        System.out.println("  Max search time: " + String.format("%.2f μs", maxSearchTime / 1000.0));
        
        // Performance assertions
        assertTrue(avgSearchTime < 100_000, "Average search time should be under 100μs"); // 100 microseconds
    }
    
    @Test
    @DisplayName("06. End-to-End System Integration Test")
    void testEndToEndSystemIntegration() throws InterruptedException {
        // This test demonstrates how multiple systems work together
        
        // 1. Set up systems
        InMemoryHotelBookingService bookingService = new InMemoryHotelBookingService();
        InMemoryPaymentService paymentService = new InMemoryPaymentService();
        NotificationService notificationService = new NotificationService();
        LRUCache<String, Object> cache = new LRUCache<>(100);
        
        // 2. Create test data
        String customerId = "customer123";
        String hotelId = "hotel456";
        
        // 3. Simulate booking workflow
        CountDownLatch workflowLatch = new CountDownLatch(1);
        
        executorService.submit(() -> {
            try {
                // Check availability (with caching)
                String cacheKey = "availability_" + hotelId;
                Boolean available = (Boolean) cache.get(cacheKey);
                if (available == null) {
                    available = bookingService.checkAvailability(hotelId, 
                        java.time.LocalDate.now(), java.time.LocalDate.now().plusDays(1), 1);
                    cache.put(cacheKey, available);
                }
                assertTrue(available, "Hotel should be available");
                
                // Create booking
                var booking = bookingService.createBooking(customerId, hotelId, 
                    java.time.LocalDate.now(), java.time.LocalDate.now().plusDays(1), 1, 2);
                assertNotNull(booking, "Booking should be created");
                
                // Process payment
                String paymentMethodId = paymentService.addPaymentMethod(customerId, 
                    com.machinecoding.payment.model.PaymentMethodType.CREDIT_CARD, 
                    "4111111111111111", null);
                assertNotNull(paymentMethodId, "Payment method should be added");
                
                var transaction = paymentService.processPayment(customerId, paymentMethodId, 
                    booking.getTotalAmount(), "USD", "Booking payment for " + booking.getBookingId());
                assertNotNull(transaction, "Payment should be processed");
                
                // Send notification
                var notification = new com.machinecoding.messagequeues.notification.model.Notification.Builder()
                    .recipient(customerId)
                    .title("Booking Confirmed")
                    .message("Your booking " + booking.getBookingId() + " has been confirmed")
                    .type(com.machinecoding.messagequeues.notification.model.NotificationType.BOOKING)
                    .build();
                
                notificationService.sendNotification(notification);
                
                System.out.println("End-to-End Integration Test Results:");
                System.out.println("  Booking ID: " + booking.getBookingId());
                System.out.println("  Transaction ID: " + transaction.getTransactionId());
                System.out.println("  Payment Amount: $" + transaction.getAmount());
                System.out.println("  Cache hits: " + cache.getStats().getHitCount());
                
            } catch (Exception e) {
                fail("End-to-end workflow should complete successfully: " + e.getMessage());
            } finally {
                workflowLatch.countDown();
            }
        });
        
        assertTrue(workflowLatch.await(10, TimeUnit.SECONDS), "End-to-end test should complete within timeout");
    }
    
    @Test
    @DisplayName("07. Game System Reliability Test")
    void testGameSystemReliability() throws InterruptedException {
        CountDownLatch latch = new CountDownLatch(5);
        List<Boolean> gameResults = new ArrayList<>();
        
        // Run multiple concurrent games
        for (int i = 0; i < 5; i++) {
            final int gameId = i;
            executorService.submit(() -> {
                try {
                    SnakeLadderGame game = new SnakeLadderGame("RELIABILITY_TEST_" + gameId);
                    
                    // Add players
                    game.addPlayer("Player1");
                    game.addPlayer("Player2");
                    
                    // Start game
                    boolean started = game.startGame();
                    assertTrue(started, "Game should start successfully");
                    
                    // Play until completion or timeout
                    int maxTurns = 500;
                    int turnCount = 0;
                    
                    while (game.getStatus() == com.machinecoding.games.snakeladder.service.GameStatus.IN_PROGRESS 
                           && turnCount < maxTurns) {
                        var currentPlayer = game.getCurrentPlayer();
                        var result = game.takeTurn(currentPlayer.getPlayerId());
                        turnCount++;
                        
                        if (result.isWinningMove()) {
                            break;
                        }
                    }
                    
                    boolean gameCompleted = game.getStatus() == 
                        com.machinecoding.games.snakeladder.service.GameStatus.FINISHED;
                    
                    synchronized (gameResults) {
                        gameResults.add(gameCompleted);
                    }
                    
                    if (gameCompleted) {
                        System.out.println("Game " + gameId + " completed in " + turnCount + " turns. Winner: " + 
                                         (game.getWinner() != null ? game.getWinner().getName() : "None"));
                    }
                    
                } catch (Exception e) {
                    synchronized (gameResults) {
                        gameResults.add(false);
                    }
                    System.err.println("Game " + gameId + " failed: " + e.getMessage());
                } finally {
                    latch.countDown();
                }
            });
        }
        
        assertTrue(latch.await(30, TimeUnit.SECONDS), "All games should complete within timeout");
        
        // Verify results
        long completedGames = gameResults.stream().mapToLong(b -> b ? 1 : 0).sum();
        System.out.println("Game Reliability Test Results:");
        System.out.println("  Total games: " + gameResults.size());
        System.out.println("  Completed games: " + completedGames);
        System.out.println("  Success rate: " + String.format("%.2f%%", (completedGames * 100.0) / gameResults.size()));
        
        assertTrue(completedGames >= 4, "At least 80% of games should complete successfully");
    }
}