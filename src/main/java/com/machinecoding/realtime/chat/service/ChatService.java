package com.machinecoding.realtime.chat.service;

import com.machinecoding.realtime.chat.model.*;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Set;

/**
 * Interface for chat service operations.
 */
public interface ChatService {
    
    // User management
    User registerUser(String username, String email);
    User getUser(String userId);
    boolean updateUserStatus(String userId, UserStatus status);
    List<User> getOnlineUsers();
    List<User> getAllUsers();
    
    // Room management
    ChatRoom createRoom(String roomName, String creatorId);
    ChatRoom createPrivateRoom(String roomName, String description, String creatorId, int maxMembers);
    ChatRoom getRoom(String roomId);
    List<ChatRoom> getUserRooms(String userId);
    List<ChatRoom> getPublicRooms();
    boolean joinRoom(String roomId, String userId);
    boolean leaveRoom(String roomId, String userId);
    boolean deleteRoom(String roomId, String requesterId);
    
    // Message management
    Message sendMessage(String senderId, String content, String roomId);
    Message sendDirectMessage(String senderId, String content, String recipientId);
    List<Message> getRoomMessages(String roomId, int count);
    List<Message> getDirectMessages(String userId1, String userId2, int count);
    List<Message> getMessagesSince(String roomId, LocalDateTime since);
    boolean markMessageAsRead(String messageId, String userId);
    
    // Notification and presence
    void addMessageListener(MessageListener listener);
    void removeMessageListener(MessageListener listener);
    void addUserStatusListener(UserStatusListener listener);
    void removeUserStatusListener(UserStatusListener listener);
    
    // Statistics
    ChatStats getStats();
    
    /**
     * Interface for message event listeners.
     */
    interface MessageListener {
        void onMessageSent(Message message);
        void onMessageDelivered(Message message);
        void onMessageRead(Message message);
    }
    
    /**
     * Interface for user status event listeners.
     */
    interface UserStatusListener {
        void onUserStatusChanged(String userId, UserStatus oldStatus, UserStatus newStatus);
        void onUserJoinedRoom(String userId, String roomId);
        void onUserLeftRoom(String userId, String roomId);
    }
}