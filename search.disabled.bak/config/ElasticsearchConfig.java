package com.odop.root.search.config;

import co.elastic.clients.elasticsearch.ElasticsearchClient;
import co.elastic.clients.json.jackson.JacksonJsonpMapper;
import co.elastic.clients.transport.ElasticsearchTransport;
import co.elastic.clients.transport.rest_client.RestClientTransport;
import jakarta.annotation.PostConstruct;
import org.apache.http.HttpHost;
import org.apache.http.auth.AuthScope;
import org.apache.http.auth.UsernamePasswordCredentials;
import org.apache.http.impl.client.BasicCredentialsProvider;
import org.elasticsearch.client.RestClient;
import org.elasticsearch.client.RestClientBuilder;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.elasticsearch.repository.config.EnableElasticsearchRepositories;

/**
 * Elasticsearch Configuration for Advanced Search
 * 
 * Features:
 * - Full-text search with relevance scoring
 * - Fuzzy matching for typo tolerance
 * - Autocomplete suggestions
 * - Faceted search (filters + counts)
 * - Geo-spatial search
 * 
 * Setup:
 * 1. Download Elasticsearch from https://www.elastic.co/downloads/elasticsearch
 * 2. Or use Docker: docker run -d -p 9200:9200 -e "discovery.type=single-node" elasticsearch:8.11.0
 * 3. Update application.yml with connection details
 * 
 * Set search.enabled=true in application.yml to enable Elasticsearch
 */
@Configuration
@ConditionalOnProperty(name = "search.enabled", havingValue = "true", matchIfMissing = false)
@EnableElasticsearchRepositories(basePackages = "com.odop.root.search.repository")
public class ElasticsearchConfig {

    private static final Logger logger = LoggerFactory.getLogger(ElasticsearchConfig.class);

    @Value("${spring.elasticsearch.uris:http://localhost:9200}")
    private String elasticsearchUri;

    @Value("${spring.elasticsearch.username:}")
    private String username;

    @Value("${spring.elasticsearch.password:}")
    private String password;

    @Value("${search.enabled:true}")
    private boolean searchEnabled;

    @PostConstruct
    public void init() {
        if (searchEnabled) {
            logger.info("🔍 Elasticsearch search enabled. URI: {}", elasticsearchUri);
        } else {
            logger.warn("⚠️ Elasticsearch search disabled. Using MongoDB fallback.");
        }
    }

    @Bean
    public RestClient restClient() {
        // Parse URI
        String host = "localhost";
        int port = 9200;
        String scheme = "http";

        try {
            java.net.URI uri = java.net.URI.create(elasticsearchUri);
            host = uri.getHost() != null ? uri.getHost() : "localhost";
            port = uri.getPort() > 0 ? uri.getPort() : 9200;
            scheme = uri.getScheme() != null ? uri.getScheme() : "http";
        } catch (Exception e) {
            logger.warn("Failed to parse ES URI, using defaults: {}", e.getMessage());
        }

        RestClientBuilder builder = RestClient.builder(new HttpHost(host, port, scheme));

        // Add authentication if provided
        if (username != null && !username.isEmpty() && password != null && !password.isEmpty()) {
            BasicCredentialsProvider credentialsProvider = new BasicCredentialsProvider();
            credentialsProvider.setCredentials(AuthScope.ANY,
                new UsernamePasswordCredentials(username, password));

            builder.setHttpClientConfigCallback(httpClientBuilder ->
                httpClientBuilder.setDefaultCredentialsProvider(credentialsProvider));
        }

        // Configure timeouts
        builder.setRequestConfigCallback(requestConfigBuilder ->
            requestConfigBuilder
                .setConnectTimeout(5000)
                .setSocketTimeout(30000));

        return builder.build();
    }

    @Bean
    public ElasticsearchTransport elasticsearchTransport(RestClient restClient) {
        return new RestClientTransport(restClient, new JacksonJsonpMapper());
    }

    @Bean
    public ElasticsearchClient elasticsearchClient(ElasticsearchTransport transport) {
        return new ElasticsearchClient(transport);
    }

    public boolean isSearchEnabled() {
        return searchEnabled;
    }
}
