package jtrenado.scanFiles.application.schedulers;

import jtrenado.scanFiles.infrastructure.entities.File;
import jtrenado.scanFiles.infrastructure.repositories.FileRepository;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
@Slf4j
public class DupeCleaner {

    @Autowired
    private FileRepository fileRepository;

    @Scheduled(fixedDelayString = "${app.dupe-cleaner.fixed-delay}", initialDelayString = "${app.dupe-cleaner.initial-delay}")
    public void cleanDuped() {
        List<String> hashes = fileRepository.findDuplicatedHashes();
        log.info(hashes.toString());

        hashes.stream().forEach(this::cleanDuped);
    }

    private void cleanDuped(String hash) {
        List<File> files = fileRepository.findByHash(hash);

        files.sort((f1, f2) -> f1.getPath().compareTo(f2.getPath()));

        File original = files.get(0);
        List<File> copies = files.subList(1, files.size());
        copies.stream().forEach(c -> markToDelete(c, original.getPath()));
    }

    private void markToDelete(File file, String path) {
        file.setDelete(true);
        file.setPathToOriginal(path);
        fileRepository.save(file);
    }

}
