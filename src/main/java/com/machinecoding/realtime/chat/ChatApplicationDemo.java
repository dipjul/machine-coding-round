package com.machinecoding.realtime.chat;

import com.machinecoding.realtime.chat.model.*;
import com.machinecoding.realtime.chat.service.*;
import java.util.List;
import java.util.concurrent.*;

/**
 * Comprehensive demonstration of the Chat Application.
 * Shows user management, room operations, messaging, and real-time features.
 */
public class ChatApplicationDemo {
    
    public static void main(String[] args) throws Exception {
        System.out.println("=== Chat Application Demo ===\n");
        
        // Demo 1: User Registration and Management
        System.out.println("=== Demo 1: User Registration and Management ===");
        demonstrateUserManagement();
        
        // Demo 2: Room Creation and Management
        System.out.println("\n=== Demo 2: Room Creation and Management ===");
        demonstrateRoomManagement();
        
        // Demo 3: Messaging and Communication
        System.out.println("\n=== Demo 3: Messaging and Communication ===");
        demonstrateMessaging();
        
        // Demo 4: Real-time Features and Listeners
        System.out.println("\n=== Demo 4: Real-time Features and Listeners ===");
        demonstrateRealTimeFeatures();
        
        // Demo 5: Concurrent Chat Simulation
        System.out.println("\n=== Demo 5: Concurrent Chat Simulation ===");
        demonstrateConcurrentChat();
        
        System.out.println("\n=== Demo Complete ===");
    }
    
    private static void demonstrateUserManagement() {
        ChatService chatService = new InMemoryChatService();
        
        System.out.println("1. Registering users:");
        User alice = chatService.registerUser("Alice", "alice@example.com");
        User bob = chatService.registerUser("Bob", "bob@example.com");
        User charlie = chatService.registerUser("Charlie", "charlie@example.com");
        
        System.out.println("   Registered: " + alice);
        System.out.println("   Registered: " + bob);
        System.out.println("   Registered: " + charlie);
        
        System.out.println("\n2. User status management:");
        chatService.updateUserStatus(alice.getUserId(), UserStatus.ONLINE);
        chatService.updateUserStatus(bob.getUserId(), UserStatus.ONLINE);
        chatService.updateUserStatus(charlie.getUserId(), UserStatus.AWAY);
        
        System.out.println("   Alice status: " + chatService.getUser(alice.getUserId()).getStatus());
        System.out.println("   Bob status: " + chatService.getUser(bob.getUserId()).getStatus());
        System.out.println("   Charlie status: " + chatService.getUser(charlie.getUserId()).getStatus());
        
        System.out.println("\n3. Online users:");
        List<User> onlineUsers = chatService.getOnlineUsers();
        System.out.println("   Online users count: " + onlineUsers.size());
        onlineUsers.forEach(user -> System.out.println("   - " + user.getUsername()));
        
        System.out.println("\n4. All users:");
        List<User> allUsers = chatService.getAllUsers();
        System.out.println("   Total users: " + allUsers.size());
        allUsers.forEach(user -> System.out.println("   - " + user.getUsername() + " (" + user.getStatus() + ")"));
    }
    
    private static void demonstrateRoomManagement() {
        ChatService chatService = new InMemoryChatService();
        
        // Register users first
        User alice = chatService.registerUser("Alice", "alice@example.com");
        User bob = chatService.registerUser("Bob", "bob@example.com");
        User charlie = chatService.registerUser("Charlie", "charlie@example.com");
        
        System.out.println("1. Creating chat rooms:");
        ChatRoom generalRoom = chatService.createRoom("General", alice.getUserId());
        ChatRoom techRoom = chatService.createPrivateRoom("Tech Talk", "Discuss technology topics", 
                                                          bob.getUserId(), 50);
        
        System.out.println("   Created room: " + generalRoom);
        System.out.println("   Created room: " + techRoom);
        
        System.out.println("\n2. Users joining rooms:");
        boolean bobJoined = chatService.joinRoom(generalRoom.getRoomId(), bob.getUserId());
        boolean charlieJoined = chatService.joinRoom(generalRoom.getRoomId(), charlie.getUserId());
        boolean aliceJoinedTech = chatService.joinRoom(techRoom.getRoomId(), alice.getUserId());
        
        System.out.println("   Bob joined General: " + bobJoined);
        System.out.println("   Charlie joined General: " + charlieJoined);
        System.out.println("   Alice joined Tech Talk: " + aliceJoinedTech);
        
        System.out.println("\n3. Room member information:");
        ChatRoom updatedGeneral = chatService.getRoom(generalRoom.getRoomId());
        System.out.println("   General room members: " + updatedGeneral.getMemberCount());
        System.out.println("   General room admins: " + updatedGeneral.getAdmins().size());
        
        System.out.println("\n4. User's rooms:");
        List<ChatRoom> aliceRooms = chatService.getUserRooms(alice.getUserId());
        System.out.println("   Alice's rooms: " + aliceRooms.size());
        aliceRooms.forEach(room -> System.out.println("   - " + room.getRoomName()));
        
        System.out.println("\n5. Public rooms:");
        List<ChatRoom> publicRooms = chatService.getPublicRooms();
        System.out.println("   Public rooms: " + publicRooms.size());
        publicRooms.forEach(room -> System.out.println("   - " + room.getRoomName() + 
                                                      " (" + room.getMemberCount() + " members)"));
    }
    
    private static void demonstrateMessaging() {
        ChatService chatService = new InMemoryChatService();
        
        // Setup users and rooms
        User alice = chatService.registerUser("Alice", "alice@example.com");
        User bob = chatService.registerUser("Bob", "bob@example.com");
        User charlie = chatService.registerUser("Charlie", "charlie@example.com");
        
        ChatRoom generalRoom = chatService.createRoom("General", alice.getUserId());
        chatService.joinRoom(generalRoom.getRoomId(), bob.getUserId());
        chatService.joinRoom(generalRoom.getRoomId(), charlie.getUserId());
        
        System.out.println("1. Sending room messages:");
        Message msg1 = chatService.sendMessage(alice.getUserId(), "Hello everyone!", generalRoom.getRoomId());
        Message msg2 = chatService.sendMessage(bob.getUserId(), "Hi Alice! How are you?", generalRoom.getRoomId());
        Message msg3 = chatService.sendMessage(charlie.getUserId(), "Good morning all!", generalRoom.getRoomId());
        
        System.out.println("   Alice: " + msg1.getContent());
        System.out.println("   Bob: " + msg2.getContent());
        System.out.println("   Charlie: " + msg3.getContent());
        
        System.out.println("\n2. Retrieving room messages:");
        List<Message> roomMessages = chatService.getRoomMessages(generalRoom.getRoomId(), 10);
        System.out.println("   Room messages count: " + roomMessages.size());
        roomMessages.forEach(msg -> {
            String sender;
            if (msg.isSystemMessage()) {
                sender = "SYSTEM";
            } else {
                User senderUser = chatService.getUser(msg.getSenderId());
                sender = senderUser != null ? senderUser.getUsername() : "Unknown";
            }
            System.out.println("   [" + msg.getTimestamp().toString().substring(11, 19) + "] " + 
                             sender + ": " + msg.getContent());
        });
        
        System.out.println("\n3. Direct messaging:");
        Message directMsg1 = chatService.sendDirectMessage(alice.getUserId(), 
                                                          "Hey Bob, can we talk privately?", bob.getUserId());
        Message directMsg2 = chatService.sendDirectMessage(bob.getUserId(), 
                                                          "Sure Alice, what's up?", alice.getUserId());
        
        System.out.println("   Alice to Bob: " + directMsg1.getContent());
        System.out.println("   Bob to Alice: " + directMsg2.getContent());
        
        System.out.println("\n4. Retrieving direct messages:");
        List<Message> directMessages = chatService.getDirectMessages(alice.getUserId(), bob.getUserId(), 10);
        System.out.println("   Direct messages count: " + directMessages.size());
        directMessages.forEach(msg -> {
            String sender = chatService.getUser(msg.getSenderId()).getUsername();
            String recipient = chatService.getUser(msg.getRecipientId()).getUsername();
            System.out.println("   [" + msg.getTimestamp().toString().substring(11, 19) + "] " + 
                             sender + " -> " + recipient + ": " + msg.getContent());
        });
        
        System.out.println("\n5. Message status management:");
        boolean marked = chatService.markMessageAsRead(msg1.getMessageId(), bob.getUserId());
        System.out.println("   Bob marked Alice's message as read: " + marked);
        System.out.println("   Message status: " + msg1.getStatus());
    }
    
    private static void demonstrateRealTimeFeatures() {
        ChatService chatService = new InMemoryChatService();
        
        // Setup users and room
        User alice = chatService.registerUser("Alice", "alice@example.com");
        User bob = chatService.registerUser("Bob", "bob@example.com");
        ChatRoom room = chatService.createRoom("Test Room", alice.getUserId());
        chatService.joinRoom(room.getRoomId(), bob.getUserId());
        
        System.out.println("1. Setting up real-time listeners:");
        
        // Message listener
        ChatService.MessageListener messageListener = new ChatService.MessageListener() {
            @Override
            public void onMessageSent(Message message) {
                String sender = chatService.getUser(message.getSenderId()).getUsername();
                System.out.println("   [MESSAGE SENT] " + sender + ": " + message.getContent());
            }
            
            @Override
            public void onMessageDelivered(Message message) {
                System.out.println("   [MESSAGE DELIVERED] " + message.getMessageId());
            }
            
            @Override
            public void onMessageRead(Message message) {
                System.out.println("   [MESSAGE READ] " + message.getMessageId());
            }
        };
        
        // User status listener
        ChatService.UserStatusListener statusListener = new ChatService.UserStatusListener() {
            @Override
            public void onUserStatusChanged(String userId, UserStatus oldStatus, UserStatus newStatus) {
                String username = chatService.getUser(userId).getUsername();
                System.out.println("   [STATUS CHANGE] " + username + ": " + oldStatus + " -> " + newStatus);
            }
            
            @Override
            public void onUserJoinedRoom(String userId, String roomId) {
                String username = chatService.getUser(userId).getUsername();
                String roomName = chatService.getRoom(roomId).getRoomName();
                System.out.println("   [USER JOINED] " + username + " joined " + roomName);
            }
            
            @Override
            public void onUserLeftRoom(String userId, String roomId) {
                String username = chatService.getUser(userId).getUsername();
                String roomName = chatService.getRoom(roomId).getRoomName();
                System.out.println("   [USER LEFT] " + username + " left " + roomName);
            }
        };
        
        chatService.addMessageListener(messageListener);
        chatService.addUserStatusListener(statusListener);
        
        System.out.println("\n2. Triggering real-time events:");
        
        // Status changes
        chatService.updateUserStatus(alice.getUserId(), UserStatus.ONLINE);
        chatService.updateUserStatus(bob.getUserId(), UserStatus.AWAY);
        
        // Messages
        chatService.sendMessage(alice.getUserId(), "Testing real-time messaging!", room.getRoomId());
        chatService.sendMessage(bob.getUserId(), "I can see your message in real-time!", room.getRoomId());
        
        // Room operations
        User charlie = chatService.registerUser("Charlie", "charlie@example.com");
        chatService.joinRoom(room.getRoomId(), charlie.getUserId());
        chatService.leaveRoom(room.getRoomId(), charlie.getUserId());
        
        System.out.println("\n3. Chat statistics:");
        ChatStats stats = chatService.getStats();
        System.out.println("   " + stats);
    }
    
    private static void demonstrateConcurrentChat() throws Exception {
        ChatService chatService = new InMemoryChatService();
        
        System.out.println("1. Setting up concurrent chat simulation:");
        
        // Create users
        User[] users = new User[10];
        for (int i = 0; i < 10; i++) {
            users[i] = chatService.registerUser("User" + (i + 1), "user" + (i + 1) + "@example.com");
            chatService.updateUserStatus(users[i].getUserId(), UserStatus.ONLINE);
        }
        
        // Create rooms
        ChatRoom room1 = chatService.createRoom("Busy Room", users[0].getUserId());
        ChatRoom room2 = chatService.createRoom("Tech Discussion", users[1].getUserId());
        
        // Join users to rooms
        for (int i = 1; i < 10; i++) {
            chatService.joinRoom(room1.getRoomId(), users[i].getUserId());
            if (i % 2 == 0) {
                chatService.joinRoom(room2.getRoomId(), users[i].getUserId());
            }
        }
        
        System.out.println("   Created 10 users and 2 rooms");
        System.out.println("   Room 1 members: " + chatService.getRoom(room1.getRoomId()).getMemberCount());
        System.out.println("   Room 2 members: " + chatService.getRoom(room2.getRoomId()).getMemberCount());
        
        System.out.println("\n2. Concurrent messaging simulation:");
        
        ExecutorService executor = Executors.newFixedThreadPool(5);
        CountDownLatch latch = new CountDownLatch(50); // 50 messages total
        
        // Simulate concurrent messaging
        for (int i = 0; i < 50; i++) {
            final int messageIndex = i;
            executor.submit(() -> {
                try {
                    User sender = users[messageIndex % 10];
                    String roomId = (messageIndex % 3 == 0) ? room2.getRoomId() : room1.getRoomId();
                    
                    String content = "Concurrent message #" + messageIndex + " from " + sender.getUsername();
                    chatService.sendMessage(sender.getUserId(), content, roomId);
                    
                    // Simulate some processing time
                    Thread.sleep(10);
                } catch (InterruptedException e) {
                    Thread.currentThread().interrupt();
                } finally {
                    latch.countDown();
                }
            });
        }
        
        // Wait for all messages to be sent
        latch.await(10, TimeUnit.SECONDS);
        executor.shutdown();
        
        System.out.println("   Sent 50 concurrent messages");
        
        System.out.println("\n3. Final statistics:");
        ChatStats finalStats = chatService.getStats();
        System.out.println("   " + finalStats);
        
        System.out.println("\n4. Message distribution:");
        List<Message> room1Messages = chatService.getRoomMessages(room1.getRoomId(), 100);
        List<Message> room2Messages = chatService.getRoomMessages(room2.getRoomId(), 100);
        
        System.out.println("   Room 1 messages: " + room1Messages.size());
        System.out.println("   Room 2 messages: " + room2Messages.size());
        
        System.out.println("\n5. Sample messages from Room 1:");
        room1Messages.stream()
                .limit(5)
                .forEach(msg -> {
                    String sender;
                    if (msg.isSystemMessage()) {
                        sender = "SYSTEM";
                    } else {
                        User senderUser = chatService.getUser(msg.getSenderId());
                        sender = senderUser != null ? senderUser.getUsername() : "Unknown";
                    }
                    System.out.println("   [" + msg.getTimestamp().toString().substring(11, 19) + "] " + 
                                     sender + ": " + msg.getContent());
                });
    }
}