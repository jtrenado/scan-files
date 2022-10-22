package jtrenado.scanFiles.application.schedulers;

import jtrenado.scanFiles.infrastructure.entities.File;
import jtrenado.scanFiles.infrastructure.repositories.FileRepository;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import reactor.core.publisher.Flux;
import reactor.core.scheduler.Schedulers;

import java.util.concurrent.atomic.AtomicBoolean;

@Component
@Slf4j
public class DupeCleaner {

    @Autowired
    private FileRepository fileRepository;

    private AtomicBoolean isExecuting = new AtomicBoolean(false);

    @Scheduled(fixedDelayString = "${app.dupe-cleaner.fixed-delay}", initialDelayString = "${app.dupe-cleaner.initial-delay}")
    public void cleanDuped() {
        if (isExecuting.compareAndSet(false, true)) {

            log.info("Starting DupeCleaner");
            fileRepository.findDuplicatedHashes().parallel(Runtime.getRuntime().availableProcessors()).runOn(Schedulers.boundedElastic()).doOnNext(this::cleanDuped).doOnComplete(() -> log.info("DupeCleaner completed")).doAfterTerminate(() -> isExecuting.set(false)).subscribe();
            log.info("Ending DupeCleaner");

        } else {
            log.info("Skipping DupeCleaner");
        }
    }

    private void cleanDuped(String hash) {
        log.debug("Hash {} will be processed", hash);

        final Flux<File> files = fileRepository.findByHash(hash).filter(f -> !f.isMissing() && !f.isDelete()).sort((f1, f2) -> f1.getPath().compareTo(f2.getPath()));
        files.subscribe();
        final File original = files.next().block();
        files.skip(1).doOnNext(c -> markToDelete(c, original.getPath())).subscribe();

    }

    private void markToDelete(File file, String path) {
        log.debug("File with hash {} will be marked for deletion with reference {}: {} ", file.getHash(), path, file.getPath());
        file.setDelete(true);
        file.setPathToOriginal(path);
        fileRepository.save(file).subscribe(result -> log.debug("File marked to deletion: {}", result.getId()));
    }

}
