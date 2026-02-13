package com.odop.root.models;

import java.time.LocalDateTime;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

/**
 * ProductCategory entity for organizing products.
 * Supports hierarchical categories via parentCategoryId.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Document(collection = "categories")
public class ProductCategory {

    @Id  
    private String prodCategoryId;
    private String categoryName;
    private String categoryDescription;
    private String categoryImageURL;
    private String parentCategoryId; // Reference to parent category if subcategory
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;

    /**
     * Convenience constructor for new category with all fields.
     */
    public ProductCategory(String prodCategoryId, String categoryName, String categoryDescription, String categoryImageURL) {
        this.prodCategoryId = prodCategoryId;
        this.categoryName = categoryName;
        this.categoryDescription = categoryDescription;
        this.categoryImageURL = categoryImageURL;
    }

    /**
     * Convenience constructor for new category without ID.
     */
    public ProductCategory(String categoryName, String categoryDescription, String categoryImageURL) {
        this.categoryName = categoryName;
        this.categoryDescription = categoryDescription;
        this.categoryImageURL = categoryImageURL;
    }
}
