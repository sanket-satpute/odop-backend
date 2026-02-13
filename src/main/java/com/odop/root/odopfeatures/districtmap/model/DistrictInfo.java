package com.odop.root.odopfeatures.districtmap.model;

import lombok.*;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.index.CompoundIndex;
import org.springframework.data.mongodb.core.index.Indexed;
import org.springframework.data.mongodb.core.mapping.Document;

import java.time.LocalDateTime;
import java.util.*;

/**
 * District Information Model for ODOP Platform
 * 
 * Contains:
 * - District geographical data (state, region, coordinates)
 * - ODOP products associated with district
 * - Artisan statistics
 * - GI tag information
 * - Cultural/historical significance
 */
@Document(collection = "district_info")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@CompoundIndex(name = "state_district", def = "{'stateCode': 1, 'districtCode': 1}", unique = true)
public class DistrictInfo {
    
    @Id
    private String id;
    
    // ==================== Basic Information ====================
    
    @Indexed
    private String districtCode;         // ISO/Census code
    private String name;
    private String nameHindi;
    private String nameLocal;            // Local language name
    
    @Indexed
    private String stateCode;
    private String stateName;
    private String stateNameHindi;
    
    // Region grouping
    private Region region;
    private String division;             // Administrative division
    
    // ==================== Geographic Data ====================
    
    private double latitude;
    private double longitude;
    private double[] boundingBox;        // [minLng, minLat, maxLng, maxLat]
    private String mapSvgPath;           // SVG path for district boundary
    private String geoJsonId;            // Reference to TopoJSON feature ID
    
    // ==================== ODOP Information ====================
    
    private List<OdopProduct> odopProducts;      // Official ODOP products
    private List<String> giTaggedProducts;       // GI tagged product names
    private List<CraftTradition> craftTraditions; // Traditional crafts
    private String primaryCraft;                  // Main craft of district
    private List<String> craftCategoryIds;       // Reference to craft categories
    
    // ==================== Artisan Information ====================
    
    private long registeredArtisans;
    private long activeVendors;
    private long totalProducts;
    private List<FamousArtisan> famousArtisans;  // Notable artisans
    
    // ==================== Cultural Information ====================
    
    private String historicalSignificance;
    private String craftHistory;
    private List<String> famousFor;              // What district is famous for
    private List<String> festivals;              // Major festivals
    private String touristInfo;
    
    // ==================== Visual Assets ====================
    
    private String heroImageUrl;
    private String thumbnailUrl;
    private String bannerImageUrl;
    private List<String> galleryImages;
    private String videoUrl;
    private String mapTileColor;                 // Color for map visualization
    
    // ==================== Status ====================
    
    private boolean active;
    private boolean featured;
    private int displayPriority;                 // For featured districts
    
    // ==================== SEO ====================
    
    private String seoTitle;
    private String seoDescription;
    private List<String> seoKeywords;
    
    // ==================== Timestamps ====================
    
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
    
    // ==================== Enums ====================
    
    public enum Region {
        NORTH("North India", "उत्तर भारत"),
        SOUTH("South India", "दक्षिण भारत"),
        EAST("East India", "पूर्वी भारत"),
        WEST("West India", "पश्चिमी भारत"),
        CENTRAL("Central India", "मध्य भारत"),
        NORTHEAST("Northeast India", "पूर्वोत्तर भारत");
        
        private final String displayName;
        private final String displayNameHindi;
        
        Region(String displayName, String displayNameHindi) {
            this.displayName = displayName;
            this.displayNameHindi = displayNameHindi;
        }
        
        public String getDisplayName() { return displayName; }
        public String getDisplayNameHindi() { return displayNameHindi; }
    }
    
    // ==================== Inner Classes ====================
    
    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    public static class OdopProduct {
        private String name;
        private String nameHindi;
        private String description;
        private String imageUrl;
        private boolean giTagged;
        private String giTagNumber;
        private int yearRecognized;
        private String craftCategoryId;
    }
    
    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    public static class CraftTradition {
        private String name;
        private String nameHindi;
        private String description;
        private String history;
        private List<String> materials;
        private List<String> techniques;
        private String imageUrl;
        private int ageInYears;              // How old is this tradition
        private String unescoStatus;         // If UNESCO recognized
    }
    
    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    public static class FamousArtisan {
        private String name;
        private String vendorId;             // If registered on platform
        private String craft;
        private String achievement;          // Awards, recognition
        private String imageUrl;
        private String storyUrl;             // Link to artisan story
    }
    
    // ==================== Static Data for Indian States ====================
    
    public static Map<String, StateInfo> getIndianStates() {
        Map<String, StateInfo> states = new LinkedHashMap<>();
        
        // North India
        states.put("JK", new StateInfo("Jammu & Kashmir", "जम्मू और कश्मीर", Region.NORTH, "#E3735E"));
        states.put("HP", new StateInfo("Himachal Pradesh", "हिमाचल प्रदेश", Region.NORTH, "#7AA874"));
        states.put("PB", new StateInfo("Punjab", "पंजाब", Region.NORTH, "#F9B572"));
        states.put("HR", new StateInfo("Haryana", "हरियाणा", Region.NORTH, "#D4A5A5"));
        states.put("RJ", new StateInfo("Rajasthan", "राजस्थान", Region.NORTH, "#FFB84C"));
        states.put("UP", new StateInfo("Uttar Pradesh", "उत्तर प्रदेश", Region.NORTH, "#F266AB"));
        states.put("UK", new StateInfo("Uttarakhand", "उत्तराखंड", Region.NORTH, "#A1C298"));
        states.put("DL", new StateInfo("Delhi", "दिल्ली", Region.NORTH, "#FF6969"));
        
        // South India
        states.put("KA", new StateInfo("Karnataka", "कर्नाटक", Region.SOUTH, "#FF9B9B"));
        states.put("KL", new StateInfo("Kerala", "केरल", Region.SOUTH, "#94AF9F"));
        states.put("TN", new StateInfo("Tamil Nadu", "तमिलनाडु", Region.SOUTH, "#FFD93D"));
        states.put("AP", new StateInfo("Andhra Pradesh", "आंध्र प्रदेश", Region.SOUTH, "#6BCB77"));
        states.put("TS", new StateInfo("Telangana", "तेलंगाना", Region.SOUTH, "#FF8FB1"));
        
        // East India
        states.put("WB", new StateInfo("West Bengal", "पश्चिम बंगाल", Region.EAST, "#FFB562"));
        states.put("OR", new StateInfo("Odisha", "ओडिशा", Region.EAST, "#F7EC09"));
        states.put("BR", new StateInfo("Bihar", "बिहार", Region.EAST, "#3AB0FF"));
        states.put("JH", new StateInfo("Jharkhand", "झारखंड", Region.EAST, "#F0A500"));
        
        // West India
        states.put("MH", new StateInfo("Maharashtra", "महाराष्ट्र", Region.WEST, "#FF6B6B"));
        states.put("GJ", new StateInfo("Gujarat", "गुजरात", Region.WEST, "#4CACBC"));
        states.put("GA", new StateInfo("Goa", "गोवा", Region.WEST, "#F9D923"));
        
        // Central India
        states.put("MP", new StateInfo("Madhya Pradesh", "मध्य प्रदेश", Region.CENTRAL, "#A460ED"));
        states.put("CG", new StateInfo("Chhattisgarh", "छत्तीसगढ़", Region.CENTRAL, "#62CDFF"));
        
        // Northeast India
        states.put("AS", new StateInfo("Assam", "असम", Region.NORTHEAST, "#BACDDB"));
        states.put("AR", new StateInfo("Arunachal Pradesh", "अरुणाचल प्रदेश", Region.NORTHEAST, "#7EC8E3"));
        states.put("MN", new StateInfo("Manipur", "मणिपुर", Region.NORTHEAST, "#FF87B2"));
        states.put("ML", new StateInfo("Meghalaya", "मेघालय", Region.NORTHEAST, "#98D8AA"));
        states.put("MZ", new StateInfo("Mizoram", "मिजोरम", Region.NORTHEAST, "#B6E2A1"));
        states.put("NL", new StateInfo("Nagaland", "नागालैंड", Region.NORTHEAST, "#FEBE8C"));
        states.put("SK", new StateInfo("Sikkim", "सिक्किम", Region.NORTHEAST, "#F7C8E0"));
        states.put("TR", new StateInfo("Tripura", "त्रिपुरा", Region.NORTHEAST, "#DFFFD8"));
        
        return states;
    }
    
    @Data
    @AllArgsConstructor
    public static class StateInfo {
        private String name;
        private String nameHindi;
        private Region region;
        private String mapColor;
    }
    
    // ==================== Sample Districts with ODOP Products ====================
    
    public static List<DistrictInfo> getSampleDistricts() {
        List<DistrictInfo> districts = new ArrayList<>();
        
        // Varanasi - Famous for Banarasi Silk
        districts.add(DistrictInfo.builder()
                .districtCode("UP-VAR")
                .name("Varanasi")
                .nameHindi("वाराणसी")
                .stateCode("UP")
                .stateName("Uttar Pradesh")
                .stateNameHindi("उत्तर प्रदेश")
                .region(Region.NORTH)
                .latitude(25.3176)
                .longitude(82.9739)
                .primaryCraft("Banarasi Silk Sarees")
                .odopProducts(Arrays.asList(
                        OdopProduct.builder()
                                .name("Banarasi Silk Saree")
                                .nameHindi("बनारसी सिल्क साड़ी")
                                .description("Handwoven silk sarees with gold and silver brocade")
                                .giTagged(true)
                                .giTagNumber("GI/2009/1")
                                .yearRecognized(2009)
                                .build()
                ))
                .craftTraditions(Arrays.asList(
                        CraftTradition.builder()
                                .name("Banarasi Weaving")
                                .nameHindi("बनारसी बुनाई")
                                .description("Ancient silk weaving tradition dating back to Mughal era")
                                .ageInYears(500)
                                .materials(Arrays.asList("Silk", "Gold thread", "Silver thread"))
                                .techniques(Arrays.asList("Jacquard", "Kadhua", "Jangla"))
                                .build()
                ))
                .famousFor(Arrays.asList("Banarasi Silk", "Wooden Toys", "Brass Items", "Religious Tourism"))
                .historicalSignificance("One of the oldest continuously inhabited cities in the world")
                .mapTileColor("#F266AB")
                .active(true)
                .featured(true)
                .displayPriority(1)
                .build());
        
        // Jaipur - Pink City Crafts
        districts.add(DistrictInfo.builder()
                .districtCode("RJ-JAI")
                .name("Jaipur")
                .nameHindi("जयपुर")
                .stateCode("RJ")
                .stateName("Rajasthan")
                .stateNameHindi("राजस्थान")
                .region(Region.NORTH)
                .latitude(26.9124)
                .longitude(75.7873)
                .primaryCraft("Blue Pottery")
                .odopProducts(Arrays.asList(
                        OdopProduct.builder()
                                .name("Blue Pottery")
                                .nameHindi("नीला मिट्टी के बर्तन")
                                .description("Traditional glazed pottery with Persian influence")
                                .giTagged(true)
                                .build(),
                        OdopProduct.builder()
                                .name("Jaipuri Razai")
                                .nameHindi("जयपुरी रजाई")
                                .description("Traditional lightweight quilts with block print")
                                .giTagged(false)
                                .build()
                ))
                .famousFor(Arrays.asList("Blue Pottery", "Block Printing", "Kundan Jewelry", "Lac Bangles"))
                .mapTileColor("#FFB84C")
                .active(true)
                .featured(true)
                .displayPriority(2)
                .build());
        
        // Moradabad - Brass City
        districts.add(DistrictInfo.builder()
                .districtCode("UP-MOR")
                .name("Moradabad")
                .nameHindi("मुरादाबाद")
                .stateCode("UP")
                .stateName("Uttar Pradesh")
                .stateNameHindi("उत्तर प्रदेश")
                .region(Region.NORTH)
                .latitude(28.8386)
                .longitude(78.7733)
                .primaryCraft("Brass Items")
                .odopProducts(Arrays.asList(
                        OdopProduct.builder()
                                .name("Moradabad Brassware")
                                .nameHindi("मुरादाबाद पीतल के बर्तन")
                                .description("Handcrafted brass items including utensils and decor")
                                .giTagged(true)
                                .build()
                ))
                .famousFor(Arrays.asList("Brass Items", "Metal Handicrafts", "Export Hub"))
                .mapTileColor("#F9B572")
                .active(true)
                .featured(true)
                .displayPriority(3)
                .build());
        
        // Kanchipuram - Silk City
        districts.add(DistrictInfo.builder()
                .districtCode("TN-KAN")
                .name("Kanchipuram")
                .nameHindi("कांचीपुरम")
                .stateCode("TN")
                .stateName("Tamil Nadu")
                .stateNameHindi("तमिलनाडु")
                .region(Region.SOUTH)
                .latitude(12.8185)
                .longitude(79.6947)
                .primaryCraft("Kanchipuram Silk Sarees")
                .odopProducts(Arrays.asList(
                        OdopProduct.builder()
                                .name("Kanchipuram Silk Saree")
                                .nameHindi("कांजीवरम सिल्क साड़ी")
                                .description("Traditional mulberry silk sarees with temple designs")
                                .giTagged(true)
                                .giTagNumber("GI/2005/1")
                                .build()
                ))
                .famousFor(Arrays.asList("Kanchipuram Silk", "Temple Architecture", "Religious Tourism"))
                .mapTileColor("#FFD93D")
                .active(true)
                .featured(true)
                .displayPriority(4)
                .build());
        
        // Murshidabad - Silk District
        districts.add(DistrictInfo.builder()
                .districtCode("WB-MUR")
                .name("Murshidabad")
                .nameHindi("मुर्शिदाबाद")
                .stateCode("WB")
                .stateName("West Bengal")
                .stateNameHindi("पश्चिम बंगाल")
                .region(Region.EAST)
                .latitude(24.1750)
                .longitude(88.2667)
                .primaryCraft("Murshidabad Silk")
                .odopProducts(Arrays.asList(
                        OdopProduct.builder()
                                .name("Murshidabad Silk")
                                .nameHindi("मुर्शिदाबाद सिल्क")
                                .description("Traditional silk known for its luster and durability")
                                .giTagged(true)
                                .build(),
                        OdopProduct.builder()
                                .name("Bell Metal Craft")
                                .nameHindi("कांसे का शिल्प")
                                .description("Traditional bell metal utensils and artifacts")
                                .giTagged(false)
                                .build()
                ))
                .famousFor(Arrays.asList("Murshidabad Silk", "Bell Metal", "Historical Sites"))
                .mapTileColor("#FFB562")
                .active(true)
                .displayPriority(5)
                .build());
        
        return districts;
    }
}
