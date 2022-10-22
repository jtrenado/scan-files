package jtrenado.newScanFiles.adapters.mongoadapter.entities;

import jtrenado.scanFiles.infrastructure.entities.FileType;
import lombok.Data;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

import java.time.LocalDateTime;

@Document(collection = "#{@properties.getCollectionNameFile()}")
@Data
public class FileEntity {

    @Id
    private String id;
    private String hash;
    private String footprint;
    private Long size;
    private String path;
    private FileType type;
    private Boolean missing = false;
    private Boolean toDelete = false;
    private Boolean archived = false;
    private String pathToOriginal;
    private LocalDateTime dateTime;
}
