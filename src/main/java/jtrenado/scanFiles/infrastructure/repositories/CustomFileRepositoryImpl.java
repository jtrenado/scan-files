package jtrenado.scanFiles.infrastructure.repositories;

import jtrenado.scanFiles.infrastructure.entities.File;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.aggregation.*;
import org.springframework.data.mongodb.core.query.Criteria;

import java.util.List;
import java.util.stream.Collectors;

import static org.springframework.data.mongodb.core.aggregation.Aggregation.*;

public class CustomFileRepositoryImpl implements CustomFileRepository {

    @Autowired
    private MongoTemplate mongoTemplate;


    /*
    db.files3.aggregate([
    {"$group" : { "_id": "$hash", "count": { "$sum": 1 } } },
    {"$match": {"_id" :{ "$ne" : null } , "count" : {"$gt": 1} } },
    {"$project": {"hash" : "$_id", "_id" : 0} }
])
     */

    @Override
    public List<String> findDuplicatedHashes() {

        MatchOperation match1 = match(Criteria.where("delete").is(Boolean.FALSE));
        GroupOperation group = group("hash").count().as("count");

        MatchOperation match2 = match(Criteria.where("count").gt(1).and("_id").ne(null));
        ProjectionOperation project = project().andExclude("_id").andExpression("_id").as("hash");
        Aggregation aggregation = newAggregation(match1, group, match2, project);
        AggregationResults<OutType> output = mongoTemplate.aggregate(aggregation, mongoTemplate.getCollectionName(File.class), OutType.class);

        return output.getMappedResults().stream().map(o -> o.hash).collect(Collectors.toList());

    }

    private class OutType {
        String hash;
    }
}

/**
 * db.files_dev.aggregate(
 * { "aggregate" : "__collection__", "pipeline" : [{ "$group" : { "_id" : "$hash", "count" : { "$sum" : 1}}}, { "$match" : { "count" : { "$gt" : 1}, "_id" : { "$ne" : null}}}, { "$project" : { "_id" : 0, "hash" : "$_id"}}]}
 * )
 */