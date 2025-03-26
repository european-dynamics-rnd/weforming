package com.ed.edc_handler.dto.edc.provide;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Data
@AllArgsConstructor
public class DataPlaneInstanceDTO {
    @JsonProperty("@context")
    private Map<String, String> context;
    private String edctype;
    @JsonProperty("@id")
    private String id;
    private String url;
    private List<String> allowedSourceTypes;
    private List<String> allowedDestTypes;
    private Map<String, String> properties;

    // Constructor with default values
    public DataPlaneInstanceDTO() {
        this.context = Map.of("@vocab", "https://w3id.org/edc/v0.0.1/ns/");
        this.edctype = "dataspaceconnector:dataplaneinstance";
        this.id = "http-pull-provider-dataplane";
        this.url = "";
        this.allowedSourceTypes = Arrays.asList("HttpData");
        this.allowedDestTypes = Arrays.asList("HttpProxy", "HttpData");
        this.properties = new HashMap<>();
    }
}
