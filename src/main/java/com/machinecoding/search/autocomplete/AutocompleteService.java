package com.machinecoding.search.autocomplete;

import com.machinecoding.search.autocomplete.model.SearchSuggestion;
import java.util.List;
import java.util.Set;

/**
 * Interface for autocomplete search functionality.
 */
public interface AutocompleteService {
    
    /**
     * Adds a suggestion to the autocomplete index.
     * 
     * @param suggestion the suggestion to add
     */
    void addSuggestion(SearchSuggestion suggestion);
    
    /**
     * Adds multiple suggestions to the autocomplete index.
     * 
     * @param suggestions the suggestions to add
     */
    void addSuggestions(List<SearchSuggestion> suggestions);
    
    /**
     * Gets autocomplete suggestions for the given prefix.
     * 
     * @param prefix the search prefix
     * @param limit maximum number of suggestions to return
     * @return list of matching suggestions, sorted by relevance
     */
    List<SearchSuggestion> getSuggestions(String prefix, int limit);
    
    /**
     * Gets autocomplete suggestions with category filtering.
     * 
     * @param prefix the search prefix
     * @param limit maximum number of suggestions to return
     * @param categories categories to filter by (null for all categories)
     * @return list of matching suggestions, sorted by relevance
     */
    List<SearchSuggestion> getSuggestions(String prefix, int limit, Set<String> categories);
    
    /**
     * Updates the frequency of an existing suggestion.
     * 
     * @param text the suggestion text
     * @param newFrequency the new frequency value
     * @return true if the suggestion was found and updated
     */
    boolean updateSuggestionFrequency(String text, int newFrequency);
    
    /**
     * Removes a suggestion from the index.
     * 
     * @param text the suggestion text to remove
     * @return true if the suggestion was found and removed
     */
    boolean removeSuggestion(String text);
    
    /**
     * Records a search query to improve suggestions.
     * 
     * @param query the search query
     */
    void recordSearch(String query);
    
    /**
     * Records a click on a suggestion to improve ranking.
     * 
     * @param suggestion the clicked suggestion
     */
    void recordClick(String suggestion);
    
    /**
     * Clears all suggestions from the index.
     */
    void clear();
    
    /**
     * Gets the total number of suggestions in the index.
     * 
     * @return total number of suggestions
     */
    int getTotalSuggestions();
    
    /**
     * Gets statistics about the autocomplete service.
     * 
     * @return service statistics
     */
    AutocompleteStats getStats();
    
    /**
     * Gets the most popular suggestions.
     * 
     * @param limit maximum number of suggestions to return
     * @return list of most popular suggestions
     */
    List<SearchSuggestion> getPopularSuggestions(int limit);
    
    /**
     * Gets suggestions by category.
     * 
     * @param category the category to filter by
     * @param limit maximum number of suggestions to return
     * @return list of suggestions in the specified category
     */
    List<SearchSuggestion> getSuggestionsByCategory(String category, int limit);
}