package jtrenado.oldscanfiles.infrastructure.repositories;

import reactor.core.publisher.Flux;

public interface CustomFileRepository {

    Flux<String> findDuplicatedHashes();

}
