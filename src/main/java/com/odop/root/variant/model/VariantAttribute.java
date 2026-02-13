package com.odop.root.variant.model;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

import java.util.List;

/**
 * Variant Attribute Definition - defines what attributes a product can have
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Document(collection = "variant_attributes")
public class VariantAttribute {
    
    @Id
    private String id;
    
    private String name;            // e.g., "Size", "Color", "Material"
    private String displayName;     // For UI display
    private String code;            // Internal code (lowercase, no spaces)
    
    // Predefined values for this attribute
    private List<AttributeValue> values;
    
    // Display settings
    private AttributeDisplayType displayType;
    private int displayOrder;
    
    // Category association (optional - some attributes are category-specific)
    private List<String> categoryIds;
    
    // Is this required for products?
    private boolean required;
    
    // Allow custom values beyond predefined list
    private boolean allowCustomValues;
    
    /**
     * Attribute value with optional display properties
     */
    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class AttributeValue {
        private String value;       // e.g., "Red", "XL"
        private String displayValue;// For UI
        private String colorCode;   // Hex color for color attributes
        private String imageUrl;    // Optional image for swatch
        private int displayOrder;
        private boolean active;
    }
    
    /**
     * How to display this attribute in UI
     */
    public enum AttributeDisplayType {
        DROPDOWN,       // Standard dropdown
        BUTTONS,        // Button group
        COLOR_SWATCH,   // Color picker
        IMAGE_SWATCH,   // Image thumbnails
        RADIO,          // Radio buttons
        TEXT            // Text input (for custom)
    }
    
    /**
     * Common predefined attributes
     */
    public static VariantAttribute createSizeAttribute() {
        return VariantAttribute.builder()
                .name("Size")
                .displayName("Size")
                .code("size")
                .displayType(AttributeDisplayType.BUTTONS)
                .displayOrder(1)
                .required(false)
                .allowCustomValues(true)
                .values(List.of(
                        AttributeValue.builder().value("XS").displayValue("XS").displayOrder(1).active(true).build(),
                        AttributeValue.builder().value("S").displayValue("S").displayOrder(2).active(true).build(),
                        AttributeValue.builder().value("M").displayValue("M").displayOrder(3).active(true).build(),
                        AttributeValue.builder().value("L").displayValue("L").displayOrder(4).active(true).build(),
                        AttributeValue.builder().value("XL").displayValue("XL").displayOrder(5).active(true).build(),
                        AttributeValue.builder().value("XXL").displayValue("XXL").displayOrder(6).active(true).build()
                ))
                .build();
    }
    
    public static VariantAttribute createColorAttribute() {
        return VariantAttribute.builder()
                .name("Color")
                .displayName("Color")
                .code("color")
                .displayType(AttributeDisplayType.COLOR_SWATCH)
                .displayOrder(2)
                .required(false)
                .allowCustomValues(true)
                .values(List.of(
                        AttributeValue.builder().value("Red").displayValue("Red").colorCode("#FF0000").displayOrder(1).active(true).build(),
                        AttributeValue.builder().value("Blue").displayValue("Blue").colorCode("#0000FF").displayOrder(2).active(true).build(),
                        AttributeValue.builder().value("Green").displayValue("Green").colorCode("#00FF00").displayOrder(3).active(true).build(),
                        AttributeValue.builder().value("Black").displayValue("Black").colorCode("#000000").displayOrder(4).active(true).build(),
                        AttributeValue.builder().value("White").displayValue("White").colorCode("#FFFFFF").displayOrder(5).active(true).build(),
                        AttributeValue.builder().value("Yellow").displayValue("Yellow").colorCode("#FFFF00").displayOrder(6).active(true).build(),
                        AttributeValue.builder().value("Orange").displayValue("Orange").colorCode("#FFA500").displayOrder(7).active(true).build(),
                        AttributeValue.builder().value("Purple").displayValue("Purple").colorCode("#800080").displayOrder(8).active(true).build(),
                        AttributeValue.builder().value("Pink").displayValue("Pink").colorCode("#FFC0CB").displayOrder(9).active(true).build(),
                        AttributeValue.builder().value("Brown").displayValue("Brown").colorCode("#A52A2A").displayOrder(10).active(true).build()
                ))
                .build();
    }
    
    public static VariantAttribute createMaterialAttribute() {
        return VariantAttribute.builder()
                .name("Material")
                .displayName("Material")
                .code("material")
                .displayType(AttributeDisplayType.DROPDOWN)
                .displayOrder(3)
                .required(false)
                .allowCustomValues(true)
                .values(List.of(
                        AttributeValue.builder().value("Cotton").displayValue("Cotton").displayOrder(1).active(true).build(),
                        AttributeValue.builder().value("Silk").displayValue("Silk").displayOrder(2).active(true).build(),
                        AttributeValue.builder().value("Wool").displayValue("Wool").displayOrder(3).active(true).build(),
                        AttributeValue.builder().value("Linen").displayValue("Linen").displayOrder(4).active(true).build(),
                        AttributeValue.builder().value("Polyester").displayValue("Polyester").displayOrder(5).active(true).build(),
                        AttributeValue.builder().value("Leather").displayValue("Leather").displayOrder(6).active(true).build(),
                        AttributeValue.builder().value("Wood").displayValue("Wood").displayOrder(7).active(true).build(),
                        AttributeValue.builder().value("Metal").displayValue("Metal").displayOrder(8).active(true).build(),
                        AttributeValue.builder().value("Ceramic").displayValue("Ceramic").displayOrder(9).active(true).build(),
                        AttributeValue.builder().value("Glass").displayValue("Glass").displayOrder(10).active(true).build()
                ))
                .build();
    }
    
    public static VariantAttribute createWeightAttribute() {
        return VariantAttribute.builder()
                .name("Weight")
                .displayName("Weight")
                .code("weight")
                .displayType(AttributeDisplayType.DROPDOWN)
                .displayOrder(4)
                .required(false)
                .allowCustomValues(true)
                .values(List.of(
                        AttributeValue.builder().value("100g").displayValue("100 grams").displayOrder(1).active(true).build(),
                        AttributeValue.builder().value("250g").displayValue("250 grams").displayOrder(2).active(true).build(),
                        AttributeValue.builder().value("500g").displayValue("500 grams").displayOrder(3).active(true).build(),
                        AttributeValue.builder().value("1kg").displayValue("1 kg").displayOrder(4).active(true).build(),
                        AttributeValue.builder().value("2kg").displayValue("2 kg").displayOrder(5).active(true).build()
                ))
                .build();
    }
}
