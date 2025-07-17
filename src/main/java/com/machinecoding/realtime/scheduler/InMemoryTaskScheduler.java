package com.machinecoding.realtime.scheduler;

import com.machinecoding.realtime.scheduler.model.*;
import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.*;
import java.util.concurrent.*;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.stream.Collectors;

/**
 * In-memory implementation of a task scheduler.
 * 
 * Features:
 * - Priority-based task execution
 * - Scheduled and recurring tasks
 * - Thread pool management
 * - Task retry mechanisms
 * - Comprehensive monitoring and statistics
 */
public class InMemoryTaskScheduler implements TaskScheduler {
    
    private final int corePoolSize;
    private final int maximumPoolSize;
    private final long keepAliveTime;
    private final TimeUnit timeUnit;
    
    // Thread management
    private ScheduledExecutorService scheduledExecutor;
    private ThreadPoolExecutor taskExecutor;
    private final AtomicBoolean running = new AtomicBoolean(false);
    private final AtomicInteger activeThreads = new AtomicInteger(0);
    private final long startTime;
    
    // Task management
    private final Map<String, Task> tasks;
    private final Map<String, ScheduledFuture<?>> scheduledFutures;
    private final PriorityQueue<Task> taskQueue;
    private final List<TaskExecutionListener> listeners;
    
    // Statistics
    private final AtomicInteger totalExecutions = new AtomicInteger(0);
    private final AtomicInteger completedExecutions = new AtomicInteger(0);
    private final AtomicInteger failedExecutions = new AtomicInteger(0);
    private volatile double totalExecutionTime = 0.0;
    
    public InMemoryTaskScheduler() {
        this(5, 10, 60L, TimeUnit.SECONDS);
    }
    
    public InMemoryTaskScheduler(int corePoolSize, int maximumPoolSize, long keepAliveTime, TimeUnit timeUnit) {
        this.corePoolSize = corePoolSize;
        this.maximumPoolSize = maximumPoolSize;
        this.keepAliveTime = keepAliveTime;
        this.timeUnit = timeUnit;
        this.startTime = System.currentTimeMillis();
        
        // Initialize collections
        this.tasks = new ConcurrentHashMap<>();
        this.scheduledFutures = new ConcurrentHashMap<>();
        this.taskQueue = new PriorityQueue<>((a, b) -> {
            // Higher priority first, then earlier scheduled time
            int priorityCompare = Integer.compare(b.getPriority().getValue(), a.getPriority().getValue());
            if (priorityCompare != 0) {
                return priorityCompare;
            }
            if (a.getNextExecutionTime() == null && b.getNextExecutionTime() == null) {
                return 0;
            }
            if (a.getNextExecutionTime() == null) {
                return 1;
            }
            if (b.getNextExecutionTime() == null) {
                return -1;
            }
            return a.getNextExecutionTime().compareTo(b.getNextExecutionTime());
        });
        this.listeners = new CopyOnWriteArrayList<>();
    }
    
    @Override
    public void start() {
        if (running.get()) {
            return;
        }
        
        running.set(true);
        
        // Create scheduled executor for timing
        scheduledExecutor = Executors.newScheduledThreadPool(2);
        
        // Create task executor with custom thread pool
        taskExecutor = new ThreadPoolExecutor(
            corePoolSize, maximumPoolSize, keepAliveTime, timeUnit,
            new LinkedBlockingQueue<>(),
            r -> {
                Thread t = new Thread(r, "TaskScheduler-Worker-" + System.currentTimeMillis());
                t.setDaemon(true);
                return t;
            }
        );
        
        // Start the scheduler loop
        scheduledExecutor.scheduleWithFixedDelay(this::processScheduledTasks, 0, 1, TimeUnit.SECONDS);
        
        System.out.println("Task scheduler started with " + corePoolSize + "-" + maximumPoolSize + " threads");
    }
    
    @Override
    public void stop() {
        if (!running.get()) {
            return;
        }
        
        running.set(false);
        
        // Cancel all scheduled futures
        scheduledFutures.values().forEach(future -> future.cancel(false));
        scheduledFutures.clear();
        
        // Shutdown executors
        if (scheduledExecutor != null) {
            scheduledExecutor.shutdown();
            try {
                if (!scheduledExecutor.awaitTermination(10, TimeUnit.SECONDS)) {
                    scheduledExecutor.shutdownNow();
                }
            } catch (InterruptedException e) {
                scheduledExecutor.shutdownNow();
                Thread.currentThread().interrupt();
            }
        }
        
        if (taskExecutor != null) {
            taskExecutor.shutdown();
            try {
                if (!taskExecutor.awaitTermination(10, TimeUnit.SECONDS)) {
                    taskExecutor.shutdownNow();
                }
            } catch (InterruptedException e) {
                taskExecutor.shutdownNow();
                Thread.currentThread().interrupt();
            }
        }
        
        System.out.println("Task scheduler stopped");
    }
    
    @Override
    public boolean isRunning() {
        return running.get();
    }
    
    @Override
    public String scheduleNow(Task task) {
        return scheduleAt(task, LocalDateTime.now());
    }
    
    @Override
    public String scheduleAt(Task task, LocalDateTime scheduledTime) {
        if (task == null || scheduledTime == null) {
            throw new IllegalArgumentException("Task and scheduled time cannot be null");
        }
        
        task.setScheduledTime(scheduledTime);
        tasks.put(task.getTaskId(), task);
        
        synchronized (taskQueue) {
            taskQueue.offer(task);
        }
        
        notifyListeners(listener -> listener.onTaskScheduled(task));
        
        return task.getTaskId();
    }
    
    @Override
    public String scheduleAfter(Task task, long delay, ChronoUnit unit) {
        LocalDateTime scheduledTime = LocalDateTime.now().plus(delay, unit);
        return scheduleAt(task, scheduledTime);
    }
    
    @Override
    public String scheduleRecurring(RecurringTask task) {
        if (task == null) {
            throw new IllegalArgumentException("Task cannot be null");
        }
        
        // Schedule the first execution
        LocalDateTime firstExecution = task.calculateNextExecution(LocalDateTime.now());
        if (firstExecution != null) {
            task.setScheduledTime(firstExecution);
            tasks.put(task.getTaskId(), task);
            
            synchronized (taskQueue) {
                taskQueue.offer(task);
            }
            
            notifyListeners(listener -> listener.onTaskScheduled(task));
        }
        
        return task.getTaskId();
    }
    
    @Override
    public boolean cancelTask(String taskId) {
        Task task = tasks.get(taskId);
        if (task == null) {
            return false;
        }
        
        // Cancel scheduled future if exists
        ScheduledFuture<?> future = scheduledFutures.remove(taskId);
        if (future != null) {
            future.cancel(false);
        }
        
        // Remove from queue
        synchronized (taskQueue) {
            taskQueue.remove(task);
        }
        
        // Mark as cancelled
        task.markAsCancelled();
        
        notifyListeners(listener -> listener.onTaskCancelled(task));
        
        return true;
    }
    
    @Override
    public Task getTask(String taskId) {
        return tasks.get(taskId);
    }
    
    @Override
    public List<Task> getAllTasks() {
        return new ArrayList<>(tasks.values());
    }
    
    @Override
    public List<Task> getTasksByStatus(TaskStatus status) {
        return tasks.values().stream()
                .filter(task -> task.getStatus() == status)
                .collect(Collectors.toList());
    }
    
    @Override
    public List<Task> getPendingTasks() {
        return getTasksByStatus(TaskStatus.SCHEDULED);
    }
    
    @Override
    public List<Task> getRunningTasks() {
        return getTasksByStatus(TaskStatus.RUNNING);
    }
    
    private void processScheduledTasks() {
        if (!running.get()) {
            return;
        }
        
        List<Task> readyTasks = new ArrayList<>();
        
        synchronized (taskQueue) {
            Iterator<Task> iterator = taskQueue.iterator();
            while (iterator.hasNext()) {
                Task task = iterator.next();
                if (task.isReadyToExecute()) {
                    readyTasks.add(task);
                    iterator.remove();
                }
            }
        }
        
        // Execute ready tasks
        for (Task task : readyTasks) {
            executeTask(task);
        }
    }
    
    private void executeTask(Task task) {
        if (!running.get()) {
            return;
        }
        
        task.markAsRunning();
        notifyListeners(listener -> listener.onTaskStarted(task));
        
        // Submit task for execution
        taskExecutor.submit(() -> {
            activeThreads.incrementAndGet();
            long startTime = System.currentTimeMillis();
            
            try {
                // Execute the task
                task.getAction().run();
                
                long duration = System.currentTimeMillis() - startTime;
                task.markAsCompleted(duration);
                
                // Update statistics
                totalExecutions.incrementAndGet();
                completedExecutions.incrementAndGet();
                updateAverageExecutionTime(duration);
                
                notifyListeners(listener -> listener.onTaskCompleted(task));
                
                // Handle recurring tasks
                if (task.isRecurring() && task instanceof RecurringTask) {
                    RecurringTask recurringTask = (RecurringTask) task;
                    recurringTask.scheduleNext();
                    
                    if (recurringTask.getStatus() != TaskStatus.CANCELLED) {
                        synchronized (taskQueue) {
                            taskQueue.offer(recurringTask);
                        }
                    }
                }
                
            } catch (Exception e) {
                long duration = System.currentTimeMillis() - startTime;
                String error = e.getMessage() != null ? e.getMessage() : e.getClass().getSimpleName();
                task.markAsFailed(error, duration);
                
                // Update statistics
                totalExecutions.incrementAndGet();
                failedExecutions.incrementAndGet();
                updateAverageExecutionTime(duration);
                
                notifyListeners(listener -> listener.onTaskFailed(task, error));
                
                // Handle retries
                if (task.canRetry()) {
                    task.resetForRetry();
                    // Reschedule after a delay
                    LocalDateTime retryTime = LocalDateTime.now().plusSeconds(30);
                    task.setNextExecutionTime(retryTime);
                    
                    synchronized (taskQueue) {
                        taskQueue.offer(task);
                    }
                }
            } finally {
                activeThreads.decrementAndGet();
            }
        });
    }
    
    private synchronized void updateAverageExecutionTime(long duration) {
        int executions = totalExecutions.get();
        if (executions == 1) {
            totalExecutionTime = duration;
        } else {
            totalExecutionTime = ((totalExecutionTime * (executions - 1)) + duration) / executions;
        }
    }
    
    private void notifyListeners(java.util.function.Consumer<TaskExecutionListener> action) {
        for (TaskExecutionListener listener : listeners) {
            try {
                action.accept(listener);
            } catch (Exception e) {
                System.err.println("Error notifying task listener: " + e.getMessage());
            }
        }
    }
    
    @Override
    public SchedulerStats getStats() {
        int totalTasks = tasks.size();
        int pendingTasks = getTasksByStatus(TaskStatus.SCHEDULED).size();
        int runningTasks = getTasksByStatus(TaskStatus.RUNNING).size();
        int completedTasks = getTasksByStatus(TaskStatus.COMPLETED).size();
        int failedTasks = getTasksByStatus(TaskStatus.FAILED).size();
        int cancelledTasks = getTasksByStatus(TaskStatus.CANCELLED).size();
        
        int recurringTasks = (int) tasks.values().stream()
                .filter(Task::isRecurring)
                .count();
        
        long uptime = System.currentTimeMillis() - startTime;
        
        return new SchedulerStats(
            totalTasks, pendingTasks, runningTasks, completedTasks, failedTasks, cancelledTasks,
            recurringTasks, totalExecutions.get(), totalExecutionTime, activeThreads.get(), uptime
        );
    }
    
    @Override
    public void addTaskListener(TaskExecutionListener listener) {
        if (listener != null) {
            listeners.add(listener);
        }
    }
    
    @Override
    public void removeTaskListener(TaskExecutionListener listener) {
        listeners.remove(listener);
    }
}