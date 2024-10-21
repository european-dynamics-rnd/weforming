package com.ed.edc_handler.controller;

import com.ed.edc_handler.dto.FileLogDto;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/alive")
public class AliveController {

    @GetMapping
    public String getAllFiles() {
        return "yes";
    }

}
