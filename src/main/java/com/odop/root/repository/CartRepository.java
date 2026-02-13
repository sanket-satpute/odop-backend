package com.odop.root.repository;

import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;
import java.util.List;

import com.odop.root.models.Cart;

@Repository
public interface CartRepository extends MongoRepository<Cart, String> {

    Cart findByCartId(String cartId);

    List<Cart> findByCustomerId(String customerId);

    List<Cart> findByVendorId(String vendorId);

    List<Cart> findByProductIds(String productIds);
    
    List<Cart> findByProductId(String productId);

    List<Cart> findByApproval(boolean approval);
}
