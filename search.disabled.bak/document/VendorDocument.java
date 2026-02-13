package com.odop.root.search.document;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.Id;
import org.springframework.data.elasticsearch.annotations.*;

import java.util.List;

/**
 * Elasticsearch document for Vendor indexing
 * Enables search for vendors/shops
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Document(indexName = "#{@environment.getProperty('search.index.vendors', 'odop_vendors')}")
public class VendorDocument {

    @Id
    private String id;

    @Field(type = FieldType.Text, analyzer = "standard")
    private String shoppeeName;

    @Field(type = FieldType.Text)
    private String shopkeeperName;

    @Field(type = FieldType.Keyword)
    private String emailAddress;

    @Field(type = FieldType.Text)
    private String businessDescription;

    @Field(type = FieldType.Keyword)
    private String locationDistrict;

    @Field(type = FieldType.Keyword)
    private String locationState;

    @Field(type = FieldType.Keyword)
    private String pinCode;

    @Field(type = FieldType.Text)
    private String completeAddress;

    @Field(type = FieldType.Keyword)
    private List<String> productCategories;

    @Field(type = FieldType.Integer)
    private Integer productCount;

    @Field(type = FieldType.Keyword)
    private String status;

    @Field(type = FieldType.Boolean)
    private Boolean verified;

    @Field(type = FieldType.Text)
    private String profilePictureUrl;

    @Field(type = FieldType.Double)
    private Double averageRating;

    @Field(type = FieldType.Integer)
    private Integer totalReviews;

    // Suggest field for autocomplete (stored as array of strings)
    @Field(type = FieldType.Keyword)
    private List<String> suggest;
}
