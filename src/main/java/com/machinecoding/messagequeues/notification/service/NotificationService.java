package com.machinecoding.messagequeues.notification.service;

import com.machinecoding.messagequeues.notification.model.Notification;
import com.machinecoding.messagequeues.notification.model.NotificationType;
import com.machinecoding.messagequeues.notification.model.Priority;

import java.util.*;
import java.util.concurrent.*;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.stream.Collectors;

/**
 * Main notification service that manages multiple channels and handles
 * notification delivery with retry logic and observer pattern.
 */
public class NotificationService {
    
    private final Map<NotificationType, NotificationChannel> channels;
    private final List<NotificationObserver> observers;
    private final ExecutorService executorService;
    private final ScheduledExecutorService scheduledExecutorService;
    private final PriorityBlockingQueue<NotificationTask> taskQueue;
    private final AtomicBoolean isRunning;
    private final int maxRetries;
    private final long retryDelayMs;
    
    public NotificationService() {
        this(3, 1000);
    }
    
    public NotificationService(int maxRetries, long retryDelayMs) {
        this.channels = new ConcurrentHashMap<>();
        this.observers = new CopyOnWriteArrayList<>();
        this.executorService = Executors.newFixedThreadPool(5);
        this.scheduledExecutorService = Executors.newScheduledThreadPool(2);
        this.taskQueue = new PriorityBlockingQueue<>();
        this.isRunning = new AtomicBoolean(false);
        this.maxRetries = maxRetries;
        this.retryDelayMs = retryDelayMs;
    }
    
    /**
     * Registers a notification channel.
     * 
     * @param channel the channel to register
     */
    public void registerChannel(NotificationChannel channel) {
        channels.put(channel.getType(), channel);
    }
    
    /**
     * Removes a notification channel.
     * 
     * @param type the type of channel to remove
     */
    public void unregisterChannel(NotificationType type) {
        channels.remove(type);
    }
    
    /**
     * Adds an observer to receive notification events.
     * 
     * @param observer the observer to add
     */
    public void addObserver(NotificationObserver observer) {
        observers.add(observer);
    }
    
    /**
     * Removes an observer.
     * 
     * @param observer the observer to remove
     */
    public void removeObserver(NotificationObserver observer) {
        observers.remove(observer);
    }
    
    /**
     * Sends a notification asynchronously.
     * 
     * @param notification the notification to send
     * @return CompletableFuture that completes when notification is processed
     */
    public CompletableFuture<Boolean> sendNotification(Notification notification) {
        if (!isRunning.get()) {
            start();
        }
        
        NotificationTask task = new NotificationTask(notification, 0);
        taskQueue.offer(task);
        
        return CompletableFuture.supplyAsync(() -> {
            notifyObservers(NotificationEvent.QUEUED, notification, null);
            return true;
        }, executorService);
    }
    
    /**
     * Sends multiple notifications.
     * 
     * @param notifications list of notifications to send
     * @return CompletableFuture that completes when all notifications are queued
     */
    public CompletableFuture<Void> sendNotifications(List<Notification> notifications) {
        List<CompletableFuture<Boolean>> futures = notifications.stream()
                .map(this::sendNotification)
                .collect(Collectors.toList());
        
        return CompletableFuture.allOf(futures.toArray(new CompletableFuture[0]));
    }
    
    /**
     * Starts the notification service.
     */
    public void start() {
        if (isRunning.compareAndSet(false, true)) {
            // Start worker threads to process notifications
            for (int i = 0; i < 3; i++) {
                executorService.submit(this::processNotifications);
            }
        }
    }
    
    /**
     * Stops the notification service.
     */
    public void stop() {
        isRunning.set(false);
        executorService.shutdown();
        scheduledExecutorService.shutdown();
        try {
            if (!executorService.awaitTermination(5, TimeUnit.SECONDS)) {
                executorService.shutdownNow();
            }
            if (!scheduledExecutorService.awaitTermination(5, TimeUnit.SECONDS)) {
                scheduledExecutorService.shutdownNow();
            }
        } catch (InterruptedException e) {
            executorService.shutdownNow();
            scheduledExecutorService.shutdownNow();
            Thread.currentThread().interrupt();
        }
    }
    
    /**
     * Gets the current queue size.
     * 
     * @return number of pending notifications
     */
    public int getQueueSize() {
        return taskQueue.size();
    }
    
    /**
     * Checks if a channel is registered for the given type.
     * 
     * @param type the notification type
     * @return true if channel is registered
     */
    public boolean hasChannel(NotificationType type) {
        return channels.containsKey(type);
    }
    
    private void processNotifications() {
        while (isRunning.get()) {
            try {
                NotificationTask task = taskQueue.poll(1, TimeUnit.SECONDS);
                if (task != null) {
                    processNotificationTask(task);
                }
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
                break;
            }
        }
    }
    
    private void processNotificationTask(NotificationTask task) {
        Notification notification = task.getNotification();
        NotificationChannel channel = channels.get(notification.getType());
        
        if (channel == null) {
            notifyObservers(NotificationEvent.FAILED, notification, 
                          "No channel registered for type: " + notification.getType());
            return;
        }
        
        if (!channel.isAvailable()) {
            if (task.getRetryCount() < maxRetries) {
                scheduleRetry(task);
                return;
            } else {
                notifyObservers(NotificationEvent.FAILED, notification, 
                              "Channel unavailable after " + maxRetries + " retries");
                return;
            }
        }
        
        try {
            notifyObservers(NotificationEvent.SENDING, notification, null);
            boolean success = channel.send(notification);
            
            if (success) {
                notifyObservers(NotificationEvent.SENT, notification, null);
            } else {
                if (task.getRetryCount() < maxRetries) {
                    scheduleRetry(task);
                } else {
                    notifyObservers(NotificationEvent.FAILED, notification, 
                                  "Send failed after " + maxRetries + " retries");
                }
            }
        } catch (Exception e) {
            if (task.getRetryCount() < maxRetries) {
                scheduleRetry(task);
            } else {
                notifyObservers(NotificationEvent.FAILED, notification, 
                              "Exception after " + maxRetries + " retries: " + e.getMessage());
            }
        }
    }
    
    private void scheduleRetry(NotificationTask task) {
        NotificationTask retryTask = new NotificationTask(
            task.getNotification(), 
            task.getRetryCount() + 1
        );
        
        scheduledExecutorService.schedule(() -> {
            taskQueue.offer(retryTask);
            notifyObservers(NotificationEvent.RETRY, task.getNotification(), 
                          "Retry attempt " + retryTask.getRetryCount());
        }, retryDelayMs, TimeUnit.MILLISECONDS);
    }
    
    private void notifyObservers(NotificationEvent event, Notification notification, String message) {
        for (NotificationObserver observer : observers) {
            try {
                observer.onNotificationEvent(event, notification, message);
            } catch (Exception e) {
                // Log observer exception but don't let it affect notification processing
                System.err.println("Observer exception: " + e.getMessage());
            }
        }
    }
    
    /**
     * Task wrapper for notifications with priority and retry count.
     */
    private static class NotificationTask implements Comparable<NotificationTask> {
        private final Notification notification;
        private final int retryCount;
        private final long timestamp;
        
        public NotificationTask(Notification notification, int retryCount) {
            this.notification = notification;
            this.retryCount = retryCount;
            this.timestamp = System.currentTimeMillis();
        }
        
        public Notification getNotification() { return notification; }
        public int getRetryCount() { return retryCount; }
        
        @Override
        public int compareTo(NotificationTask other) {
            // Higher priority first, then older notifications first
            int priorityCompare = Integer.compare(
                other.notification.getPriority().getLevel(),
                this.notification.getPriority().getLevel()
            );
            
            if (priorityCompare != 0) {
                return priorityCompare;
            }
            
            return Long.compare(this.timestamp, other.timestamp);
        }
    }
    
    /**
     * Observer interface for notification events.
     */
    public interface NotificationObserver {
        void onNotificationEvent(NotificationEvent event, Notification notification, String message);
    }
    
    /**
     * Notification event types.
     */
    public enum NotificationEvent {
        QUEUED, SENDING, SENT, RETRY, FAILED
    }
}