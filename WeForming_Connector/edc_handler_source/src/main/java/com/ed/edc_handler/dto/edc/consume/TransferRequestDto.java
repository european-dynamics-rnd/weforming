package com.ed.edc_handler.dto.edc.consume;


import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class TransferRequestDto {

    @JsonProperty("@context")
    private Context context = new Context();
    @JsonProperty("@type")
    private String type = "TransferRequestDto";
    private String connectorId = "provider";
    private String counterPartyAddress = "";
    private String contractId = "";
    private String assetId = "assetId";
    private String protocol = "dataspace-protocol-http";
    private String transferType = "HttpData-PUSH";
    private DataDestination dataDestination = new DataDestination();

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    public static class Context {
        @JsonProperty("@vocab")
        private String vocab = "https://w3id.org/edc/v0.0.1/ns/";
    }

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    public static class DataDestination {
        private String type = "HttpData";
        private String baseUrl = "";
    }
}
