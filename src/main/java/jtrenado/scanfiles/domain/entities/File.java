package jtrenado.scanfiles.domain.entities;

import jtrenado.oldscanfiles.infrastructure.entities.FileType;
import lombok.*;

import java.time.LocalDateTime;

@Data
@Builder
@EqualsAndHashCode(of = "path")
@NoArgsConstructor
@AllArgsConstructor
public class File {

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
