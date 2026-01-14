package com.ed.edc_handler.dto.edc.consume;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;
import java.util.Map;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class CatalogRequestWithIdDTO {

    @JsonProperty("@context")
    private Map<String, String> context;

    private String counterPartyAddress;
    private String counterPartyId;
    private String protocol;
    private QuerySpec querySpec;

    public CatalogRequestWithIdDTO(String assetId) {
        this.context = Map.of("@vocab", "https://w3id.org/edc/v0.0.1/ns/");
        this.counterPartyAddress = "";
        this.protocol = "dataspace-protocol-http";
        this.querySpec = new QuerySpec(List.of(new QuerySpec.FilterExpression(
                "id", "=", assetId
        )));
    }

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    public static class QuerySpec {
        private List<FilterExpression> filterExpression;

        @Data
        @NoArgsConstructor
        @AllArgsConstructor
        public static class FilterExpression {
            private String operandLeft;
            private String operator;
            private Object operandRight;
        }
    }
}
