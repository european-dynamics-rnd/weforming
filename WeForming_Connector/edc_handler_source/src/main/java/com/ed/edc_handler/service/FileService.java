package com.ed.edc_handler.service;

import com.ed.edc_handler.dto.FileLogDto;
import com.ed.edc_handler.model.FileEntity;
import com.ed.edc_handler.repository.FileRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.time.Instant;
import java.util.List;
import java.util.UUID;

@Service
public class FileService {

    @Autowired
    private FileRepository fileRepository;



    public FileEntity importFile(FileEntity fileEntity)  {
        fileEntity.setCreationDate(Instant.now());
        return fileRepository.save(fileEntity);
    }

    public FileEntity saveFile(MultipartFile file) throws IOException {
        FileEntity fileEntity = new FileEntity();
        fileEntity.setFileContent(file.getBytes());
        fileEntity.setFileName(file.getOriginalFilename());
        fileEntity.setFileSize(file.getSize());
        fileEntity.setCreationDate(Instant.now());

        return fileRepository.save(fileEntity);
    }

    public List<FileLogDto> getAllFiles() {
        return fileRepository.findAllFileLogs();
    }

    public FileEntity getFileById(String id) {
        return fileRepository.findById(id).orElse(null);
    }

}
