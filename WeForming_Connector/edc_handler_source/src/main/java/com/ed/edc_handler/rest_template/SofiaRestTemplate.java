package com.ed.edc_handler.rest_template;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.net.URI;
import java.util.Map;

@Service
@Slf4j
public class SofiaRestTemplate {

    private final RestTemplate restTemplate;

    @Value("${sofia.uri}")
    private String sofiaUri;

    public SofiaRestTemplate(RestTemplate restTemplate) {
        this.restTemplate = restTemplate;
    }

    public String post(Map<String, Map<String, Object>> parameters,
                       Map<String, String> headers) {

        HttpHeaders httpHeaders = new HttpHeaders();
        httpHeaders.add("Content-Type", "application/json");
        httpHeaders.add("Authorization", headers.get("authorization"));

        log.debug("headers " + httpHeaders.toString());
        HttpEntity<Map<String, Map<String, Object>>> httpEntity =
                new HttpEntity<Map<String, Map<String, Object>>>(parameters, httpHeaders);

        ResponseEntity<Map> response =
                restTemplate.exchange(
                        URI.create(sofiaUri + "/dataset/v2/provide-data"),
                        HttpMethod.POST,
                        httpEntity,
                        new ParameterizedTypeReference<Map>() {
                        }
                );

        log.debug("responce body " + response.getBody().toString());
        return (String) response.getBody().get("response");
    }

}
