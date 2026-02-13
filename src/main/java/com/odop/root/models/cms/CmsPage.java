package com.odop.root.models.cms;

import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;
import org.springframework.data.mongodb.core.index.Indexed;
import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Document(collection = "cms_pages")
public class CmsPage {
    
    @Id
    private String id;
    
    private String title;
    
    @Indexed(unique = true)
    private String slug;
    
    private String type; // Page, Banner, Landing
    
    private String content;
    
    private String metaTitle;
    
    private String metaDescription;
    
    private String metaKeywords;
    
    private String status; // Draft, Published, Archived
    
    private LocalDateTime createdAt;
    
    private LocalDateTime updatedAt;
    
    private LocalDateTime publishedAt;
    
    private String author;
}
