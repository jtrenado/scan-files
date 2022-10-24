package jtrenado.oldscanfiles.infrastructure.entities;

import lombok.Builder;
import lombok.Data;
import lombok.EqualsAndHashCode;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

import java.util.List;

@Document(collection = "#{@properties.getCollectionNameHash()}")
@Data
@Builder
@EqualsAndHashCode(of = "hash")
public class Hash {

    @Id
    private String id;

    String hash;

    List<String> paths;
}
