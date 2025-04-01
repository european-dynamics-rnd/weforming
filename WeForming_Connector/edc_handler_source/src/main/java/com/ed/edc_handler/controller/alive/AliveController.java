package com.ed.edc_handler.controller.alive;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("/health")
public class AliveController {

    @GetMapping
    public Map getAllFiles() {
        Map response = new HashMap();
        response.put("version", "1.0");
        response.put("message", "ok");

        return response;
    }

}
