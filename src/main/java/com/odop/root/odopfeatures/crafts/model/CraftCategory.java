package com.odop.root.odopfeatures.crafts.model;

import lombok.*;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;
import org.springframework.data.mongodb.core.index.Indexed;

import java.time.LocalDateTime;
import java.util.*;

/**
 * Craft Category for ODOP products
 * Hierarchical category structure: Main Category > Sub Category > Craft Type
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Document(collection = "craft_categories")
public class CraftCategory {
    
    @Id
    private String id;
    
    @Indexed(unique = true)
    private String slug;
    
    private String name;
    
    private String nameHindi;  // Hindi name for regional support
    
    private String description;
    
    private String descriptionHindi;
    
    // Hierarchy
    private String parentId;
    
    private int level;  // 1 = Main, 2 = Sub, 3 = Craft Type
    
    private List<String> childIds;
    
    private String ancestorPath;  // e.g., "textiles/handloom/silk"
    
    // Visual
    private String imageUrl;
    
    private String iconName;  // Icon identifier for UI
    
    private String bannerImageUrl;
    
    private String themeColor;  // Hex color for category theming
    
    // SEO
    private String metaTitle;
    
    private String metaDescription;
    
    private List<String> keywords;
    
    // Attributes specific to this category
    private List<CategoryAttribute> attributes;
    
    // Related data
    private List<String> relatedGiTags;  // GI tags associated
    
    private List<String> majorStates;  // States known for this craft
    
    private List<String> famousDistricts;  // Districts famous for this
    
    // Statistics
    private long productCount;
    
    private long artisanCount;
    
    private long viewCount;
    
    // Status
    private boolean active;
    
    private boolean featured;
    
    private int displayOrder;
    
    // Timestamps
    private LocalDateTime createdAt;
    
    private LocalDateTime updatedAt;
    
    // ==================== INNER CLASSES ====================
    
    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    public static class CategoryAttribute {
        private String name;
        private String label;
        private String labelHindi;
        private AttributeType type;
        private List<String> options;  // For SELECT type
        private String unit;  // For NUMERIC type
        private boolean required;
        private boolean filterable;
        private int displayOrder;
    }
    
    public enum AttributeType {
        TEXT,
        NUMERIC,
        SELECT,
        MULTI_SELECT,
        BOOLEAN,
        COLOR,
        SIZE
    }
    
    // ==================== PREDEFINED CATEGORIES ====================
    
    public static List<CraftCategory> getDefaultCategories() {
        List<CraftCategory> categories = new ArrayList<>();
        
        // ========== MAIN CATEGORIES (Level 1) ==========
        
        // 1. TEXTILES
        categories.add(CraftCategory.builder()
                .slug("textiles")
                .name("Textiles & Fabrics")
                .nameHindi("वस्त्र एवं कपड़े")
                .description("Traditional Indian textiles including handloom, silk, cotton, and embroidered fabrics")
                .level(1)
                .iconName("scissors")
                .themeColor("#8B4513")
                .attributes(getTextileAttributes())
                .majorStates(List.of("West Bengal", "Tamil Nadu", "Uttar Pradesh", "Gujarat", "Rajasthan"))
                .active(true)
                .featured(true)
                .displayOrder(1)
                .createdAt(LocalDateTime.now())
                .build());
        
        // 2. POTTERY & CERAMICS
        categories.add(CraftCategory.builder()
                .slug("pottery")
                .name("Pottery & Ceramics")
                .nameHindi("मिट्टी के बर्तन")
                .description("Traditional clay pottery, terracotta, and ceramic crafts from across India")
                .level(1)
                .iconName("coffee")
                .themeColor("#CD853F")
                .attributes(getPotteryAttributes())
                .majorStates(List.of("Rajasthan", "Gujarat", "West Bengal", "Uttar Pradesh"))
                .active(true)
                .featured(true)
                .displayOrder(2)
                .createdAt(LocalDateTime.now())
                .build());
        
        // 3. JEWELRY & ORNAMENTS
        categories.add(CraftCategory.builder()
                .slug("jewelry")
                .name("Jewelry & Ornaments")
                .nameHindi("आभूषण")
                .description("Traditional Indian jewelry including gold, silver, kundan, meenakari, and tribal ornaments")
                .level(1)
                .iconName("gem")
                .themeColor("#FFD700")
                .attributes(getJewelryAttributes())
                .majorStates(List.of("Rajasthan", "Gujarat", "Tamil Nadu", "Kerala", "West Bengal"))
                .active(true)
                .featured(true)
                .displayOrder(3)
                .createdAt(LocalDateTime.now())
                .build());
        
        // 4. FOOD PRODUCTS
        categories.add(CraftCategory.builder()
                .slug("food-products")
                .name("Food Products")
                .nameHindi("खाद्य उत्पाद")
                .description("Authentic regional food products, spices, sweets, and specialty items")
                .level(1)
                .iconName("utensils")
                .themeColor("#228B22")
                .attributes(getFoodAttributes())
                .majorStates(List.of("Kerala", "Kashmir", "Rajasthan", "Maharashtra", "West Bengal"))
                .active(true)
                .featured(true)
                .displayOrder(4)
                .createdAt(LocalDateTime.now())
                .build());
        
        // 5. HANDICRAFTS
        categories.add(CraftCategory.builder()
                .slug("handicrafts")
                .name("Handicrafts")
                .nameHindi("हस्तशिल्प")
                .description("Handcrafted items including woodwork, metalwork, bamboo crafts, and decorative items")
                .level(1)
                .iconName("hammer")
                .themeColor("#8B0000")
                .attributes(getHandicraftAttributes())
                .majorStates(List.of("Kashmir", "Rajasthan", "Uttar Pradesh", "Odisha", "Assam"))
                .active(true)
                .featured(true)
                .displayOrder(5)
                .createdAt(LocalDateTime.now())
                .build());
        
        // 6. PAINTINGS & ART
        categories.add(CraftCategory.builder()
                .slug("paintings")
                .name("Paintings & Art")
                .nameHindi("चित्रकला")
                .description("Traditional and folk paintings including Madhubani, Warli, Pattachitra, and Tanjore")
                .level(1)
                .iconName("palette")
                .themeColor("#4B0082")
                .attributes(getPaintingAttributes())
                .majorStates(List.of("Bihar", "Maharashtra", "Odisha", "Rajasthan", "Tamil Nadu"))
                .active(true)
                .featured(true)
                .displayOrder(6)
                .createdAt(LocalDateTime.now())
                .build());
        
        // 7. HOME DECOR
        categories.add(CraftCategory.builder()
                .slug("home-decor")
                .name("Home Decor")
                .nameHindi("घर की सजावट")
                .description("Decorative items for home including lamps, wall hangings, and traditional artifacts")
                .level(1)
                .iconName("home")
                .themeColor("#556B2F")
                .majorStates(List.of("Rajasthan", "Gujarat", "Andhra Pradesh", "Karnataka"))
                .active(true)
                .featured(true)
                .displayOrder(7)
                .createdAt(LocalDateTime.now())
                .build());
        
        // 8. LEATHER GOODS
        categories.add(CraftCategory.builder()
                .slug("leather")
                .name("Leather Goods")
                .nameHindi("चमड़े के सामान")
                .description("Traditional leather crafts including juttis, bags, and accessories")
                .level(1)
                .iconName("briefcase")
                .themeColor("#8B4513")
                .majorStates(List.of("Rajasthan", "Punjab", "Tamil Nadu", "Uttar Pradesh"))
                .active(true)
                .displayOrder(8)
                .createdAt(LocalDateTime.now())
                .build());
        
        // 9. MUSICAL INSTRUMENTS
        categories.add(CraftCategory.builder()
                .slug("musical-instruments")
                .name("Musical Instruments")
                .nameHindi("वाद्य यंत्र")
                .description("Traditional Indian musical instruments handcrafted by master artisans")
                .level(1)
                .iconName("music")
                .themeColor("#800080")
                .majorStates(List.of("West Bengal", "Rajasthan", "Tamil Nadu", "Maharashtra"))
                .active(true)
                .displayOrder(9)
                .createdAt(LocalDateTime.now())
                .build());
        
        // 10. BEAUTY & WELLNESS
        categories.add(CraftCategory.builder()
                .slug("beauty-wellness")
                .name("Beauty & Wellness")
                .nameHindi("सौंदर्य एवं कल्याण")
                .description("Natural beauty products, Ayurvedic items, and traditional wellness products")
                .level(1)
                .iconName("heart")
                .themeColor("#FF69B4")
                .majorStates(List.of("Kerala", "Uttarakhand", "Himachal Pradesh", "Rajasthan"))
                .active(true)
                .displayOrder(10)
                .createdAt(LocalDateTime.now())
                .build());
        
        return categories;
    }
    
    // ==================== CATEGORY-SPECIFIC ATTRIBUTES ====================
    
    private static List<CategoryAttribute> getTextileAttributes() {
        return List.of(
                CategoryAttribute.builder()
                        .name("fabric_type").label("Fabric Type").labelHindi("कपड़े का प्रकार")
                        .type(AttributeType.SELECT)
                        .options(List.of("Silk", "Cotton", "Wool", "Linen", "Jute", "Khadi", "Pashmina"))
                        .required(true).filterable(true).displayOrder(1).build(),
                CategoryAttribute.builder()
                        .name("weave_type").label("Weave Type").labelHindi("बुनाई का प्रकार")
                        .type(AttributeType.SELECT)
                        .options(List.of("Handloom", "Powerloom", "Jacquard", "Ikat", "Jamdani"))
                        .filterable(true).displayOrder(2).build(),
                CategoryAttribute.builder()
                        .name("embroidery").label("Embroidery Style").labelHindi("कढ़ाई शैली")
                        .type(AttributeType.SELECT)
                        .options(List.of("None", "Chikankari", "Zardozi", "Kantha", "Phulkari", "Mirror Work"))
                        .filterable(true).displayOrder(3).build(),
                CategoryAttribute.builder()
                        .name("thread_count").label("Thread Count").labelHindi("धागा गिनती")
                        .type(AttributeType.NUMERIC).unit("per inch")
                        .displayOrder(4).build()
        );
    }
    
    private static List<CategoryAttribute> getPotteryAttributes() {
        return List.of(
                CategoryAttribute.builder()
                        .name("material").label("Material").labelHindi("सामग्री")
                        .type(AttributeType.SELECT)
                        .options(List.of("Terracotta", "Clay", "Ceramic", "Stoneware", "Porcelain"))
                        .required(true).filterable(true).displayOrder(1).build(),
                CategoryAttribute.builder()
                        .name("finish").label("Finish").labelHindi("फिनिश")
                        .type(AttributeType.SELECT)
                        .options(List.of("Glazed", "Unglazed", "Painted", "Natural"))
                        .filterable(true).displayOrder(2).build(),
                CategoryAttribute.builder()
                        .name("food_safe").label("Food Safe").labelHindi("खाद्य सुरक्षित")
                        .type(AttributeType.BOOLEAN)
                        .filterable(true).displayOrder(3).build()
        );
    }
    
    private static List<CategoryAttribute> getJewelryAttributes() {
        return List.of(
                CategoryAttribute.builder()
                        .name("metal_type").label("Metal Type").labelHindi("धातु का प्रकार")
                        .type(AttributeType.SELECT)
                        .options(List.of("Gold", "Silver", "Brass", "Copper", "German Silver", "Oxidized"))
                        .required(true).filterable(true).displayOrder(1).build(),
                CategoryAttribute.builder()
                        .name("jewelry_style").label("Style").labelHindi("शैली")
                        .type(AttributeType.SELECT)
                        .options(List.of("Kundan", "Meenakari", "Temple", "Tribal", "Contemporary", "Antique"))
                        .filterable(true).displayOrder(2).build(),
                CategoryAttribute.builder()
                        .name("stone_type").label("Stone Type").labelHindi("पत्थर का प्रकार")
                        .type(AttributeType.MULTI_SELECT)
                        .options(List.of("None", "Precious", "Semi-precious", "Pearls", "Beads", "Glass"))
                        .filterable(true).displayOrder(3).build(),
                CategoryAttribute.builder()
                        .name("purity").label("Purity").labelHindi("शुद्धता")
                        .type(AttributeType.SELECT)
                        .options(List.of("24K", "22K", "18K", "925 Silver", "N/A"))
                        .displayOrder(4).build()
        );
    }
    
    private static List<CategoryAttribute> getFoodAttributes() {
        return List.of(
                CategoryAttribute.builder()
                        .name("food_type").label("Food Type").labelHindi("खाद्य प्रकार")
                        .type(AttributeType.SELECT)
                        .options(List.of("Spices", "Sweets", "Pickles", "Dry Fruits", "Tea/Coffee", "Grains", "Oil"))
                        .required(true).filterable(true).displayOrder(1).build(),
                CategoryAttribute.builder()
                        .name("dietary").label("Dietary").labelHindi("आहार")
                        .type(AttributeType.MULTI_SELECT)
                        .options(List.of("Vegetarian", "Vegan", "Organic", "Gluten-Free", "Sugar-Free"))
                        .filterable(true).displayOrder(2).build(),
                CategoryAttribute.builder()
                        .name("shelf_life").label("Shelf Life").labelHindi("शेल्फ जीवन")
                        .type(AttributeType.TEXT).displayOrder(3).build(),
                CategoryAttribute.builder()
                        .name("fssai_certified").label("FSSAI Certified").labelHindi("FSSAI प्रमाणित")
                        .type(AttributeType.BOOLEAN)
                        .filterable(true).displayOrder(4).build()
        );
    }
    
    private static List<CategoryAttribute> getHandicraftAttributes() {
        return List.of(
                CategoryAttribute.builder()
                        .name("craft_material").label("Material").labelHindi("सामग्री")
                        .type(AttributeType.SELECT)
                        .options(List.of("Wood", "Metal", "Bamboo", "Cane", "Stone", "Paper Mache", "Glass"))
                        .required(true).filterable(true).displayOrder(1).build(),
                CategoryAttribute.builder()
                        .name("technique").label("Technique").labelHindi("तकनीक")
                        .type(AttributeType.SELECT)
                        .options(List.of("Carving", "Inlay", "Lacquer", "Casting", "Weaving", "Embossing"))
                        .filterable(true).displayOrder(2).build(),
                CategoryAttribute.builder()
                        .name("handmade").label("100% Handmade").labelHindi("100% हस्तनिर्मित")
                        .type(AttributeType.BOOLEAN)
                        .filterable(true).displayOrder(3).build()
        );
    }
    
    private static List<CategoryAttribute> getPaintingAttributes() {
        return List.of(
                CategoryAttribute.builder()
                        .name("art_style").label("Art Style").labelHindi("कला शैली")
                        .type(AttributeType.SELECT)
                        .options(List.of("Madhubani", "Warli", "Pattachitra", "Tanjore", "Pichwai", "Kalamkari", "Gond", "Miniature"))
                        .required(true).filterable(true).displayOrder(1).build(),
                CategoryAttribute.builder()
                        .name("medium").label("Medium").labelHindi("माध्यम")
                        .type(AttributeType.SELECT)
                        .options(List.of("Natural Colors", "Acrylic", "Oil", "Watercolor", "Gold Leaf"))
                        .filterable(true).displayOrder(2).build(),
                CategoryAttribute.builder()
                        .name("base").label("Base/Canvas").labelHindi("आधार")
                        .type(AttributeType.SELECT)
                        .options(List.of("Cloth", "Paper", "Wood", "Canvas", "Silk", "Palm Leaf"))
                        .displayOrder(3).build(),
                CategoryAttribute.builder()
                        .name("framed").label("Framed").labelHindi("फ्रेम")
                        .type(AttributeType.BOOLEAN)
                        .filterable(true).displayOrder(4).build()
        );
    }
}
