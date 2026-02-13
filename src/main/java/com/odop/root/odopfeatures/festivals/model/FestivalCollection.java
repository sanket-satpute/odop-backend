package com.odop.root.odopfeatures.festivals.model;

import lombok.*;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.index.Indexed;
import org.springframework.data.mongodb.core.mapping.Document;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.*;

/**
 * Festival Collection Model for ODOP Platform
 * 
 * Manages festival-based product collections:
 * - Major Indian festivals (Diwali, Holi, Durga Puja, Eid, Christmas, etc.)
 * - Regional festivals (Onam, Pongal, Baisakhi, Bihu, etc.)
 * - Craft-focused festivals (Surajkund Mela, Dilli Haat themes)
 * - Seasonal collections (Wedding season, Summer, Monsoon)
 */
@Document(collection = "festival_collections")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class FestivalCollection {
    
    @Id
    private String id;
    
    @Indexed(unique = true)
    private String slug;
    
    // Basic Information
    private String name;
    private String nameHindi;
    private String description;
    private String descriptionHindi;
    private String tagline;  // Short catchy text like "Light up your Diwali with handcrafted diyas"
    
    // Festival Type
    private FestivalType type;
    private FestivalCategory category;
    
    // Dates
    private LocalDate startDate;        // Collection goes live
    private LocalDate endDate;          // Collection ends
    private LocalDate festivalDate;     // Actual festival date
    private List<FestivalDateEntry> upcomingDates;  // Pre-calculated for 5 years
    
    // Visual Assets
    private String heroImageUrl;
    private String bannerImageUrl;
    private String thumbnailUrl;
    private String mobileHeroUrl;
    private String videoUrl;
    private String themeColor;
    private String accentColor;
    private String backgroundPattern;   // Pattern/texture for theme
    
    // Products
    private List<String> featuredProductIds;
    private List<String> productCategoryIds;     // Which craft categories to show
    private List<ProductHighlight> productHighlights;  // Featured items with custom descriptions
    
    // Regional Information
    private List<String> primaryStates;     // Where this festival is major
    private List<String> secondaryStates;   // Where it's celebrated but not major
    private RegionalRelevance regionalRelevance;
    
    // Gift Guide
    private List<GiftSuggestion> giftGuide;
    private String giftGuideTitle;
    private String giftGuideDescription;
    
    // Shopping Features
    private List<CollectionSection> sections;   // Organized sections (Gifts, Decor, Clothing, etc.)
    private List<String> promotionalTags;       // Tags like "Best Seller", "Limited Edition"
    private boolean hasCountdownTimer;
    private boolean hasEarlyBirdOffers;
    private DiscountInfo discountInfo;
    
    // SEO
    private String seoTitle;
    private String seoDescription;
    private List<String> seoKeywords;
    
    // Status
    private boolean active;
    private boolean featured;
    private int displayOrder;
    private int year;
    
    // Timestamps
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
    private String createdBy;
    
    // Statistics
    private long viewCount;
    private long productCount;
    private long orderCount;  // Orders from this collection
    
    // ==================== Enums ====================
    
    public enum FestivalType {
        NATIONAL,           // Celebrated across India (Diwali, Holi, Independence Day)
        REGIONAL,           // Specific to regions (Onam, Pongal, Bihu)
        RELIGIOUS,          // Religious festivals (Eid, Christmas, Navratri)
        SEASONAL,           // Seasonal collections (Wedding, Summer, Monsoon)
        CRAFT_FAIR,         // Craft mela themed (Surajkund, Dilli Haat)
        SPECIAL_OCCASION    // Raksha Bandhan, Mother's Day, etc.
    }
    
    public enum FestivalCategory {
        DIWALI,
        HOLI,
        DURGA_PUJA,
        NAVRATRI,
        GANESH_CHATURTHI,
        ONAM,
        PONGAL,
        BAISAKHI,
        BIHU,
        MAKAR_SANKRANTI,
        EID,
        CHRISTMAS,
        RAKSHA_BANDHAN,
        KARWA_CHAUTH,
        WEDDING_SEASON,
        SUMMER_COLLECTION,
        MONSOON_COLLECTION,
        WINTER_COLLECTION,
        NEW_YEAR,
        INDEPENDENCE_DAY,
        REPUBLIC_DAY,
        CRAFT_MELA,
        CUSTOM
    }
    
    public enum RegionalRelevance {
        PAN_INDIA,          // Entire country
        NORTH_INDIA,
        SOUTH_INDIA,
        EAST_INDIA,
        WEST_INDIA,
        CENTRAL_INDIA,
        NORTHEAST_INDIA,
        MULTI_REGIONAL,     // Multiple specific regions
        STATE_SPECIFIC      // One or two states only
    }
    
    // ==================== Inner Classes ====================
    
    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    public static class FestivalDateEntry {
        private int year;
        private LocalDate date;
        private String note;  // Like "Lunar calendar - date may vary"
    }
    
    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    public static class ProductHighlight {
        private String productId;
        private String customTitle;
        private String customDescription;
        private String badgeText;        // "Editor's Pick", "Best Gift"
        private int displayOrder;
    }
    
    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    public static class GiftSuggestion {
        private String title;            // "For Your Parents"
        private String description;
        private String imageUrl;
        private String priceRange;       // "₹500 - ₹2000"
        private List<String> productIds;
        private String targetAudience;   // "parents", "friends", "colleagues"
    }
    
    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    public static class CollectionSection {
        private String sectionId;
        private String title;
        private String titleHindi;
        private String description;
        private String imageUrl;
        private SectionType type;
        private List<String> productIds;
        private String filterQuery;      // Category/tag filter for dynamic products
        private int displayOrder;
    }
    
    public enum SectionType {
        GIFTS,
        HOME_DECOR,
        CLOTHING,
        JEWELRY,
        FOOD_ITEMS,
        PUJA_ITEMS,
        SWEETS_SNACKS,
        CUSTOM
    }
    
    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    public static class DiscountInfo {
        private String code;
        private int percentage;
        private double maxDiscount;
        private double minOrderValue;
        private LocalDate validFrom;
        private LocalDate validUntil;
        private String terms;
    }
    
    // ==================== Static Methods for Default Festivals ====================
    
    public static List<FestivalCollection> getDefaultFestivals() {
        List<FestivalCollection> festivals = new ArrayList<>();
        int currentYear = LocalDate.now().getYear();
        
        // Diwali Collection
        festivals.add(FestivalCollection.builder()
                .slug("diwali-" + currentYear)
                .name("Diwali Collection")
                .nameHindi("दिवाली संग्रह")
                .description("Celebrate the Festival of Lights with handcrafted diyas, lanterns, home decor, and traditional gifts from artisans across India.")
                .descriptionHindi("पूरे भारत के कारीगरों से हस्तनिर्मित दीये, लालटेन, घर की सजावट और पारंपरिक उपहारों के साथ रोशनी के त्योहार का जश्न मनाएं।")
                .tagline("Light up your home with handcrafted treasures")
                .type(FestivalType.NATIONAL)
                .category(FestivalCategory.DIWALI)
                .themeColor("#FF9933")
                .accentColor("#FFD700")
                .regionalRelevance(RegionalRelevance.PAN_INDIA)
                .primaryStates(Arrays.asList("Uttar Pradesh", "Rajasthan", "Gujarat", "Maharashtra", "Delhi"))
                .hasCountdownTimer(true)
                .hasEarlyBirdOffers(true)
                .active(true)
                .featured(true)
                .displayOrder(1)
                .year(currentYear)
                .sections(getDiwaliSections())
                .giftGuide(getDiwaliGiftGuide())
                .build());
        
        // Durga Puja Collection
        festivals.add(FestivalCollection.builder()
                .slug("durga-puja-" + currentYear)
                .name("Durga Puja Collection")
                .nameHindi("दुर्गा पूजा संग्रह")
                .description("Discover exquisite handloom sarees, dokra jewelry, and traditional Bengali crafts for Durga Puja celebrations.")
                .descriptionHindi("दुर्गा पूजा समारोह के लिए उत्कृष्ट हथकरघा साड़ी, डोकरा गहने और पारंपरिक बंगाली शिल्प की खोज करें।")
                .tagline("Bengal's finest crafts for the grandest celebration")
                .type(FestivalType.REGIONAL)
                .category(FestivalCategory.DURGA_PUJA)
                .themeColor("#DC143C")
                .accentColor("#FFD700")
                .regionalRelevance(RegionalRelevance.EAST_INDIA)
                .primaryStates(Arrays.asList("West Bengal", "Odisha", "Assam", "Jharkhand"))
                .hasCountdownTimer(true)
                .active(true)
                .featured(true)
                .displayOrder(2)
                .year(currentYear)
                .build());
        
        // Onam Collection
        festivals.add(FestivalCollection.builder()
                .slug("onam-" + currentYear)
                .name("Onam Collection")
                .nameHindi("ओणम संग्रह")
                .description("Explore Kerala's rich heritage with Kasavu sarees, banana fiber crafts, and traditional Onam essentials.")
                .descriptionHindi("कासवु साड़ियों, केले के फाइबर शिल्प और पारंपरिक ओणम आवश्यकताओं के साथ केरल की समृद्ध विरासत का अन्वेषण करें।")
                .tagline("Thiruvonam wishes with Kerala's traditional crafts")
                .type(FestivalType.REGIONAL)
                .category(FestivalCategory.ONAM)
                .themeColor("#228B22")
                .accentColor("#FFD700")
                .regionalRelevance(RegionalRelevance.SOUTH_INDIA)
                .primaryStates(Arrays.asList("Kerala"))
                .hasCountdownTimer(true)
                .active(true)
                .displayOrder(3)
                .year(currentYear)
                .build());
        
        // Pongal Collection
        festivals.add(FestivalCollection.builder()
                .slug("pongal-" + currentYear)
                .name("Pongal Collection")
                .nameHindi("पोंगल संग्रह")
                .description("Celebrate the harvest festival with Kanchipuram silks, Tanjore paintings, and traditional Tamil crafts.")
                .descriptionHindi("कांचीपुरम सिल्क, तंजौर पेंटिंग और पारंपरिक तमिल शिल्प के साथ फसल उत्सव मनाएं।")
                .tagline("Traditional Tamil crafts for the harvest celebration")
                .type(FestivalType.REGIONAL)
                .category(FestivalCategory.PONGAL)
                .themeColor("#FF6B35")
                .accentColor("#F7C59F")
                .regionalRelevance(RegionalRelevance.SOUTH_INDIA)
                .primaryStates(Arrays.asList("Tamil Nadu"))
                .active(true)
                .displayOrder(4)
                .year(currentYear)
                .build());
        
        // Holi Collection
        festivals.add(FestivalCollection.builder()
                .slug("holi-" + currentYear)
                .name("Holi Collection")
                .nameHindi("होली संग्रह")
                .description("Splash colors with organic gulal, traditional white kurtas, Banaras brocade, and festive home decor.")
                .descriptionHindi("जैविक गुलाल, पारंपरिक सफेद कुर्ते, बनारस ब्रोकेड और उत्सव की घर की सजावट के साथ रंगों की छटा बिखेरें।")
                .tagline("Play Holi the organic, traditional way")
                .type(FestivalType.NATIONAL)
                .category(FestivalCategory.HOLI)
                .themeColor("#FF1493")
                .accentColor("#00CED1")
                .regionalRelevance(RegionalRelevance.PAN_INDIA)
                .primaryStates(Arrays.asList("Uttar Pradesh", "Rajasthan", "Bihar", "Madhya Pradesh"))
                .active(true)
                .featured(true)
                .displayOrder(5)
                .year(currentYear)
                .build());
        
        // Navratri/Garba Collection
        festivals.add(FestivalCollection.builder()
                .slug("navratri-" + currentYear)
                .name("Navratri Collection")
                .nameHindi("नवरात्रि संग्रह")
                .description("Celebrate nine nights with vibrant Chaniya Cholis, Bandhani dupattas, oxidized jewelry, and dandiya sticks.")
                .descriptionHindi("जीवंत चनिया चोली, बांधनी दुपट्टे, ऑक्सीडाइज्ड गहने और डांडिया स्टिक के साथ नौ रातों का जश्न मनाएं।")
                .tagline("Garba nights in traditional splendor")
                .type(FestivalType.RELIGIOUS)
                .category(FestivalCategory.NAVRATRI)
                .themeColor("#FF4500")
                .accentColor("#32CD32")
                .regionalRelevance(RegionalRelevance.WEST_INDIA)
                .primaryStates(Arrays.asList("Gujarat", "Maharashtra", "Rajasthan"))
                .hasCountdownTimer(true)
                .active(true)
                .displayOrder(6)
                .year(currentYear)
                .build());
        
        // Raksha Bandhan Collection
        festivals.add(FestivalCollection.builder()
                .slug("raksha-bandhan-" + currentYear)
                .name("Raksha Bandhan Collection")
                .nameHindi("रक्षाबंधन संग्रह")
                .description("Celebrate sibling love with handcrafted rakhis, silver thalis, traditional sweets, and thoughtful gifts.")
                .descriptionHindi("हस्तनिर्मित राखियों, चांदी की थालियों, पारंपरिक मिठाइयों और विचारशील उपहारों के साथ भाई-बहन के प्यार का जश्न मनाएं।")
                .tagline("Handcrafted bonds of love")
                .type(FestivalType.SPECIAL_OCCASION)
                .category(FestivalCategory.RAKSHA_BANDHAN)
                .themeColor("#FF69B4")
                .accentColor("#FFD700")
                .regionalRelevance(RegionalRelevance.PAN_INDIA)
                .hasCountdownTimer(true)
                .hasEarlyBirdOffers(true)
                .active(true)
                .featured(true)
                .displayOrder(7)
                .year(currentYear)
                .build());
        
        // Wedding Season Collection
        festivals.add(FestivalCollection.builder()
                .slug("wedding-season-" + currentYear)
                .name("Wedding Season Collection")
                .nameHindi("विवाह सीजन संग्रह")
                .description("Complete your wedding trousseau with Banarasi silks, Kundan jewelry, handwoven lehengas, and artisan gifts.")
                .descriptionHindi("बनारसी सिल्क, कुंदन गहने, हथकरघा लहंगे और कारीगर उपहारों के साथ अपना विवाह का सामान पूरा करें।")
                .tagline("Handcrafted elegance for your special day")
                .type(FestivalType.SEASONAL)
                .category(FestivalCategory.WEDDING_SEASON)
                .themeColor("#C41E3A")
                .accentColor("#FFD700")
                .regionalRelevance(RegionalRelevance.PAN_INDIA)
                .active(true)
                .featured(true)
                .displayOrder(8)
                .year(currentYear)
                .build());
        
        return festivals;
    }
    
    private static List<CollectionSection> getDiwaliSections() {
        return Arrays.asList(
                CollectionSection.builder()
                        .sectionId("diwali-diyas")
                        .title("Handcrafted Diyas & Lanterns")
                        .titleHindi("हस्तनिर्मित दीये और लालटेन")
                        .description("Traditional clay diyas, brass lamps, and decorative lanterns")
                        .type(SectionType.HOME_DECOR)
                        .displayOrder(1)
                        .build(),
                CollectionSection.builder()
                        .sectionId("diwali-gifts")
                        .title("Festive Gift Hampers")
                        .titleHindi("उत्सव उपहार हैम्पर")
                        .description("Curated gift boxes with sweets, dry fruits, and craft items")
                        .type(SectionType.GIFTS)
                        .displayOrder(2)
                        .build(),
                CollectionSection.builder()
                        .sectionId("diwali-clothing")
                        .title("Festive Wear")
                        .titleHindi("उत्सव परिधान")
                        .description("Traditional kurtas, sarees, and ethnic wear")
                        .type(SectionType.CLOTHING)
                        .displayOrder(3)
                        .build(),
                CollectionSection.builder()
                        .sectionId("diwali-puja")
                        .title("Puja Essentials")
                        .titleHindi("पूजा की आवश्यकताएं")
                        .description("Brass idols, puja thalis, and spiritual items")
                        .type(SectionType.PUJA_ITEMS)
                        .displayOrder(4)
                        .build()
        );
    }
    
    private static List<GiftSuggestion> getDiwaliGiftGuide() {
        return Arrays.asList(
                GiftSuggestion.builder()
                        .title("For Parents")
                        .description("Show your love with traditional gifts")
                        .priceRange("₹1000 - ₹5000")
                        .targetAudience("parents")
                        .build(),
                GiftSuggestion.builder()
                        .title("For Colleagues")
                        .description("Professional yet thoughtful corporate gifts")
                        .priceRange("₹500 - ₹2000")
                        .targetAudience("colleagues")
                        .build(),
                GiftSuggestion.builder()
                        .title("For Friends")
                        .description("Trendy and unique handcrafted items")
                        .priceRange("₹500 - ₹3000")
                        .targetAudience("friends")
                        .build()
        );
    }
}
