package com.machinecoding.search.autocomplete;

/**
 * Statistics for the autocomplete service.
 */
public class AutocompleteStats {
    private final int totalSuggestions;
    private final int totalSearches;
    private final int totalClicks;
    private final int uniqueCategories;
    private final double averageFrequency;
    private final String mostPopularSuggestion;
    private final TrieNode.TrieStats trieStats;
    private final long indexSizeBytes;
    
    public AutocompleteStats(int totalSuggestions, int totalSearches, int totalClicks,
                           int uniqueCategories, double averageFrequency, String mostPopularSuggestion,
                           TrieNode.TrieStats trieStats, long indexSizeBytes) {
        this.totalSuggestions = totalSuggestions;
        this.totalSearches = totalSearches;
        this.totalClicks = totalClicks;
        this.uniqueCategories = uniqueCategories;
        this.averageFrequency = averageFrequency;
        this.mostPopularSuggestion = mostPopularSuggestion;
        this.trieStats = trieStats;
        this.indexSizeBytes = indexSizeBytes;
    }
    
    // Getters
    public int getTotalSuggestions() { return totalSuggestions; }
    public int getTotalSearches() { return totalSearches; }
    public int getTotalClicks() { return totalClicks; }
    public int getUniqueCategories() { return uniqueCategories; }
    public double getAverageFrequency() { return averageFrequency; }
    public String getMostPopularSuggestion() { return mostPopularSuggestion; }
    public TrieNode.TrieStats getTrieStats() { return trieStats; }
    public long getIndexSizeBytes() { return indexSizeBytes; }
    
    public double getClickThroughRate() {
        return totalSearches == 0 ? 0.0 : (double) totalClicks / totalSearches * 100;
    }
    
    @Override
    public String toString() {
        return String.format(
            "AutocompleteStats{suggestions=%d, searches=%d, clicks=%d (%.1f%% CTR), " +
            "categories=%d, avgFreq=%.1f, popular='%s', indexSize=%d bytes, trie=%s}",
            totalSuggestions, totalSearches, totalClicks, getClickThroughRate(),
            uniqueCategories, averageFrequency, mostPopularSuggestion, indexSizeBytes, trieStats
        );
    }
}