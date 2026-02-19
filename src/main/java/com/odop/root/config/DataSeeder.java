package com.odop.root.config;

import com.odop.root.models.*;
import com.odop.root.odopfeatures.artisans.model.ArtisanStory;
import com.odop.root.odopfeatures.artisans.repository.ArtisanStoryRepository;
import com.odop.root.odopfeatures.crafts.model.CraftCategory;
import com.odop.root.odopfeatures.crafts.repository.CraftCategoryRepository;
import com.odop.root.odopfeatures.districtmap.model.DistrictInfo;
import com.odop.root.odopfeatures.districtmap.repository.DistrictInfoRepository;
import com.odop.root.odopfeatures.festivals.model.FestivalCollection;
import com.odop.root.odopfeatures.festivals.repository.FestivalCollectionRepository;
import com.odop.root.odopfeatures.govschemes.model.GovernmentScheme;
import com.odop.root.odopfeatures.govschemes.repository.GovernmentSchemeRepository;
import com.odop.root.repository.*;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.CommandLineRunner;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.UUID;

@Component
@RequiredArgsConstructor
@Slf4j
public class DataSeeder implements CommandLineRunner {

    private final AdminRepository adminRepository;
    private final CustomerRepository customerRepository;
    private final VendorRepository vendorRepository;
    private final ProductRepository productRepository;
    private final ProductCategoryRepository productCategoryRepository;

    private final ArtisanStoryRepository artisanStoryRepository;
    private final CraftCategoryRepository craftCategoryRepository;
    private final DistrictInfoRepository districtInfoRepository;
    private final FestivalCollectionRepository festivalCollectionRepository;
    private final GovernmentSchemeRepository governmentSchemeRepository;

    private final PasswordEncoder passwordEncoder;

    @Override
    public void run(String... args) throws Exception {
        // Check if data exists in any major collection
        if (isDatabaseEmpty()) {
            log.info("Database appears to be empty. Seeding with initial data...");
            seedData();
            log.info("Data seeding completed successfully.");
        } else {
            log.info("Database already contains data. Skipping seeding.");
        }
    }

    private boolean isDatabaseEmpty() {
        return adminRepository.count() == 0 &&
                customerRepository.count() == 0 &&
                vendorRepository.count() == 0;
    }

    private void seedData() {
        seedFeatureModels();
        seedAdmins();
        List<Vendor> vendors = seedVendors();
        seedCustomers();
        seedProducts(vendors);
    }

    private void seedFeatureModels() {
        log.info("Seeding feature models...");

        // 1. Craft Categories
        if (craftCategoryRepository.count() == 0) {
            List<CraftCategory> categories = CraftCategory.getDefaultCategories();
            craftCategoryRepository.saveAll(categories);

            // Sync with Product Categories
            for (CraftCategory cc : categories) {
                ProductCategory pc = ProductCategory.builder()
                        .prodCategoryId(UUID.randomUUID().toString())
                        .categoryName(cc.getName())
                        .categoryDescription(cc.getDescription())
                        .categoryImageURL("https://source.unsplash.com/random/300x200?craft," + cc.getSlug())
                        .createdAt(LocalDateTime.now())
                        .updatedAt(LocalDateTime.now())
                        .build();
                productCategoryRepository.save(pc);
            }
            log.info("Seeded {} craft categories.", categories.size());
        }

        // 2. Artisan Stories
        if (artisanStoryRepository.count() == 0) {
            List<ArtisanStory> stories = ArtisanStory.getSampleStories();
            // Add dummy images
            for (ArtisanStory story : stories) {
                story.setProfileImageUrl("https://source.unsplash.com/random/200x200?portrait,indian");
                story.setCoverImageUrl("https://source.unsplash.com/random/800x400?craft,workshop");
            }
            artisanStoryRepository.saveAll(stories);
            log.info("Seeded {} artisan stories.", stories.size());
        }

        // 3. District Info
        if (districtInfoRepository.count() == 0) {
            List<DistrictInfo> districts = DistrictInfo.getSampleDistricts();
            districtInfoRepository.saveAll(districts);
            log.info("Seeded {} districts.", districts.size());
        }

        // 4. Festival Collections
        if (festivalCollectionRepository.count() == 0) {
            List<FestivalCollection> festivals = FestivalCollection.getDefaultFestivals();
            for (FestivalCollection festival : festivals) {
                festival.setHeroImageUrl("https://source.unsplash.com/random/1200x500?festival,india");
                festival.setBannerImageUrl("https://source.unsplash.com/random/800x300?celebration");
            }
            festivalCollectionRepository.saveAll(festivals);
            log.info("Seeded {} festival collections.", festivals.size());
        }

        // 5. Government Schemes
        if (governmentSchemeRepository.count() == 0) {
            List<GovernmentScheme> schemes = GovernmentScheme.getDefaultSchemes();
            governmentSchemeRepository.saveAll(schemes);
            log.info("Seeded {} government schemes.", schemes.size());
        }
    }

    private void seedAdmins() {
        if (adminRepository.count() == 0) {
            List<Admin> admins = Arrays.asList(
                    Admin.builder()
                            .adminId(UUID.randomUUID().toString())
                            .fullName("Super Admin")
                            .emailAddress("admin@odop.com")
                            .password(passwordEncoder.encode("123456789"))
                            .contactNumber(9876543210L)
                            .positionAndRole("SUPER_ADMIN")
                            .active(true)
                            .createdAt(LocalDateTime.now())
                            .updatedAt(LocalDateTime.now())
                            .roles(Collections.singletonList("ROLE_ADMIN"))
                            .build(),
                    Admin.builder()
                            .adminId(UUID.randomUUID().toString())
                            .fullName("Manager One")
                            .emailAddress("manager1@odop.com")
                            .password(passwordEncoder.encode("123456789"))
                            .contactNumber(9876543211L)
                            .positionAndRole("MANAGER")
                            .active(true)
                            .createdAt(LocalDateTime.now())
                            .updatedAt(LocalDateTime.now())
                            .roles(Collections.singletonList("ROLE_ADMIN"))
                            .build());
            adminRepository.saveAll(admins);
            log.info("Seeded admins.");
        }
    }

    private List<Vendor> seedVendors() {
        if (vendorRepository.count() == 0) {
            List<Vendor> vendors = new ArrayList<>();
            String[] craftTypes = { "Pottery", "Weaving", "Wood Carving", "Metal Work", "Painting" };
            String[] locations = { "Jaipur, Rajasthan", "Varanasi, UP", "Kutch, Gujarat", "Mysore, Karnataka",
                    "Kolkata, WB" };

            for (int i = 1; i <= 10; i++) {
                String craft = craftTypes[i % craftTypes.length];
                String location = locations[i % locations.length];

                Vendor vendor = Vendor.builder()
                        .vendorId(UUID.randomUUID().toString())
                        .shoppeeName("Artisan Shop " + i)
                        .shopkeeperName("Vendor " + i)
                        .emailAddress("vendor" + i + "@odop.com")
                        .password(passwordEncoder.encode("123456789"))
                        .contactNumber(9000000000L + i)
                        .shoppeeAddress("Market Street " + i)
                        .locationDistrict(location.split(",")[0])
                        .locationState(location.split(",")[1].trim())
                        .pinCode("10000" + i)
                        .status(i % 3 == 0 ? "PENDING" : "VERIFIED")
                        .verified(i % 3 != 0)
                        .businessDescription("Specializing in authentic " + craft)
                        .profilePictureUrl("https://source.unsplash.com/random/200x200?face,indian," + i)
                        .shopImages(Arrays.asList(
                                "https://source.unsplash.com/random/400x300?shop," + craft,
                                "https://source.unsplash.com/random/400x300?crafts"))
                        .ratings(4.0 + (i % 2))
                        .reviewCount(10 * i)
                        .vendorType(i % 2 == 0 ? "SHG" : "Individual")
                        .roles(Collections.singletonList("ROLE_VENDOR"))
                        .createdAt(LocalDateTime.now())
                        .updatedAt(LocalDateTime.now())
                        .build();
                vendors.add(vendor);
            }
            return vendorRepository.saveAll(vendors);
        }
        return vendorRepository.findAll();
    }

    private void seedCustomers() {
        if (customerRepository.count() == 0) {
            List<Customer> customers = new ArrayList<>();
            for (int i = 1; i <= 10; i++) {
                Customer customer = Customer.builder()
                        .customerId(UUID.randomUUID().toString())
                        .fullName("Customer " + i)
                        .emailAddress("customer" + i + "@odop.com")
                        .password(passwordEncoder.encode("123456789"))
                        .contactNumber(8000000000L + i)
                        .address("House No " + i + ", Civil Lines")
                        .city("Delhi")
                        .state("New Delhi")
                        .pinCode("11000" + i)
                        .status("ACTIVE")
                        .createdAt(LocalDateTime.now())
                        .updatedAt(LocalDateTime.now())
                        .roles(Collections.singletonList("ROLE_CUSTOMER"))
                        .profilePictureUrl("https://source.unsplash.com/random/200x200?portrait," + i)
                        .build();
                customers.add(customer);
            }
            customerRepository.saveAll(customers);
            log.info("Seeded customers.");
        }
    }

    private void seedProducts(List<Vendor> vendors) {
        if (productRepository.count() == 0 && !vendors.isEmpty()) {
            List<Products> products = new ArrayList<>();
            List<ProductCategory> categories = productCategoryRepository.findAll();

            String[] productNames = {
                    "Handcrafted Terracotta Vase", "Banarasi Silk Saree", "Blue Pottery Plate",
                    "Wooden Carved Elephant", "Kutch Embroidered Shawl", "Madhubani Painting",
                    "Brass Oil Lamp", "Jute Wall Hanging", "Sandalwood Incense Holder", "Pashmina Shawl"
            };

            for (int i = 0; i < 20; i++) {
                Vendor vendor = vendors.get(i % vendors.size());
                String categoryId = categories.isEmpty() ? null
                        : categories.get(i % categories.size()).getProdCategoryId();
                String name = productNames[i % productNames.length] + " " + (i + 1);

                Products product = Products.builder()
                        .productId(UUID.randomUUID().toString())
                        .productName(name)
                        .productDescription("Authentic handcrafted " + name
                                + " made by skilled artisans. Perfect for home decor or gifting.")
                        .categoryId(categoryId)
                        .price(500.0 + (i * 150))
                        .productQuantity(10 + i)
                        .productImageURL("https://source.unsplash.com/random/400x400?craft," + (i + 1))
                        .vendorId(vendor.getVendorId())
                        .discount(i % 5 == 0 ? 10 : 0)
                        .rating(4 + (i % 2))
                        .stockStatus("IN_STOCK")
                        .originDistrict(vendor.getLocationDistrict())
                        .originState(vendor.getLocationState())
                        .giTagCertified(i % 3 == 0)
                        .approvalStatus("APPROVED")
                        .isActive(true)
                        .createdAt(LocalDateTime.now())
                        .updatedAt(LocalDateTime.now())
                        .build();
                products.add(product);
            }
            productRepository.saveAll(products);
            log.info("Seeded products.");
        }
    }
}
