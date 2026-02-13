package com.odop.root.search.dto;

import java.util.List;
import java.util.Map;

/**
 * DTO for search results response
 * Includes results, pagination, and aggregations
 */
public class SearchResponse<T> {

    private boolean success;
    private String message;
    private String query;

    // Results
    private List<T> results;
    private long totalHits;
    private int page;
    private int size;
    private int totalPages;
    private boolean hasNext;
    private boolean hasPrevious;

    // Search metadata
    private long took; // Time in milliseconds
    private Double maxScore;
    private String searchType;

    // Aggregations (facets)
    private Map<String, List<FacetBucket>> aggregations;

    // Suggestions for "did you mean"
    private List<String> suggestions;

    // Highlighted snippets
    private Map<String, List<String>> highlights;

    // Static factory methods
    public static <T> SearchResponse<T> success(List<T> results, long totalHits, int page, int size) {
        SearchResponse<T> response = new SearchResponse<>();
        response.setSuccess(true);
        response.setResults(results);
        response.setTotalHits(totalHits);
        response.setPage(page);
        response.setSize(size);
        response.setTotalPages((int) Math.ceil((double) totalHits / size));
        response.setHasNext(page < response.getTotalPages() - 1);
        response.setHasPrevious(page > 0);
        return response;
    }

    public static <T> SearchResponse<T> error(String message) {
        SearchResponse<T> response = new SearchResponse<>();
        response.setSuccess(false);
        response.setMessage(message);
        return response;
    }

    public static <T> SearchResponse<T> empty(String query) {
        SearchResponse<T> response = new SearchResponse<>();
        response.setSuccess(true);
        response.setQuery(query);
        response.setResults(List.of());
        response.setTotalHits(0);
        response.setMessage("No results found for: " + query);
        return response;
    }

    // Inner class for facet buckets
    public static class FacetBucket {
        private String key;
        private long count;

        public FacetBucket() {}

        public FacetBucket(String key, long count) {
            this.key = key;
            this.count = count;
        }

        public String getKey() { return key; }
        public void setKey(String key) { this.key = key; }

        public long getCount() { return count; }
        public void setCount(long count) { this.count = count; }
    }

    // Constructors
    public SearchResponse() {}

    // Getters and Setters
    public boolean isSuccess() { return success; }
    public void setSuccess(boolean success) { this.success = success; }

    public String getMessage() { return message; }
    public void setMessage(String message) { this.message = message; }

    public String getQuery() { return query; }
    public void setQuery(String query) { this.query = query; }

    public List<T> getResults() { return results; }
    public void setResults(List<T> results) { this.results = results; }

    public long getTotalHits() { return totalHits; }
    public void setTotalHits(long totalHits) { this.totalHits = totalHits; }

    public int getPage() { return page; }
    public void setPage(int page) { this.page = page; }

    public int getSize() { return size; }
    public void setSize(int size) { this.size = size; }

    public int getTotalPages() { return totalPages; }
    public void setTotalPages(int totalPages) { this.totalPages = totalPages; }

    public boolean isHasNext() { return hasNext; }
    public void setHasNext(boolean hasNext) { this.hasNext = hasNext; }

    public boolean isHasPrevious() { return hasPrevious; }
    public void setHasPrevious(boolean hasPrevious) { this.hasPrevious = hasPrevious; }

    public long getTook() { return took; }
    public void setTook(long took) { this.took = took; }

    public Double getMaxScore() { return maxScore; }
    public void setMaxScore(Double maxScore) { this.maxScore = maxScore; }

    public String getSearchType() { return searchType; }
    public void setSearchType(String searchType) { this.searchType = searchType; }

    public Map<String, List<FacetBucket>> getAggregations() { return aggregations; }
    public void setAggregations(Map<String, List<FacetBucket>> aggregations) { this.aggregations = aggregations; }

    public List<String> getSuggestions() { return suggestions; }
    public void setSuggestions(List<String> suggestions) { this.suggestions = suggestions; }

    public Map<String, List<String>> getHighlights() { return highlights; }
    public void setHighlights(Map<String, List<String>> highlights) { this.highlights = highlights; }
}
