package jtrenado.oldscanfiles.infrastructure.entities;

import lombok.*;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

@Document(collection = "#{@properties.getCollectionNameFile()}")
@Data
@Builder
@EqualsAndHashCode(of = "path")
@NoArgsConstructor
@AllArgsConstructor
public class File {

    @Id
    private String id;

    private String hash;

    private String footprint;

    private Integer size;

    private String path;

    private FileType type;

    private boolean missing = false;

    private boolean delete = false;

    private String pathToOriginal;

}
