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
@Document(collection = "cms_faqs")
public class CmsFaq {
    
    @Id
    private String id;
    
    private String question;
    
    private String answer;
    
    private String category;
    
    private Integer position;
    
    private Boolean active;
    
    private LocalDateTime createdAt;
    
    private LocalDateTime updatedAt;
}
