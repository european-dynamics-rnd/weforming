package com.ed.edc_handler.dto.edc.consume;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.PrimitiveIterator;

@Data
@AllArgsConstructor
public class CatalogRequestDTO {

    @JsonProperty("@context")
    private Map<String, String> context;

    private String counterPartyAddress;
    private String protocol;
    private Map<String,Integer> querySpec;

    public CatalogRequestDTO() {
        this.context = Map.of("@vocab", "https://w3id.org/edc/v0.0.1/ns/");
        this.counterPartyAddress = "";
        this.protocol = "dataspace-protocol-http";
        this.querySpec = new HashMap<>();
    }
}
