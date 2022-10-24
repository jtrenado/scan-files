package jtrenado.oldscanfiles.infrastructure.repositories;

import jtrenado.oldscanfiles.infrastructure.entities.File;
import org.springframework.data.mongodb.repository.ReactiveMongoRepository;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

public interface FileRepository extends ReactiveMongoRepository<File, String>, CustomFileRepository {

    Mono<File> findByPath(String path);

    Mono<Boolean> existsByPath(String path);

    Flux<File> findByHash(String hash);

    Flux<File> findAllByHashIsNullOrFootprintIsNullOrSizeIsNull();

    Flux<File> findAllByHashIsNullOrSizeIsNull();

}
