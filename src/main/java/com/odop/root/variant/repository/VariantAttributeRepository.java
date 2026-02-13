package com.odop.root.variant.repository;

import com.odop.root.variant.model.VariantAttribute;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface VariantAttributeRepository extends MongoRepository<VariantAttribute, String> {
    
    // Find by code
    Optional<VariantAttribute> findByCode(String code);
    
    // Find by name
    Optional<VariantAttribute> findByName(String name);
    
    // Find by category
    List<VariantAttribute> findByCategoryIdsContaining(String categoryId);
    
    // Find all ordered
    List<VariantAttribute> findAllByOrderByDisplayOrderAsc();
    
    // Check if exists
    boolean existsByCode(String code);
}
