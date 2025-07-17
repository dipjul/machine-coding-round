package com.machinecoding.realtime.scheduler;

import com.machinecoding.realtime.scheduler.model.*;
import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.List;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * Comprehensive demonstration of the Task Scheduler implementation.
 * Shows task scheduling, priority handling, recurring tasks, and monitoring features.
 */
public class TaskSchedulerDemo {
    
    public static void main(String[] args) throws Exception {
        System.out.println("=== Task Scheduler Demo ===\n");
        
        // Demo 1: Basic Task Scheduling
        System.out.println("=== Demo 1: Basic Task Scheduling ===");
        demonstrateBasicScheduling();
        
        // Demo 2: Priority-Based Scheduling
        System.out.println("\n=== Demo 2: Priority-Based Scheduling ===");
        demonstratePriorityScheduling();
        
        // Demo 3: Recurring Tasks
        System.out.println("\n=== Demo 3: Recurring Tasks ===");
        demonstrateRecurringTasks();
        
        // Demo 4: Task Monitoring and Events
        System.out.println("\n=== Demo 4: Task Monitoring and Events ===");
        demonstrateTaskMonitoring();
        
        // Demo 5: Error Handling and Retries
        System.out.println("\n=== Demo 5: Error Handling and Retries ===");
        demonstrateErrorHandling();
        
        System.out.println("\n=== Demo Complete ===");
    }
    
    private static void demonstrateBasicScheduling() throws InterruptedException {
        System.out.println("1. Creating task scheduler:");
        TaskScheduler scheduler = new InMemoryTaskScheduler();
        scheduler.start();
        
        System.out.println("   Scheduler started: " + scheduler.isRunning());
        
        System.out.println("\n2. Scheduling immediate tasks:");
        
        // Create some simple tasks
        Task task1 = new Task("Hello Task", "Prints hello message", 
                             () -> System.out.println("   [TASK] Hello from Task 1!"));
        
        Task task2 = new Task("Math Task", "Performs calculation", 
                             () -> {
                                 int result = 42 * 2;
                                 System.out.println("   [TASK] Math result: " + result);
                             });
        
        Task task3 = new Task("Sleep Task", "Simulates work", 
                             () -> {
                                 try {
                                     Thread.sleep(500);
                                     System.out.println("   [TASK] Work completed after sleep");
                                 } catch (InterruptedException e) {
                                     Thread.currentThread().interrupt();
                                 }
                             });
        
        // Schedule tasks
        String id1 = scheduler.scheduleNow(task1);
        String id2 = scheduler.scheduleNow(task2);
        String id3 = scheduler.scheduleNow(task3);
        
        System.out.println("   Scheduled task 1: " + id1);
        System.out.println("   Scheduled task 2: " + id2);
        System.out.println("   Scheduled task 3: " + id3);
        
        System.out.println("\n3. Scheduling delayed tasks:");
        
        Task delayedTask = new Task("Delayed Task", "Executes after delay", 
                                   () -> System.out.println("   [TASK] Delayed task executed!"));
        
        String delayedId = scheduler.scheduleAfter(delayedTask, 2, ChronoUnit.SECONDS);
        System.out.println("   Scheduled delayed task: " + delayedId);
        
        System.out.println("\n4. Scheduling tasks for specific time:");
        
        LocalDateTime futureTime = LocalDateTime.now().plusSeconds(3);
        Task timedTask = new Task("Timed Task", "Executes at specific time", 
                                 () -> System.out.println("   [TASK] Timed task executed at: " + LocalDateTime.now()));
        
        String timedId = scheduler.scheduleAt(timedTask, futureTime);
        System.out.println("   Scheduled timed task for: " + futureTime);
        
        // Wait for tasks to complete
        Thread.sleep(5000);
        
        System.out.println("\n5. Final statistics:");
        SchedulerStats stats = scheduler.getStats();
        System.out.println("   " + stats);
        
        scheduler.stop();
    }
    
    private static void demonstratePriorityScheduling() throws InterruptedException {
        System.out.println("1. Creating scheduler for priority demo:");
        TaskScheduler scheduler = new InMemoryTaskScheduler(2, 4, 60L, TimeUnit.SECONDS);
        scheduler.start();
        
        System.out.println("\n2. Creating tasks with different priorities:");
        
        Task lowPriorityTask = new Task("Low Priority", "Low priority task", 
                                       () -> {
                                           System.out.println("   [LOW] Executing low priority task");
                                           try { Thread.sleep(1000); } catch (InterruptedException e) {}
                                       }, TaskPriority.LOW);
        
        Task normalPriorityTask = new Task("Normal Priority", "Normal priority task", 
                                          () -> {
                                              System.out.println("   [NORMAL] Executing normal priority task");
                                              try { Thread.sleep(1000); } catch (InterruptedException e) {}
                                          }, TaskPriority.NORMAL);
        
        Task highPriorityTask = new Task("High Priority", "High priority task", 
                                        () -> {
                                            System.out.println("   [HIGH] Executing high priority task");
                                            try { Thread.sleep(1000); } catch (InterruptedException e) {}
                                        }, TaskPriority.HIGH);
        
        Task criticalPriorityTask = new Task("Critical Priority", "Critical priority task", 
                                            () -> {
                                                System.out.println("   [CRITICAL] Executing critical priority task");
                                                try { Thread.sleep(1000); } catch (InterruptedException e) {}
                                            }, TaskPriority.CRITICAL);
        
        System.out.println("\n3. Scheduling tasks (notice execution order):");
        
        // Schedule in reverse priority order to demonstrate priority handling
        scheduler.scheduleAfter(lowPriorityTask, 1, ChronoUnit.SECONDS);
        scheduler.scheduleAfter(normalPriorityTask, 1, ChronoUnit.SECONDS);
        scheduler.scheduleAfter(criticalPriorityTask, 1, ChronoUnit.SECONDS);
        scheduler.scheduleAfter(highPriorityTask, 1, ChronoUnit.SECONDS);
        
        System.out.println("   Tasks scheduled - watch execution order based on priority");
        
        // Wait for execution
        Thread.sleep(6000);
        
        System.out.println("\n4. Task status summary:");
        List<Task> allTasks = scheduler.getAllTasks();
        for (Task task : allTasks) {
            System.out.println("   " + task.getName() + ": " + task.getStatus() + 
                             " (Priority: " + task.getPriority() + ", Executions: " + task.getExecutionCount() + ")");
        }
        
        scheduler.stop();
    }
    
    private static void demonstrateRecurringTasks() throws InterruptedException {
        System.out.println("1. Creating scheduler for recurring tasks:");
        TaskScheduler scheduler = new InMemoryTaskScheduler();
        scheduler.start();
        
        System.out.println("\n2. Creating recurring tasks:");
        
        AtomicInteger counter1 = new AtomicInteger(0);
        RecurringTask intervalTask = new RecurringTask(
            "Interval Task", 
            "Runs every 2 seconds", 
            () -> System.out.println("   [INTERVAL] Execution #" + counter1.incrementAndGet() + 
                                   " at " + LocalDateTime.now().toString().substring(11, 19)),
            2, ChronoUnit.SECONDS
        );
        intervalTask.setMaxExecutions(5); // Limit to 5 executions
        
        AtomicInteger counter2 = new AtomicInteger(0);
        RecurringTask hourlyTask = new RecurringTask(
            "Hourly Task", 
            "Runs every hour (simulated as 3 seconds)", 
            () -> System.out.println("   [HOURLY] Execution #" + counter2.incrementAndGet() + 
                                   " at " + LocalDateTime.now().toString().substring(11, 19)),
            RecurringTask.RecurrencePattern.HOURLY
        );
        hourlyTask.setMaxExecutions(3); // Limit to 3 executions
        
        AtomicInteger counter3 = new AtomicInteger(0);
        RecurringTask dailyTask = new RecurringTask(
            "Daily Task", 
            "Runs daily (simulated as 4 seconds)", 
            () -> System.out.println("   [DAILY] Execution #" + counter3.incrementAndGet() + 
                                   " at " + LocalDateTime.now().toString().substring(11, 19)),
            RecurringTask.RecurrencePattern.DAILY
        );
        dailyTask.setMaxExecutions(2); // Limit to 2 executions
        
        System.out.println("\n3. Scheduling recurring tasks:");
        
        String intervalId = scheduler.scheduleRecurring(intervalTask);
        String hourlyId = scheduler.scheduleRecurring(hourlyTask);
        String dailyId = scheduler.scheduleRecurring(dailyTask);
        
        System.out.println("   Scheduled interval task: " + intervalId);
        System.out.println("   Scheduled hourly task: " + hourlyId);
        System.out.println("   Scheduled daily task: " + dailyId);
        
        System.out.println("\n4. Monitoring recurring task executions:");
        
        // Monitor for 15 seconds
        for (int i = 0; i < 15; i++) {
            Thread.sleep(1000);
            SchedulerStats stats = scheduler.getStats();
            System.out.println("   [" + (i + 1) + "s] " + stats);
        }
        
        System.out.println("\n5. Disabling a recurring task:");
        intervalTask.disable();
        System.out.println("   Disabled interval task");
        
        Thread.sleep(3000);
        
        System.out.println("\n6. Final recurring task status:");
        System.out.println("   Interval task: " + intervalTask.getStatus() + 
                         " (Enabled: " + intervalTask.isEnabled() + 
                         ", Executions: " + intervalTask.getExecutionCount() + ")");
        System.out.println("   Hourly task: " + hourlyTask.getStatus() + 
                         " (Enabled: " + hourlyTask.isEnabled() + 
                         ", Executions: " + hourlyTask.getExecutionCount() + ")");
        System.out.println("   Daily task: " + dailyTask.getStatus() + 
                         " (Enabled: " + dailyTask.isEnabled() + 
                         ", Executions: " + dailyTask.getExecutionCount() + ")");
        
        scheduler.stop();
    }
    
    private static void demonstrateTaskMonitoring() throws InterruptedException {
        System.out.println("1. Creating scheduler with event monitoring:");
        TaskScheduler scheduler = new InMemoryTaskScheduler();
        
        // Add event listener
        TaskScheduler.TaskExecutionListener listener = new TaskScheduler.TaskExecutionListener() {
            @Override
            public void onTaskScheduled(Task task) {
                System.out.println("   [EVENT] Task scheduled: " + task.getName());
            }
            
            @Override
            public void onTaskStarted(Task task) {
                System.out.println("   [EVENT] Task started: " + task.getName());
            }
            
            @Override
            public void onTaskCompleted(Task task) {
                System.out.println("   [EVENT] Task completed: " + task.getName() + 
                                 " (Duration: " + task.getExecutionDurationMs() + "ms)");
            }
            
            @Override
            public void onTaskFailed(Task task, String error) {
                System.out.println("   [EVENT] Task failed: " + task.getName() + " - " + error);
            }
            
            @Override
            public void onTaskCancelled(Task task) {
                System.out.println("   [EVENT] Task cancelled: " + task.getName());
            }
        };
        
        scheduler.addTaskListener(listener);
        scheduler.start();
        
        System.out.println("\n2. Scheduling tasks with monitoring:");
        
        Task quickTask = new Task("Quick Task", "Fast execution", 
                                 () -> System.out.println("   [TASK] Quick task executed"));
        
        Task slowTask = new Task("Slow Task", "Slow execution", 
                                () -> {
                                    try {
                                        Thread.sleep(2000);
                                        System.out.println("   [TASK] Slow task completed");
                                    } catch (InterruptedException e) {
                                        Thread.currentThread().interrupt();
                                    }
                                });
        
        Task cancelledTask = new Task("Cancelled Task", "Will be cancelled", 
                                     () -> System.out.println("   [TASK] This should not execute"));
        
        scheduler.scheduleNow(quickTask);
        scheduler.scheduleAfter(slowTask, 1, ChronoUnit.SECONDS);
        String cancelledId = scheduler.scheduleAfter(cancelledTask, 5, ChronoUnit.SECONDS);
        
        System.out.println("\n3. Cancelling a task:");
        Thread.sleep(2000);
        boolean cancelled = scheduler.cancelTask(cancelledId);
        System.out.println("   Task cancellation successful: " + cancelled);
        
        System.out.println("\n4. Monitoring task status:");
        Thread.sleep(2000);
        
        List<Task> pendingTasks = scheduler.getPendingTasks();
        List<Task> runningTasks = scheduler.getRunningTasks();
        List<Task> completedTasks = scheduler.getTasksByStatus(TaskStatus.COMPLETED);
        List<Task> cancelledTasks = scheduler.getTasksByStatus(TaskStatus.CANCELLED);
        
        System.out.println("   Pending tasks: " + pendingTasks.size());
        System.out.println("   Running tasks: " + runningTasks.size());
        System.out.println("   Completed tasks: " + completedTasks.size());
        System.out.println("   Cancelled tasks: " + cancelledTasks.size());
        
        scheduler.stop();
    }
    
    private static void demonstrateErrorHandling() throws InterruptedException {
        System.out.println("1. Creating scheduler for error handling demo:");
        TaskScheduler scheduler = new InMemoryTaskScheduler();
        scheduler.start();
        
        System.out.println("\n2. Creating tasks that will fail:");
        
        Task divisionByZeroTask = new Task("Division Error", "Will cause division by zero", 
                                          () -> {
                                              System.out.println("   [TASK] Attempting division by zero...");
                                              int result = 10 / 0; // This will throw ArithmeticException
                                              System.out.println("   [TASK] Result: " + result);
                                          });
        divisionByZeroTask.setMaxRetries(2);
        
        Task nullPointerTask = new Task("Null Pointer Error", "Will cause null pointer exception", 
                                       () -> {
                                           System.out.println("   [TASK] Attempting null operation...");
                                           String str = null;
                                           int length = str.length(); // This will throw NullPointerException
                                           System.out.println("   [TASK] Length: " + length);
                                       });
        nullPointerTask.setMaxRetries(1);
        
        Task successAfterRetryTask = new Task("Success After Retry", "Fails first time, succeeds on retry", 
                                             new Runnable() {
                                                 private int attempts = 0;
                                                 
                                                 @Override
                                                 public void run() {
                                                     attempts++;
                                                     System.out.println("   [TASK] Attempt #" + attempts);
                                                     if (attempts < 2) {
                                                         throw new RuntimeException("Simulated failure on attempt " + attempts);
                                                     }
                                                     System.out.println("   [TASK] Success on attempt " + attempts + "!");
                                                 }
                                             });
        successAfterRetryTask.setMaxRetries(3);
        
        System.out.println("\n3. Scheduling error-prone tasks:");
        
        scheduler.scheduleNow(divisionByZeroTask);
        scheduler.scheduleAfter(nullPointerTask, 1, ChronoUnit.SECONDS);
        scheduler.scheduleAfter(successAfterRetryTask, 2, ChronoUnit.SECONDS);
        
        System.out.println("\n4. Monitoring task execution and retries:");
        
        // Wait for tasks to complete (including retries)
        Thread.sleep(10000);
        
        System.out.println("\n5. Final task status:");
        List<Task> allTasks = scheduler.getAllTasks();
        for (Task task : allTasks) {
            System.out.println("   " + task.getName() + ":");
            System.out.println("     Status: " + task.getStatus());
            System.out.println("     Executions: " + task.getExecutionCount());
            System.out.println("     Retries: " + task.getRetryCount() + "/" + task.getMaxRetries());
            if (task.getLastError() != null) {
                System.out.println("     Last Error: " + task.getLastError());
            }
        }
        
        System.out.println("\n6. Final scheduler statistics:");
        SchedulerStats stats = scheduler.getStats();
        System.out.println("   " + stats);
        
        scheduler.stop();
    }
}