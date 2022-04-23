package com.example.demo.infrastructure;

import org.springframework.data.mongodb.repository.MongoRepository;

import java.util.Optional;

public interface FileRepository extends MongoRepository<File, String> {

    Optional<File> findByPath(String path);

}
