package jtrenado.newScanFiles.adapters.mongoadapter.repository;


import jtrenado.newScanFiles.adapters.mongoadapter.entities.FileEntity;
import org.springframework.data.mongodb.repository.ReactiveMongoRepository;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

public interface FileRepository extends ReactiveMongoRepository<FileEntity, String>, CustomFileRepository {

    Mono<FileEntity> findByPath(String path);

    Mono<Boolean> existsByPath(String path);

    Flux<FileEntity> findByHash(String hash);

    Flux<FileEntity> findAllByHashIsNullOrFootprintIsNullOrSizeIsNull();

    Flux<FileEntity> findAllByHashIsNullOrSizeIsNull();

}

