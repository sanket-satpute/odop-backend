package com.odop.root.search.dto;

import java.util.List;

/**
 * DTO for autocomplete/suggestion response
 */
public class AutocompleteResponse {

    private boolean success;
    private String query;
    private List<Suggestion> suggestions;
    private long took;

    public static class Suggestion {
        private String text;
        private String type; // product, vendor, category
        private String id;
        private Double score;
        private String imageUrl;
        private String additionalInfo; // price, location, etc.

        public Suggestion() {}

        public Suggestion(String text, String type, String id) {
            this.text = text;
            this.type = type;
            this.id = id;
        }

        public String getText() { return text; }
        public void setText(String text) { this.text = text; }

        public String getType() { return type; }
        public void setType(String type) { this.type = type; }

        public String getId() { return id; }
        public void setId(String id) { this.id = id; }

        public Double getScore() { return score; }
        public void setScore(Double score) { this.score = score; }

        public String getImageUrl() { return imageUrl; }
        public void setImageUrl(String imageUrl) { this.imageUrl = imageUrl; }

        public String getAdditionalInfo() { return additionalInfo; }
        public void setAdditionalInfo(String additionalInfo) { this.additionalInfo = additionalInfo; }
    }

    // Static factory methods
    public static AutocompleteResponse success(String query, List<Suggestion> suggestions) {
        AutocompleteResponse response = new AutocompleteResponse();
        response.setSuccess(true);
        response.setQuery(query);
        response.setSuggestions(suggestions);
        return response;
    }

    public static AutocompleteResponse empty(String query) {
        AutocompleteResponse response = new AutocompleteResponse();
        response.setSuccess(true);
        response.setQuery(query);
        response.setSuggestions(List.of());
        return response;
    }

    // Constructors
    public AutocompleteResponse() {}

    // Getters and Setters
    public boolean isSuccess() { return success; }
    public void setSuccess(boolean success) { this.success = success; }

    public String getQuery() { return query; }
    public void setQuery(String query) { this.query = query; }

    public List<Suggestion> getSuggestions() { return suggestions; }
    public void setSuggestions(List<Suggestion> suggestions) { this.suggestions = suggestions; }

    public long getTook() { return took; }
    public void setTook(long took) { this.took = took; }
}
