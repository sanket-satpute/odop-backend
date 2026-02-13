package com.odop.root.repository.cms;

import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;
import java.util.List;

import com.odop.root.models.cms.CmsFaq;

@Repository
public interface CmsFaqRepository extends MongoRepository<CmsFaq, String> {
    
    List<CmsFaq> findByActiveTrue();
    
    List<CmsFaq> findByActiveTrueOrderByPositionAsc();
    
    List<CmsFaq> findByCategory(String category);
    
    List<CmsFaq> findByCategoryAndActiveTrue(String category);
    
    List<CmsFaq> findAllByOrderByPositionAsc();
    
    long countByActiveTrue();
}
