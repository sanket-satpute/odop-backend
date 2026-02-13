package com.odop.root.bulkupload.dto;

import lombok.*;

import java.util.List;
import java.util.Map;

/**
 * DTO for template information
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class UploadTemplateInfo {
    
    private String uploadType;
    private String description;
    private List<TemplateColumn> requiredColumns;
    private List<TemplateColumn> optionalColumns;
    private String sampleCsvContent;
    private String downloadUrl;
    
    /**
     * Column definition for template
     */
    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class TemplateColumn {
        private String name;
        private String displayName;
        private String dataType;          // STRING, NUMBER, DECIMAL, BOOLEAN, DATE
        private String description;
        private boolean required;
        private String defaultValue;
        private List<String> allowedValues; // For enum-like fields
        private String example;
        
        public static TemplateColumn required(String name, String dataType, String description, String example) {
            return TemplateColumn.builder()
                    .name(name)
                    .displayName(name.replace("_", " "))
                    .dataType(dataType)
                    .description(description)
                    .required(true)
                    .example(example)
                    .build();
        }
        
        public static TemplateColumn optional(String name, String dataType, String description, String example) {
            return TemplateColumn.builder()
                    .name(name)
                    .displayName(name.replace("_", " "))
                    .dataType(dataType)
                    .description(description)
                    .required(false)
                    .example(example)
                    .build();
        }
    }
    
    /**
     * Get product upload template
     */
    public static UploadTemplateInfo getProductTemplate() {
        return UploadTemplateInfo.builder()
                .uploadType("PRODUCTS")
                .description("Upload products with details like name, price, description, stock")
                .requiredColumns(List.of(
                        TemplateColumn.required("product_name", "STRING", "Name of the product", "Handmade Silk Saree"),
                        TemplateColumn.required("price", "DECIMAL", "Selling price in INR", "2500.00"),
                        TemplateColumn.required("stock_quantity", "NUMBER", "Available stock", "50")
                ))
                .optionalColumns(List.of(
                        TemplateColumn.optional("sku", "STRING", "Unique product code (auto-generated if empty)", "SILK-SAR-001"),
                        TemplateColumn.optional("mrp", "DECIMAL", "Maximum retail price", "3000.00"),
                        TemplateColumn.optional("description", "STRING", "Product description", "Beautiful handwoven silk saree"),
                        TemplateColumn.optional("short_description", "STRING", "Brief description", "Silk saree with zari work"),
                        TemplateColumn.optional("category_id", "STRING", "Category ID", "cat-123"),
                        TemplateColumn.optional("weight", "DECIMAL", "Weight in kg", "0.5"),
                        TemplateColumn.optional("length", "DECIMAL", "Length in cm", "550"),
                        TemplateColumn.optional("width", "DECIMAL", "Width in cm", "120"),
                        TemplateColumn.optional("height", "DECIMAL", "Height in cm", "5"),
                        TemplateColumn.optional("tags", "STRING", "Comma-separated tags", "silk,handmade,traditional"),
                        TemplateColumn.optional("image_urls", "STRING", "Comma-separated image URLs", "url1,url2"),
                        TemplateColumn.optional("is_active", "BOOLEAN", "Is product active", "true"),
                        TemplateColumn.optional("hsn_code", "STRING", "HSN code for GST", "5007"),
                        TemplateColumn.optional("gst_rate", "NUMBER", "GST percentage", "12")
                ))
                .sampleCsvContent("""
                        product_name,price,stock_quantity,mrp,description,category_id,tags,is_active
                        Handmade Silk Saree,2500.00,50,3000.00,Beautiful handwoven silk saree,cat-123,"silk,handmade",true
                        Cotton Kurta Set,1200.00,100,1500.00,Traditional cotton kurta with pajama,cat-456,"cotton,ethnic",true
                        Brass Diya Set,450.00,200,550.00,Set of 5 decorative brass diyas,cat-789,"brass,decor",true
                        """)
                .build();
    }
    
    /**
     * Get variant upload template
     */
    public static UploadTemplateInfo getVariantTemplate() {
        return UploadTemplateInfo.builder()
                .uploadType("VARIANTS")
                .description("Upload product variants with size, color, and other attributes")
                .requiredColumns(List.of(
                        TemplateColumn.required("product_id", "STRING", "Parent product ID", "prod-123"),
                        TemplateColumn.required("price", "DECIMAL", "Variant price", "2500.00"),
                        TemplateColumn.required("stock_quantity", "NUMBER", "Available stock", "25")
                ))
                .optionalColumns(List.of(
                        TemplateColumn.optional("sku", "STRING", "Unique variant code", "SILK-SAR-001-RED-M"),
                        TemplateColumn.optional("size", "STRING", "Size attribute", "M"),
                        TemplateColumn.optional("color", "STRING", "Color attribute", "Red"),
                        TemplateColumn.optional("material", "STRING", "Material attribute", "Silk"),
                        TemplateColumn.optional("weight", "STRING", "Weight variant", "500g"),
                        TemplateColumn.optional("mrp", "DECIMAL", "Maximum retail price", "3000.00"),
                        TemplateColumn.optional("image_url", "STRING", "Variant image URL", "https://..."),
                        TemplateColumn.optional("is_default", "BOOLEAN", "Is default variant", "false"),
                        TemplateColumn.optional("is_active", "BOOLEAN", "Is variant active", "true")
                ))
                .sampleCsvContent("""
                        product_id,sku,size,color,price,mrp,stock_quantity,is_active
                        prod-123,SILK-SAR-001-RED-S,S,Red,2400.00,3000.00,10,true
                        prod-123,SILK-SAR-001-RED-M,M,Red,2500.00,3000.00,15,true
                        prod-123,SILK-SAR-001-BLUE-M,M,Blue,2500.00,3000.00,20,true
                        """)
                .build();
    }
    
    /**
     * Get price update template
     */
    public static UploadTemplateInfo getPriceUpdateTemplate() {
        return UploadTemplateInfo.builder()
                .uploadType("PRICE_UPDATE")
                .description("Bulk update product or variant prices")
                .requiredColumns(List.of(
                        TemplateColumn.required("identifier", "STRING", "Product ID, Variant ID, or SKU", "prod-123 or SKU-001"),
                        TemplateColumn.required("new_price", "DECIMAL", "New selling price", "2500.00")
                ))
                .optionalColumns(List.of(
                        TemplateColumn.optional("identifier_type", "STRING", "Type: PRODUCT_ID, VARIANT_ID, SKU", "SKU"),
                        TemplateColumn.optional("new_mrp", "DECIMAL", "New MRP", "3000.00"),
                        TemplateColumn.optional("new_cost_price", "DECIMAL", "New cost price", "1500.00")
                ))
                .sampleCsvContent("""
                        identifier,identifier_type,new_price,new_mrp
                        SKU-001,SKU,2500.00,3000.00
                        prod-123,PRODUCT_ID,1200.00,1500.00
                        var-456,VARIANT_ID,450.00,550.00
                        """)
                .build();
    }
    
    /**
     * Get stock update template
     */
    public static UploadTemplateInfo getStockUpdateTemplate() {
        return UploadTemplateInfo.builder()
                .uploadType("STOCK_UPDATE")
                .description("Bulk update product or variant stock quantities")
                .requiredColumns(List.of(
                        TemplateColumn.required("identifier", "STRING", "Product ID, Variant ID, or SKU", "prod-123 or SKU-001"),
                        TemplateColumn.required("quantity", "NUMBER", "Stock quantity", "50")
                ))
                .optionalColumns(List.of(
                        TemplateColumn.optional("identifier_type", "STRING", "Type: PRODUCT_ID, VARIANT_ID, SKU", "SKU"),
                        TemplateColumn.optional("adjustment_type", "STRING", "ABSOLUTE (set to) or RELATIVE (add/subtract)", "ABSOLUTE"),
                        TemplateColumn.optional("low_stock_threshold", "NUMBER", "Low stock alert threshold", "10"),
                        TemplateColumn.optional("reason", "STRING", "Reason for stock change", "New shipment received")
                ))
                .sampleCsvContent("""
                        identifier,identifier_type,quantity,adjustment_type,reason
                        SKU-001,SKU,100,ABSOLUTE,Full restock
                        prod-123,PRODUCT_ID,25,RELATIVE,Additional units
                        var-456,VARIANT_ID,-5,RELATIVE,Damaged units removed
                        """)
                .build();
    }
}
