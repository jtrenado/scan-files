package jtrenado.scanFiles.infrastructure.configuration;

import jtrenado.scanFiles.infrastructure.entities.File;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.domain.Sort;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.index.Index;

import javax.annotation.PostConstruct;

@Configuration
public class MongoConfig {

    @Autowired
    private MongoTemplate mongoTemplate;

    @PostConstruct
    public final void ensureIndex() {
        createIndexes();
    }

    private void createIndexes() {
        final boolean exitsCollection = mongoTemplate.collectionExists(File.class);

        if (!exitsCollection) {
            mongoTemplate.createCollection(File.class);
        }
        ensureIndexes();
    }

    private void ensureIndexes() {

        mongoTemplate.indexOps(File.class).ensureIndex(new Index().named("path").on("path", Sort.Direction.ASC).unique().sparse());
    }

}
