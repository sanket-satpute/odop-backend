package com.odop.root.controller;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import com.odop.root.models.ProductCategory;
import com.odop.root.services.ProductCategoryService;

@RestController
@RequestMapping("odop/category")
@CrossOrigin
public class ProductCategoryController {

    @Autowired
    private ProductCategoryService productCategoryService;

    @PostMapping("/save_category")
    public ProductCategory saveCategory(@RequestBody ProductCategory category) {
        return this.productCategoryService.saveProductCategory(category);
    }

    @GetMapping("/get_all_categories")
    public List<ProductCategory> getAllCategories() {
        return this.productCategoryService.getAllProductCategories();
    }

    // Backward compatibility - support old typo endpoint
    @GetMapping("/get_all_categorie")
    public List<ProductCategory> getAllCategoriesLegacy() {
        return this.productCategoryService.getAllProductCategories();
    }

    @GetMapping("/get_category_id/{id}")
    public ProductCategory getCategoryById(@PathVariable("id") String uid) {
        return this.productCategoryService.getProductCategorieById(uid);
    }

    @GetMapping("/get_category_name/{name}")
    public ProductCategory getCategoryByName(@PathVariable("name") String name) {
        return this.productCategoryService.getProductCategorieByName(name);
    }

    @DeleteMapping("/delete_by_id/{id}")
    public boolean deleteCategoryById(@PathVariable("id") String id) {
        return this.productCategoryService.deleteById(id);
    }
}