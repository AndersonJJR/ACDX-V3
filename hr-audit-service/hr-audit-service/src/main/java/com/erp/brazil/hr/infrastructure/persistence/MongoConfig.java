package com.erp.brazil.hr.infrastructure.persistence;

import com.erp.brazil.hr.domain.model.AuditLog;
import com.mongodb.client.MongoCollection;
import com.mongodb.client.model.Indexes;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.index.IndexOperations;
import org.springframework.data.mongodb.repository.config.EnableMongoRepositories;

import jakarta.annotation.PostConstruct;

@Configuration
@EnableMongoRepositories(basePackages = "com.erp.brazil.hr.domain.repository")
public class MongoConfig {

    private final MongoTemplate mongoTemplate;

    @Value("${mongodb.audit.collection.size:1073741824}") // 1GB default
    private long collectionSize;

    @Value("${mongodb.audit.collection.max-docs:10000000}") // 10M docs default
    private long maxDocuments;

    @Value("${mongodb.audit.ttl.days:2555}") // 7 years default
    private long ttlDays;

    public MongoConfig(MongoTemplate mongoTemplate) {
        this.mongoTemplate = mongoTemplate;
    }

    @PostConstruct
    public void initAuditCollection() {
        String collectionName = "audit_logs";

        if (!mongoTemplate.collectionExists(collectionName)) {
            // Create capped collection
            mongoTemplate.createCollection(collectionName, new org.springframework.data.mongodb.core.CollectionOptions()
                .capped()
                .size(collectionSize)
                .maxDocuments(maxDocuments));

            // Create TTL index on timestamp (7 years = 2555 days)
            IndexOperations indexOps = mongoTemplate.indexOps(collectionName);
            indexOps.ensureIndex(new org.springframework.data.mongodb.core.index.Index()
                .on("timestamp", org.springframework.data.mongodb.core.index.IndexDirection.DESC)
                .expire(ttlDays * 24 * 60 * 60)); // Convert days to seconds

            // Create compound indexes for common queries
            indexOps.ensureIndex(new org.springframework.data.mongodb.core.index.Index()
                .on("companyId", org.springframework.data.mongodb.core.index.IndexDirection.ASC)
                .on("timestamp", org.springframework.data.mongodb.core.index.IndexDirection.DESC));

            indexOps.ensureIndex(new org.springframework.data.mongodb.core.index.Index()
                .on("entityType", org.springframework.data.mongodb.core.index.IndexDirection.ASC)
                .on("entityId", org.springframework.data.mongodb.core.index.IndexDirection.ASC));

            indexOps.ensureIndex(new org.springframework.data.mongodb.core.index.Index()
                .on("userId", org.springframework.data.mongodb.core.index.IndexDirection.ASC));

            indexOps.ensureIndex(new org.springframework.data.mongodb.core.index.Index()
                .on("correlationId", org.springframework.data.mongodb.core.index.IndexDirection.ASC));

            // Create a text index for searching
            indexOps.ensureIndex(new org.springframework.data.mongodb.core.index.Index()
                .on("eventId", org.springframework.data.mongodb.core.index.IndexDirection.ASC));
        }
    }
}