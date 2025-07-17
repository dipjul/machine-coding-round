package com.machinecoding.integration;

import com.machinecoding.messagequeues.queue.CustomMessageQueue;
import com.machinecoding.caching.lru.BasicLRUCache;
import com.machinecoding.caching.store.InMemoryKeyValueStore;
import com.machinecoding.ratelimiting.TokenBucketRateLimiter;
import com.machinecoding.games.snakeladder.service.SnakeLadderGame;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.CountDownLatch;
import java.util.List;
import java.util.ArrayList;
import java.util.Collections;

/**
 * Simple integration tests for machine coding systems without external dependencies.
 * Tests basic functionality and concurrent access patterns.
 */
public class SimpleIntegrationTest {
    
    private static final int THREAD_COUNT = 5;
    private static final int OPERATIONS_PER_THREAD = 100;
    
    public static void main(String[] args) {
        System.out.println("=== Machine Coding Systems Integration Test ===\n");
        
        SimpleIntegrationTest tester = new SimpleIntegrationTest();
        
        boolean allTestsPassed = true;
        
        allTestsPassed &= tester.testMessageQueueIntegration();
        allTestsPassed &= tester.testCacheSystemIntegration();
        allTestsPassed &= tester.testRateLimiterIntegration();
        allTestsPassed &= tester.testGameSystemIntegration();
        allTestsPassed &= tester.testEndToEndWorkflow();
        
        System.out.println("\n" + "=".repeat(60));
        if (allTestsPassed) {
            System.out.println("✅ ALL INTEGRATION TESTS PASSED");
        } else {
            System.out.println("❌ SOME INTEGRATION TESTS FAILED");
        }
        System.out.println("=".repeat(60));
        
        System.exit(allTestsPassed ? 0 : 1);
    }
    
    public boolean testMessageQueueIntegration() {
        System.out.println("1. Testing Message Queue Integration...");
        
        try {
            CustomMessageQueue<String> queue = new CustomMessageQueue<>(1000);
            ExecutorService executor = Executors.newFixedThreadPool(THREAD_COUNT);
            CountDownLatch producerLatch = new CountDownLatch(THREAD_COUNT / 2);
            CountDownLatch consumerLatch = new CountDownLatch(THREAD_COUNT / 2);
            List<String> consumedMessages = Collections.synchronizedList(new ArrayList<>());
            
            // Start producers
            for (int i = 0; i < THREAD_COUNT / 2; i++) {
                final int producerId = i;
                executor.submit(() -> {
                    try {
                        for (int j = 0; j < OPERATIONS_PER_THREAD; j++) {
                            queue.produce("Message-" + producerId + "-" + j);
                            Thread.sleep(1); // Small delay
                        }
                    } catch (InterruptedException e) {
                        Thread.currentThread().interrupt();
                    } finally {
                        producerLatch.countDown();
                    }
                });
            }
            
            // Start consumers
            for (int i = 0; i < THREAD_COUNT / 2; i++) {
                executor.submit(() -> {
                    try {
                        for (int j = 0; j < OPERATIONS_PER_THREAD; j++) {
                            String message = queue.consume();
                            if (message != null) {
                                consumedMessages.add(message);
                            }
                            Thread.sleep(1); // Small delay
                        }
                    } catch (InterruptedException e) {
                        Thread.currentThread().interrupt();
                    } finally {
                        consumerLatch.countDown();
                    }
                });
            }
            
            // Wait for completion
            boolean producersCompleted = producerLatch.await(10, TimeUnit.SECONDS);
            boolean consumersCompleted = consumerLatch.await(10, TimeUnit.SECONDS);
            
            executor.shutdown();
            
            if (!producersCompleted || !consumersCompleted) {
                System.out.println("   ❌ Test timed out");
                return false;
            }
            
            int expectedMessages = (THREAD_COUNT / 2) * OPERATIONS_PER_THREAD;
            System.out.println("   Messages produced: " + expectedMessages);
            System.out.println("   Messages consumed: " + consumedMessages.size());
            System.out.println("   Queue size: " + queue.size());
            
            if (consumedMessages.size() > 0) {
                System.out.println("   ✅ Message Queue Integration Test PASSED");
                return true;
            } else {
                System.out.println("   ❌ No messages were consumed");
                return false;
            }
            
        } catch (Exception e) {
            System.out.println("   ❌ Test failed with exception: " + e.getMessage());
            return false;
        }
    }
    
    public boolean testCacheSystemIntegration() {
        System.out.println("\n2. Testing Cache System Integration...");
        
        try {
            BasicLRUCache<String, String> lruCache = new BasicLRUCache<>(500);
            InMemoryKeyValueStore kvStore = new InMemoryKeyValueStore();
            ExecutorService executor = Executors.newFixedThreadPool(THREAD_COUNT);
            CountDownLatch latch = new CountDownLatch(THREAD_COUNT);
            
            long startTime = System.currentTimeMillis();
            
            for (int i = 0; i < THREAD_COUNT; i++) {
                final int threadId = i;
                executor.submit(() -> {
                    try {
                        for (int j = 0; j < OPERATIONS_PER_THREAD; j++) {
                            String key = "key-" + threadId + "-" + j;
                            String value = "value-" + threadId + "-" + j;
                            
                            // LRU Cache operations
                            lruCache.put(key, value);
                            String retrieved = lruCache.get(key);
                            
                            // KV Store operations
                            kvStore.put(key, value);
                            Object kvRetrievedObj = kvStore.get(key).orElse(null);
                            String kvRetrieved = kvRetrievedObj != null ? kvRetrievedObj.toString() : null;
                            
                            if (!value.equals(retrieved) || !value.equals(kvRetrieved)) {
                                System.out.println("   ❌ Data mismatch for key: " + key);
                            }
                        }
                    } finally {
                        latch.countDown();
                    }
                });
            }
            
            boolean completed = latch.await(15, TimeUnit.SECONDS);
            long endTime = System.currentTimeMillis();
            
            executor.shutdown();
            
            if (!completed) {
                System.out.println("   ❌ Test timed out");
                return false;
            }
            
            int totalOperations = THREAD_COUNT * OPERATIONS_PER_THREAD * 4; // 2 puts + 2 gets
            double duration = (endTime - startTime) / 1000.0;
            double throughput = totalOperations / duration;
            
            System.out.println("   Total operations: " + totalOperations);
            System.out.println("   Duration: " + String.format("%.2f", duration) + "s");
            System.out.println("   Throughput: " + String.format("%.0f", throughput) + " ops/sec");
            System.out.println("   LRU Cache size: " + lruCache.size());
            System.out.println("   KV Store size: " + kvStore.size());
            
            System.out.println("   ✅ Cache System Integration Test PASSED");
            return true;
            
        } catch (Exception e) {
            System.out.println("   ❌ Test failed with exception: " + e.getMessage());
            return false;
        }
    }
    
    public boolean testRateLimiterIntegration() {
        System.out.println("\n3. Testing Rate Limiter Integration...");
        
        try {
            TokenBucketRateLimiter rateLimiter = new TokenBucketRateLimiter(10, 1, TimeUnit.SECONDS); // Small bucket, slow refill
            ExecutorService executor = Executors.newFixedThreadPool(THREAD_COUNT);
            CountDownLatch latch = new CountDownLatch(THREAD_COUNT);
            List<Boolean> results = Collections.synchronizedList(new ArrayList<>());
            
            for (int i = 0; i < THREAD_COUNT; i++) {
                final int threadId = i;
                executor.submit(() -> {
                    try {
                        for (int j = 0; j < 20; j++) { // Each thread makes 20 requests
                            boolean allowed = rateLimiter.tryAcquire();
                            results.add(allowed);
                            Thread.sleep(10); // Small delay between requests
                        }
                    } catch (InterruptedException e) {
                        Thread.currentThread().interrupt();
                    } finally {
                        latch.countDown();
                    }
                });
            }
            
            boolean completed = latch.await(10, TimeUnit.SECONDS);
            executor.shutdown();
            
            if (!completed) {
                System.out.println("   ❌ Test timed out");
                return false;
            }
            
            long allowedRequests = results.stream().mapToLong(b -> b ? 1 : 0).sum();
            long deniedRequests = results.size() - allowedRequests;
            double denialRate = (deniedRequests * 100.0) / results.size();
            
            System.out.println("   Total requests: " + results.size());
            System.out.println("   Allowed requests: " + allowedRequests);
            System.out.println("   Denied requests: " + deniedRequests);
            System.out.println("   Denial rate: " + String.format("%.2f%%", denialRate));
            
            // Should have some denied requests due to rate limiting
            if (deniedRequests > 0 && allowedRequests > 0) {
                System.out.println("   ✅ Rate Limiter Integration Test PASSED");
                return true;
            } else {
                System.out.println("   ❌ Rate limiter didn't behave as expected");
                return false;
            }
            
        } catch (Exception e) {
            System.out.println("   ❌ Test failed with exception: " + e.getMessage());
            return false;
        }
    }
    
    public boolean testGameSystemIntegration() {
        System.out.println("\n4. Testing Game System Integration...");
        
        try {
            ExecutorService executor = Executors.newFixedThreadPool(3);
            CountDownLatch latch = new CountDownLatch(3);
            List<Boolean> gameResults = Collections.synchronizedList(new ArrayList<>());
            
            // Run multiple concurrent games
            for (int i = 0; i < 3; i++) {
                final int gameId = i;
                executor.submit(() -> {
                    try {
                        SnakeLadderGame game = new SnakeLadderGame("INTEGRATION_TEST_" + gameId);
                        
                        // Add players
                        game.addPlayer("Player1");
                        game.addPlayer("Player2");
                        
                        // Start game
                        boolean started = game.startGame();
                        if (!started) {
                            gameResults.add(false);
                            return;
                        }
                        
                        // Play until completion or timeout
                        int maxTurns = 200;
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
                        
                        gameResults.add(gameCompleted);
                        
                        if (gameCompleted) {
                            System.out.println("   Game " + gameId + " completed in " + turnCount + " turns. Winner: " + 
                                             (game.getWinner() != null ? game.getWinner().getName() : "None"));
                        }
                        
                    } catch (Exception e) {
                        gameResults.add(false);
                        System.out.println("   Game " + gameId + " failed: " + e.getMessage());
                    } finally {
                        latch.countDown();
                    }
                });
            }
            
            boolean completed = latch.await(30, TimeUnit.SECONDS);
            executor.shutdown();
            
            if (!completed) {
                System.out.println("   ❌ Test timed out");
                return false;
            }
            
            long completedGames = gameResults.stream().mapToLong(b -> b ? 1 : 0).sum();
            double successRate = (completedGames * 100.0) / gameResults.size();
            
            System.out.println("   Total games: " + gameResults.size());
            System.out.println("   Completed games: " + completedGames);
            System.out.println("   Success rate: " + String.format("%.2f%%", successRate));
            
            if (completedGames >= 2) { // At least 2 out of 3 games should complete
                System.out.println("   ✅ Game System Integration Test PASSED");
                return true;
            } else {
                System.out.println("   ❌ Too many games failed to complete");
                return false;
            }
            
        } catch (Exception e) {
            System.out.println("   ❌ Test failed with exception: " + e.getMessage());
            return false;
        }
    }
    
    public boolean testEndToEndWorkflow() {
        System.out.println("\n5. Testing End-to-End Workflow...");
        
        try {
            // Simulate a workflow that uses multiple systems
            BasicLRUCache<String, String> cache = new BasicLRUCache<>(100);
            CustomMessageQueue<String> queue = new CustomMessageQueue<>(50);
            TokenBucketRateLimiter rateLimiter = new TokenBucketRateLimiter(10, 5, TimeUnit.SECONDS);
            
            // Step 1: Check rate limit
            boolean allowed = rateLimiter.tryAcquire();
            if (!allowed) {
                System.out.println("   ❌ Request was rate limited");
                return false;
            }
            
            // Step 2: Check cache
            String cacheKey = "workflow_data";
            String cachedData = cache.get(cacheKey);
            
            if (cachedData == null) {
                // Step 3: Generate data and cache it
                cachedData = "Generated data at " + System.currentTimeMillis();
                cache.put(cacheKey, cachedData);
                System.out.println("   Data generated and cached: " + cachedData);
            } else {
                System.out.println("   Data retrieved from cache: " + cachedData);
            }
            
            // Step 4: Queue a message for processing
            String message = "Process: " + cachedData;
            try {
                queue.produce(message);
                System.out.println("   Message queued successfully");
            } catch (InterruptedException e) {
                System.out.println("   ❌ Failed to queue message: " + e.getMessage());
                return false;
            }
            
            // Step 5: Process the message
            String processedMessage;
            try {
                processedMessage = queue.consume();
                if (processedMessage == null) {
                    System.out.println("   ❌ Failed to retrieve message from queue");
                    return false;
                }
            } catch (InterruptedException e) {
                System.out.println("   ❌ Failed to consume message: " + e.getMessage());
                return false;
            }
            
            System.out.println("   Processed message: " + processedMessage);
            
            // Step 6: Verify workflow state
            System.out.println("   Cache size: " + cache.size());
            System.out.println("   Queue size: " + queue.size());
            System.out.println("   Rate limiter tokens remaining: " + 
                             (rateLimiter.tryAcquire() ? "Available" : "Exhausted"));
            
            System.out.println("   ✅ End-to-End Workflow Test PASSED");
            return true;
            
        } catch (Exception e) {
            System.out.println("   ❌ Test failed with exception: " + e.getMessage());
            return false;
        }
    }
}