package com.ed.edc_handler.dto;

import lombok.Data;

import java.time.Instant;

@Data
public class FileLogDto {

    private String id;
    private String fileName;
    private long fileSize;
    private Instant creationDate;

    public FileLogDto(String id, String fileName, long fileSize, Instant creationDate) {
        this.id = id;
        this.fileName = fileName;
        this.fileSize = fileSize;
        this.creationDate = creationDate;
    }

}
