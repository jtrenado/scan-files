package jtrenado.scanfiles.application;

import lombok.Getter;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;

import java.util.List;

@Configuration
@Getter
public class Properties {

    @Value("#{'${app.paths}'.split(',')}")
    private List<String> paths;

    @Value("#{'${app.extensions.image}'.split(',')}")
    private List<String> imageExtensions;

    @Value("#{'${app.extensions.video}'.split(',')}")
    private List<String> videoExtensions;

    @Value("#{new Boolean('${app.videos.ignore}')}")
    private boolean ignoreVideos;

    @Value("#{new Boolean('${app.images.ignore}')}")
    private boolean ignoreImages;

    @Value("#{new Boolean('${app.others.ignore}')}")
    private boolean ignoreOthers;

    @Value("#{new Boolean('${app.videos.delete}')}")
    private boolean deleteVideos;

    @Value("#{new Boolean('${app.images.delete}')}")
    private boolean deleteImages;

    @Value("#{new Boolean('${app.others.delete}')}")
    private boolean deleteOthers;

    @Value("${app.mongodb.collection.file}")
    private String collectionNameFile;

    @Value("${app.mongodb.collection.hash}")
    private String collectionNameHash;

    @Value("${app.task-publisher.initial-max:200}")
    private int initialMaxTasks;

    @Value("${app.task-publisher.increment:20}")
    private int initialTasksIncrement;

}
