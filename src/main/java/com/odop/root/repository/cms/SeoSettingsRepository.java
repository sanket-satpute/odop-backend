package com.odop.root.repository.cms;

import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;

import com.odop.root.models.cms.SeoSettings;

@Repository
public interface SeoSettingsRepository extends MongoRepository<SeoSettings, String> {
    // SEO settings is a singleton document, so we just use findAll().get(0) or create if not exists
}
