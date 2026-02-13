package com.odop.root.odopfeatures.govschemes.model;

import lombok.*;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.index.Indexed;
import org.springframework.data.mongodb.core.mapping.Document;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.*;

/**
 * Government Scheme Model for ODOP Platform
 * 
 * Provides information about government schemes for:
 * - Artisans and craftspeople
 * - Small businesses and MSMEs
 * - Rural entrepreneurs
 * - Women entrepreneurs
 * - Export-oriented businesses
 */
@Document(collection = "government_schemes")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class GovernmentScheme {
    
    @Id
    private String id;
    
    @Indexed(unique = true)
    private String slug;
    
    // ==================== Basic Information ====================
    
    private String name;
    private String nameHindi;
    private String shortName;            // Like PMMY, SFURTI, etc.
    private String description;
    private String descriptionHindi;
    private String tagline;
    
    // ==================== Classification ====================
    
    private SchemeType type;
    private SchemeCategory category;
    private List<TargetBeneficiary> targetBeneficiaries;
    private GovernmentLevel level;       // Central, State, or Both
    
    // ==================== Administering Body ====================
    
    private String ministry;             // Ministry of MSME, Ministry of Textiles, etc.
    private String ministryHindi;
    private String implementingAgency;
    private String implementingAgencyWebsite;
    private String contactEmail;
    private String contactPhone;
    private String helplineNumber;
    
    // ==================== Benefits ====================
    
    private List<SchemeBenefit> benefits;
    private String maxFundingAmount;     // Maximum funding available
    private String subsidyPercentage;    // If applicable
    private String interestRate;         // If loan scheme
    private String collateralRequirement;
    
    // ==================== Eligibility ====================
    
    private List<EligibilityCriterion> eligibilityCriteria;
    private String eligibilitySummary;
    private String eligibilitySummaryHindi;
    private List<String> requiredDocuments;
    
    // ==================== Application Process ====================
    
    private String applicationProcess;
    private String applicationProcessHindi;
    private List<ApplicationStep> applicationSteps;
    private String applicationUrl;
    private String applicationFormUrl;
    private ApplicationMode applicationMode;
    private boolean onlineApplicationAvailable;
    
    // ==================== Timeline & Dates ====================
    
    private LocalDate launchDate;
    private LocalDate lastDateToApply;   // If applicable
    private String processingTime;       // Expected processing time
    private boolean openForApplications;
    
    // ==================== Geographic Coverage ====================
    
    private List<String> applicableStates; // Empty means all India
    private boolean panIndiaScheme;
    
    // ==================== Visual Assets ====================
    
    private String logoUrl;
    private String bannerUrl;
    private String thumbnailUrl;
    private String pdfBrochureUrl;
    
    // ==================== Related Information ====================
    
    private List<String> relatedSchemeIds;
    private List<String> craftCategoryIds;  // Applicable craft categories
    private List<String> tags;
    
    // ==================== Success Stories ====================
    
    private List<SuccessStory> successStories;
    
    // ==================== FAQ ====================
    
    private List<FaqItem> faqs;
    
    // ==================== Status ====================
    
    private boolean active;
    private boolean featured;
    private int displayOrder;
    private int popularityScore;         // Based on views/applications
    
    // ==================== SEO ====================
    
    private String seoTitle;
    private String seoDescription;
    private List<String> seoKeywords;
    
    // ==================== Timestamps ====================
    
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
    private LocalDateTime lastVerified;  // When info was last verified
    
    // ==================== Statistics ====================
    
    private long viewCount;
    private long applicationClicks;      // Clicks on "Apply Now"
    
    // ==================== Enums ====================
    
    public enum SchemeType {
        LOAN("Loan Scheme", "ऋण योजना"),
        SUBSIDY("Subsidy Scheme", "सब्सिडी योजना"),
        GRANT("Grant Scheme", "अनुदान योजना"),
        TRAINING("Training & Skill Development", "प्रशिक्षण एवं कौशल विकास"),
        MARKETING("Marketing Support", "विपणन सहायता"),
        INFRASTRUCTURE("Infrastructure Support", "बुनियादी ढांचा सहायता"),
        INSURANCE("Insurance Scheme", "बीमा योजना"),
        PENSION("Pension Scheme", "पेंशन योजना"),
        CERTIFICATION("Certification & Recognition", "प्रमाणन और मान्यता"),
        CLUSTER("Cluster Development", "क्लस्टर विकास"),
        EXPORT("Export Promotion", "निर्यात प्रोत्साहन"),
        COMPOSITE("Composite/Multiple Benefits", "समग्र/बहुविध लाभ");
        
        private final String displayName;
        private final String displayNameHindi;
        
        SchemeType(String displayName, String displayNameHindi) {
            this.displayName = displayName;
            this.displayNameHindi = displayNameHindi;
        }
        
        public String getDisplayName() { return displayName; }
        public String getDisplayNameHindi() { return displayNameHindi; }
    }
    
    public enum SchemeCategory {
        MSME,
        ARTISAN,
        TEXTILE,
        HANDICRAFT,
        HANDLOOM,
        KHADI,
        RURAL_DEVELOPMENT,
        WOMEN_EMPOWERMENT,
        SC_ST_WELFARE,
        SKILL_DEVELOPMENT,
        EXPORT,
        STARTUP,
        AGRICULTURE,
        FOOD_PROCESSING
    }
    
    public enum TargetBeneficiary {
        ARTISANS("Artisans", "कारीगर"),
        WEAVERS("Weavers", "बुनकर"),
        CRAFTSMEN("Craftsmen", "शिल्पकार"),
        MSME("MSMEs", "एमएसएमई"),
        WOMEN_ENTREPRENEURS("Women Entrepreneurs", "महिला उद्यमी"),
        SC_ST("SC/ST Entrepreneurs", "अनुसूचित जाति/जनजाति उद्यमी"),
        RURAL_ENTREPRENEURS("Rural Entrepreneurs", "ग्रामीण उद्यमी"),
        FIRST_GENERATION("First Generation Entrepreneurs", "पहली पीढ़ी के उद्यमी"),
        SHG("Self Help Groups", "स्वयं सहायता समूह"),
        COOPERATIVES("Cooperatives", "सहकारी समितियां"),
        EXPORTERS("Exporters", "निर्यातक"),
        STARTUP("Startups", "स्टार्टअप");
        
        private final String displayName;
        private final String displayNameHindi;
        
        TargetBeneficiary(String displayName, String displayNameHindi) {
            this.displayName = displayName;
            this.displayNameHindi = displayNameHindi;
        }
        
        public String getDisplayName() { return displayName; }
        public String getDisplayNameHindi() { return displayNameHindi; }
    }
    
    public enum GovernmentLevel {
        CENTRAL,
        STATE,
        BOTH
    }
    
    public enum ApplicationMode {
        ONLINE_ONLY,
        OFFLINE_ONLY,
        BOTH
    }
    
    // ==================== Inner Classes ====================
    
    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    public static class SchemeBenefit {
        private String title;
        private String titleHindi;
        private String description;
        private String descriptionHindi;
        private String iconName;
        private String amount;           // If quantifiable
    }
    
    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    public static class EligibilityCriterion {
        private String criterion;
        private String criterionHindi;
        private boolean mandatory;
    }
    
    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    public static class ApplicationStep {
        private int stepNumber;
        private String title;
        private String titleHindi;
        private String description;
        private String descriptionHindi;
        private String actionUrl;
    }
    
    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    public static class SuccessStory {
        private String beneficiaryName;
        private String location;
        private String craft;
        private String story;
        private String imageUrl;
        private String amountReceived;
        private int year;
    }
    
    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    public static class FaqItem {
        private String question;
        private String questionHindi;
        private String answer;
        private String answerHindi;
    }
    
    // ==================== Default Schemes ====================
    
    public static List<GovernmentScheme> getDefaultSchemes() {
        List<GovernmentScheme> schemes = new ArrayList<>();
        
        // PMMY - Pradhan Mantri Mudra Yojana
        schemes.add(GovernmentScheme.builder()
                .slug("pmmy-mudra-loan")
                .name("Pradhan Mantri Mudra Yojana (PMMY)")
                .nameHindi("प्रधानमंत्री मुद्रा योजना")
                .shortName("PMMY")
                .description("Provides loans up to ₹10 lakh to non-corporate, non-farm small/micro enterprises under three categories - Shishu, Kishore, and Tarun.")
                .descriptionHindi("शिशु, किशोर और तरुण तीन श्रेणियों के तहत गैर-कॉर्पोरेट, गैर-कृषि लघु/सूक्ष्म उद्यमों को ₹10 लाख तक का ऋण प्रदान करता है।")
                .tagline("Fund the Unfunded")
                .type(SchemeType.LOAN)
                .category(SchemeCategory.MSME)
                .targetBeneficiaries(Arrays.asList(
                        TargetBeneficiary.ARTISANS,
                        TargetBeneficiary.MSME,
                        TargetBeneficiary.WOMEN_ENTREPRENEURS,
                        TargetBeneficiary.FIRST_GENERATION
                ))
                .level(GovernmentLevel.CENTRAL)
                .ministry("Ministry of Finance")
                .ministryHindi("वित्त मंत्रालय")
                .helplineNumber("1800-180-1111")
                .benefits(Arrays.asList(
                        SchemeBenefit.builder()
                                .title("Shishu Loan")
                                .titleHindi("शिशु ऋण")
                                .description("Loans up to ₹50,000 for startups")
                                .amount("Up to ₹50,000")
                                .build(),
                        SchemeBenefit.builder()
                                .title("Kishore Loan")
                                .titleHindi("किशोर ऋण")
                                .description("Loans from ₹50,001 to ₹5 lakh")
                                .amount("₹50,001 - ₹5,00,000")
                                .build(),
                        SchemeBenefit.builder()
                                .title("Tarun Loan")
                                .titleHindi("तरुण ऋण")
                                .description("Loans from ₹5,00,001 to ₹10 lakh")
                                .amount("₹5,00,001 - ₹10,00,000")
                                .build()
                ))
                .collateralRequirement("No collateral required")
                .applicationUrl("https://www.mudra.org.in")
                .onlineApplicationAvailable(true)
                .applicationMode(ApplicationMode.BOTH)
                .panIndiaScheme(true)
                .openForApplications(true)
                .active(true)
                .featured(true)
                .displayOrder(1)
                .build());
        
        // PM Vishwakarma
        schemes.add(GovernmentScheme.builder()
                .slug("pm-vishwakarma")
                .name("PM Vishwakarma Yojana")
                .nameHindi("पीएम विश्वकर्मा योजना")
                .shortName("PM Vishwakarma")
                .description("A central government scheme to support traditional artisans and craftspeople with recognition, skill training, toolkit support, credit, and market linkage.")
                .descriptionHindi("पारंपरिक कारीगरों और शिल्पकारों को मान्यता, कौशल प्रशिक्षण, टूलकिट सहायता, ऋण और बाजार संपर्क के साथ समर्थन करने के लिए एक केंद्रीय सरकार योजना।")
                .tagline("Empowering Traditional Artisans")
                .type(SchemeType.COMPOSITE)
                .category(SchemeCategory.ARTISAN)
                .targetBeneficiaries(Arrays.asList(
                        TargetBeneficiary.ARTISANS,
                        TargetBeneficiary.CRAFTSMEN
                ))
                .level(GovernmentLevel.CENTRAL)
                .ministry("Ministry of MSME")
                .ministryHindi("सूक्ष्म, लघु और मध्यम उद्यम मंत्रालय")
                .helplineNumber("1800-599-0036")
                .benefits(Arrays.asList(
                        SchemeBenefit.builder()
                                .title("Recognition as Vishwakarma")
                                .titleHindi("विश्वकर्मा के रूप में मान्यता")
                                .description("PM Vishwakarma Certificate and ID Card")
                                .build(),
                        SchemeBenefit.builder()
                                .title("Skill Training")
                                .titleHindi("कौशल प्रशिक्षण")
                                .description("5-15 days training with ₹500/day stipend")
                                .amount("₹500/day")
                                .build(),
                        SchemeBenefit.builder()
                                .title("Toolkit Incentive")
                                .titleHindi("टूलकिट प्रोत्साहन")
                                .description("₹15,000 toolkit grant")
                                .amount("₹15,000")
                                .build(),
                        SchemeBenefit.builder()
                                .title("Credit Support")
                                .titleHindi("ऋण सहायता")
                                .description("Collateral-free loan up to ₹3 lakh at 5% interest")
                                .amount("Up to ₹3,00,000")
                                .build()
                ))
                .applicationUrl("https://pmvishwakarma.gov.in")
                .onlineApplicationAvailable(true)
                .applicationMode(ApplicationMode.ONLINE_ONLY)
                .panIndiaScheme(true)
                .openForApplications(true)
                .active(true)
                .featured(true)
                .displayOrder(2)
                .build());
        
        // SFURTI
        schemes.add(GovernmentScheme.builder()
                .slug("sfurti")
                .name("Scheme of Fund for Regeneration of Traditional Industries (SFURTI)")
                .nameHindi("पारंपरिक उद्योगों के पुनरुद्धार के लिए निधि योजना")
                .shortName("SFURTI")
                .description("Organizes traditional artisans and industries into clusters to enhance their competitiveness and provide support for improved technologies, market access, and skill development.")
                .descriptionHindi("पारंपरिक कारीगरों और उद्योगों को क्लस्टर में संगठित करता है ताकि उनकी प्रतिस्पर्धात्मकता बढ़ाई जा सके।")
                .tagline("Reviving Traditional Industries Through Clusters")
                .type(SchemeType.CLUSTER)
                .category(SchemeCategory.KHADI)
                .targetBeneficiaries(Arrays.asList(
                        TargetBeneficiary.ARTISANS,
                        TargetBeneficiary.COOPERATIVES,
                        TargetBeneficiary.SHG
                ))
                .level(GovernmentLevel.CENTRAL)
                .ministry("Ministry of MSME")
                .ministryHindi("सूक्ष्म, लघु और मध्यम उद्यम मंत्रालय")
                .benefits(Arrays.asList(
                        SchemeBenefit.builder()
                                .title("Cluster Development")
                                .titleHindi("क्लस्टर विकास")
                                .description("Up to ₹8 crore for heritage and regular clusters")
                                .amount("Up to ₹8 Crore")
                                .build(),
                        SchemeBenefit.builder()
                                .title("Skill Development")
                                .titleHindi("कौशल विकास")
                                .description("Training programs for artisans")
                                .build(),
                        SchemeBenefit.builder()
                                .title("Market Access")
                                .titleHindi("बाजार पहुंच")
                                .description("Support for marketing and brand building")
                                .build()
                ))
                .panIndiaScheme(true)
                .openForApplications(true)
                .active(true)
                .featured(true)
                .displayOrder(3)
                .build());
        
        // National Handicrafts Development Programme
        schemes.add(GovernmentScheme.builder()
                .slug("nhdp")
                .name("National Handicrafts Development Programme")
                .nameHindi("राष्ट्रीय हस्तशिल्प विकास कार्यक्रम")
                .shortName("NHDP")
                .description("Comprehensive program for development of handicrafts sector including training, exhibitions, marketing, design development, and welfare of artisans.")
                .descriptionHindi("प्रशिक्षण, प्रदर्शनियों, विपणन, डिजाइन विकास और कारीगरों के कल्याण सहित हस्तशिल्प क्षेत्र के विकास के लिए व्यापक कार्यक्रम।")
                .tagline("Preserving India's Craft Heritage")
                .type(SchemeType.COMPOSITE)
                .category(SchemeCategory.HANDICRAFT)
                .targetBeneficiaries(Arrays.asList(
                        TargetBeneficiary.ARTISANS,
                        TargetBeneficiary.CRAFTSMEN
                ))
                .level(GovernmentLevel.CENTRAL)
                .ministry("Ministry of Textiles")
                .ministryHindi("वस्त्र मंत्रालय")
                .benefits(Arrays.asList(
                        SchemeBenefit.builder()
                                .title("Artisan Card")
                                .titleHindi("कारीगर कार्ड")
                                .description("Pehchan Card for official recognition")
                                .build(),
                        SchemeBenefit.builder()
                                .title("Marketing Events")
                                .titleHindi("विपणन कार्यक्रम")
                                .description("Participation in national and international exhibitions")
                                .build(),
                        SchemeBenefit.builder()
                                .title("Design Development")
                                .titleHindi("डिजाइन विकास")
                                .description("Support for product design and innovation")
                                .build()
                ))
                .panIndiaScheme(true)
                .openForApplications(true)
                .active(true)
                .displayOrder(4)
                .build());
        
        // National Handloom Development Programme
        schemes.add(GovernmentScheme.builder()
                .slug("nhdp-handloom")
                .name("National Handloom Development Programme")
                .nameHindi("राष्ट्रीय हथकरघा विकास कार्यक्रम")
                .shortName("NHDP-Handloom")
                .description("Integrated scheme for development of handloom sector covering cluster development, marketing support, and welfare measures for weavers.")
                .descriptionHindi("हथकरघा क्षेत्र के विकास के लिए एकीकृत योजना जिसमें क्लस्टर विकास, विपणन सहायता और बुनकरों के लिए कल्याणकारी उपाय शामिल हैं।")
                .type(SchemeType.COMPOSITE)
                .category(SchemeCategory.HANDLOOM)
                .targetBeneficiaries(Arrays.asList(
                        TargetBeneficiary.WEAVERS,
                        TargetBeneficiary.COOPERATIVES,
                        TargetBeneficiary.SHG
                ))
                .level(GovernmentLevel.CENTRAL)
                .ministry("Ministry of Textiles")
                .ministryHindi("वस्त्र मंत्रालय")
                .benefits(Arrays.asList(
                        SchemeBenefit.builder()
                                .title("Cluster Development")
                                .titleHindi("क्लस्टर विकास")
                                .description("Block level clusters with common facility centers")
                                .build(),
                        SchemeBenefit.builder()
                                .title("Yarn Supply")
                                .titleHindi("धागा आपूर्ति")
                                .description("Subsidized yarn supply through NHDC")
                                .build(),
                        SchemeBenefit.builder()
                                .title("Design Input")
                                .titleHindi("डिजाइन इनपुट")
                                .description("Design development and product diversification")
                                .build()
                ))
                .panIndiaScheme(true)
                .openForApplications(true)
                .active(true)
                .displayOrder(5)
                .build());
        
        return schemes;
    }
}
