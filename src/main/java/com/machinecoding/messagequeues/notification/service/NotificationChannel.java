package com.machinecoding.messagequeues.notification.service;

import com.machinecoding.messagequeues.notification.model.Notification;
import com.machinecoding.messagequeues.notification.model.NotificationType;

/**
 * Interface for notification delivery channels.
 * Each channel handles a specific type of notification delivery.
 */
public interface NotificationChannel {
    
    /**
     * Gets the notification type this channel handles.
     * 
     * @return the notification type
     */
    NotificationType getType();
    
    /**
     * Sends a notification through this channel.
     * 
     * @param notification the notification to send
     * @return true if sent successfully, false otherwise
     */
    boolean send(Notification notification);
    
    /**
     * Checks if this channel is currently available.
     * 
     * @return true if the channel is available
     */
    boolean isAvailable();
    
    /**
     * Gets the name of this channel for logging/debugging.
     * 
     * @return channel name
     */
    String getName();
}