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
@Document(collection = "cms_testimonials")
public class CmsTestimonial {
    
    @Id
    private String id;
    
    private String name;
    
    private String avatar;
    
    private String designation;
    
    private String company;
    
    private Integer rating;
    
    private String text;
    
    private String imageUrl;
    
    private Boolean active;
    
    private Boolean featured;
    
    private LocalDateTime createdAt;
    
    private LocalDateTime updatedAt;
}
