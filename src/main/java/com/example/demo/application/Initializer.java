package com.example.demo.application;

import com.example.demo.infrastructure.File;
import com.example.demo.infrastructure.FileRepository;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import javax.annotation.PostConstruct;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Component
@Slf4j
public class Initializer {


    private List<String> paths = Arrays.asList("/home/jtrenado/Imágenes");

    @Autowired
    private FileRepository fileRepository;


    @PostConstruct
    void init() {

        List<Path> p = paths.stream()
                .map(Paths::get).collect(Collectors.toList());

        boolean allDirectories = p.stream().map(Files::isDirectory).allMatch(Boolean.TRUE::equals);

        if (allDirectories) {
            p.stream().forEach(this::init);
        } else {
            log.error("Not all paths are directories");
        }

    }

    private void init(Path path) {

        try {
            Files.walk(path)
                    .filter(Files::isRegularFile)
                    .forEach(this::persist);
        } catch (IOException e) {
            log.error(path.toString(), e);
        }
    }

    private void persist(Path path) {

        Path absolutePath = path.toAbsolutePath();
        Optional<File> file = fileRepository.findByPath(absolutePath.toString());
        if (file.isEmpty()) {
            File f = File.builder().path(absolutePath.toString()).build();
            fileRepository.save(f);
        }
    }


}
