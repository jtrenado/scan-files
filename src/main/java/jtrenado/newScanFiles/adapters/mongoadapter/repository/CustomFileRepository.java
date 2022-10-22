package jtrenado.newScanFiles.adapters.mongoadapter.repository;


import reactor.core.publisher.Flux;

public interface CustomFileRepository {

    Flux<String> findDuplicatedHashes();

}
