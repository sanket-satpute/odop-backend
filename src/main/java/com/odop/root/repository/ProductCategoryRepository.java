package com.odop.root.repository;

import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;

import com.odop.root.models.ProductCategory;

@Repository
public interface ProductCategoryRepository extends MongoRepository<ProductCategory, String> {

    ProductCategory findByProdCategoryId(String prodCategoryId);

    ProductCategory findByCategoryName(String categoryName);
}
