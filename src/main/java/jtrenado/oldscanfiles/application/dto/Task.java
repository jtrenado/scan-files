package jtrenado.oldscanfiles.application.dto;

import lombok.*;
import lombok.extern.slf4j.Slf4j;

import java.nio.file.Path;

@Data
@Builder
@ToString(exclude = {"contents"})
@EqualsAndHashCode(of = "path")
@Slf4j
public class Task {

    @NonNull
    private byte[] contents;

    @NonNull
    private Path path;

    private String footprint;

    private String hash;

    private Integer size;

    private String meta;

    public boolean isCompleted() {
        return hash != null && size != null;
    }
}
