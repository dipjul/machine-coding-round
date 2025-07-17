package com.machinecoding.messagequeues.events;

import com.machinecoding.messagequeues.events.model.Event;
import com.machinecoding.messagequeues.events.model.EventPriority;

/**
 * Event fired when a new user registers in the system.
 */
public class UserRegisteredEvent extends Event {
    private final String userId;
    private final String email;
    private final String username;
    
    public UserRegisteredEvent(String userId, String email, String username) {
        super("USER_REGISTERED", "UserService");
        this.userId = userId;
        this.email = email;
        this.username = username;
    }
    
    @Override
    public Object getPayload() {
        return new UserData(userId, email, username);
    }
    
    @Override
    public EventPriority getPriority() {
        return EventPriority.HIGH; // User registration is important
    }
    
    public String getUserId() { return userId; }
    public String getEmail() { return email; }
    public String getUsername() { return username; }
    
    public static class UserData {
        private final String userId;
        private final String email;
        private final String username;
        
        public UserData(String userId, String email, String username) {
            this.userId = userId;
            this.email = email;
            this.username = username;
        }
        
        public String getUserId() { return userId; }
        public String getEmail() { return email; }
        public String getUsername() { return username; }
        
        @Override
        public String toString() {
            return String.format("UserData{userId='%s', email='%s', username='%s'}", 
                               userId, email, username);
        }
    }
}