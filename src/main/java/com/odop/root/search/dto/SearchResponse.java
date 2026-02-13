package com.odop.root.search.dto;

import java.util.List;
import java.util.Map;

/**
 * Response DTO for search operations.
 */
public class SearchResponse {
    
    private String query;
    private List<SearchResultItem> results;
    private long totalResults;
    private int page;
    private int size;
    private int totalPages;
    private long searchTimeMs;
    
    // Facets for filtering
    private Map<String, List<FacetItem>> facets;
    
    // Search suggestions
    private List<String> suggestions;
    private List<String> relatedSearches;
    
    // Status
    private boolean success;
    private String message;
    
    // Constructors
    public SearchResponse() {
        this.success = true;
    }
    
    public static SearchResponse success(List<SearchResultItem> results, long totalResults, int page, int size) {
        SearchResponse response = new SearchResponse();
        response.results = results;
        response.totalResults = totalResults;
        response.page = page;
        response.size = size;
        response.totalPages = (int) Math.ceil((double) totalResults / size);
        response.success = true;
        return response;
    }
    
    public static SearchResponse error(String message) {
        SearchResponse response = new SearchResponse();
        response.success = false;
        response.message = message;
        return response;
    }
    
    // Nested class for facet items
    public static class FacetItem {
        private String value;
        private String label;
        private long count;
        
        public FacetItem() {}
        
        public FacetItem(String value, String label, long count) {
            this.value = value;
            this.label = label;
            this.count = count;
        }
        
        public String getValue() { return value; }
        public void setValue(String value) { this.value = value; }
        
        public String getLabel() { return label; }
        public void setLabel(String label) { this.label = label; }
        
        public long getCount() { return count; }
        public void setCount(long count) { this.count = count; }
    }
    
    // Getters and Setters
    public String getQuery() { return query; }
    public void setQuery(String query) { this.query = query; }
    
    public List<SearchResultItem> getResults() { return results; }
    public void setResults(List<SearchResultItem> results) { this.results = results; }
    
    public long getTotalResults() { return totalResults; }
    public void setTotalResults(long totalResults) { this.totalResults = totalResults; }
    
    public int getPage() { return page; }
    public void setPage(int page) { this.page = page; }
    
    public int getSize() { return size; }
    public void setSize(int size) { this.size = size; }
    
    public int getTotalPages() { return totalPages; }
    public void setTotalPages(int totalPages) { this.totalPages = totalPages; }
    
    public long getSearchTimeMs() { return searchTimeMs; }
    public void setSearchTimeMs(long searchTimeMs) { this.searchTimeMs = searchTimeMs; }
    
    public Map<String, List<FacetItem>> getFacets() { return facets; }
    public void setFacets(Map<String, List<FacetItem>> facets) { this.facets = facets; }
    
    public List<String> getSuggestions() { return suggestions; }
    public void setSuggestions(List<String> suggestions) { this.suggestions = suggestions; }
    
    public List<String> getRelatedSearches() { return relatedSearches; }
    public void setRelatedSearches(List<String> relatedSearches) { this.relatedSearches = relatedSearches; }
    
    public boolean isSuccess() { return success; }
    public void setSuccess(boolean success) { this.success = success; }
    
    public String getMessage() { return message; }
    public void setMessage(String message) { this.message = message; }
}
