package com.ed.edc_handler.service.asset;

import com.ed.edc_handler.dto.edc.consume.CatalogRequestDTO;
import com.ed.edc_handler.dto.edc.consume.CatalogRequestWithIdDTO;
import com.ed.edc_handler.dto.edc.consume.ContractRequestDTO;
import com.ed.edc_handler.dto.edc.consume.TransferRequestDto;
import com.ed.edc_handler.dto.edc.provide.AssetDTO;
import com.ed.edc_handler.dto.edc.provide.ContractDTO;
import com.ed.edc_handler.dto.edc.provide.DataPlaneInstanceDTO;
import com.ed.edc_handler.dto.edc.provide.PolicyDTO;
import com.ed.edc_handler.model.FileEntity;
import com.ed.edc_handler.rest_template.ConnectorRestTemplate;
import com.ed.edc_handler.rest_template.FileHandlerRestTemplate;
import com.ed.edc_handler.rest_template.SofiaRestTemplate;
import com.ed.edc_handler.rest_template.custom_query.CustomQueryRestTemplate;
import com.ed.edc_handler.service.file.FileService;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.util.*;

@Slf4j
@Service
public class AssetService {

    @Value("${connector.url}")
    private String connectorUrl;

    private final ConnectorRestTemplate connectorRestTemplate;
    private final SofiaRestTemplate sofiaRestTemplate;
    private final CustomQueryRestTemplate customQueryRestTemplate;
    private FileService fileService;

    private final FileHandlerRestTemplate fileHandlerRestTemplate;

    public AssetService(ConnectorRestTemplate connectorRestTemplate,
                        SofiaRestTemplate sofiaRestTemplate,
                        CustomQueryRestTemplate customQueryRestTemplate,
                        FileService fileService,
                        FileHandlerRestTemplate fileHandlerRestTemplate) {
        this.connectorRestTemplate = connectorRestTemplate;
        this.sofiaRestTemplate = sofiaRestTemplate;
        this.customQueryRestTemplate = customQueryRestTemplate;
        this.fileService = fileService;
        this.fileHandlerRestTemplate = fileHandlerRestTemplate;
    }

    public Map provideData(Map<String, Map<String, Object>> parameters,
                                    Map<String, String> headers) {

        log.debug("*** Retrieve  Connector Settings");
        List<Map<String,Object>> data = this.customQueryRestTemplate.get(
                Collections.emptyMap(),
                headers,
                "get-user-settings");

        String handlerUrl = (String) data.get(0).get("handler_url");
        String handlerPublicUrl = (String) data.get(0).get("handler_public_url");
        String connectorPublicUrl = (String) data.get(0).get("connector_public_url");  //8002
        String connectorManagementUrl = (String) data.get(0).get("connector_management_url"); // 8003
        String connectorProtocolUrl = (String) data.get(0).get("connector_protocol_url"); // 8004
        String connectorControlUrl = (String) data.get(0).get("connector_control_url"); //8005
        String provider_id = (String) data.get(0).get("id");
        String provider_username = (String) data.get(0).get("username");
        String provider_email = (String) data.get(0).get("email");

        log.debug("*** Save Data Offering to Central Registry");
        parameters.get("provide_data_obj").put("status","pending");
        String fileData = parameters.get("provide_data_obj").get("file").toString();
        parameters.get("provide_data_obj").put("file", "");
        String id = this.sofiaRestTemplate.post(parameters, headers);
        parameters.get("provide_data_obj").put("id", id);
        parameters.get("provide_data_obj").put("file", fileData);

        log.debug("New id from central registry= "+ id);

        String title = parameters.get("provide_data_obj").get("title").toString();
        String service_offering_id = parameters.get("provide_data_obj").get("service_offering_id").toString();

        Map<String, Object> offeringAppViewObj = (Map<String, Object>) parameters.get("provide_data_obj").get("offering_app_view_obj");
        String offering_title = "";
        String service_id = "";
        String service_title = "";
        if(offeringAppViewObj != null){
             offering_title = offeringAppViewObj.get("offering_title").toString();
             service_id = offeringAppViewObj.get("service_id").toString();
             service_title = offeringAppViewObj.get("service_title").toString();
        }

        log.debug("*** Save File to EDC Handler");
//        String filedata = (String) parameters.get("provide_data_obj").get("file");
        long fileSizeInByte = (long) (Math.ceil((double) fileData.length() / 4) * 3);
        FileEntity fileEntity = FileEntity.builder()
                .id(id)
                .fileName(id)
                .fileContent(fileData.getBytes())
                .fileSize(fileSizeInByte)
                .build();

      //  fileService.importFile(fileEntity);
        this.connectorRestTemplate.post(fileEntity, handlerUrl + "/files/import");

        log.debug("*** 1. Register Dataplane to Connector");
        DataPlaneInstanceDTO dataPlane = new DataPlaneInstanceDTO();
        dataPlane.setUrl(connectorControlUrl + "/transfer");
        dataPlane.getProperties().put("publicApiUrl", connectorPublicUrl + "/");
        this.connectorRestTemplate.post(dataPlane, connectorManagementUrl + "/v2/dataplanes");

        log.debug("*** 2. Create Asset");
        AssetDTO assetDTO = new AssetDTO();
        assetDTO.setId(id);
        // assetDTO.getProperties().put("id" , id);
        assetDTO.getProperties().put("title" , title);

        assetDTO.getProperties().put("service_offering_id" , service_offering_id);
        assetDTO.getProperties().put("offering_title" , offering_title);
        assetDTO.getProperties().put("service_id" , service_id);
        assetDTO.getProperties().put("service_title" , service_title);

        assetDTO.getProperties().put("provider_id" , provider_id);
        assetDTO.getProperties().put("provider_username" , provider_username);
        assetDTO.getProperties().put("provider_email" , provider_email);
        assetDTO.getProperties().put("created_on" , Instant.now().getEpochSecond());

        assetDTO.getDataAddress().setName(id);
        assetDTO.getDataAddress().setBaseUrl(handlerPublicUrl +"/files/" + id);
        this.connectorRestTemplate.post(assetDTO, connectorManagementUrl + "/v3/assets");

        log.debug("*** 3. Create Policy 'general-policy' if it does not Exist");
        Object response  = this.connectorRestTemplate.get(connectorManagementUrl + "/v2/policydefinitions/general-policy");
        if(response == null){
            PolicyDTO policyDTO = new PolicyDTO();
            policyDTO.setId("general-policy");
            this.connectorRestTemplate.post(policyDTO, connectorManagementUrl + "/v2/policydefinitions");
        }

        log.debug("***  4. Create Contract 'general-contract' if it does not Exist");
        response  = this.connectorRestTemplate.get(connectorManagementUrl + "/v2/contractdefinitions/general-contract");
        if(response == null) {
            ContractDTO contractDTO = new ContractDTO();
            contractDTO.setId("general-contract");
            contractDTO.setAccessPolicyId("general-policy");
            contractDTO.setContractPolicyId("general-policy");
            this.connectorRestTemplate.post(contractDTO, connectorManagementUrl + "/v2/contractdefinitions");
        }

        log.debug("*** Activate Data Offering to Central Registry By id="+ id);
        Map<String, String> activationParameters = new HashMap<>();
        activationParameters.put("data_send_id", id);
        this.customQueryRestTemplate.post(activationParameters, headers, "activate-data-offering");

        return Collections.singletonMap("id",id);
    }

    public Map consumeData(Map<String, String> parameters,
                                    Map<String, String> headers) {

        log.debug("*** Retrieve  Connector Settings");
        List<Map<String,Object>> data = this.customQueryRestTemplate.get(
                Collections.singletonMap("dataset_id", parameters.get("id")),
                headers,
                "exchange-connector-addresses");

        String consumerHandlerUrl = (String) data.get(0).get("consumer_handler_url");
        String consumerHandlerPublicUrl = (String) data.get(0).get("consumer_handler_public_url");
        String consumerConnectorPublicUrl = (String) data.get(0).get("consumer_connector_public_url");  //8002
        String consumerConnectorManagementUrl = (String) data.get(0).get("consumer_connector_management_url"); // 8003
        String consumerConnectorProtocolUrl = (String) data.get(0).get("consumer_connector_protocol_url"); // 8004
        String consumerConnectorControlUrl = (String) data.get(0).get("consumer_connector_control_url"); //8005

        String providerHandlerUrl = (String) data.get(0).get("provider_handler_url");
        String providerHandlerPublicUrl = (String) data.get(0).get("provider_handler_public_url");
        String providerConnectorPublicUrl = (String) data.get(0).get("provider_connector_public_url");  //8002
        String providerConnectorManagementUrl = (String) data.get(0).get("provider_connector_management_url"); // 8003
        String providerConnectorProtocolUrl = (String) data.get(0).get("provider_connector_protocol_url"); // 8004
        String providerConnectorControlUrl = (String) data.get(0).get("provider_connector_control_url"); //8005

        if(consumerConnectorPublicUrl.equals(providerConnectorPublicUrl)){
            log.debug("*** 4.9 Connectors are the same, so down nagotiate anything, just download file.");
            FileEntity fileEntity = fetchFileEntityWithRetries(consumerHandlerUrl + "/files/" + parameters.get("id"));

            String fileContent = new String( fileEntity.getFileContent() );
            return new HashMap<String, Object>() {{
                put("fileContent", fileContent);
                put("id", fileEntity.getId());
                put("retrieved", true);
            }};
        }

        log.debug("*** 5. Fetch Provider's Catalog");
        CatalogRequestWithIdDTO dataCatalogRequestWithIdDto = new CatalogRequestWithIdDTO(parameters.get("id"));
        dataCatalogRequestWithIdDto.setCounterPartyAddress(providerConnectorProtocolUrl);
        Map response = (Map) this.connectorRestTemplate.post(dataCatalogRequestWithIdDto, consumerConnectorManagementUrl + "/v2/catalog/request/");
        Map dataset = (Map) response.get("dcat:dataset");
        Map policy;
        Object policiesObj = dataset.get("odrl:hasPolicy");
        if (policiesObj instanceof List policies) {
            policy = (Map) policies.get(0);
        } else  {
            policy = (Map) policiesObj;
        }
        String contractOfferId = (String) policy.get("@id");

        log.debug("*** 6. Negotiate a contract");
        ContractRequestDTO contractRequestDTO = new ContractRequestDTO();
        contractRequestDTO.setCounterPartyAddress(providerConnectorProtocolUrl );
        contractRequestDTO.getPolicy().setId(contractOfferId);
        response = (Map) this.connectorRestTemplate.post(contractRequestDTO, consumerConnectorManagementUrl + "/v2/contractnegotiations");
        String contractNegotiationId = (String) response.get("@id");

        log.debug("*** 7. Check negotiation status");
        String contractAgreementId = getFinalizedContractAgreementId(consumerConnectorManagementUrl, contractNegotiationId);

        log.debug("*** 8. Push");
        TransferRequestDto transferRequestDto = new TransferRequestDto();
        transferRequestDto.setCounterPartyAddress(providerConnectorProtocolUrl);
        transferRequestDto.setContractId(contractAgreementId);
        transferRequestDto.getDataDestination().setBaseUrl(consumerHandlerPublicUrl+"/files/import");
        response = (Map) this.connectorRestTemplate.post(transferRequestDto, consumerConnectorManagementUrl + "/v2/transferprocesses");

        log.debug("*** Get Data for the frontend");
        FileEntity fileEntity = fetchFileEntityWithRetries(consumerHandlerUrl + "/files/" + parameters.get("id"));

        String fileContent = new String( fileEntity.getFileContent() );
        return new HashMap<String, Object>() {{
            put("fileContent", fileContent);
            put("id", fileEntity.getId());
            put("retrieved", true);
        }};
    }

    public String getFinalizedContractAgreementId(String consumerConnectorManagementUrl, String contractNegotiationId) {
        String state;
        String contractAgreementId = null;
        int retryCount = 0;
        int maxRetries = 100;  // Maximum number of retries to avoid infinite loop
        int waitTime = 200;  // Wait time between retries in milliseconds

        // Loop until the state is FINALIZED or the maximum number of retries is reached
        do {
            // Fetch the contract negotiation state
            Map<String, Object> response = (Map<String, Object>) this.connectorRestTemplate.get(
                    consumerConnectorManagementUrl + "/v2/contractnegotiations/" + contractNegotiationId
            );

            // Get the state of the contract negotiation
            state = (String) response.get("state");

            // Check if contractAgreementId is available
            if ("FINALIZED".equals(state)) {
                contractAgreementId = (String) response.get("contractAgreementId");
            } else {
                try {
                    // Wait for a while before retrying
                    Thread.sleep(waitTime);
                } catch (InterruptedException e) {
                    Thread.currentThread().interrupt();  // Handle the interrupt properly
                    throw new IllegalStateException("Thread was interrupted", e);
                }
            }

            retryCount++;
        } while (!"FINALIZED".equals(state) && retryCount < maxRetries);

        if (contractAgreementId == null) {
            throw new IllegalStateException("Failed to finalize the contract after " + retryCount + " attempts.");
        }

        return contractAgreementId;
    }

    public FileEntity fetchFileEntityWithRetries(String path) {
        FileEntity fileEntity = null;
        int retryCount = 0;
        int maxRetries = 1000;
        long retryDelay = 200;
        // Retry logic using a do-while loop
        do {
            try {
                // Attempt to fetch the fileEntity
                fileEntity = this.fileHandlerRestTemplate.get(path);

                if (fileEntity == null) {
                    retryCount++;
                    System.out.println("FileEntity is null, retrying... (" + retryCount + "/" + maxRetries + ")");
                    Thread.sleep(retryDelay); // Delay before the next attempt
                }
            } catch (Exception e) {
                // Handle any exception that occurs during the request
                System.err.println("Error fetching FileEntity: " + e.getMessage());
                retryCount++;
            }
        } while (fileEntity == null && retryCount < maxRetries);

        // Check if we exhausted retries
        if (fileEntity == null) {
            System.err.println("Failed to fetch FileEntity after " + maxRetries + " retries.");
        } else {
            System.out.println("Successfully fetched FileEntity.");
        }

        return fileEntity;
    }

    public Map getMyAssets(int page) {

//        CatalogRequestWithIdDTO dataCatalogRequestWithIdDto = new CatalogRequestWithIdDTO("");
//        dataCatalogRequestWithIdDto.setCounterPartyAddress(this.connectorUrl + ":8004/protocol");
//
//        dataCatalogRequestWithIdDto.getQuerySpec().setFilterExpression(new ArrayList<>());
//
//        CatalogRequestWithIdDTO.QuerySpec.FilterExpression fromOperator = new CatalogRequestWithIdDTO.QuerySpec.FilterExpression();
//        fromOperator.setOperandLeft("created_on");
//        fromOperator.setOperator(">");
//        fromOperator.setOperandRight(from);
//        dataCatalogRequestWithIdDto.getQuerySpec().getFilterExpression().add(fromOperator);
//
//        CatalogRequestWithIdDTO.QuerySpec.FilterExpression toOperator = new CatalogRequestWithIdDTO.QuerySpec.FilterExpression();
//        toOperator.setOperandLeft("created_on");
//        toOperator.setOperator("<");
//        toOperator.setOperandRight(to);
//        dataCatalogRequestWithIdDto.getQuerySpec().getFilterExpression().add(toOperator);

        CatalogRequestDTO catalogRequestDTO = new CatalogRequestDTO();
        catalogRequestDTO.setCounterPartyAddress(this.connectorUrl + ":8004/protocol");
        catalogRequestDTO.getQuerySpec().put("offset", page);
        catalogRequestDTO.getQuerySpec().put("limit", 50);

        Map response = (Map) this.connectorRestTemplate.post(catalogRequestDTO, this.connectorUrl + ":8003/management/v2/catalog/request/");
        return response;
    }
}
