package jtrenado.scanFiles.infrastructure.repositories;

import jtrenado.scanFiles.infrastructure.entities.File;
import org.springframework.data.domain.Pageable;
import org.springframework.data.mongodb.repository.MongoRepository;

import java.util.List;
import java.util.Optional;

public interface FileRepository extends MongoRepository<File, String>, CustomFileRepository {

    Optional<File> findByPath(String path);

    List<File> findByHash(String hash);

    List<File> findAllByHashIsNullOrFootprintIsNullOrSizeIsNull();

    List<File> findAllByHashIsNullOrSizeIsNull(Pageable pageable);

}
