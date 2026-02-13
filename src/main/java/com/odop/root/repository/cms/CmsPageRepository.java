package com.odop.root.repository.cms;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;
import java.util.List;
import java.util.Optional;

import com.odop.root.models.cms.CmsPage;

@Repository
public interface CmsPageRepository extends MongoRepository<CmsPage, String> {
    
    Optional<CmsPage> findBySlug(String slug);
    
    List<CmsPage> findByStatus(String status);
    
    Page<CmsPage> findByStatus(String status, Pageable pageable);
    
    List<CmsPage> findByType(String type);
    
    Page<CmsPage> findByType(String type, Pageable pageable);
    
    List<CmsPage> findByStatusOrderByCreatedAtDesc(String status);
    
    long countByStatus(String status);
    
    boolean existsBySlug(String slug);
}
