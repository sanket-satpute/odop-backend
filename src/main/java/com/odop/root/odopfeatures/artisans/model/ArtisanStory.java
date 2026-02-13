package com.odop.root.odopfeatures.artisans.model;

import lombok.*;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.index.Indexed;
import org.springframework.data.mongodb.core.index.TextIndexed;
import org.springframework.data.mongodb.core.mapping.Document;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.*;

/**
 * Artisan Story Model for ODOP Platform
 * 
 * Captures the stories and journeys of traditional artisans,
 * their craft heritage, family traditions, and achievements.
 */
@Document(collection = "artisan_stories")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ArtisanStory {
    
    @Id
    private String id;
    
    @Indexed(unique = true)
    private String slug;
    
    // ==================== Artisan Information ====================
    
    private String artisanName;
    private String artisanNameHindi;
    private String title;                     // Master Weaver, Traditional Potter, etc.
    private String titleHindi;
    private String profileImageUrl;
    private String coverImageUrl;
    
    // ==================== Location ====================
    
    private String village;
    private String district;
    private String state;
    private String stateCode;
    private String pincode;
    private Double latitude;
    private Double longitude;
    
    // ==================== Craft Information ====================
    
    private String primaryCraft;
    private String primaryCraftHindi;
    private String craftCategoryId;           // Reference to CraftCategory
    private List<String> additionalCrafts;
    private int yearsOfExperience;
    private int generationsInCraft;           // 3rd generation weaver, etc.
    private String craftOriginStory;          // History of the craft
    private String craftOriginStoryHindi;
    
    // ==================== Story Content ====================
    
    @TextIndexed
    private String shortBio;
    private String shortBioHindi;
    
    @TextIndexed
    private String fullStory;
    private String fullStoryHindi;
    
    private String quote;                     // Memorable quote from artisan
    private String quoteHindi;
    
    private List<StorySection> storySections;
    
    // ==================== Family & Legacy ====================
    
    private FamilyBackground familyBackground;
    private String teachingPhilosophy;
    private int apprenticesTrained;
    private List<String> familyMembersInCraft;
    
    // ==================== Media ====================
    
    private List<MediaItem> gallery;          // Photos
    private List<VideoItem> videos;           // Video testimonials, work process
    private String primaryVideoUrl;           // Main featured video
    private String primaryVideoThumbnail;
    
    // ==================== Products & Work ====================
    
    private List<String> featuredProductIds;  // Links to products
    private List<Signature> signatureWorks;
    private String workshopDescription;
    private String workshopImageUrl;
    
    // ==================== Recognition & Awards ====================
    
    private List<Award> awards;
    private List<String> certifications;
    private boolean giTagHolder;
    private String giTagDetails;
    private boolean nationalAwardee;
    private boolean stateAwardee;
    
    // ==================== Social & Contact ====================
    
    private ContactInfo contactInfo;
    private SocialLinks socialLinks;
    private String vendorId;                  // If artisan is also a vendor
    
    // ==================== Exhibition & Events ====================
    
    private List<Exhibition> exhibitions;
    private boolean availableForWorkshops;
    private boolean availableForCommissions;
    private String workshopDetails;
    
    // ==================== Status & Metadata ====================
    
    private StoryStatus status;
    private boolean featured;
    private boolean verified;
    private int displayOrder;
    private long viewCount;
    private long shareCount;
    
    private List<String> tags;
    private String seoTitle;
    private String seoDescription;
    private List<String> seoKeywords;
    
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
    private LocalDateTime publishedAt;
    private String createdBy;
    private String lastModifiedBy;
    
    // ==================== Enums ====================
    
    public enum StoryStatus {
        DRAFT,
        PENDING_REVIEW,
        PUBLISHED,
        ARCHIVED
    }
    
    // ==================== Inner Classes ====================
    
    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    public static class StorySection {
        private String title;
        private String titleHindi;
        private String content;
        private String contentHindi;
        private String imageUrl;
        private String imageCaption;
        private int orderIndex;
    }
    
    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    public static class FamilyBackground {
        private String fatherName;
        private String fatherCraft;
        private String grandfatherName;
        private String grandfatherCraft;
        private String familyHistory;
        private String familyHistoryHindi;
        private int generationCount;
        private String familyPhotoUrl;
    }
    
    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    public static class MediaItem {
        private String url;
        private String thumbnailUrl;
        private String caption;
        private String captionHindi;
        private MediaType type;
        private int orderIndex;
    }
    
    public enum MediaType {
        PHOTO,
        ILLUSTRATION,
        INFOGRAPHIC
    }
    
    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    public static class VideoItem {
        private String title;
        private String titleHindi;
        private String description;
        private String videoUrl;
        private String thumbnailUrl;
        private String duration;             // e.g., "3:45"
        private VideoType type;
        private boolean featured;
    }
    
    public enum VideoType {
        TESTIMONIAL,
        WORK_PROCESS,
        DOCUMENTARY,
        INTERVIEW,
        WORKSHOP
    }
    
    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    public static class Signature {
        private String name;
        private String nameHindi;
        private String description;
        private String descriptionHindi;
        private String imageUrl;
        private String price;
        private boolean available;
    }
    
    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    public static class Award {
        private String name;
        private String nameHindi;
        private String organization;
        private int year;
        private String description;
        private String imageUrl;
        private AwardLevel level;
    }
    
    public enum AwardLevel {
        NATIONAL,
        STATE,
        DISTRICT,
        INTERNATIONAL,
        PRIVATE
    }
    
    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    public static class ContactInfo {
        private String phone;
        private String alternatePhone;
        private String email;
        private String address;
        private String workshopAddress;
        private String bestTimeToContact;
    }
    
    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    public static class SocialLinks {
        private String facebook;
        private String instagram;
        private String youtube;
        private String twitter;
        private String website;
    }
    
    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    public static class Exhibition {
        private String name;
        private String location;
        private LocalDate startDate;
        private LocalDate endDate;
        private String description;
        private boolean upcoming;
    }
    
    // ==================== Sample Data ====================
    
    public static List<ArtisanStory> getSampleStories() {
        List<ArtisanStory> stories = new ArrayList<>();
        
        // Story 1: Banarasi Silk Weaver
        stories.add(ArtisanStory.builder()
                .slug("ram-prasad-ansari-banarasi-weaver")
                .artisanName("Ram Prasad Ansari")
                .artisanNameHindi("राम प्रसाद अंसारी")
                .title("Master Banarasi Silk Weaver")
                .titleHindi("मास्टर बनारसी सिल्क बुनकर")
                .village("Alaipura")
                .district("Varanasi")
                .state("Uttar Pradesh")
                .stateCode("UP")
                .primaryCraft("Banarasi Silk Weaving")
                .primaryCraftHindi("बनारसी रेशम बुनाई")
                .yearsOfExperience(45)
                .generationsInCraft(5)
                .shortBio("A 5th generation Banarasi silk weaver keeping alive the centuries-old tradition of creating exquisite silk sarees with intricate zari work.")
                .shortBioHindi("पाँचवीं पीढ़ी के बनारसी रेशम बुनकर जो जटिल ज़री के काम के साथ उत्कृष्ट रेशम साड़ियाँ बनाने की सदियों पुरानी परंपरा को जीवित रख रहे हैं।")
                .quote("Each thread I weave carries the prayers and dreams of my ancestors.")
                .quoteHindi("मेरा बुना हर धागा मेरे पूर्वजों की प्रार्थनाओं और सपनों को वहन करता है।")
                .awards(Arrays.asList(
                        Award.builder()
                                .name("National Award for Master Craftsman")
                                .nameHindi("मास्टर शिल्पकार के लिए राष्ट्रीय पुरस्कार")
                                .organization("Ministry of Textiles")
                                .year(2015)
                                .level(AwardLevel.NATIONAL)
                                .build(),
                        Award.builder()
                                .name("State Merit Award")
                                .nameHindi("राज्य योग्यता पुरस्कार")
                                .organization("UP Handloom Board")
                                .year(2010)
                                .level(AwardLevel.STATE)
                                .build()
                ))
                .nationalAwardee(true)
                .stateAwardee(true)
                .giTagHolder(true)
                .giTagDetails("Banaras Brocades and Sarees GI Tag")
                .apprenticesTrained(12)
                .availableForWorkshops(true)
                .status(StoryStatus.PUBLISHED)
                .featured(true)
                .verified(true)
                .displayOrder(1)
                .build());
        
        // Story 2: Blue Pottery Artisan
        stories.add(ArtisanStory.builder()
                .slug("lakshmi-devi-blue-pottery")
                .artisanName("Lakshmi Devi")
                .artisanNameHindi("लक्ष्मी देवी")
                .title("Blue Pottery Artist")
                .titleHindi("ब्लू पॉटरी कलाकार")
                .village("Sanganer")
                .district("Jaipur")
                .state("Rajasthan")
                .stateCode("RJ")
                .primaryCraft("Jaipur Blue Pottery")
                .primaryCraftHindi("जयपुर ब्लू पॉटरी")
                .yearsOfExperience(30)
                .generationsInCraft(3)
                .shortBio("A pioneering woman artisan who has mastered the Turkish-inspired blue pottery technique and trained over 50 women in her village.")
                .shortBioHindi("एक अग्रणी महिला कारीगर जिन्होंने तुर्की-प्रेरित ब्लू पॉटरी तकनीक में महारत हासिल की है और अपने गाँव में 50 से अधिक महिलाओं को प्रशिक्षित किया है।")
                .quote("Art has no gender. A woman's hands can create masterpieces just as any man's.")
                .quoteHindi("कला का कोई लिंग नहीं है। एक महिला के हाथ किसी भी पुरुष की तरह उत्कृष्ट कृतियाँ बना सकते हैं।")
                .awards(Arrays.asList(
                        Award.builder()
                                .name("Nari Shakti Puraskar")
                                .nameHindi("नारी शक्ति पुरस्कार")
                                .organization("President of India")
                                .year(2019)
                                .level(AwardLevel.NATIONAL)
                                .build()
                ))
                .nationalAwardee(true)
                .apprenticesTrained(50)
                .availableForWorkshops(true)
                .availableForCommissions(true)
                .status(StoryStatus.PUBLISHED)
                .featured(true)
                .verified(true)
                .displayOrder(2)
                .build());
        
        // Story 3: Brass Worker
        stories.add(ArtisanStory.builder()
                .slug("mohammad-salim-brass-artisan")
                .artisanName("Mohammad Salim")
                .artisanNameHindi("मोहम्मद सलीम")
                .title("Traditional Brass Artisan")
                .titleHindi("पारंपरिक पीतल कारीगर")
                .village("Moradabad")
                .district("Moradabad")
                .state("Uttar Pradesh")
                .stateCode("UP")
                .primaryCraft("Moradabad Brass Work")
                .primaryCraftHindi("मुरादाबाद पीतल का काम")
                .yearsOfExperience(35)
                .generationsInCraft(4)
                .shortBio("Continuing the legacy of Moradabad's famous brass industry, Mohammad Salim creates intricate brass artifacts that blend traditional motifs with contemporary designs.")
                .shortBioHindi("मुरादाबाद के प्रसिद्ध पीतल उद्योग की विरासत को जारी रखते हुए, मोहम्मद सलीम जटिल पीतल की कलाकृतियाँ बनाते हैं जो पारंपरिक रूपांकनों को समकालीन डिज़ाइनों के साथ मिश्रित करती हैं।")
                .quote("Brass tells stories - of kings and queens, of temples and mosques, of India's unity.")
                .quoteHindi("पीतल कहानियाँ सुनाता है - राजाओं और रानियों की, मंदिरों और मस्जिदों की, भारत की एकता की।")
                .stateAwardee(true)
                .giTagHolder(true)
                .giTagDetails("Moradabad Metal Craft GI Tag")
                .apprenticesTrained(20)
                .availableForCommissions(true)
                .status(StoryStatus.PUBLISHED)
                .featured(true)
                .verified(true)
                .displayOrder(3)
                .build());
        
        // Story 4: Kanchipuram Weaver
        stories.add(ArtisanStory.builder()
                .slug("selvi-muthu-kanchipuram-weaver")
                .artisanName("Selvi Muthu")
                .artisanNameHindi("सेल्वी मुत्तु")
                .title("Kanchipuram Silk Weaver")
                .titleHindi("कांचीपुरम सिल्क बुनकर")
                .village("Kanchipuram")
                .district("Kanchipuram")
                .state("Tamil Nadu")
                .stateCode("TN")
                .primaryCraft("Kanchipuram Silk Weaving")
                .primaryCraftHindi("कांचीपुरम सिल्क बुनाई")
                .yearsOfExperience(40)
                .generationsInCraft(6)
                .shortBio("A 6th generation weaver whose family has been creating temple sarees for centuries, Selvi continues the tradition of weaving pure zari Kanchipuram silk sarees.")
                .shortBioHindi("छठी पीढ़ी की बुनकर जिनका परिवार सदियों से मंदिर साड़ियाँ बना रहा है, सेल्वी शुद्ध ज़री कांचीपुरम सिल्क साड़ियाँ बुनने की परंपरा जारी रखती हैं।")
                .quote("When I weave a temple border, I weave the blessings of a thousand years.")
                .quoteHindi("जब मैं मंदिर का बॉर्डर बुनती हूँ, मैं हज़ार साल के आशीर्वाद बुनती हूँ।")
                .awards(Arrays.asList(
                        Award.builder()
                                .name("Padma Shri")
                                .nameHindi("पद्म श्री")
                                .organization("Government of India")
                                .year(2018)
                                .level(AwardLevel.NATIONAL)
                                .build()
                ))
                .nationalAwardee(true)
                .giTagHolder(true)
                .giTagDetails("Kanchipuram Silk GI Tag")
                .apprenticesTrained(25)
                .status(StoryStatus.PUBLISHED)
                .featured(true)
                .verified(true)
                .displayOrder(4)
                .build());
        
        // Story 5: Madhubani Painter
        stories.add(ArtisanStory.builder()
                .slug("parvati-devi-madhubani-artist")
                .artisanName("Parvati Devi")
                .artisanNameHindi("पार्वती देवी")
                .title("Madhubani Artist")
                .titleHindi("मधुबनी कलाकार")
                .village("Ranti")
                .district("Madhubani")
                .state("Bihar")
                .stateCode("BR")
                .primaryCraft("Madhubani Painting")
                .primaryCraftHindi("मधुबनी चित्रकला")
                .yearsOfExperience(50)
                .generationsInCraft(4)
                .shortBio("A living legend of Madhubani art, Parvati Devi has transformed wall paintings of rural Bihar into internationally acclaimed fine art.")
                .shortBioHindi("मधुबनी कला की जीवित किंवदंती, पार्वती देवी ने ग्रामीण बिहार की दीवार चित्रों को अंतरराष्ट्रीय स्तर पर प्रशंसित ललित कला में बदल दिया है।")
                .quote("My brush speaks the language of my grandmother, my mother, and all the women who came before me.")
                .quoteHindi("मेरी तूलिका मेरी दादी, मेरी माँ और मुझसे पहले आई सभी महिलाओं की भाषा बोलती है।")
                .awards(Arrays.asList(
                        Award.builder()
                                .name("Padma Shri")
                                .nameHindi("पद्म श्री")
                                .organization("Government of India")
                                .year(2017)
                                .level(AwardLevel.NATIONAL)
                                .build(),
                        Award.builder()
                                .name("National Award")
                                .nameHindi("राष्ट्रीय पुरस्कार")
                                .organization("All India Handicrafts Board")
                                .year(2005)
                                .level(AwardLevel.NATIONAL)
                                .build()
                ))
                .nationalAwardee(true)
                .giTagHolder(true)
                .giTagDetails("Madhubani Paintings GI Tag")
                .apprenticesTrained(100)
                .availableForWorkshops(true)
                .status(StoryStatus.PUBLISHED)
                .featured(true)
                .verified(true)
                .displayOrder(5)
                .build());
        
        return stories;
    }
}
