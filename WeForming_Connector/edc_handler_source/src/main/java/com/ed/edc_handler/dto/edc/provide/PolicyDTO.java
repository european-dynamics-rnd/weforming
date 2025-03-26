package com.ed.edc_handler.dto.edc.provide;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;
import java.util.Map;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class PolicyDTO {

    @JsonProperty("@context")
    private Map<String, String> context = Map.of(
            "@vocab", "https://w3id.org/edc/v0.0.1/ns/",
            "odrl", "http://www.w3.org/ns/odrl/2/"
    );
    @JsonProperty("@id")
    private String id = "";
    private PolicyDetails policy = new PolicyDetails(
            "http://www.w3.org/ns/odrl.jsonld",
            "Set",
            List.of(), // empty list for permissions
            List.of(), // empty list for prohibitions
            List.of()  // empty list for obligations
    );

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    public static class PolicyDetails {

        @JsonProperty("@context")
        private String context = "http://www.w3.org/ns/odrl.jsonld";

        @JsonProperty("@type")
        private String type = "Set";
        private List<String> permission = List.of();
        private List<String> prohibition = List.of();
        private List<String> obligation = List.of();
    }

}
