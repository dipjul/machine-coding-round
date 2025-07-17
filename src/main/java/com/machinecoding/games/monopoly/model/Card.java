package com.machinecoding.games.monopoly.model;

/**
 * Represents a Chance or Community Chest card in Monopoly.
 */
public class Card {
    private final String cardId;
    private final String description;
    private final CardType type;
    private final String value; // Can be position, amount, or other data
    
    public Card(String cardId, String description, CardType type, String value) {
        this.cardId = cardId != null ? cardId.trim() : "";
        this.description = description != null ? description.trim() : "";
        this.type = type != null ? type : CardType.MONEY;
        this.value = value != null ? value.trim() : "";
    }
    
    // Getters
    public String getCardId() { return cardId; }
    public String getDescription() { return description; }
    public CardType getType() { return type; }
    public String getValue() { return value; }
    
    @Override
    public String toString() {
        return String.format("Card{id='%s', description='%s', type=%s, value='%s'}", 
                           cardId, description, type, value);
    }
    
    @Override
    public boolean equals(Object obj) {
        if (this == obj) return true;
        if (obj == null || getClass() != obj.getClass()) return false;
        Card card = (Card) obj;
        return cardId.equals(card.cardId);
    }
    
    @Override
    public int hashCode() {
        return cardId.hashCode();
    }
}