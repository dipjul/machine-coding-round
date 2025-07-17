package com.machinecoding.search.autocomplete;

import com.machinecoding.search.autocomplete.model.SearchSuggestion;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Trie node for efficient prefix-based search suggestions.
 * Thread-safe implementation supporting concurrent operations.
 */
public class TrieNode {
    private final Map<Character, TrieNode> children;
    private final Set<SearchSuggestion> suggestions;
    private boolean isEndOfWord;
    private int maxSuggestions;
    
    public TrieNode() {
        this(10); // Default max suggestions per node
    }
    
    public TrieNode(int maxSuggestions) {
        this.children = new ConcurrentHashMap<>();
        this.suggestions = ConcurrentHashMap.newKeySet();
        this.isEndOfWord = false;
        this.maxSuggestions = maxSuggestions;
    }
    
    /**
     * Inserts a suggestion into the trie.
     */
    public void insert(SearchSuggestion suggestion) {
        if (suggestion == null || suggestion.getText().isEmpty()) {
            return;
        }
        
        insert(suggestion.getText().toLowerCase(), suggestion, 0);
    }
    
    private void insert(String text, SearchSuggestion suggestion, int index) {
        // Add suggestion to current node (for prefix matching)
        addSuggestionToNode(suggestion);
        
        if (index == text.length()) {
            isEndOfWord = true;
            return;
        }
        
        char ch = text.charAt(index);
        TrieNode child = children.computeIfAbsent(ch, k -> new TrieNode(maxSuggestions));
        child.insert(text, suggestion, index + 1);
    }
    
    /**
     * Searches for suggestions matching the given prefix.
     */
    public List<SearchSuggestion> search(String prefix, int limit) {
        if (prefix == null) {
            prefix = "";
        }
        
        final String finalPrefix = prefix; // Make effectively final for lambda
        
        TrieNode node = findNode(prefix.toLowerCase());
        if (node == null) {
            return new ArrayList<>();
        }
        
        // Get suggestions from the prefix node and its subtree
        Set<SearchSuggestion> allSuggestions = new HashSet<>();
        collectSuggestions(node, allSuggestions);
        
        // Sort by score/frequency and return top results
        return allSuggestions.stream()
                .filter(s -> s.matchesPrefix(finalPrefix))
                .sorted()
                .limit(limit)
                .collect(ArrayList::new, ArrayList::add, ArrayList::addAll);
    }
    
    /**
     * Finds the node corresponding to the given prefix.
     */
    private TrieNode findNode(String prefix) {
        TrieNode current = this;
        
        for (char ch : prefix.toCharArray()) {
            current = current.children.get(ch);
            if (current == null) {
                return null;
            }
        }
        
        return current;
    }
    
    /**
     * Collects all suggestions from the current node and its subtree.
     */
    private void collectSuggestions(TrieNode node, Set<SearchSuggestion> result) {
        if (node == null) {
            return;
        }
        
        result.addAll(node.suggestions);
        
        // Recursively collect from children
        for (TrieNode child : node.children.values()) {
            collectSuggestions(child, result);
        }
    }
    
    /**
     * Adds a suggestion to the current node, maintaining the top suggestions.
     */
    private void addSuggestionToNode(SearchSuggestion suggestion) {
        suggestions.add(suggestion);
        
        // If we exceed max suggestions, remove the lowest scored one
        if (suggestions.size() > maxSuggestions) {
            SearchSuggestion lowest = suggestions.stream()
                    .min(SearchSuggestion::compareTo)
                    .orElse(null);
            
            if (lowest != null && lowest.compareTo(suggestion) < 0) {
                suggestions.remove(lowest);
            }
        }
    }
    
    /**
     * Updates the frequency of an existing suggestion.
     */
    public boolean updateSuggestion(String text, int newFrequency) {
        if (text == null || text.isEmpty()) {
            return false;
        }
        
        return updateSuggestion(text.toLowerCase(), newFrequency, 0);
    }
    
    private boolean updateSuggestion(String text, int newFrequency, int index) {
        boolean updated = false;
        
        // Update suggestion in current node
        SearchSuggestion toUpdate = null;
        for (SearchSuggestion suggestion : suggestions) {
            if (suggestion.getText().equals(text)) {
                toUpdate = suggestion;
                break;
            }
        }
        
        if (toUpdate != null) {
            suggestions.remove(toUpdate);
            suggestions.add(toUpdate.withFrequency(newFrequency));
            updated = true;
        }
        
        // Continue to children
        if (index < text.length()) {
            char ch = text.charAt(index);
            TrieNode child = children.get(ch);
            if (child != null) {
                updated |= child.updateSuggestion(text, newFrequency, index + 1);
            }
        }
        
        return updated;
    }
    
    /**
     * Removes a suggestion from the trie.
     */
    public boolean remove(String text) {
        if (text == null || text.isEmpty()) {
            return false;
        }
        
        return remove(text.toLowerCase(), 0);
    }
    
    private boolean remove(String text, int index) {
        boolean removed = false;
        
        // Remove suggestion from current node
        suggestions.removeIf(s -> s.getText().equals(text));
        
        if (index == text.length()) {
            isEndOfWord = false;
            removed = true;
        } else {
            char ch = text.charAt(index);
            TrieNode child = children.get(ch);
            if (child != null) {
                removed = child.remove(text, index + 1);
                
                // Remove child if it's empty
                if (child.isEmpty()) {
                    children.remove(ch);
                }
            }
        }
        
        return removed;
    }
    
    /**
     * Checks if the node is empty (no suggestions and no children).
     */
    private boolean isEmpty() {
        return suggestions.isEmpty() && children.isEmpty() && !isEndOfWord;
    }
    
    /**
     * Gets the total number of suggestions in the trie.
     */
    public int getTotalSuggestions() {
        Set<String> uniqueTexts = new HashSet<>();
        collectUniqueTexts(this, uniqueTexts);
        return uniqueTexts.size();
    }
    
    private void collectUniqueTexts(TrieNode node, Set<String> texts) {
        if (node == null) {
            return;
        }
        
        for (SearchSuggestion suggestion : node.suggestions) {
            texts.add(suggestion.getText());
        }
        
        for (TrieNode child : node.children.values()) {
            collectUniqueTexts(child, texts);
        }
    }
    
    /**
     * Gets statistics about the trie structure.
     */
    public TrieStats getStats() {
        int[] stats = new int[4]; // [nodes, suggestions, maxDepth, totalChildren]
        calculateStats(this, 0, stats);
        
        return new TrieStats(stats[0], stats[1], stats[2], stats[3]);
    }
    
    private void calculateStats(TrieNode node, int depth, int[] stats) {
        if (node == null) {
            return;
        }
        
        stats[0]++; // node count
        stats[1] += node.suggestions.size(); // suggestion count
        stats[2] = Math.max(stats[2], depth); // max depth
        stats[3] += node.children.size(); // total children
        
        for (TrieNode child : node.children.values()) {
            calculateStats(child, depth + 1, stats);
        }
    }
    
    /**
     * Statistics about the trie structure.
     */
    public static class TrieStats {
        private final int nodeCount;
        private final int suggestionCount;
        private final int maxDepth;
        private final int totalChildren;
        
        public TrieStats(int nodeCount, int suggestionCount, int maxDepth, int totalChildren) {
            this.nodeCount = nodeCount;
            this.suggestionCount = suggestionCount;
            this.maxDepth = maxDepth;
            this.totalChildren = totalChildren;
        }
        
        public int getNodeCount() { return nodeCount; }
        public int getSuggestionCount() { return suggestionCount; }
        public int getMaxDepth() { return maxDepth; }
        public int getTotalChildren() { return totalChildren; }
        
        @Override
        public String toString() {
            return String.format("TrieStats{nodes=%d, suggestions=%d, maxDepth=%d, avgChildren=%.2f}", 
                               nodeCount, suggestionCount, maxDepth, 
                               nodeCount > 0 ? (double) totalChildren / nodeCount : 0.0);
        }
    }
}