package com.machinecoding.realtime.chat.service;

import com.machinecoding.realtime.chat.model.*;
import java.time.LocalDateTime;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.stream.Collectors;

/**
 * In-memory implementation of the chat service.
 * Thread-safe implementation supporting concurrent operations.
 */
public class InMemoryChatService implements ChatService {
    
    private final Map<String, User> users;
    private final Map<String, ChatRoom> rooms;
    private final Map<String, List<Message>> directMessages; // key: sorted userId pair
    private final List<MessageListener> messageListeners;
    private final List<UserStatusListener> userStatusListeners;
    private final AtomicInteger userIdCounter;
    private final AtomicInteger roomIdCounter;
    
    public InMemoryChatService() {
        this.users = new ConcurrentHashMap<>();
        this.rooms = new ConcurrentHashMap<>();
        this.directMessages = new ConcurrentHashMap<>();
        this.messageListeners = new CopyOnWriteArrayList<>();
        this.userStatusListeners = new CopyOnWriteArrayList<>();
        this.userIdCounter = new AtomicInteger(1);
        this.roomIdCounter = new AtomicInteger(1);
    }
    
    @Override
    public User registerUser(String username, String email) {
        if (username == null || username.trim().isEmpty()) {
            throw new IllegalArgumentException("Username cannot be null or empty");
        }
        if (email == null || email.trim().isEmpty()) {
            throw new IllegalArgumentException("Email cannot be null or empty");
        }
        
        // Check if username already exists
        boolean usernameExists = users.values().stream()
                .anyMatch(user -> user.getUsername().equalsIgnoreCase(username));
        if (usernameExists) {
            throw new IllegalArgumentException("Username already exists: " + username);
        }
        
        String userId = "user_" + userIdCounter.getAndIncrement();
        User user = new User(userId, username, email);
        users.put(userId, user);
        
        return user;
    }
    
    @Override
    public User getUser(String userId) {
        return users.get(userId);
    }
    
    @Override
    public boolean updateUserStatus(String userId, UserStatus status) {
        User user = users.get(userId);
        if (user == null || status == null) {
            return false;
        }
        
        UserStatus oldStatus = user.getStatus();
        switch (status) {
            case ONLINE:
                user.setOnline();
                break;
            case OFFLINE:
                user.setOffline();
                break;
            case AWAY:
                user.setAway();
                break;
            default:
                return false;
        }
        
        // Notify listeners
        notifyUserStatusChanged(userId, oldStatus, status);
        return true;
    }
    
    @Override
    public List<User> getOnlineUsers() {
        return users.values().stream()
                .filter(User::isOnline)
                .collect(Collectors.toList());
    }
    
    @Override
    public List<User> getAllUsers() {
        return new ArrayList<>(users.values());
    }
    
    @Override
    public ChatRoom createRoom(String roomName, String creatorId) {
        return createPrivateRoom(roomName, "", creatorId, 100);
    }
    
    @Override
    public ChatRoom createPrivateRoom(String roomName, String description, String creatorId, int maxMembers) {
        if (roomName == null || roomName.trim().isEmpty()) {
            throw new IllegalArgumentException("Room name cannot be null or empty");
        }
        if (!users.containsKey(creatorId)) {
            throw new IllegalArgumentException("Creator user does not exist: " + creatorId);
        }
        
        String roomId = "room_" + roomIdCounter.getAndIncrement();
        ChatRoom room = new ChatRoom(roomId, roomName, description, creatorId, false, maxMembers);
        rooms.put(roomId, room);
        
        return room;
    }
    
    @Override
    public ChatRoom getRoom(String roomId) {
        return rooms.get(roomId);
    }
    
    @Override
    public List<ChatRoom> getUserRooms(String userId) {
        return rooms.values().stream()
                .filter(room -> room.isMember(userId))
                .collect(Collectors.toList());
    }
    
    @Override
    public List<ChatRoom> getPublicRooms() {
        return rooms.values().stream()
                .filter(room -> !room.isPrivate())
                .collect(Collectors.toList());
    }
    
    @Override
    public boolean joinRoom(String roomId, String userId) {
        ChatRoom room = rooms.get(roomId);
        User user = users.get(userId);
        
        if (room == null || user == null) {
            return false;
        }
        
        boolean joined = room.addMember(userId);
        if (joined) {
            notifyUserJoinedRoom(userId, roomId);
        }
        return joined;
    }
    
    @Override
    public boolean leaveRoom(String roomId, String userId) {
        ChatRoom room = rooms.get(roomId);
        if (room == null) {
            return false;
        }
        
        boolean left = room.removeMember(userId);
        if (left) {
            notifyUserLeftRoom(userId, roomId);
        }
        return left;
    }
    
    @Override
    public boolean deleteRoom(String roomId, String requesterId) {
        ChatRoom room = rooms.get(roomId);
        if (room == null || !room.getCreatedBy().equals(requesterId)) {
            return false;
        }
        
        rooms.remove(roomId);
        return true;
    }
    
    @Override
    public Message sendMessage(String senderId, String content, String roomId) {
        ChatRoom room = rooms.get(roomId);
        User sender = users.get(senderId);
        
        if (room == null || sender == null || !room.isMember(senderId)) {
            return null;
        }
        
        Message message = new Message(senderId, content, roomId);
        if (room.addMessage(message)) {
            // Update sender's last seen
            sender.updateLastSeen();
            
            // Notify listeners
            notifyMessageSent(message);
            return message;
        }
        
        return null;
    }
    
    @Override
    public Message sendDirectMessage(String senderId, String content, String recipientId) {
        User sender = users.get(senderId);
        User recipient = users.get(recipientId);
        
        if (sender == null || recipient == null) {
            return null;
        }
        
        Message message = new Message(senderId, content, recipientId, true);
        
        // Store in direct messages map
        String key = getDirectMessageKey(senderId, recipientId);
        directMessages.computeIfAbsent(key, k -> new CopyOnWriteArrayList<>()).add(message);
        
        // Update sender's last seen
        sender.updateLastSeen();
        
        // Notify listeners
        notifyMessageSent(message);
        return message;
    }
    
    @Override
    public List<Message> getRoomMessages(String roomId, int count) {
        ChatRoom room = rooms.get(roomId);
        if (room == null) {
            return new ArrayList<>();
        }
        
        return room.getRecentMessages(count);
    }
    
    @Override
    public List<Message> getDirectMessages(String userId1, String userId2, int count) {
        String key = getDirectMessageKey(userId1, userId2);
        List<Message> messages = directMessages.get(key);
        
        if (messages == null || messages.isEmpty()) {
            return new ArrayList<>();
        }
        
        if (count <= 0) {
            return new ArrayList<>(messages);
        }
        
        int size = messages.size();
        int fromIndex = Math.max(0, size - count);
        return new ArrayList<>(messages.subList(fromIndex, size));
    }
    
    @Override
    public List<Message> getMessagesSince(String roomId, LocalDateTime since) {
        ChatRoom room = rooms.get(roomId);
        if (room == null) {
            return new ArrayList<>();
        }
        
        return room.getMessagesSince(since);
    }
    
    @Override
    public boolean markMessageAsRead(String messageId, String userId) {
        // Find message in rooms
        for (ChatRoom room : rooms.values()) {
            if (room.isMember(userId)) {
                for (Message message : room.getMessages()) {
                    if (message.getMessageId().equals(messageId)) {
                        message.markAsRead();
                        room.updateLastRead(userId);
                        notifyMessageRead(message);
                        return true;
                    }
                }
            }
        }
        
        // Find message in direct messages
        for (List<Message> messages : directMessages.values()) {
            for (Message message : messages) {
                if (message.getMessageId().equals(messageId) && 
                    (message.getSenderId().equals(userId) || message.getRecipientId().equals(userId))) {
                    message.markAsRead();
                    notifyMessageRead(message);
                    return true;
                }
            }
        }
        
        return false;
    }
    
    @Override
    public void addMessageListener(MessageListener listener) {
        if (listener != null) {
            messageListeners.add(listener);
        }
    }
    
    @Override
    public void removeMessageListener(MessageListener listener) {
        messageListeners.remove(listener);
    }
    
    @Override
    public void addUserStatusListener(UserStatusListener listener) {
        if (listener != null) {
            userStatusListeners.add(listener);
        }
    }
    
    @Override
    public void removeUserStatusListener(UserStatusListener listener) {
        userStatusListeners.remove(listener);
    }
    
    @Override
    public ChatStats getStats() {
        int totalUsers = users.size();
        int onlineUsers = (int) users.values().stream().filter(User::isOnline).count();
        int totalRooms = rooms.size();
        
        int totalMessages = rooms.values().stream()
                .mapToInt(room -> room.getMessages().size())
                .sum();
        totalMessages += directMessages.values().stream()
                .mapToInt(List::size)
                .sum();
        
        LocalDateTime oneHourAgo = LocalDateTime.now().minusHours(1);
        int messagesLastHour = rooms.values().stream()
                .mapToInt(room -> room.getMessagesSince(oneHourAgo).size())
                .sum();
        
        double averageRoomSize = rooms.isEmpty() ? 0.0 : 
                rooms.values().stream()
                        .mapToInt(ChatRoom::getMemberCount)
                        .average()
                        .orElse(0.0);
        
        int activeRooms = (int) rooms.values().stream()
                .filter(room -> room.getMemberCount() > 1)
                .count();
        
        return new ChatStats(totalUsers, onlineUsers, totalRooms, totalMessages,
                           messagesLastHour, averageRoomSize, activeRooms);
    }
    
    // Helper methods
    private String getDirectMessageKey(String userId1, String userId2) {
        // Create consistent key regardless of order
        return userId1.compareTo(userId2) < 0 ? 
                userId1 + ":" + userId2 : userId2 + ":" + userId1;
    }
    
    private void notifyMessageSent(Message message) {
        for (MessageListener listener : messageListeners) {
            try {
                listener.onMessageSent(message);
            } catch (Exception e) {
                System.err.println("Error notifying message listener: " + e.getMessage());
            }
        }
    }
    
    private void notifyMessageRead(Message message) {
        for (MessageListener listener : messageListeners) {
            try {
                listener.onMessageRead(message);
            } catch (Exception e) {
                System.err.println("Error notifying message listener: " + e.getMessage());
            }
        }
    }
    
    private void notifyUserStatusChanged(String userId, UserStatus oldStatus, UserStatus newStatus) {
        for (UserStatusListener listener : userStatusListeners) {
            try {
                listener.onUserStatusChanged(userId, oldStatus, newStatus);
            } catch (Exception e) {
                System.err.println("Error notifying user status listener: " + e.getMessage());
            }
        }
    }
    
    private void notifyUserJoinedRoom(String userId, String roomId) {
        for (UserStatusListener listener : userStatusListeners) {
            try {
                listener.onUserJoinedRoom(userId, roomId);
            } catch (Exception e) {
                System.err.println("Error notifying user status listener: " + e.getMessage());
            }
        }
    }
    
    private void notifyUserLeftRoom(String userId, String roomId) {
        for (UserStatusListener listener : userStatusListeners) {
            try {
                listener.onUserLeftRoom(userId, roomId);
            } catch (Exception e) {
                System.err.println("Error notifying user status listener: " + e.getMessage());
            }
        }
    }
}