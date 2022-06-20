package jtrenado.scanFiles.application.services;

import jtrenado.scanFiles.infrastructure.entities.File;
import jtrenado.scanFiles.infrastructure.repositories.FileRepository;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

@Service
@Slf4j
public class FileServiceImpl implements FileService {

    @Autowired
    private FileRepository fileRepository;

    @Autowired
    private FileFinder fileFinder;

    Map<String, File> loadedFiles = new ConcurrentHashMap<>();
    Map<String, File> loadedAndDisposedFiles = new ConcurrentHashMap<>();

    private long secondsToWait = 1800L;

    private int numRecoveredDocsFromMong = 500;

    @Override
    public synchronized List<File> getNextFiles(int n) {

        if (loadedFiles.isEmpty()) {
            loadedAndDisposedFiles.clear();
            loadFiles();
        }

        List<File> nextFiles = new ArrayList<>();

        Iterator<String> itr = loadedFiles.keySet().iterator();
        for (int i = 0; i < n; i++) {
            if (itr.hasNext()) {
                File f = loadedFiles.get(itr.next());
                loadedAndDisposedFiles.put(f.getId(), f);
                nextFiles.add(f);
            } else {
                break;
            }
        }

        nextFiles.stream().map(File::getId).forEach(loadedFiles::remove);

        if (nextFiles.isEmpty()) {
            log.warn("No more files to process");
        }

        return nextFiles;
    }

    private void load(File file) {
        if (!exists(file)) {
            markAsMissing(file);
        } else {
            markAsExisting(file);
        }
        loadedFiles.put(file.getId(), file);

    }

    @Override
    public void markAsMissing(File file) {
        markAsExisting(file, true);
    }

    @Override
    public void markAsExisting(File file) {
        markAsExisting(file, false);
    }

    private void markAsExisting(File file, boolean existing) {

        Optional<File> f = fileRepository.findById(file.getId());
        if (f.isPresent()) {
            f.get().setMissing(existing);
            fileRepository.save(f.get());
        }
    }

    @Override
    public boolean exists(File file) {
        Path p = Paths.get(file.getPath());
        return Files.isRegularFile(p);
    }

    @Override
    public byte[] getContents(File file) throws IOException {
        Path p = Paths.get(file.getPath());
        return Files.readAllBytes(p);
    }

    @Override
    public void save(Path path, String hash, String footprint, int size) {
        Optional<File> file = fileRepository.findByPath(path.toString());
        if (file.isPresent()) {
            File f = file.get();
            f.setHash(hash);
            f.setFootprint(footprint);
            f.setSize(size);
            fileRepository.save(f);
        } else {
            log.error("File not found in mongo {}", path.toString());
        }

    }

    private void loadFiles() {
        Pageable pageableRequest = PageRequest.of(0, numRecoveredDocsFromMong);
        List<File> allIncompleteFiles = fileRepository.findAllByHashIsNullOrSizeIsNull(pageableRequest);
        if (allIncompleteFiles.isEmpty()) {
            fileFinder.reloadFiles();
            allIncompleteFiles = fileRepository.findAllByHashIsNullOrSizeIsNull(pageableRequest);
        }

        if (allIncompleteFiles.isEmpty()) {
            log.info("No new files recovered, sleeping {} seconds", secondsToWait);
            try {
                Thread.sleep(secondsToWait * 1000L);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        } else {
            log.info("{} documents recovered from mongo", allIncompleteFiles.size());
            allIncompleteFiles.parallelStream().forEach(this::load);
        }
    }
}