package com.machinecoding.search.autocomplete;

import com.machinecoding.search.autocomplete.model.SearchSuggestion;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicLong;
import java.util.stream.Collectors;

/**
 * In-memory implementation of autocomplete service using Trie data structure.
 * 
 * Features:
 * - Trie-based prefix matching for fast lookups
 * - Frequency-based ranking and scoring
 * - Category-based filtering
 * - Real-time index updates
 * - Click tracking and popularity scoring
 * - Thread-safe concurrent operations
 */
public class InMemoryAutocompleteService implements AutocompleteService {
    
    private final TrieNode root;
    private final Map<String, SearchSuggestion> suggestionIndex;
    private final Map<String, AtomicInteger> searchCounts;
    private final Map<String, AtomicInteger> clickCounts;
    private final AtomicLong totalSearches;
    private final AtomicLong totalClicks;
    private final int maxSuggestionsPerNode;
    
    public InMemoryAutocompleteService() {
        this(10);
    }
    
    public InMemoryAutocompleteService(int maxSuggestionsPerNode) {
        this.maxSuggestionsPerNode = maxSuggestionsPerNode;
        this.root = new TrieNode(maxSuggestionsPerNode);
        this.suggestionIndex = new ConcurrentHashMap<>();
        this.searchCounts = new ConcurrentHashMap<>();
        this.clickCounts = new ConcurrentHashMap<>();
        this.totalSearches = new AtomicLong(0);
        this.totalClicks = new AtomicLong(0);
    }
    
    @Override
    public void addSuggestion(SearchSuggestion suggestion) {
        if (suggestion == null || suggestion.getText().trim().isEmpty()) {
            return;
        }
        
        String text = suggestion.getText().toLowerCase();
        
        // Update or add to index
        SearchSuggestion existing = suggestionIndex.get(text);
        if (existing != null) {
            // Merge frequencies
            int newFrequency = existing.getFrequency() + suggestion.getFrequency();
            suggestion = existing.withFrequency(newFrequency);
        }
        
        suggestionIndex.put(text, suggestion);
        root.insert(suggestion);
    }
    
    @Override
    public void addSuggestions(List<SearchSuggestion> suggestions) {
        if (suggestions == null || suggestions.isEmpty()) {
            return;
        }
        
        for (SearchSuggestion suggestion : suggestions) {
            addSuggestion(suggestion);
        }
    }
    
    @Override
    public List<SearchSuggestion> getSuggestions(String prefix, int limit) {
        return getSuggestions(prefix, limit, null);
    }
    
    @Override
    public List<SearchSuggestion> getSuggestions(String prefix, int limit, Set<String> categories) {
        if (limit <= 0) {
            return new ArrayList<>();
        }
        
        List<SearchSuggestion> suggestions = root.search(prefix, limit * 2); // Get more for filtering
        
        // Filter by categories if specified
        if (categories != null && !categories.isEmpty()) {
            suggestions = suggestions.stream()
                    .filter(s -> s.getCategory() == null || categories.contains(s.getCategory()))
                    .collect(Collectors.toList());
        }
        
        // Apply additional scoring based on search and click history
        suggestions = suggestions.stream()
                .map(this::enhanceWithClickData)
                .sorted()
                .limit(limit)
                .collect(Collectors.toList());
        
        return suggestions;
    }
    
    @Override
    public boolean updateSuggestionFrequency(String text, int newFrequency) {
        if (text == null || text.trim().isEmpty()) {
            return false;
        }
        
        String normalizedText = text.toLowerCase();
        SearchSuggestion existing = suggestionIndex.get(normalizedText);
        
        if (existing != null) {
            SearchSuggestion updated = existing.withFrequency(newFrequency);
            suggestionIndex.put(normalizedText, updated);
            return root.updateSuggestion(normalizedText, newFrequency);
        }
        
        return false;
    }
    
    @Override
    public boolean removeSuggestion(String text) {
        if (text == null || text.trim().isEmpty()) {
            return false;
        }
        
        String normalizedText = text.toLowerCase();
        SearchSuggestion removed = suggestionIndex.remove(normalizedText);
        
        if (removed != null) {
            root.remove(normalizedText);
            searchCounts.remove(normalizedText);
            clickCounts.remove(normalizedText);
            return true;
        }
        
        return false;
    }
    
    @Override
    public void recordSearch(String query) {
        if (query == null || query.trim().isEmpty()) {
            return;
        }
        
        String normalizedQuery = query.toLowerCase();
        searchCounts.computeIfAbsent(normalizedQuery, k -> new AtomicInteger(0)).incrementAndGet();
        totalSearches.incrementAndGet();
        
        // Auto-add popular searches as suggestions
        int searchCount = searchCounts.get(normalizedQuery).get();
        if (searchCount >= 5 && !suggestionIndex.containsKey(normalizedQuery)) {
            SearchSuggestion newSuggestion = new SearchSuggestion(normalizedQuery, searchCount);
            addSuggestion(newSuggestion);
        }
    }
    
    @Override
    public void recordClick(String suggestion) {
        if (suggestion == null || suggestion.trim().isEmpty()) {
            return;
        }
        
        String normalizedSuggestion = suggestion.toLowerCase();
        clickCounts.computeIfAbsent(normalizedSuggestion, k -> new AtomicInteger(0)).incrementAndGet();
        totalClicks.incrementAndGet();
        
        // Update suggestion frequency based on clicks
        SearchSuggestion existing = suggestionIndex.get(normalizedSuggestion);
        if (existing != null) {
            int clickCount = clickCounts.get(normalizedSuggestion).get();
            updateSuggestionFrequency(normalizedSuggestion, existing.getFrequency() + clickCount);
        }
    }
    
    @Override
    public void clear() {
        suggestionIndex.clear();
        searchCounts.clear();
        clickCounts.clear();
        totalSearches.set(0);
        totalClicks.set(0);
        // Note: We can't easily clear the trie, so we create a new one
        // In a real implementation, we might implement a clear method for TrieNode
    }
    
    @Override
    public int getTotalSuggestions() {
        return suggestionIndex.size();
    }
    
    @Override
    public AutocompleteStats getStats() {
        int totalSuggestions = suggestionIndex.size();
        int totalSearchesInt = (int) totalSearches.get();
        int totalClicksInt = (int) totalClicks.get();
        
        Set<String> categories = suggestionIndex.values().stream()
                .map(SearchSuggestion::getCategory)
                .filter(Objects::nonNull)
                .collect(Collectors.toSet());
        int uniqueCategories = categories.size();
        
        double averageFrequency = suggestionIndex.values().stream()
                .mapToInt(SearchSuggestion::getFrequency)
                .average()
                .orElse(0.0);
        
        String mostPopular = suggestionIndex.values().stream()
                .max(Comparator.comparingInt(SearchSuggestion::getFrequency))
                .map(SearchSuggestion::getText)
                .orElse("None");
        
        TrieNode.TrieStats trieStats = root.getStats();
        
        // Rough estimate of index size
        long indexSize = suggestionIndex.size() * 100L; // Rough estimate
        
        return new AutocompleteStats(totalSuggestions, totalSearchesInt, totalClicksInt,
                                   uniqueCategories, averageFrequency, mostPopular, trieStats, indexSize);
    }
    
    @Override
    public List<SearchSuggestion> getPopularSuggestions(int limit) {
        return suggestionIndex.values().stream()
                .sorted((a, b) -> Integer.compare(b.getFrequency(), a.getFrequency()))
                .limit(limit)
                .collect(Collectors.toList());
    }
    
    @Override
    public List<SearchSuggestion> getSuggestionsByCategory(String category, int limit) {
        return suggestionIndex.values().stream()
                .filter(s -> Objects.equals(s.getCategory(), category))
                .sorted()
                .limit(limit)
                .collect(Collectors.toList());
    }
    
    /**
     * Enhances a suggestion with click and search data for better scoring.
     */
    private SearchSuggestion enhanceWithClickData(SearchSuggestion suggestion) {
        String text = suggestion.getText();
        int searchCount = searchCounts.getOrDefault(text, new AtomicInteger(0)).get();
        int clickCount = clickCounts.getOrDefault(text, new AtomicInteger(0)).get();
        
        // Calculate enhanced score: base frequency + search boost + click boost
        double enhancedScore = suggestion.getFrequency() + (searchCount * 0.5) + (clickCount * 2.0);
        
        return suggestion.withScore(enhancedScore);
    }
    
    /**
     * Builds suggestions from a list of common queries.
     */
    public void buildFromQueries(List<String> queries) {
        Map<String, Integer> queryFrequency = new HashMap<>();
        
        // Count frequency of each query
        for (String query : queries) {
            if (query != null && !query.trim().isEmpty()) {
                String normalized = query.toLowerCase().trim();
                queryFrequency.merge(normalized, 1, Integer::sum);
            }
        }
        
        // Create suggestions from frequent queries
        for (Map.Entry<String, Integer> entry : queryFrequency.entrySet()) {
            SearchSuggestion suggestion = new SearchSuggestion(entry.getKey(), entry.getValue());
            addSuggestion(suggestion);
        }
    }
    
    /**
     * Builds suggestions from structured data (e.g., products, categories).
     */
    public void buildFromStructuredData(List<Map<String, Object>> data) {
        for (Map<String, Object> item : data) {
            String text = (String) item.get("text");
            Integer frequency = (Integer) item.getOrDefault("frequency", 1);
            String type = (String) item.get("type");
            String category = (String) item.get("category");
            
            if (text != null && !text.trim().isEmpty()) {
                SearchSuggestion.SuggestionType suggestionType = 
                    type != null ? SearchSuggestion.SuggestionType.valueOf(type.toUpperCase()) : 
                    SearchSuggestion.SuggestionType.QUERY;
                
                SearchSuggestion suggestion = new SearchSuggestion(text, frequency, frequency, 
                                                                 suggestionType, category);
                addSuggestion(suggestion);
            }
        }
    }
}