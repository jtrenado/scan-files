package jtrenado.newScanFiles.adapters.mongoadapter.repository;


import jtrenado.newScanFiles.adapters.mongoadapter.entities.FileEntity;
import lombok.Getter;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.mongodb.core.ReactiveMongoTemplate;
import org.springframework.data.mongodb.core.aggregation.Aggregation;
import org.springframework.data.mongodb.core.aggregation.GroupOperation;
import org.springframework.data.mongodb.core.aggregation.MatchOperation;
import org.springframework.data.mongodb.core.aggregation.ProjectionOperation;
import org.springframework.data.mongodb.core.query.Criteria;
import reactor.core.publisher.Flux;

import static org.springframework.data.mongodb.core.aggregation.Aggregation.*;

public class CustomFileRepositoryImpl implements CustomFileRepository {

    @Autowired
    private ReactiveMongoTemplate mongoTemplate;

    @Override
    public Flux<String> findDuplicatedHashes() {

        MatchOperation match1 = match(Criteria.where("delete").is(Boolean.FALSE));
        GroupOperation group = group("hash").count().as("count");

        MatchOperation match2 = match(Criteria.where("count").gt(1).and("_id").ne(null));
        ProjectionOperation project = project().andExclude("_id").andExpression("_id").as("hash");
        Aggregation aggregation = newAggregation(match1, group, match2, project);
        return mongoTemplate.aggregate(aggregation, mongoTemplate.getCollectionName(FileEntity.class), OutType.class).map(CustomFileRepositoryImpl.OutType::getHash);

    }

    private class OutType {
        @Getter
        String hash;
    }
}