package com.machinecoding.games.monopoly.service;

import com.machinecoding.games.monopoly.model.Player;

/**
 * Result of a player's turn in Monopoly.
 */
public class TurnResult {
    private final Player player;
    private final int die1;
    private final int die2;
    private final boolean doubles;
    private final String message;
    
    public TurnResult(Player player, int die1, int die2, boolean doubles, String message) {
        this.player = player;
        this.die1 = die1;
        this.die2 = die2;
        this.doubles = doubles;
        this.message = message != null ? message : "";
    }
    
    // Getters
    public Player getPlayer() { return player; }
    public int getDie1() { return die1; }
    public int getDie2() { return die2; }
    public int getTotal() { return die1 + die2; }
    public boolean isDoubles() { return doubles; }
    public String getMessage() { return message; }
    
    @Override
    public String toString() {
        return String.format("TurnResult{player=%s, dice=[%d,%d], doubles=%s, message='%s'}",
                           player.getName(), die1, die2, doubles, message);
    }
}