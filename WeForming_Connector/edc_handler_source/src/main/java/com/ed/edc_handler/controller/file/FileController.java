package com.ed.edc_handler.controller.file;

import com.ed.edc_handler.dto.file.FileLogDto;
import com.ed.edc_handler.model.FileEntity;
import com.ed.edc_handler.service.file.FileService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.Collections;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/files")
public class FileController {

    @Autowired
    private FileService fileService;

    @PostMapping("/upload")
    public ResponseEntity<Map> uploadFile(@RequestParam("file") MultipartFile file) {
        try {
            FileEntity savedFile = fileService.saveFile(file);
            return new ResponseEntity<>(Collections.singletonMap("id",savedFile.getId()), HttpStatus.OK);
        } catch (Exception e) {
            return new ResponseEntity<>(null, HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    @PostMapping("/import")
    public void importFile(@RequestBody FileEntity fileEntity) {
            fileService.importFile(fileEntity);
    }

    @GetMapping
    public ResponseEntity<List<FileLogDto>> getAllFiles() {
        List<FileLogDto> files = fileService.getAllFiles();
        return new ResponseEntity<>(files, HttpStatus.OK);
    }

    @GetMapping("/{id}")
    public ResponseEntity<?> getFileById(@PathVariable String id) {
        FileEntity fileEntity = fileService.getFileById(id);
        if (fileEntity == null) {
            return new ResponseEntity<>(HttpStatus.NOT_FOUND);
        }
        HttpHeaders headers = new HttpHeaders();
        headers.add("Content-Disposition", "attachment; filename=" + fileEntity.getFileName());
        return ResponseEntity.ok()
                .headers(headers)
                .body(fileEntity);
    }

}
