package jtrenado.oldscanfiles.infrastructure.repositories;

import jtrenado.oldscanfiles.infrastructure.entities.File;
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


    /*
    db.files3.aggregate([
    {"$group" : { "_id": "$hash", "count": { "$sum": 1 } } },
    {"$match": {"_id" :{ "$ne" : null } , "count" : {"$gt": 1} } },
    {"$project": {"hash" : "$_id", "_id" : 0} }
])
     */

    @Override
    public Flux<String> findDuplicatedHashes() {

        MatchOperation match1 = match(Criteria.where("delete").is(Boolean.FALSE));
        GroupOperation group = group("hash").count().as("count");

        MatchOperation match2 = match(Criteria.where("count").gt(1).and("_id").ne(null));
        ProjectionOperation project = project().andExclude("_id").andExpression("_id").as("hash");
        Aggregation aggregation = newAggregation(match1, group, match2, project);
        return mongoTemplate.aggregate(aggregation, mongoTemplate.getCollectionName(File.class), OutType.class).map(OutType::getHash);

    }

    private class OutType {
        @Getter
        String hash;
    }
}

/**
 * db.files_dev.aggregate(
 * { "aggregate" : "__collection__", "pipeline" : [{ "$group" : { "_id" : "$hash", "count" : { "$sum" : 1}}}, { "$match" : { "count" : { "$gt" : 1}, "_id" : { "$ne" : null}}}, { "$project" : { "_id" : 0, "hash" : "$_id"}}]}
 * )
 */