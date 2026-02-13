package com.odop.root.services;

import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.CommandLineRunner;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import com.odop.root.models.*;
import com.odop.root.repository.*;

/**
 * Data Initializer Service - Seeds dummy data for development and testing
 * This runs automatically when the application starts
 */
@Component
public class DataInitializerService implements CommandLineRunner {

    private static final Logger logger = LogManager.getLogger(DataInitializerService.class);

    @Autowired
    private AdminRepository adminRepository;

    @Autowired
    private VendorRepository vendorRepository;

    @Autowired
    private CustomerRepository customerRepository;

    @Autowired
    private ProductCategoryRepository categoryRepository;

    @Autowired
    private ProductRepository productRepository;

    @Autowired
    private OrderRepository orderRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @Override
    public void run(String... args) throws Exception {
        // Always run migration to fix any users with missing roles
        migrateExistingUsers();
        
        logger.info("Checking data initialization requirements...");
        
        // Check and initialize each collection independently
        if (adminRepository.count() == 0) {
            logger.info("Initializing admin data...");
            initializeAdmins();
        }
        
        if (categoryRepository.count() == 0) {
            logger.info("Initializing category data...");
            initializeCategories();
        }
        
        if (vendorRepository.count() == 0) {
            logger.info("Initializing vendor data...");
            initializeVendors();
        }
        
        if (productRepository.count() == 0) {
            logger.info("Initializing product data...");
            initializeProducts();
        }
        
        if (customerRepository.count() == 0) {
            logger.info("Initializing customer data...");
            initializeCustomers();
        }
        
        if (orderRepository.count() == 0 && customerRepository.count() > 0 && vendorRepository.count() > 0 && productRepository.count() > 0) {
            logger.info("Initializing sample order data...");
            initializeOrders();
        }
        
        logger.info("Data initialization check complete!");
    }

    /**
     * Migration method to fix existing users with missing roles
     * This ensures all users can authenticate properly
     */
    private void migrateExistingUsers() {
        logger.info("Running user migration to fix missing roles...");
        int vendorFixed = 0;
        int customerFixed = 0;
        int adminFixed = 0;

        // Fix vendors with missing roles
        List<Vendor> allVendors = vendorRepository.findAll();
        for (Vendor vendor : allVendors) {
            boolean needsSave = false;
            
            if (vendor.getRoles() == null || vendor.getRoles().isEmpty()) {
                vendor.setRoles(List.of("ROLE_VENDOR"));
                needsSave = true;
                vendorFixed++;
                logger.info("Fixed roles for vendor: {}", vendor.getEmailAddress());
            }
            
            // Fix password if not encoded (doesn't start with $2a$)
            if (vendor.getPassword() != null && !vendor.getPassword().startsWith("$2a$")) {
                vendor.setPassword(passwordEncoder.encode(vendor.getPassword()));
                needsSave = true;
                logger.info("Fixed password encoding for vendor: {}", vendor.getEmailAddress());
            }
            
            if (needsSave) {
                vendor.setUpdatedAt(LocalDateTime.now());
                vendorRepository.save(vendor);
            }
        }

        // Fix customers with missing roles
        List<Customer> allCustomers = customerRepository.findAll();
        for (Customer customer : allCustomers) {
            boolean needsSave = false;
            
            if (customer.getRoles() == null || customer.getRoles().isEmpty()) {
                customer.setRoles(List.of("ROLE_CUSTOMER"));
                needsSave = true;
                customerFixed++;
                logger.info("Fixed roles for customer: {}", customer.getEmailAddress());
            }
            
            // Fix password if not encoded (doesn't start with $2a$)
            if (customer.getPassword() != null && !customer.getPassword().startsWith("$2a$")) {
                customer.setPassword(passwordEncoder.encode(customer.getPassword()));
                needsSave = true;
                logger.info("Fixed password encoding for customer: {}", customer.getEmailAddress());
            }
            
            if (needsSave) {
                customer.setUpdatedAt(LocalDateTime.now());
                customerRepository.save(customer);
            }
        }

        // Fix admins with missing roles
        List<Admin> allAdmins = adminRepository.findAll();
        for (Admin admin : allAdmins) {
            boolean needsSave = false;
            
            if (admin.getRoles() == null || admin.getRoles().isEmpty()) {
                admin.setRoles(List.of("ROLE_ADMIN"));
                needsSave = true;
                adminFixed++;
                logger.info("Fixed roles for admin: {}", admin.getEmailAddress());
            }
            
            if (needsSave) {
                admin.setUpdatedAt(LocalDateTime.now());
                adminRepository.save(admin);
            }
        }

        if (vendorFixed > 0 || customerFixed > 0 || adminFixed > 0) {
            logger.info("Migration complete: Fixed {} vendors, {} customers, {} admins", 
                vendorFixed, customerFixed, adminFixed);
        } else {
            logger.info("Migration complete: No users needed fixing.");
        }
    }

    private void initializeAdmins() {
        Admin admin = new Admin();
        admin.setFullName("ODOP Administrator");
        admin.setEmailAddress("admin@odop.gov.in");
        admin.setPassword(passwordEncoder.encode("admin123"));
        admin.setContactNumber(9876543210L);
        admin.setPositionAndRole("Super Admin");
        admin.setActive(true);
        admin.setRoles(List.of("ROLE_ADMIN"));
        admin.setCreatedAt(LocalDateTime.now());
        admin.setUpdatedAt(LocalDateTime.now());
        adminRepository.save(admin);
        logger.info("Created admin: {}", admin.getEmailAddress());
    }

    private void initializeCategories() {
        // Main Categories
        ProductCategory textiles = createCategory("Textiles & Fabrics", 
            "Traditional handwoven textiles, sarees, and fabrics", 
            "https://example.com/textiles.jpg", null);
        
        createCategory("Handicrafts", 
            "Traditional handmade crafts and art pieces", 
            "https://example.com/handicrafts.jpg", null);
        
        ProductCategory foodProducts = createCategory("Food Products", 
            "Traditional food items, spices, and delicacies", 
            "https://example.com/food.jpg", null);
        
        createCategory("Pottery & Ceramics", 
            "Traditional pottery, terracotta, and ceramic items", 
            "https://example.com/pottery.jpg", null);
        
        createCategory("Jewelry & Accessories", 
            "Traditional jewelry and fashion accessories", 
            "https://example.com/jewelry.jpg", null);
        
        createCategory("Wood Crafts", 
            "Traditional woodwork and wooden artifacts", 
            "https://example.com/woodcraft.jpg", null);

        // Subcategories for Textiles
        createCategory("Sarees", "Traditional Indian sarees", null, textiles.getProdCategoryId());
        createCategory("Shawls", "Handwoven shawls and stoles", null, textiles.getProdCategoryId());
        createCategory("Durries & Rugs", "Traditional floor coverings", null, textiles.getProdCategoryId());

        // Subcategories for Food Products
        createCategory("Spices", "Traditional spices and masalas", null, foodProducts.getProdCategoryId());
        createCategory("Pickles", "Traditional pickles and preserves", null, foodProducts.getProdCategoryId());
        createCategory("Sweets", "Traditional Indian sweets", null, foodProducts.getProdCategoryId());

        logger.info("Created {} categories", categoryRepository.count());
    }

    private ProductCategory createCategory(String name, String description, String imageUrl, String parentId) {
        ProductCategory category = new ProductCategory();
        category.setCategoryName(name);
        category.setCategoryDescription(description);
        category.setCategoryImageURL(imageUrl);
        category.setParentCategoryId(parentId);
        category.setCreatedAt(LocalDateTime.now());
        category.setUpdatedAt(LocalDateTime.now());
        return categoryRepository.save(category);
    }

    private void initializeVendors() {
        // Vendor 1 - Chanderi Saree Artisan from MP
        Vendor vendor1 = Vendor.builder()
            .shoppeeName("Chanderi Handloom House")
            .shopkeeperName("Ramesh Kumar Patel")
            .emailAddress("chanderi.handloom@example.com")
            .password(passwordEncoder.encode("vendor123"))
            .contactNumber(9876543211L)
            .shoppeeAddress("Main Market, Chanderi")
            .locationDistrict("Ashoknagar")
            .locationState("Madhya Pradesh")
            .pinCode("473446")
            .businessRegistryNumber("MP12345678")
            .status("verified")
            .verified(true)
            .giTagCertified(true)
            .isVerified(true)
            .vendorType("medium")
            .shopDescription("Traditional Chanderi saree weaving since 3 generations")
            .specializations(Arrays.asList("Chanderi Sarees", "Chanderi Fabric"))
            .deliveryAvailable(true)
            .deliveryRadiusInKm(500.0)
            .deliveryCharges(100.0)
            .freeDeliveryAbove(2000.0)
            .deliveryOptions(Arrays.asList("post", "courier"))
            .isPhysicalVisitAllowed(true)
            .shopTimings("10 AM - 7 PM")
            .shopClosedDays("Sunday")
            .ratings(4.5)
            .reviewCount(25)
            .roles(List.of("ROLE_VENDOR"))
            .createdAt(LocalDateTime.now())
            .updatedAt(LocalDateTime.now())
            .build();
        vendorRepository.save(vendor1);

        // Vendor 2 - Banarasi Silk from UP
        Vendor vendor2 = Vendor.builder()
            .shoppeeName("Kashi Silk Emporium")
            .shopkeeperName("Suresh Chandra Gupta")
            .emailAddress("kashi.silk@example.com")
            .password(passwordEncoder.encode("vendor123"))
            .contactNumber(9876543212L)
            .shoppeeAddress("Vishwanath Gali, Varanasi")
            .locationDistrict("Varanasi")
            .locationState("Uttar Pradesh")
            .pinCode("221001")
            .businessRegistryNumber("UP98765432")
            .status("verified")
            .verified(true)
            .giTagCertified(true)
            .isVerified(true)
            .vendorType("large")
            .shopDescription("Premium Banarasi silk sarees and fabrics")
            .specializations(Arrays.asList("Banarasi Sarees", "Silk Fabrics", "Brocade"))
            .deliveryAvailable(true)
            .deliveryRadiusInKm(1000.0)
            .deliveryCharges(150.0)
            .freeDeliveryAbove(5000.0)
            .deliveryOptions(Arrays.asList("courier", "post"))
            .isPhysicalVisitAllowed(true)
            .shopTimings("9 AM - 8 PM")
            .shopClosedDays("None")
            .ratings(4.8)
            .reviewCount(150)
            .roles(List.of("ROLE_VENDOR"))
            .createdAt(LocalDateTime.now())
            .updatedAt(LocalDateTime.now())
            .build();
        vendorRepository.save(vendor2);

        // Vendor 3 - Blue Pottery from Rajasthan
        Vendor vendor3 = Vendor.builder()
            .shoppeeName("Jaipur Blue Art")
            .shopkeeperName("Mohammad Salim")
            .emailAddress("jaipur.blue@example.com")
            .password(passwordEncoder.encode("vendor123"))
            .contactNumber(9876543213L)
            .shoppeeAddress("Johari Bazaar, Jaipur")
            .locationDistrict("Jaipur")
            .locationState("Rajasthan")
            .pinCode("302001")
            .businessRegistryNumber("RJ55667788")
            .status("verified")
            .verified(true)
            .giTagCertified(true)
            .isVerified(true)
            .vendorType("medium")
            .shopDescription("Authentic Jaipur Blue Pottery - Turkish-Persian art tradition")
            .specializations(Arrays.asList("Blue Pottery", "Decorative Items", "Tiles"))
            .deliveryAvailable(true)
            .deliveryRadiusInKm(800.0)
            .deliveryCharges(200.0)
            .freeDeliveryAbove(3000.0)
            .deliveryOptions(Arrays.asList("courier"))
            .isPhysicalVisitAllowed(true)
            .shopTimings("10 AM - 6 PM")
            .shopClosedDays("Friday")
            .ratings(4.6)
            .reviewCount(80)
            .roles(List.of("ROLE_VENDOR"))
            .createdAt(LocalDateTime.now())
            .updatedAt(LocalDateTime.now())
            .build();
        vendorRepository.save(vendor3);

        // Vendor 4 - Kashmiri Pashmina from J&K
        Vendor vendor4 = Vendor.builder()
            .shoppeeName("Kashmir Pashmina Palace")
            .shopkeeperName("Ghulam Ahmad Mir")
            .emailAddress("kashmir.pashmina@example.com")
            .password(passwordEncoder.encode("vendor123"))
            .contactNumber(9876543214L)
            .shoppeeAddress("Residency Road, Srinagar")
            .locationDistrict("Srinagar")
            .locationState("Jammu and Kashmir")
            .pinCode("190001")
            .businessRegistryNumber("JK11223344")
            .status("verified")
            .verified(true)
            .giTagCertified(true)
            .isVerified(true)
            .vendorType("large")
            .shopDescription("Authentic Kashmiri Pashmina shawls and stoles")
            .specializations(Arrays.asList("Pashmina Shawls", "Kashmiri Embroidery", "Kani Shawls"))
            .deliveryAvailable(true)
            .deliveryRadiusInKm(2000.0)
            .deliveryCharges(250.0)
            .freeDeliveryAbove(10000.0)
            .deliveryOptions(Arrays.asList("courier", "post"))
            .isPhysicalVisitAllowed(true)
            .shopTimings("10 AM - 7 PM")
            .shopClosedDays("Friday")
            .ratings(4.9)
            .reviewCount(200)
            .roles(List.of("ROLE_VENDOR"))
            .createdAt(LocalDateTime.now())
            .updatedAt(LocalDateTime.now())
            .build();
        vendorRepository.save(vendor4);

        logger.info("Created {} vendors", vendorRepository.count());
    }

    private void initializeProducts() {
        List<Vendor> vendors = vendorRepository.findAll();
        List<ProductCategory> categories = categoryRepository.findAll();

        if (vendors.isEmpty() || categories.isEmpty()) {
            logger.warn("No vendors or categories found. Skipping product creation.");
            return;
        }

        ProductCategory textilesCat = categories.stream()
            .filter(c -> "Textiles & Fabrics".equals(c.getCategoryName()))
            .findFirst().orElse(categories.get(0));
        
        ProductCategory potteryCat = categories.stream()
            .filter(c -> "Pottery & Ceramics".equals(c.getCategoryName()))
            .findFirst().orElse(categories.get(0));

        Vendor chanderiVendor = vendors.stream()
            .filter(v -> v.getShoppeeName().contains("Chanderi"))
            .findFirst().orElse(vendors.get(0));
        
        Vendor banarsiVendor = vendors.stream()
            .filter(v -> v.getShoppeeName().contains("Kashi"))
            .findFirst().orElse(vendors.get(0));
        
        Vendor potteryVendor = vendors.stream()
            .filter(v -> v.getShoppeeName().contains("Blue"))
            .findFirst().orElse(vendors.get(0));
        
        Vendor kashmiryVendor = vendors.stream()
            .filter(v -> v.getShoppeeName().contains("Kashmir"))
            .findFirst().orElse(vendors.get(0));

        // Product 1 - Chanderi Saree
        createProduct("Chanderi Silk Saree - Royal Blue", 
            "Handwoven Chanderi silk saree with zari border. Traditional motifs with modern elegance.",
            textilesCat.getProdCategoryId(), null, 8500.0, 10, 
            chanderiVendor.getVendorId(),
            "Ashoknagar", "Madhya Pradesh", "473446",
            "चंदेरी साड़ी", "GI/MP/2005/001", true,
            "Chanderi weaving dates back to 2nd-3rd century BC. Known for sheer texture and lightweight feel.",
            "Handloom Weaving", "Ramesh Kumar Patel", "Silk, Zari, Cotton",
            Arrays.asList("chanderi", "saree", "silk", "handloom", "GI tagged"),
            95, 250);

        // Product 2 - Banarasi Silk Saree
        createProduct("Banarasi Katan Silk Saree - Maroon Gold",
            "Pure Katan silk Banarasi saree with intricate brocade work. Perfect for weddings.",
            textilesCat.getProdCategoryId(), null, 25000.0, 5,
            banarsiVendor.getVendorId(),
            "Varanasi", "Uttar Pradesh", "221001",
            "बनारसी साड़ी", "GI/UP/2009/003", true,
            "Banarasi sarees are among India's finest. Mughal-era tradition perfected over centuries.",
            "Brocade Weaving", "Master Weavers of Kashi", "Pure Silk, Gold Zari",
            Arrays.asList("banarasi", "silk", "bridal", "wedding", "GI tagged"),
            98, 500);

        // Product 3 - Blue Pottery Vase
        createProduct("Jaipur Blue Pottery Flower Vase",
            "Handcrafted blue pottery vase with traditional floral patterns. Lead-free and eco-friendly.",
            potteryCat.getProdCategoryId(), null, 1800.0, 25,
            potteryVendor.getVendorId(),
            "Jaipur", "Rajasthan", "302001",
            "नीली मिट्टी का फूलदान", "GI/RJ/2008/002", true,
            "Blue Pottery is a Persian-Turkish craft brought to Jaipur. Made without clay using quartz stone powder.",
            "Pottery Making", "Mohammad Salim", "Quartz, Multani Mitti, Gum",
            Arrays.asList("blue pottery", "vase", "decor", "handmade", "GI tagged"),
            88, 180);

        // Product 4 - Kashmiri Pashmina Shawl
        createProduct("Pure Pashmina Shawl - Natural Ivory",
            "100% pure Pashmina shawl from Kashmir. Hand-spun and hand-woven. Exceptionally soft and warm.",
            textilesCat.getProdCategoryId(), null, 35000.0, 8,
            kashmiryVendor.getVendorId(),
            "Srinagar", "Jammu and Kashmir", "190001",
            "पश्मीना शॉल", "GI/JK/2007/001", true,
            "Pashmina comes from Changthangi goats of Ladakh. World's finest natural fiber after vicuña.",
            "Hand Spinning & Weaving", "Ghulam Ahmad Mir", "Pure Pashmina Wool",
            Arrays.asList("pashmina", "shawl", "kashmir", "luxury", "GI tagged"),
            99, 120);

        // Product 5 - Chanderi Cotton Saree (non-GI for variety)
        createProduct("Chanderi Cotton Saree - Peacock Design",
            "Light cotton Chanderi saree with traditional peacock motifs. Perfect for daily wear.",
            textilesCat.getProdCategoryId(), null, 3500.0, 20,
            chanderiVendor.getVendorId(),
            "Ashoknagar", "Madhya Pradesh", "473446",
            "चंदेरी सूती साड़ी", null, false,
            "Cotton variant of famous Chanderi. Comfortable for all seasons.",
            "Handloom Weaving", "Local Artisans", "Cotton, Silk Border",
            Arrays.asList("chanderi", "cotton", "saree", "daily wear"),
            75, 400);

        // Product 6 - Blue Pottery Plate Set
        createProduct("Blue Pottery Dinner Plate Set (6 pieces)",
            "Set of 6 handcrafted blue pottery dinner plates. Microwave and dishwasher safe.",
            potteryCat.getProdCategoryId(), null, 4500.0, 15,
            potteryVendor.getVendorId(),
            "Jaipur", "Rajasthan", "302001",
            "नीली थाली सेट", "GI/RJ/2008/002", true,
            "Traditional craft meeting modern utility. Each piece is unique.",
            "Pottery Making", "Jaipur Artisan Collective", "Quartz, Glazing Materials",
            Arrays.asList("blue pottery", "plates", "dining", "GI tagged"),
            82, 95);

        logger.info("Created {} products", productRepository.count());
    }

    private void createProduct(String name, String description, String categoryId, String subCategoryId,
                                double price, long quantity, String vendorId,
                                String district, String state, String pinCode,
                                String localName, String giTagNumber, boolean giTagCertified,
                                String originStory, String craftType, String madeBy, String materialsUsed,
                                List<String> tags, int popularityScore, int totalSold) {
        Products product = new Products();
        product.setProductName(name);
        product.setProductDescription(description);
        product.setCategoryId(categoryId);
        product.setSubCategoryId(subCategoryId);
        product.setPrice(price);
        product.setProductQuantity(quantity);
        product.setProductImageURL("https://example.com/products/" + name.toLowerCase().replace(" ", "-") + ".jpg");
        product.setDiscount(10);
        product.setPromotionEnabled(true);
        product.setSpecification("Handcrafted authentic product");
        product.setWarranty("Quality guaranteed");
        product.setRating(4);
        product.setVendorId(vendorId);
        product.setTags(tags);
        product.setStockStatus("In Stock");
        product.setOriginDistrict(district);
        product.setOriginState(state);
        product.setOriginPinCode(pinCode);
        product.setLocalName(localName);
        product.setGiTagNumber(giTagNumber);
        product.setGiTagCertified(giTagCertified);
        product.setGiTagCertificateUrl(giTagCertified ? "https://example.com/gi-cert/" + giTagNumber + ".pdf" : null);
        product.setOriginStory(originStory);
        product.setCraftType(craftType);
        product.setMadeBy(madeBy);
        product.setMaterialsUsed(materialsUsed);
        product.setPopularityScore(popularityScore);
        product.setTotalSold(totalSold);
        product.setCreatedAt(LocalDateTime.now());
        product.setUpdatedAt(LocalDateTime.now());
        productRepository.save(product);
    }

    private void initializeCustomers() {
        // Customer 1
        Customer customer1 = new Customer();
        customer1.setFullName("Priya Sharma");
        customer1.setEmailAddress("priya.sharma@example.com");
        customer1.setPassword(passwordEncoder.encode("customer123"));
        customer1.setContactNumber(9988776655L);
        customer1.setAddress("123, MG Road");
        customer1.setDistrict("Indore");
        customer1.setCity("Indore");
        customer1.setState("Madhya Pradesh");
        customer1.setPinCode("452001");
        customer1.setStatus("active");
        customer1.setRoles(List.of("ROLE_CUSTOMER"));
        customer1.setCreatedAt(LocalDateTime.now());
        customer1.setUpdatedAt(LocalDateTime.now());
        customerRepository.save(customer1);

        // Customer 2
        Customer customer2 = new Customer();
        customer2.setFullName("Rahul Verma");
        customer2.setEmailAddress("rahul.verma@example.com");
        customer2.setPassword(passwordEncoder.encode("customer123"));
        customer2.setContactNumber(9988776644L);
        customer2.setAddress("456, Civil Lines");
        customer2.setDistrict("Lucknow");
        customer2.setCity("Lucknow");
        customer2.setState("Uttar Pradesh");
        customer2.setPinCode("226001");
        customer2.setStatus("active");
        customer2.setRoles(List.of("ROLE_CUSTOMER"));
        customer2.setCreatedAt(LocalDateTime.now());
        customer2.setUpdatedAt(LocalDateTime.now());
        customerRepository.save(customer2);

        // Customer 3
        Customer customer3 = new Customer();
        customer3.setFullName("Anjali Patel");
        customer3.setEmailAddress("anjali.patel@example.com");
        customer3.setPassword(passwordEncoder.encode("customer123"));
        customer3.setContactNumber(9988776633L);
        customer3.setAddress("789, Satellite Road");
        customer3.setDistrict("Ahmedabad");
        customer3.setCity("Ahmedabad");
        customer3.setState("Gujarat");
        customer3.setPinCode("380015");
        customer3.setStatus("active");
        customer3.setRoles(List.of("ROLE_CUSTOMER"));
        customer3.setCreatedAt(LocalDateTime.now());
        customer3.setUpdatedAt(LocalDateTime.now());
        customerRepository.save(customer3);

        logger.info("Created {} customers", customerRepository.count());
    }

    private void initializeOrders() {
        List<Customer> customers = customerRepository.findAll();
        List<Vendor> vendors = vendorRepository.findAll();
        List<Products> products = productRepository.findAll();

        if (customers.isEmpty() || vendors.isEmpty() || products.isEmpty()) {
            logger.warn("Missing customers, vendors, or products. Skipping order creation.");
            return;
        }

        // Get first few customers and vendors
        Customer customer1 = customers.get(0);
        Vendor vendor1 = vendors.stream()
            .filter(v -> v.getShoppeeName() != null && v.getShoppeeName().contains("Chanderi"))
            .findFirst().orElse(vendors.get(0));
        
        Products product1 = products.stream()
            .filter(p -> p.getProductName() != null && p.getProductName().contains("Chanderi"))
            .findFirst().orElse(products.get(0));

        // Order 1 - Delivered
        Order order1 = Order.builder()
            .customerId(customer1.getCustomerId())
            .vendorId(vendor1.getVendorId())
            .orderItems(List.of(
                OrderItem.builder()
                    .productId(product1.getProductId())
                    .productName(product1.getProductName())
                    .quantity(1)
                    .unitPrice(product1.getPrice())
                    .totalPrice(product1.getPrice())
                    .build()
            ))
            .totalAmount(product1.getPrice())
            .discountAmount(product1.getPrice() * 0.1)
            .deliveryCharges(100.0)
            .finalAmount(product1.getPrice() - (product1.getPrice() * 0.1) + 100)
            .shippingAddress(customer1.getAddress())
            .shippingDistrict(customer1.getDistrict())
            .shippingState(customer1.getState())
            .shippingPinCode(customer1.getPinCode())
            .shippingContactNumber(customer1.getContactNumber())
            .orderStatus("DELIVERED")
            .paymentStatus("PAID")
            .paymentMethod("UPI")
            .paymentTransactionId("TXN" + System.currentTimeMillis())
            .trackingNumber("TRACK001")
            .courierPartner("BlueDart")
            .estimatedDeliveryDate(LocalDateTime.now().minusDays(2))
            .actualDeliveryDate(LocalDateTime.now().minusDays(1))
            .createdAt(LocalDateTime.now().minusDays(10))
            .updatedAt(LocalDateTime.now().minusDays(1))
            .build();
        orderRepository.save(order1);

        // Order 2 - Processing
        if (customers.size() > 1 && products.size() > 1) {
            Customer customer2 = customers.get(1);
            Products product2 = products.size() > 1 ? products.get(1) : products.get(0);
            Vendor vendor2 = vendors.stream()
                .filter(v -> v.getShoppeeName() != null && v.getShoppeeName().contains("Kashi"))
                .findFirst().orElse(vendors.get(0));

            Order order2 = Order.builder()
                .customerId(customer2.getCustomerId())
                .vendorId(vendor2.getVendorId())
                .orderItems(List.of(
                    OrderItem.builder()
                        .productId(product2.getProductId())
                        .productName(product2.getProductName())
                        .quantity(1)
                        .unitPrice(product2.getPrice())
                        .totalPrice(product2.getPrice())
                        .build()
                ))
                .totalAmount(product2.getPrice())
                .discountAmount(0)
                .deliveryCharges(150.0)
                .finalAmount(product2.getPrice() + 150)
                .shippingAddress(customer2.getAddress())
                .shippingDistrict(customer2.getDistrict())
                .shippingState(customer2.getState())
                .shippingPinCode(customer2.getPinCode())
                .shippingContactNumber(customer2.getContactNumber())
                .orderStatus("PROCESSING")
                .paymentStatus("PAID")
                .paymentMethod("CARD")
                .paymentTransactionId("TXN" + (System.currentTimeMillis() + 1))
                .trackingNumber("TRACK002")
                .courierPartner("DTDC")
                .estimatedDeliveryDate(LocalDateTime.now().plusDays(3))
                .createdAt(LocalDateTime.now().minusDays(3))
                .updatedAt(LocalDateTime.now())
                .build();
            orderRepository.save(order2);
        }

        // Order 3 - Shipped
        if (customers.size() > 2 && products.size() > 2) {
            Customer customer3 = customers.get(2);
            Products product3 = products.size() > 2 ? products.get(2) : products.get(0);
            Vendor vendor3 = vendors.stream()
                .filter(v -> v.getShoppeeName() != null && v.getShoppeeName().contains("Blue"))
                .findFirst().orElse(vendors.get(0));

            Order order3 = Order.builder()
                .customerId(customer3.getCustomerId())
                .vendorId(vendor3.getVendorId())
                .orderItems(List.of(
                    OrderItem.builder()
                        .productId(product3.getProductId())
                        .productName(product3.getProductName())
                        .quantity(2)
                        .unitPrice(product3.getPrice())
                        .totalPrice(product3.getPrice() * 2)
                        .build()
                ))
                .totalAmount(product3.getPrice() * 2)
                .discountAmount(product3.getPrice() * 0.15)
                .deliveryCharges(0)
                .finalAmount((product3.getPrice() * 2) - (product3.getPrice() * 0.15))
                .shippingAddress(customer3.getAddress())
                .shippingDistrict(customer3.getDistrict())
                .shippingState(customer3.getState())
                .shippingPinCode(customer3.getPinCode())
                .shippingContactNumber(customer3.getContactNumber())
                .orderStatus("SHIPPED")
                .paymentStatus("PAID")
                .paymentMethod("UPI")
                .paymentTransactionId("TXN" + (System.currentTimeMillis() + 2))
                .trackingNumber("TRACK003")
                .courierPartner("Delhivery")
                .estimatedDeliveryDate(LocalDateTime.now().plusDays(2))
                .createdAt(LocalDateTime.now().minusDays(5))
                .updatedAt(LocalDateTime.now().minusDays(1))
                .build();
            orderRepository.save(order3);
        }

        // Order 4 - Pending
        Order order4 = Order.builder()
            .customerId(customer1.getCustomerId())
            .vendorId(vendor1.getVendorId())
            .orderItems(List.of(
                OrderItem.builder()
                    .productId(product1.getProductId())
                    .productName(product1.getProductName())
                    .quantity(1)
                    .unitPrice(product1.getPrice())
                    .totalPrice(product1.getPrice())
                    .build()
            ))
            .totalAmount(product1.getPrice())
            .discountAmount(0)
            .deliveryCharges(100.0)
            .finalAmount(product1.getPrice() + 100)
            .shippingAddress(customer1.getAddress())
            .shippingDistrict(customer1.getDistrict())
            .shippingState(customer1.getState())
            .shippingPinCode(customer1.getPinCode())
            .shippingContactNumber(customer1.getContactNumber())
            .orderStatus("PENDING")
            .paymentStatus("PENDING")
            .paymentMethod("COD")
            .estimatedDeliveryDate(LocalDateTime.now().plusDays(7))
            .createdAt(LocalDateTime.now())
            .updatedAt(LocalDateTime.now())
            .build();
        orderRepository.save(order4);

        // Order 5 - Confirmed
        Order order5 = Order.builder()
            .customerId(customer1.getCustomerId())
            .vendorId(vendor1.getVendorId())
            .orderItems(List.of(
                OrderItem.builder()
                    .productId(product1.getProductId())
                    .productName(product1.getProductName())
                    .quantity(3)
                    .unitPrice(product1.getPrice())
                    .totalPrice(product1.getPrice() * 3)
                    .build()
            ))
            .totalAmount(product1.getPrice() * 3)
            .discountAmount(product1.getPrice() * 0.2)
            .deliveryCharges(0)
            .finalAmount((product1.getPrice() * 3) - (product1.getPrice() * 0.2))
            .shippingAddress(customer1.getAddress())
            .shippingDistrict(customer1.getDistrict())
            .shippingState(customer1.getState())
            .shippingPinCode(customer1.getPinCode())
            .shippingContactNumber(customer1.getContactNumber())
            .orderStatus("CONFIRMED")
            .paymentStatus("PAID")
            .paymentMethod("NET_BANKING")
            .paymentTransactionId("TXN" + (System.currentTimeMillis() + 3))
            .estimatedDeliveryDate(LocalDateTime.now().plusDays(5))
            .createdAt(LocalDateTime.now().minusDays(1))
            .updatedAt(LocalDateTime.now())
            .build();
        orderRepository.save(order5);

        logger.info("Created {} sample orders", orderRepository.count());
    }
}
