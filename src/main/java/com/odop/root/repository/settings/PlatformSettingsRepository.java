package com.odop.root.repository.settings;

import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;

import com.odop.root.models.settings.PlatformSettings;

@Repository
public interface PlatformSettingsRepository extends MongoRepository<PlatformSettings, String> {
    // Platform settings is a singleton document
}
