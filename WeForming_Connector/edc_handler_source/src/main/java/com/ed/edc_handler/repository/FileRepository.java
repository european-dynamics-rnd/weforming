package com.ed.edc_handler.repository;

import com.ed.edc_handler.dto.file.FileLogDto;
import com.ed.edc_handler.model.FileEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.List;

public interface FileRepository extends JpaRepository<FileEntity, String> {

    @Query("SELECT new com.ed.edc_handler.dto.file.FileLogDto(f.id, f.fileName, f.fileSize, f.creationDate) FROM FileEntity f")
    List<FileLogDto> findAllFileLogs();

}
