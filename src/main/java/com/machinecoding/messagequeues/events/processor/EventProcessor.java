package com.machinecoding.messagequeues.events.processor;

import com.machinecoding.messagequeues.events.model.Event;
import com.machinecoding.messagequeues.events.model.EventPriority;

import java.util.*;
import java.util.concurrent.*;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicLong;
import java.util.function.Predicate;
import java.util.stream.Collectors;

/**
 * Core event processing system that manages event handlers, filtering,
 * routing, and asynchronous processing with thread pools.
 */
public class EventProcessor {
    
    private final Map<String, List<EventHandler>> handlers;
    private final List<EventFilter> globalFilters;
    private final PriorityBlockingQueue<EventTask> eventQueue;
    private final ExecutorService processingExecutor;
    private final ExecutorService handlerExecutor;
    private final AtomicBoolean isRunning;
    private final AtomicLong processedEventCount;
    private final AtomicLong failedEventCount;
    private final int maxRetries;
    private final long retryDelayMs;
    
    public EventProcessor() {
        this(4, 8, 3, 1000);
    }
    
    public EventProcessor(int processingThreads, int handlerThreads, int maxRetries, long retryDelayMs) {
        this.handlers = new ConcurrentHashMap<>();
        this.globalFilters = new CopyOnWriteArrayList<>();
        this.eventQueue = new PriorityBlockingQueue<>();
        this.processingExecutor = Executors.newFixedThreadPool(processingThreads);
        this.handlerExecutor = Executors.newFixedThreadPool(handlerThreads);
        this.isRunning = new AtomicBoolean(false);
        this.processedEventCount = new AtomicLong(0);
        this.failedEventCount = new AtomicLong(0);
        this.maxRetries = maxRetries;
        this.retryDelayMs = retryDelayMs;
    }
    
    /**
     * Registers an event handler for a specific event type.
     */
    public void registerHandler(String eventType, EventHandler handler) {
        handlers.computeIfAbsent(eventType, k -> new CopyOnWriteArrayList<>()).add(handler);
    }
    
    /**
     * Unregisters an event handler.
     */
    public void unregisterHandler(String eventType, EventHandler handler) {
        List<EventHandler> eventHandlers = handlers.get(eventType);
        if (eventHandlers != null) {
            eventHandlers.remove(handler);
            if (eventHandlers.isEmpty()) {
                handlers.remove(eventType);
            }
        }
    }
    
    /**
     * Adds a global filter that applies to all events.
     */
    public void addGlobalFilter(EventFilter filter) {
        globalFilters.add(filter);
    }
    
    /**
     * Removes a global filter.
     */
    public void removeGlobalFilter(EventFilter filter) {
        globalFilters.remove(filter);
    }
    
    /**
     * Publishes an event for processing.
     */
    public CompletableFuture<Void> publishEvent(Event event) {
        if (!isRunning.get()) {
            start();
        }
        
        // Apply global filters
        for (EventFilter filter : globalFilters) {
            if (!filter.shouldProcess(event)) {
                return CompletableFuture.completedFuture(null);
            }
        }
        
        EventTask task = new EventTask(event, 0);
        eventQueue.offer(task);
        
        return CompletableFuture.runAsync(() -> {
            // Event queued successfully
        }, processingExecutor);
    }
    
    /**
     * Publishes multiple events.
     */
    public CompletableFuture<Void> publishEvents(List<Event> events) {
        List<CompletableFuture<Void>> futures = events.stream()
                .map(this::publishEvent)
                .collect(Collectors.toList());
        
        return CompletableFuture.allOf(futures.toArray(new CompletableFuture[0]));
    }
    
    /**
     * Starts the event processor.
     */
    public void start() {
        if (isRunning.compareAndSet(false, true)) {
            // Start processing threads
            for (int i = 0; i < ((ThreadPoolExecutor) processingExecutor).getCorePoolSize(); i++) {
                processingExecutor.submit(this::processEvents);
            }
        }
    }
    
    /**
     * Stops the event processor.
     */
    public void stop() {
        isRunning.set(false);
        processingExecutor.shutdown();
        handlerExecutor.shutdown();
        
        try {
            if (!processingExecutor.awaitTermination(5, TimeUnit.SECONDS)) {
                processingExecutor.shutdownNow();
            }
            if (!handlerExecutor.awaitTermination(5, TimeUnit.SECONDS)) {
                handlerExecutor.shutdownNow();
            }
        } catch (InterruptedException e) {
            processingExecutor.shutdownNow();
            handlerExecutor.shutdownNow();
            Thread.currentThread().interrupt();
        }
    }
    
    /**
     * Gets the current queue size.
     */
    public int getQueueSize() {
        return eventQueue.size();
    }
    
    /**
     * Gets processing statistics.
     */
    public ProcessingStats getStats() {
        return new ProcessingStats(
            processedEventCount.get(),
            failedEventCount.get(),
            eventQueue.size(),
            handlers.size()
        );
    }
    
    /**
     * Gets all registered event types.
     */
    public Set<String> getRegisteredEventTypes() {
        return new HashSet<>(handlers.keySet());
    }
    
    private void processEvents() {
        while (isRunning.get()) {
            try {
                EventTask task = eventQueue.poll(1, TimeUnit.SECONDS);
                if (task != null) {
                    processEventTask(task);
                }
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
                break;
            }
        }
    }
    
    private void processEventTask(EventTask task) {
        Event event = task.getEvent();
        List<EventHandler> eventHandlers = handlers.get(event.getEventType());
        
        if (eventHandlers == null || eventHandlers.isEmpty()) {
            // No handlers registered for this event type
            return;
        }
        
        List<CompletableFuture<Void>> handlerFutures = new ArrayList<>();
        
        for (EventHandler handler : eventHandlers) {
            CompletableFuture<Void> handlerFuture = CompletableFuture.runAsync(() -> {
                try {
                    handler.handle(event);
                } catch (Exception e) {
                    System.err.println("Handler failed for event " + event.getEventId() + ": " + e.getMessage());
                    throw new RuntimeException(e);
                }
            }, handlerExecutor);
            
            handlerFutures.add(handlerFuture);
        }
        
        // Wait for all handlers to complete
        CompletableFuture<Void> allHandlers = CompletableFuture.allOf(
            handlerFutures.toArray(new CompletableFuture[0])
        );
        
        try {
            allHandlers.get(30, TimeUnit.SECONDS); // Timeout after 30 seconds
            processedEventCount.incrementAndGet();
        } catch (Exception e) {
            failedEventCount.incrementAndGet();
            
            // Retry logic
            if (task.getRetryCount() < maxRetries) {
                scheduleRetry(task);
            } else {
                System.err.println("Event processing failed after " + maxRetries + 
                                 " retries: " + event.getEventId());
            }
        }
    }
    
    private void scheduleRetry(EventTask task) {
        EventTask retryTask = new EventTask(task.getEvent(), task.getRetryCount() + 1);
        
        CompletableFuture.delayedExecutor(retryDelayMs, TimeUnit.MILLISECONDS)
                .execute(() -> eventQueue.offer(retryTask));
    }
    
    /**
     * Event handler interface.
     */
    public interface EventHandler {
        void handle(Event event) throws Exception;
        
        default String getName() {
            return this.getClass().getSimpleName();
        }
    }
    
    /**
     * Event filter interface for filtering events before processing.
     */
    public interface EventFilter {
        boolean shouldProcess(Event event);
        
        default String getName() {
            return this.getClass().getSimpleName();
        }
    }
    
    /**
     * Task wrapper for events with priority and retry count.
     */
    private static class EventTask implements Comparable<EventTask> {
        private final Event event;
        private final int retryCount;
        private final long timestamp;
        
        public EventTask(Event event, int retryCount) {
            this.event = event;
            this.retryCount = retryCount;
            this.timestamp = System.currentTimeMillis();
        }
        
        public Event getEvent() { return event; }
        public int getRetryCount() { return retryCount; }
        
        @Override
        public int compareTo(EventTask other) {
            // Higher priority first, then older events first
            int priorityCompare = Integer.compare(
                other.event.getPriority().getLevel(),
                this.event.getPriority().getLevel()
            );
            
            if (priorityCompare != 0) {
                return priorityCompare;
            }
            
            return Long.compare(this.timestamp, other.timestamp);
        }
    }
    
    /**
     * Processing statistics.
     */
    public static class ProcessingStats {
        private final long processedEvents;
        private final long failedEvents;
        private final int queueSize;
        private final int handlerCount;
        
        public ProcessingStats(long processedEvents, long failedEvents, int queueSize, int handlerCount) {
            this.processedEvents = processedEvents;
            this.failedEvents = failedEvents;
            this.queueSize = queueSize;
            this.handlerCount = handlerCount;
        }
        
        public long getProcessedEvents() { return processedEvents; }
        public long getFailedEvents() { return failedEvents; }
        public int getQueueSize() { return queueSize; }
        public int getHandlerCount() { return handlerCount; }
        public double getSuccessRate() { 
            long total = processedEvents + failedEvents;
            return total == 0 ? 0.0 : (double) processedEvents / total * 100;
        }
        
        @Override
        public String toString() {
            return String.format("ProcessingStats{processed=%d, failed=%d, queued=%d, handlers=%d, successRate=%.1f%%}",
                               processedEvents, failedEvents, queueSize, handlerCount, getSuccessRate());
        }
    }
}