package com.odop.root.models.cms;

import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Document(collection = "cms_seo_settings")
public class SeoSettings {
    
    @Id
    private String id;
    
    private String homepageTitle;
    
    private String homepageDescription;
    
    private String homepageKeywords;
    
    private String blogTitle;
    
    private String blogDescription;
    
    private String blogKeywords;
    
    private String categoryTitleTemplate;
    
    private String productTitleTemplate;
    
    private String defaultAuthor;
    
    private String robotsTxt;
    
    private String googleAnalyticsId;
    
    private String googleSearchConsole;
    
    private String facebookPixelId;
    
    private String twitterHandle;
    
    private String ogDefaultImage;
}
