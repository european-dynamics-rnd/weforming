package com.ed.edc_handler.rest_template;

import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.*;
import org.springframework.stereotype.Service;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.HttpStatusCodeException;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.server.ResponseStatusException;

import java.net.URI;
import java.util.Map;

@Service
public class ConnectorRestTemplate {

    private String endpoint = "http://resonance-server.eurodyn.com:8003";

    private final RestTemplate restTemplate;

    public ConnectorRestTemplate(RestTemplate restTemplate) {
        this.restTemplate = restTemplate;
    }

    public Map<String, Object> update(Map<String, Object> jsonLdParameters,
                                              Map<String, String> headers, String type, String id, String endpoint) {

        HttpHeaders httpHeaders = new HttpHeaders();
        httpHeaders.add("Content-Type", "application/ld+json");
        HttpEntity<Map<String, Object>> httpEntity = new HttpEntity<Map<String, Object>>(jsonLdParameters, httpHeaders);

        ResponseEntity<Object> response =
        restTemplate.exchange(
                URI.create(endpoint + "/ngsi-ld/v1/entities/urn:ngsi-ld:" + type + ":" + id + "/attrs"),
                HttpMethod.POST,
                httpEntity,
                new ParameterizedTypeReference<Object>() {
                }
        );

        return jsonLdParameters;
    }

    public Object post(Object data, String path) {
        HttpHeaders httpHeaders = new HttpHeaders();
        httpHeaders.add(HttpHeaders.CONTENT_TYPE, "application/json");

        HttpEntity<Object> httpEntity = new HttpEntity(data, httpHeaders);
        try {
            ResponseEntity<Object> response =
            restTemplate.exchange(
                    URI.create(path),
                    HttpMethod.POST,
                    httpEntity,
                    new ParameterizedTypeReference<Object>() {
                    }
            );

        return response.getBody();

        } catch (Exception ex) {
            throw new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR, "Error Accessing DataApp");
        }
    }

    public Object get(String path) {
        HttpHeaders httpHeaders = new HttpHeaders();
        httpHeaders.add(HttpHeaders.CONTENT_TYPE, "application/json");

        try {
            HttpEntity httpEntity = new HttpEntity(httpHeaders);
            ResponseEntity<Object> response =
                    restTemplate.exchange(path,
                            HttpMethod.GET,
                            httpEntity,
                            new ParameterizedTypeReference<Object>() {}
                    );
            return response.getBody();
        } catch (HttpClientErrorException.NotFound e) {
            return null;
        } catch (Exception e) {
           throw e;
        }

    }

}
