package jtrenado.scanFiles;

import jtrenado.scanFiles.application.schedulers.DupeCleaner;
import jtrenado.scanFiles.application.schedulers.FileFinder;
import jtrenado.scanFiles.application.schedulers.FileRemover;
import jtrenado.scanFiles.application.schedulers.TaskPublisher;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableScheduling;

@SpringBootApplication
@EnableScheduling
public class Application {

    // public static void main(String[] args) {
    //   SpringApplication.run(Application.class, args);
    //}

    @Autowired
    private FileFinder fileFinder;

    @Autowired
    private TaskPublisher taskPublisher;

    @Autowired
    private DupeCleaner dupeCleaner;

    @Autowired
    private FileRemover fileRemover;

}
