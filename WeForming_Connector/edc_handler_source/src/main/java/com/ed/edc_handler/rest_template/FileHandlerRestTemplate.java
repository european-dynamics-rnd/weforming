package com.ed.edc_handler.rest_template;

import com.ed.edc_handler.model.FileEntity;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.RestTemplate;

@Service
public class FileHandlerRestTemplate {

    private final RestTemplate restTemplate;

    public FileHandlerRestTemplate(RestTemplate restTemplate) {
        this.restTemplate = restTemplate;
    }

    public FileEntity get(String path) {
        HttpHeaders httpHeaders = new HttpHeaders();
        httpHeaders.add(HttpHeaders.CONTENT_TYPE, "application/json");

        try {
            HttpEntity httpEntity = new HttpEntity(httpHeaders);
            ResponseEntity<FileEntity> response =
                    restTemplate.exchange(path,
                            HttpMethod.GET,
                            httpEntity,
                            new ParameterizedTypeReference<FileEntity>() {}
                    );
            return response.getBody();
        } catch (HttpClientErrorException.NotFound e) {
            return null;
        } catch (Exception e) {
            throw e;
        }

    }


}
