package com.example.demo.infrastructure;

import lombok.Builder;
import lombok.Data;
import lombok.EqualsAndHashCode;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

@Document("files")
@Data
@Builder
@EqualsAndHashCode(of = "path")
public class File {

    @Id
    String id;

    String hash;

    String footprint;

    String size;

    String path;

}
