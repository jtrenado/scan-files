package jtrenado.scanFiles.application.schedulers;

import jtrenado.scanFiles.application.configuration.Properties;
import jtrenado.scanFiles.infrastructure.repositories.FileRepository;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

@Component
@Slf4j
public class FileRemover {

    @Autowired
    private FileRepository fileRepository;

    @Autowired
    private Properties properties;

    @Scheduled(fixedDelayString = "${app.file-remover.fixed-delay}", initialDelayString = "${app.file-remover.initial-delay}")
    public void remove() {

        if (properties.isDeleteImages()) {
            findAndDeleteImages();
        }
        if (properties.isDeleteVideos()) {
            findAndDeleteVideos();
        }
        if (properties.isDeleteOthers()) {
            findAndDeleteOthers();
        }
    }

    private void findAndDeleteVideos() {
        log.info("Deleting videos: unimplemented");

        // TODO:
        //  find videos with missing=false, delete=true
        // Delete the file
        // set missing to true

    }

    private void findAndDeleteImages() {
        log.info("Deleting images: unimplemented");
    }

    private void findAndDeleteOthers() {
        log.info("Deleting others: unimplemented");
    }
}
