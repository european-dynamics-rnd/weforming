package com.ed.edc_handler.dto.edc.consume;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class ContractRequestDTO {

    @JsonProperty("@context")
    private Context context = new Context();
    @JsonProperty("@type")
    private String type = "ContractRequest";
    private String counterPartyAddress = "";
    private String counterPartyId = "";
    private String protocol = "dataspace-protocol-http";
    private Policy policy = new Policy();

    // Nested Context Class
    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    public static class Context {
        @JsonProperty("@vocab")
        private String vocab = "https://w3id.org/edc/v0.0.1/ns/";
    }

    // Nested Policy Class
    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    public static class Policy {
        @JsonProperty("@context")
        private String context = "http://www.w3.org/ns/odrl.jsonld";
        @JsonProperty("@type")
        private String type = "Offer";
        @JsonProperty("@id")
        private String id = "";
        private String assigner = "";
        private String assignee = "";
        private String target = "assetId";
        private String[] permission = new String[0];
        private String[] prohibition = new String[0];
        private String[] obligation = new String[0];
    }
}
