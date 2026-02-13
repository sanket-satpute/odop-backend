package com.odop.root.services;

import java.util.List;
// UUID import removed - not used

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.odop.root.models.ProductCategory;
import com.odop.root.repository.ProductCategoryRepository;

@Service
public class ProductCategoryService {
	
	@Autowired
	ProductCategoryRepository pCatRepo;
	
	public ProductCategory saveProductCategory(ProductCategory category) {
		return this.pCatRepo.save(category);
	}
	
	public List<ProductCategory> getAllProductCategories() {
		return this.pCatRepo.findAll();
	}

	public ProductCategory getProductCategorieById(String categoryID) {
		return this.pCatRepo.findByProdCategoryId(categoryID);
	}
	
	public ProductCategory getProductCategorieByName(String categoryName) {
		return this.pCatRepo.findByCategoryName(categoryName);
	}
	
	public boolean deleteById(String adminId) {
		if(this.getProductCategorieById(adminId) != null) {
			this.pCatRepo.deleteById(adminId);
			return (this.getProductCategorieById(adminId)!= null);
		}
		return false;
	}
}
