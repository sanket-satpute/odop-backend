package com.odop.root.models.cms;

import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;
import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Document(collection = "cms_banners")
public class CmsBanner {
    
    @Id
    private String id;
    
    private String title;
    
    private String imageUrl;
    
    private String mobileImageUrl;
    
    private String linkUrl;
    
    private String altText;
    
    private Integer position;
    
    private Boolean active;
    
    private LocalDateTime startDate;
    
    private LocalDateTime endDate;
    
    private LocalDateTime createdAt;
    
    private LocalDateTime updatedAt;
}
