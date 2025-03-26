package com.ed.edc_handler.dto.edc.provide;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.HashMap;
import java.util.Map;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class AssetDTO {

    @JsonProperty("@context")
    private Map<String, String> context = Map.of("@vocab", "https://w3id.org/edc/v0.0.1/ns/");

    @JsonProperty("@id")
    private String id = "";

    @JsonProperty("properties")
    private Map<String, Object> properties = new HashMap<>(Map.of(
            "contenttype", "application/json"
    ));


    private DataAddressDTO dataAddress = new DataAddressDTO(
            "HttpData",
            "ed-asset",
            "",
            "true"
    );

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    public static class DataAddressDTO {
        private String type = "HttpData";
        private String name = "ed-asset";
        private String baseUrl = "{{provider_url}}:15588/api/files/{{file_id}}";
        private String proxyPath = "true";
    }

}
