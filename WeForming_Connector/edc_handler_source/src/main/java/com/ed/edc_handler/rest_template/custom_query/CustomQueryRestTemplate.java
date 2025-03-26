package com.ed.edc_handler.rest_template.custom_query;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;

import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

@Service
public class CustomQueryRestTemplate {

    @Value("${sofia.uri}")
    private String sofiaUri;

    private final RestTemplate restTemplate;

    public CustomQueryRestTemplate(RestTemplate restTemplate) {
        this.restTemplate = restTemplate;
    }

    public List<Map<String,Object>> getDataObjects(String id, Map<String, String> parameters, Map<String, String> headers) {

        List<String> parametersList = new ArrayList<>();
        parameters.forEach((key, value) -> parametersList.add( key + "=" + value) );

        HttpHeaders httpHeaders = new HttpHeaders();
        httpHeaders.add("Content-Type", "application/json");
        httpHeaders.add("Authorization", headers.get("authorization"));
        HttpEntity httpEntity = new HttpEntity(httpHeaders);

        String parametersString = String.join("&", parametersList );
        ResponseEntity<List<Map<String, Object>>> response =
                restTemplate.exchange(this.sofiaUri + "/custom-query/data-objects?id=" + id + "&" + parametersString ,
                HttpMethod.GET,
                httpEntity,
                new ParameterizedTypeReference<List<Map<String, Object>>>() {}
        );

        return response.getBody();
    }

    public Object activateDataOffering(Map<String, String> parameters, Map<String, String> headers) {

        HttpHeaders httpHeaders = new HttpHeaders();
        httpHeaders.add("Content-Type", "application/json");
        httpHeaders.add("Authorization", headers.get("authorization"));
        HttpEntity<Map<String, String>> httpEntity =
                new HttpEntity<Map<String, String>>(parameters, httpHeaders);

        ResponseEntity<Object> response =
                restTemplate.exchange(this.sofiaUri + "/custom-query/data-objects?id=b3336c63-b949-4c8e-a3f6-ab42e37c08f5" ,
                        HttpMethod.POST,
                        httpEntity,
                        new ParameterizedTypeReference<Object>() {}
                );

        return response.getBody();
    }

    public Object post(Map<String, String> parameters,
                       Map<String, String> headers,
                       String path) {

        HttpHeaders httpHeaders = new HttpHeaders();
        httpHeaders.add("Content-Type", "application/json");
        httpHeaders.add("Authorization", headers.get("authorization"));
        HttpEntity<Map<String, String>> httpEntity =
                new HttpEntity<Map<String, String>>(parameters, httpHeaders);

        ResponseEntity<Object> response =
                restTemplate.exchange(this.sofiaUri + "/custom-query/data-objects/" + path ,
                        HttpMethod.POST,
                        httpEntity,
                        new ParameterizedTypeReference<Object>() {}
                );

        return response.getBody();
    }

    public List<Map<String,Object>> get(
                                        Map<String, String> parameters,
                                        Map<String, String> headers,
                                        String path) {

        List<String> parametersList = new ArrayList<>();
        parameters.forEach((key, value) -> parametersList.add( key + "=" + value) );

        HttpHeaders httpHeaders = new HttpHeaders();
        httpHeaders.add("Content-Type", "application/json");
        httpHeaders.add("Authorization", headers.get("authorization"));
        HttpEntity httpEntity = new HttpEntity(httpHeaders);

        String parametersString = parametersList.isEmpty() ? "" : "?" + String.join("&", parametersList );

        ResponseEntity<List<Map<String, Object>>> response =
                restTemplate.exchange(this.sofiaUri + "/custom-query/data-objects/" + path + parametersString ,
                        HttpMethod.GET,
                        httpEntity,
                        new ParameterizedTypeReference<List<Map<String, Object>>>() {}
                );

        return response.getBody();
    }

}
