package com.machinecoding.games.monopoly.model;

import java.util.Random;

/**
 * Represents a pair of dice used in Monopoly.
 */
public class Dice {
    private final Random random;
    private int die1;
    private int die2;
    
    public Dice() {
        this.random = new Random();
        this.die1 = 1;
        this.die2 = 1;
    }
    
    public Dice(long seed) {
        this.random = new Random(seed);
        this.die1 = 1;
        this.die2 = 1;
    }
    
    /**
     * Roll both dice and return the total.
     */
    public int roll() {
        die1 = random.nextInt(6) + 1;
        die2 = random.nextInt(6) + 1;
        return die1 + die2;
    }
    
    /**
     * Get the value of the first die from the last roll.
     */
    public int getDie1() {
        return die1;
    }
    
    /**
     * Get the value of the second die from the last roll.
     */
    public int getDie2() {
        return die2;
    }
    
    /**
     * Get the total of both dice from the last roll.
     */
    public int getTotal() {
        return die1 + die2;
    }
    
    /**
     * Check if the last roll was doubles (both dice show the same value).
     */
    public boolean isDoubles() {
        return die1 == die2;
    }
    
    @Override
    public String toString() {
        return String.format("Dice{die1=%d, die2=%d, total=%d, doubles=%s}", 
                           die1, die2, getTotal(), isDoubles());
    }
}