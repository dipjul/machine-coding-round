package com.machinecoding.games.monopoly.model;

import java.math.BigDecimal;
import java.util.*;

/**
 * Represents a player in the Monopoly game.
 */
public class Player {
    private final String playerId;
    private final String name;
    private BigDecimal money;
    private int position;
    private boolean isInJail;
    private int jailTurns;
    private boolean hasGetOutOfJailCard;
    private final Set<Property> ownedProperties;
    private PlayerStatus status;
    private int consecutiveDoubles;
    
    public Player(String playerId, String name, BigDecimal startingMoney) {
        this.playerId = playerId != null ? playerId.trim() : "";
        this.name = name != null ? name.trim() : "";
        this.money = startingMoney != null ? startingMoney : BigDecimal.ZERO;
        this.position = 0; // Start at GO
        this.isInJail = false;
        this.jailTurns = 0;
        this.hasGetOutOfJailCard = false;
        this.ownedProperties = new HashSet<>();
        this.status = PlayerStatus.ACTIVE;
        this.consecutiveDoubles = 0;
    }
    
    // Getters
    public String getPlayerId() { return playerId; }
    public String getName() { return name; }
    public BigDecimal getMoney() { return money; }
    public int getPosition() { return position; }
    public boolean isInJail() { return isInJail; }
    public int getJailTurns() { return jailTurns; }
    public boolean hasGetOutOfJailCard() { return hasGetOutOfJailCard; }
    public Set<Property> getOwnedProperties() { return new HashSet<>(ownedProperties); }
    public PlayerStatus getStatus() { return status; }
    public int getConsecutiveDoubles() { return consecutiveDoubles; }
    
    // Money operations
    public boolean addMoney(BigDecimal amount) {
        if (amount == null || amount.compareTo(BigDecimal.ZERO) < 0) {
            return false;
        }
        money = money.add(amount);
        return true;
    }
    
    public boolean subtractMoney(BigDecimal amount) {
        if (amount == null || amount.compareTo(BigDecimal.ZERO) < 0) {
            return false;
        }
        if (money.compareTo(amount) < 0) {
            return false; // Insufficient funds
        }
        money = money.subtract(amount);
        return true;
    }
    
    public boolean canAfford(BigDecimal amount) {
        return amount != null && money.compareTo(amount) >= 0;
    }
    
    // Position operations
    public void setPosition(int position) {
        this.position = Math.max(0, position % 40); // Monopoly board has 40 spaces
    }
    
    public void moveBy(int spaces) {
        int newPosition = (position + spaces) % 40;
        boolean passedGo = newPosition < position || spaces >= 40;
        setPosition(newPosition);
        
        if (passedGo && !isInJail) {
            addMoney(new BigDecimal("200")); // Collect $200 for passing GO
        }
    }
    
    // Jail operations
    public void sendToJail() {
        this.isInJail = true;
        this.jailTurns = 0;
        this.position = 10; // Jail position
        this.consecutiveDoubles = 0;
    }
    
    public void releaseFromJail() {
        this.isInJail = false;
        this.jailTurns = 0;
    }
    
    public void incrementJailTurns() {
        if (isInJail) {
            jailTurns++;
            if (jailTurns >= 3) {
                releaseFromJail();
            }
        }
    }
    
    public void useGetOutOfJailCard() {
        if (hasGetOutOfJailCard && isInJail) {
            this.hasGetOutOfJailCard = false;
            releaseFromJail();
        }
    }
    
    public void giveGetOutOfJailCard() {
        this.hasGetOutOfJailCard = true;
    }
    
    // Property operations
    public boolean buyProperty(Property property, BigDecimal price) {
        if (property == null || price == null || !canAfford(price)) {
            return false;
        }
        if (subtractMoney(price)) {
            ownedProperties.add(property);
            property.setOwner(this);
            return true;
        }
        return false;
    }
    
    public boolean sellProperty(Property property, BigDecimal price) {
        if (property == null || price == null || !ownedProperties.contains(property)) {
            return false;
        }
        ownedProperties.remove(property);
        property.setOwner(null);
        addMoney(price);
        return true;
    }
    
    public boolean ownsProperty(Property property) {
        return property != null && ownedProperties.contains(property);
    }
    
    public List<Property> getPropertiesByGroup(PropertyGroup group) {
        return ownedProperties.stream()
                .filter(p -> p.getGroup() == group)
                .collect(ArrayList::new, (list, p) -> list.add(p), ArrayList::addAll);
    }
    
    public boolean ownsCompleteGroup(PropertyGroup group) {
        if (group == null) return false;
        
        long ownedInGroup = ownedProperties.stream()
                .filter(p -> p.getGroup() == group)
                .count();
        
        return ownedInGroup == group.getPropertyCount();
    }
    
    // Game status operations
    public void setBankrupt() {
        this.status = PlayerStatus.BANKRUPT;
    }
    
    public boolean isBankrupt() {
        return status == PlayerStatus.BANKRUPT;
    }
    
    public boolean isActive() {
        return status == PlayerStatus.ACTIVE;
    }
    
    // Doubles tracking
    public void incrementConsecutiveDoubles() {
        consecutiveDoubles++;
        if (consecutiveDoubles >= 3) {
            sendToJail();
        }
    }
    
    public void resetConsecutiveDoubles() {
        consecutiveDoubles = 0;
    }
    
    // Net worth calculation
    public BigDecimal getNetWorth() {
        BigDecimal propertyValue = ownedProperties.stream()
                .map(Property::getCurrentValue)
                .reduce(BigDecimal.ZERO, BigDecimal::add);
        return money.add(propertyValue);
    }
    
    @Override
    public String toString() {
        return String.format("Player{id='%s', name='%s', money=%s, position=%d, properties=%d, status=%s}",
                           playerId, name, money, position, ownedProperties.size(), status);
    }
    
    @Override
    public boolean equals(Object obj) {
        if (this == obj) return true;
        if (obj == null || getClass() != obj.getClass()) return false;
        Player player = (Player) obj;
        return playerId.equals(player.playerId);
    }
    
    @Override
    public int hashCode() {
        return playerId.hashCode();
    }
}