package com.odop.root.repository.cms;

import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;
import java.util.List;

import com.odop.root.models.cms.CmsTestimonial;

@Repository
public interface CmsTestimonialRepository extends MongoRepository<CmsTestimonial, String> {
    
    List<CmsTestimonial> findByActiveTrue();
    
    List<CmsTestimonial> findByFeaturedTrue();
    
    List<CmsTestimonial> findByActiveTrueAndFeaturedTrue();
    
    List<CmsTestimonial> findAllByOrderByCreatedAtDesc();
    
    long countByActiveTrue();
    
    long countByFeaturedTrue();
}
