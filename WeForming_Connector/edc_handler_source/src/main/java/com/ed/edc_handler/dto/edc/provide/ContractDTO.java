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
public class ContractDTO {

    @JsonProperty("@context")
    private Map<String, String> context = Map.of("@vocab", "https://w3id.org/edc/v0.0.1/ns/");

    @JsonProperty("@id")
    private String id = "";
    private String accessPolicyId = "";
    private String contractPolicyId = "";
    private List<String> assetsSelector = List.of();

}
