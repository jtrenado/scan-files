package jtrenado.oldscanfiles;

import jtrenado.oldscanfiles.application.schedulers.DupeCleaner;
import jtrenado.oldscanfiles.application.schedulers.FileFinder;
import jtrenado.oldscanfiles.application.schedulers.FileRemover;
import jtrenado.oldscanfiles.application.schedulers.TaskPublisher;
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
