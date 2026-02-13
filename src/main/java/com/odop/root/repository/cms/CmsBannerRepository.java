package com.odop.root.repository.cms;

import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;
import java.util.List;

import com.odop.root.models.cms.CmsBanner;

@Repository
public interface CmsBannerRepository extends MongoRepository<CmsBanner, String> {
    
    List<CmsBanner> findByActiveTrue();
    
    List<CmsBanner> findByActiveTrueOrderByPositionAsc();
    
    List<CmsBanner> findAllByOrderByPositionAsc();
    
    long countByActiveTrue();
}
