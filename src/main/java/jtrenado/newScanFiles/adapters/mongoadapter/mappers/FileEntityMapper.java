package jtrenado.newScanFiles.adapters.mongoadapter.mappers;


import jtrenado.newScanFiles.adapters.mongoadapter.entities.FileEntity;
import jtrenado.newScanFiles.domain.entities.File;
import org.mapstruct.Mapper;


@Mapper(componentModel = "spring")
public interface FileEntityMapper {


    File toDomain(FileEntity file);

    FileEntity toEntity(File file);
}
