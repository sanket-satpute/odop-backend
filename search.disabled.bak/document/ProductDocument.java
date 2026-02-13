package com.odop.root.search.document;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.Id;
import org.springframework.data.elasticsearch.annotations.*;

import java.time.LocalDateTime;
import java.util.List;

/**
 * Elasticsearch document for Product indexing
 * Maps MongoDB Products to searchable ES index
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Document(indexName = "#{@environment.getProperty('search.index.products', 'odop_products')}")
@Setting(settingPath = "elasticsearch/product-settings.json")
public class ProductDocument {

    @Id
    private String id;

    @Field(type = FieldType.Text, analyzer = "standard", searchAnalyzer = "standard")
    private String productName;

    @Field(type = FieldType.Text, analyzer = "standard")
    private String productDescription;

    @Field(type = FieldType.Keyword)
    private String categoryId;

    @Field(type = FieldType.Keyword)
    private String categoryName;

    @Field(type = FieldType.Keyword)
    private String subCategoryId;

    @Field(type = FieldType.Double)
    private Double price;

    @Field(type = FieldType.Double)
    private Double discountedPrice;

    @Field(type = FieldType.Integer)
    private Integer discount;

    @Field(type = FieldType.Long)
    private Long quantity;

    @Field(type = FieldType.Keyword)
    private String stockStatus;

    @Field(type = FieldType.Text)
    private String productImageURL;

    @Field(type = FieldType.Keyword)
    private String vendorId;

    @Field(type = FieldType.Text)
    private String vendorName;

    @Field(type = FieldType.Text)
    private String shopName;

    @Field(type = FieldType.Integer)
    private Integer rating;

    @Field(type = FieldType.Keyword)
    private List<String> tags;

    // ODOP specific fields
    @Field(type = FieldType.Keyword)
    private String originDistrict;

    @Field(type = FieldType.Keyword)
    private String originState;

    @Field(type = FieldType.Keyword)
    private String originPinCode;

    @Field(type = FieldType.Text)
    private String localName;

    @Field(type = FieldType.Text)
    private String originStory;

    @Field(type = FieldType.Keyword)
    private String craftType;

    @Field(type = FieldType.Text)
    private String madeBy;

    @Field(type = FieldType.Text)
    private String materialsUsed;

    // GI Tag fields
    @Field(type = FieldType.Boolean)
    private Boolean giTagCertified;

    @Field(type = FieldType.Keyword)
    private String giTagNumber;

    // Search boosting fields
    @Field(type = FieldType.Integer)
    private Integer popularityScore;

    @Field(type = FieldType.Integer)
    private Integer totalSold;

    @Field(type = FieldType.Boolean)
    private Boolean promotionEnabled;

    @Field(type = FieldType.Text)
    private String specification;

    @Field(type = FieldType.Date, format = DateFormat.date_hour_minute_second)
    private LocalDateTime createdAt;

    @Field(type = FieldType.Date, format = DateFormat.date_hour_minute_second)
    private LocalDateTime updatedAt;

    // Suggest field for autocomplete (stored as array of strings)
    @Field(type = FieldType.Keyword)
    private List<String> suggest;
}
