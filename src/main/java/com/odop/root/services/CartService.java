package com.odop.root.services;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import com.odop.root.models.Cart;
import com.odop.root.models.Products;
import com.odop.root.repository.CartRepository;
import com.odop.root.repository.ProductRepository;

@Service
public class CartService {

	private static final Logger logger = LogManager.getLogger(CartService.class);

	@Autowired
	CartRepository cart_repo;

	@Autowired
	ProductRepository products_repo;

	public Cart saveCart(Cart cart) {
		logger.info("Attempting to save cart for customer: {}, productId: {}", cart.getCustomerId(),
				cart.getProductId());
		if (cart.getProductId() != null && !cart.getProductId().isEmpty()) {
			Products p = products_repo.findById(cart.getProductId()).orElse(null);
			if (p != null) {
				logger.info("Found product: {}", p.getProductName());
				cart.setProduct(p);
			} else {
				logger.warn("Product not found for ID: {}", cart.getProductId());
			}
		} else {
			logger.warn("ProductId is null or empty");
		}
		return this.cart_repo.save(cart);
	}

	public List<Cart> getAllCarts() {
		return this.cart_repo.findAll();
	}

	public Cart getCartById(String cartid) {
		return this.cart_repo.findByCartId(cartid);
	}

	public List<Cart> getCartByVendorId(String vendorId) {
		return this.cart_repo.findByVendorId(vendorId);
	}

	public List<Cart> getCartByCustomerId(String customerId) {
		List<Cart> carts = this.cart_repo.findByCustomerId(customerId);
		logger.info("Found {} carts for customer {}", carts.size(), customerId);
		for (Cart c : carts) {
			logger.info("Cart {} - ProductId: {}, ProductRef: {}", c.getCartId(), c.getProductId(), c.getProduct());
		}
		return carts;
	}

	public List<Cart> getByProductId(String productId) {
		// Try both the legacy productIds array and the single productId field
		List<Cart> results = this.cart_repo.findByProductId(productId);
		if (results.isEmpty()) {
			results = this.cart_repo.findByProductIds(productId);
		}
		return results;
	}

	public List<Cart> findByApproval(boolean approval) {
		return this.cart_repo.findByApproval(approval);
	}

	public boolean deleteById(String id) {
		if (this.getCartById(id) != null) {
			this.cart_repo.deleteById(id);
			boolean result = (this.getCartById(id) == null);
			logger.debug("Cart result : {}", result);
			return result;
		}
		return false;
	}

}
