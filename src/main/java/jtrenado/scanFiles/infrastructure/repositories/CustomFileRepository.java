package jtrenado.scanFiles.infrastructure.repositories;

import java.util.List;

public interface CustomFileRepository {

    List<String> findDuplicatedHashes();

}
