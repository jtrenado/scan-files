package jtrenado.scanfiles.adapters.mongoadapter.mappers;


import jtrenado.scanfiles.adapters.mongoadapter.entities.FileEntity;
import jtrenado.scanfiles.domain.entities.File;
import org.mapstruct.Mapper;


@Mapper(componentModel = "spring")
public interface FileEntityMapper {


    File toDomain(FileEntity file);

    FileEntity toEntity(File file);
}
