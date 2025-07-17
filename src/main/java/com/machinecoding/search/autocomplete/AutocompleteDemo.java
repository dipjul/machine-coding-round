package com.machinecoding.search.autocomplete;

import com.machinecoding.search.autocomplete.model.SearchSuggestion;
import java.util.*;

/**
 * Comprehensive demonstration of the Autocomplete System.
 * Shows trie-based search, ranking algorithms, and real-time updates.
 */
public class AutocompleteDemo {
    
    public static void main(String[] args) {
        System.out.println("=== Autocomplete System Demo ===\n");
        
        // Demo 1: Basic Autocomplete Functionality
        System.out.println("=== Demo 1: Basic Autocomplete Functionality ===");
        demonstrateBasicAutocomplete();
        
        // Demo 2: Ranking and Scoring
        System.out.println("\n=== Demo 2: Ranking and Scoring ===");
        demonstrateRankingAndScoring();
        
        // Demo 3: Category-Based Filtering
        System.out.println("\n=== Demo 3: Category-Based Filtering ===");
        demonstrateCategoryFiltering();
        
        // Demo 4: Real-Time Learning and Updates
        System.out.println("\n=== Demo 4: Real-Time Learning and Updates ===");
        demonstrateRealTimeLearning();
        
        // Demo 5: Performance and Statistics
        System.out.println("\n=== Demo 5: Performance and Statistics ===");
        demonstratePerformanceAndStats();
        
        System.out.println("\n=== Demo Complete ===");
    }
    
    private static void demonstrateBasicAutocomplete() {
        System.out.println("1. Creating autocomplete service:");
        AutocompleteService autocomplete = new InMemoryAutocompleteService();
        
        System.out.println("\n2. Adding basic suggestions:");
        List<SearchSuggestion> suggestions = Arrays.asList(
            new SearchSuggestion("apple", 100),
            new SearchSuggestion("application", 80),
            new SearchSuggestion("apply", 60),
            new SearchSuggestion("appreciate", 40),
            new SearchSuggestion("approach", 70),
            new SearchSuggestion("banana", 90),
            new SearchSuggestion("band", 50),
            new SearchSuggestion("bank", 120),
            new SearchSuggestion("basketball", 85),
            new SearchSuggestion("baseball", 75)
        );
        
        autocomplete.addSuggestions(suggestions);
        System.out.println("   Added " + suggestions.size() + " suggestions");
        
        System.out.println("\n3. Testing prefix searches:");
        testPrefixSearch(autocomplete, "app", 5);
        testPrefixSearch(autocomplete, "ba", 5);
        testPrefixSearch(autocomplete, "ban", 3);
        testPrefixSearch(autocomplete, "xyz", 5);
        
        System.out.println("\n4. Testing empty and edge cases:");
        testPrefixSearch(autocomplete, "", 5);
        testPrefixSearch(autocomplete, "a", 10);
    }
    
    private static void demonstrateRankingAndScoring() {
        System.out.println("1. Creating service with frequency-based ranking:");
        AutocompleteService autocomplete = new InMemoryAutocompleteService();
        
        // Add suggestions with different frequencies
        List<SearchSuggestion> suggestions = Arrays.asList(
            new SearchSuggestion("java", 1000),      // Very popular
            new SearchSuggestion("javascript", 800), // Popular
            new SearchSuggestion("jakarta", 50),     // Less popular
            new SearchSuggestion("jamaica", 200),    // Moderate
            new SearchSuggestion("japan", 500),      // Popular
            new SearchSuggestion("jar", 100),        // Less popular
            new SearchSuggestion("jazz", 300)        // Moderate
        );
        
        autocomplete.addSuggestions(suggestions);
        
        System.out.println("\n2. Testing frequency-based ranking:");
        System.out.println("   Search for 'ja' (should be ranked by frequency):");
        List<SearchSuggestion> results = autocomplete.getSuggestions("ja", 10);
        for (int i = 0; i < results.size(); i++) {
            SearchSuggestion s = results.get(i);
            System.out.println("   " + (i + 1) + ". " + s.getText() + 
                             " (frequency: " + s.getFrequency() + ", score: " + String.format("%.1f", s.getScore()) + ")");
        }
        
        System.out.println("\n3. Testing popular suggestions:");
        List<SearchSuggestion> popular = autocomplete.getPopularSuggestions(5);
        System.out.println("   Top 5 popular suggestions:");
        for (int i = 0; i < popular.size(); i++) {
            SearchSuggestion s = popular.get(i);
            System.out.println("   " + (i + 1) + ". " + s.getText() + " (frequency: " + s.getFrequency() + ")");
        }
    }
    
    private static void demonstrateCategoryFiltering() {
        System.out.println("1. Creating service with categorized suggestions:");
        AutocompleteService autocomplete = new InMemoryAutocompleteService();
        
        // Add suggestions with categories
        List<SearchSuggestion> suggestions = Arrays.asList(
            new SearchSuggestion("apple iphone", 500, 500, SearchSuggestion.SuggestionType.PRODUCT, "electronics"),
            new SearchSuggestion("apple fruit", 200, 200, SearchSuggestion.SuggestionType.PRODUCT, "food"),
            new SearchSuggestion("apple pie", 150, 150, SearchSuggestion.SuggestionType.PRODUCT, "food"),
            new SearchSuggestion("apple store", 300, 300, SearchSuggestion.SuggestionType.LOCATION, "retail"),
            new SearchSuggestion("samsung phone", 400, 400, SearchSuggestion.SuggestionType.PRODUCT, "electronics"),
            new SearchSuggestion("samsung tv", 350, 350, SearchSuggestion.SuggestionType.PRODUCT, "electronics"),
            new SearchSuggestion("food delivery", 600, 600, SearchSuggestion.SuggestionType.QUERY, "food"),
            new SearchSuggestion("electronics store", 250, 250, SearchSuggestion.SuggestionType.LOCATION, "retail")
        );
        
        autocomplete.addSuggestions(suggestions);
        
        System.out.println("\n2. Testing category-based filtering:");
        
        // Search without category filter
        System.out.println("   Search for 'apple' (all categories):");
        List<SearchSuggestion> allResults = autocomplete.getSuggestions("apple", 10);
        for (SearchSuggestion s : allResults) {
            System.out.println("   - " + s.getText() + " [" + s.getType() + 
                             (s.getCategory() != null ? ", " + s.getCategory() : "") + "]");
        }
        
        // Search with category filter
        System.out.println("\n   Search for 'apple' (food category only):");
        Set<String> foodCategory = Collections.singleton("food");
        List<SearchSuggestion> foodResults = autocomplete.getSuggestions("apple", 10, foodCategory);
        for (SearchSuggestion s : foodResults) {
            System.out.println("   - " + s.getText() + " [" + s.getType() + 
                             (s.getCategory() != null ? ", " + s.getCategory() : "") + "]");
        }
        
        System.out.println("\n3. Testing suggestions by category:");
        List<SearchSuggestion> electronicsResults = autocomplete.getSuggestionsByCategory("electronics", 5);
        System.out.println("   Electronics category suggestions:");
        for (SearchSuggestion s : electronicsResults) {
            System.out.println("   - " + s.getText() + " (frequency: " + s.getFrequency() + ")");
        }
    }
    
    private static void demonstrateRealTimeLearning() {
        System.out.println("1. Creating service for real-time learning demo:");
        AutocompleteService autocomplete = new InMemoryAutocompleteService();
        
        // Add initial suggestions
        autocomplete.addSuggestion(new SearchSuggestion("machine learning", 100));
        autocomplete.addSuggestion(new SearchSuggestion("machine vision", 50));
        autocomplete.addSuggestion(new SearchSuggestion("machine code", 30));
        
        System.out.println("\n2. Initial suggestions for 'machine':");
        printSuggestions(autocomplete.getSuggestions("machine", 5));
        
        System.out.println("\n3. Simulating user searches and clicks:");
        
        // Simulate searches
        for (int i = 0; i < 10; i++) {
            autocomplete.recordSearch("machine learning tutorial");
        }
        for (int i = 0; i < 5; i++) {
            autocomplete.recordSearch("machine learning python");
        }
        
        // Simulate clicks
        for (int i = 0; i < 8; i++) {
            autocomplete.recordClick("machine learning");
        }
        for (int i = 0; i < 3; i++) {
            autocomplete.recordClick("machine vision");
        }
        
        System.out.println("   Recorded searches and clicks");
        
        System.out.println("\n4. Updated suggestions for 'machine' (after learning):");
        printSuggestions(autocomplete.getSuggestions("machine", 5));
        
        System.out.println("\n5. Auto-generated suggestions from popular searches:");
        List<SearchSuggestion> allSuggestions = autocomplete.getSuggestions("machine learning", 10);
        System.out.println("   Suggestions for 'machine learning':");
        printSuggestions(allSuggestions);
        
        System.out.println("\n6. Updating suggestion frequency:");
        boolean updated = autocomplete.updateSuggestionFrequency("machine code", 200);
        System.out.println("   Updated 'machine code' frequency: " + updated);
        
        System.out.println("\n7. Suggestions after frequency update:");
        printSuggestions(autocomplete.getSuggestions("machine", 5));
    }
    
    private static void demonstratePerformanceAndStats() {
        System.out.println("1. Creating large-scale autocomplete service:");
        AutocompleteService autocomplete = new InMemoryAutocompleteService();
        
        System.out.println("\n2. Building index from sample data:");
        
        // Build from query data
        List<String> sampleQueries = Arrays.asList(
            "how to learn programming", "how to cook pasta", "how to lose weight",
            "what is machine learning", "what is artificial intelligence", "what is blockchain",
            "best restaurants near me", "best movies 2023", "best programming languages",
            "weather forecast", "weather today", "weather tomorrow",
            "news today", "news headlines", "news sports",
            "online shopping", "online courses", "online banking",
            "travel destinations", "travel insurance", "travel booking",
            "health tips", "health insurance", "health food",
            "technology news", "technology trends", "technology jobs"
        );
        
        // Add each query multiple times to simulate frequency
        for (String query : sampleQueries) {
            for (int i = 0; i < (int)(Math.random() * 50) + 10; i++) {
                autocomplete.recordSearch(query);
            }
        }
        
        // Build from structured data
        List<Map<String, Object>> structuredData = new ArrayList<>();
        String[] products = {"laptop", "smartphone", "tablet", "headphones", "camera", "watch", "keyboard", "mouse"};
        String[] brands = {"apple", "samsung", "google", "microsoft", "sony", "dell", "hp", "lenovo"};
        
        for (String product : products) {
            for (String brand : brands) {
                Map<String, Object> item = new HashMap<>();
                item.put("text", brand + " " + product);
                item.put("frequency", (int)(Math.random() * 100) + 20);
                item.put("type", "PRODUCT");
                item.put("category", "electronics");
                structuredData.add(item);
            }
        }
        
        ((InMemoryAutocompleteService) autocomplete).buildFromStructuredData(structuredData);
        
        System.out.println("   Built index with sample data");
        
        System.out.println("\n3. Performance testing:");
        long startTime = System.currentTimeMillis();
        
        // Perform multiple searches
        String[] testPrefixes = {"how", "what", "best", "apple", "samsung", "tech", "news", "weather"};
        int totalResults = 0;
        
        for (String prefix : testPrefixes) {
            List<SearchSuggestion> results = autocomplete.getSuggestions(prefix, 10);
            totalResults += results.size();
        }
        
        long endTime = System.currentTimeMillis();
        System.out.println("   Performed " + testPrefixes.length + " searches in " + (endTime - startTime) + "ms");
        System.out.println("   Total results returned: " + totalResults);
        System.out.println("   Average time per search: " + String.format("%.2f", (double)(endTime - startTime) / testPrefixes.length) + "ms");
        
        System.out.println("\n4. Service statistics:");
        AutocompleteStats stats = autocomplete.getStats();
        System.out.println("   " + stats);
        
        System.out.println("\n5. Sample search results:");
        for (String prefix : Arrays.asList("apple", "how to", "best")) {
            System.out.println("   Search for '" + prefix + "':");
            List<SearchSuggestion> results = autocomplete.getSuggestions(prefix, 5);
            for (int i = 0; i < results.size(); i++) {
                SearchSuggestion s = results.get(i);
                System.out.println("     " + (i + 1) + ". " + s.getText() + 
                                 " (freq: " + s.getFrequency() + ", score: " + String.format("%.1f", s.getScore()) + ")");
            }
        }
        
        System.out.println("\n6. Memory usage estimation:");
        Runtime runtime = Runtime.getRuntime();
        long usedMemory = runtime.totalMemory() - runtime.freeMemory();
        System.out.println("   Estimated memory usage: " + (usedMemory / 1024 / 1024) + " MB");
        System.out.println("   Index size: " + stats.getIndexSizeBytes() + " bytes");
        System.out.println("   Suggestions per MB: " + (stats.getTotalSuggestions() * 1024 * 1024 / Math.max(usedMemory, 1)));
    }
    
    private static void testPrefixSearch(AutocompleteService autocomplete, String prefix, int limit) {
        List<SearchSuggestion> results = autocomplete.getSuggestions(prefix, limit);
        System.out.println("   Search for '" + prefix + "' (limit " + limit + "): " + results.size() + " results");
        for (SearchSuggestion suggestion : results) {
            System.out.println("     - " + suggestion.getText() + " (frequency: " + suggestion.getFrequency() + ")");
        }
    }
    
    private static void printSuggestions(List<SearchSuggestion> suggestions) {
        for (int i = 0; i < suggestions.size(); i++) {
            SearchSuggestion s = suggestions.get(i);
            System.out.println("   " + (i + 1) + ". " + s.getText() + 
                             " (freq: " + s.getFrequency() + ", score: " + String.format("%.1f", s.getScore()) + ")");
        }
    }
}