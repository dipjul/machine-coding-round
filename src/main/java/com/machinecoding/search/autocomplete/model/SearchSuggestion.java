package com.machinecoding.search.autocomplete.model;

import java.util.Objects;

/**
 * Represents a search suggestion with ranking information.
 */
public class SearchSuggestion implements Comparable<SearchSuggestion> {
    private final String text;
    private final int frequency;
    private final double score;
    private final SuggestionType type;
    private final String category;
    
    public SearchSuggestion(String text, int frequency) {
        this(text, frequency, frequency, SuggestionType.QUERY, null);
    }
    
    public SearchSuggestion(String text, int frequency, double score, SuggestionType type, String category) {
        if (text == null || text.trim().isEmpty()) {
            throw new IllegalArgumentException("Suggestion text cannot be null or empty");
        }
        
        this.text = text.trim().toLowerCase();
        this.frequency = Math.max(0, frequency);
        this.score = score;
        this.type = type != null ? type : SuggestionType.QUERY;
        this.category = category;
    }
    
    // Getters
    public String getText() { return text; }
    public int getFrequency() { return frequency; }
    public double getScore() { return score; }
    public SuggestionType getType() { return type; }
    public String getCategory() { return category; }
    
    /**
     * Creates a new suggestion with updated frequency.
     */
    public SearchSuggestion withFrequency(int newFrequency) {
        return new SearchSuggestion(text, newFrequency, calculateScore(newFrequency), type, category);
    }
    
    /**
     * Creates a new suggestion with updated score.
     */
    public SearchSuggestion withScore(double newScore) {
        return new SearchSuggestion(text, frequency, newScore, type, category);
    }
    
    /**
     * Calculates score based on frequency and other factors.
     */
    private double calculateScore(int freq) {
        // Simple scoring: frequency + text length penalty
        return freq - (text.length() * 0.1);
    }
    
    /**
     * Checks if this suggestion matches the given prefix.
     */
    public boolean matchesPrefix(String prefix) {
        if (prefix == null || prefix.isEmpty()) {
            return true;
        }
        return text.toLowerCase().startsWith(prefix.toLowerCase());
    }
    
    @Override
    public int compareTo(SearchSuggestion other) {
        // Higher score first, then higher frequency, then alphabetical
        int scoreCompare = Double.compare(other.score, this.score);
        if (scoreCompare != 0) {
            return scoreCompare;
        }
        
        int frequencyCompare = Integer.compare(other.frequency, this.frequency);
        if (frequencyCompare != 0) {
            return frequencyCompare;
        }
        
        return this.text.compareTo(other.text);
    }
    
    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        SearchSuggestion that = (SearchSuggestion) o;
        return Objects.equals(text, that.text);
    }
    
    @Override
    public int hashCode() {
        return Objects.hash(text);
    }
    
    @Override
    public String toString() {
        return String.format("SearchSuggestion{text='%s', frequency=%d, score=%.2f, type=%s}", 
                           text, frequency, score, type);
    }
    
    /**
     * Types of search suggestions.
     */
    public enum SuggestionType {
        QUERY("Query"),
        PRODUCT("Product"),
        CATEGORY("Category"),
        BRAND("Brand"),
        LOCATION("Location");
        
        private final String displayName;
        
        SuggestionType(String displayName) {
            this.displayName = displayName;
        }
        
        public String getDisplayName() {
            return displayName;
        }
        
        @Override
        public String toString() {
            return displayName;
        }
    }
}