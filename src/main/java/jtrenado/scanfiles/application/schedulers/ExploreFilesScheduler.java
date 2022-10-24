package jtrenado.scanfiles.application.schedulers;

import jtrenado.scanfiles.application.services.ExploreUseCase;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import javax.annotation.PostConstruct;

@Component
@Slf4j
@RequiredArgsConstructor
public class ExploreFilesScheduler {

    private final ExploreUseCase exploreUseCase;

    @PostConstruct
    void post() {
        log.info("ExploreFilesScheduler initialized");
    }

    @Scheduled(fixedDelayString = "${app.file-finder.fixed-delay}", initialDelayString = "${app.file-finder.initial-delay}")
    public void reloadFiles() {
        log.info("Start: explore files");
        exploreUseCase.explore();
        log.info("End: explore files");
    }

}
