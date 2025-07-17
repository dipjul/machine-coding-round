package com.machinecoding.realtime.chat.service;

/**
 * Statistics for the chat service.
 */
public class ChatStats {
    private final int totalUsers;
    private final int onlineUsers;
    private final int totalRooms;
    private final int totalMessages;
    private final int messagesLastHour;
    private final double averageRoomSize;
    private final int activeRooms;
    
    public ChatStats(int totalUsers, int onlineUsers, int totalRooms, int totalMessages,
                    int messagesLastHour, double averageRoomSize, int activeRooms) {
        this.totalUsers = totalUsers;
        this.onlineUsers = onlineUsers;
        this.totalRooms = totalRooms;
        this.totalMessages = totalMessages;
        this.messagesLastHour = messagesLastHour;
        this.averageRoomSize = averageRoomSize;
        this.activeRooms = activeRooms;
    }
    
    // Getters
    public int getTotalUsers() { return totalUsers; }
    public int getOnlineUsers() { return onlineUsers; }
    public int getTotalRooms() { return totalRooms; }
    public int getTotalMessages() { return totalMessages; }
    public int getMessagesLastHour() { return messagesLastHour; }
    public double getAverageRoomSize() { return averageRoomSize; }
    public int getActiveRooms() { return activeRooms; }
    
    @Override
    public String toString() {
        return String.format(
            "ChatStats{users=%d/%d, rooms=%d (%d active), messages=%d (%d last hour), avgRoomSize=%.1f}",
            onlineUsers, totalUsers, totalRooms, activeRooms, totalMessages, messagesLastHour, averageRoomSize
        );
    }
}