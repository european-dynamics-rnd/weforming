WeForming Connector - Postman Collection Documentation
==================================================================

This documentation describes the API endpoints in the **WeForming Connector ** Postman collection, used to send and receive files between connectors (Provider and Consumer). The collection facilitates operations like uploading files to the provider connector and pulling them from the consumer connector.

Overview
--------

*   **Collection Name**: Wc
*   **Schema**: [v2.1.0](https://schema.getpostman.com/json/collection/v2.1.0/collection.json)
*   **Exported By**: User ID `1647936`
*   **Base URLs**:
    *   `{{provider_url}}`: URL of the provider connector
    *   `{{consumer_url}}`: URL of the consumer connector

Key Endpoints
-------------

### 1\. \[Provider\] Upload File

Uploads a file to the provider connector.

*   **Method**: `POST`
*   **URL**: `{{provider_url}}:15588/api/files/upload`
*   **Request Body**: `multipart/form-data`
    *   **file**: The file to be uploaded from the local system.
*   **Test Script**: This script extracts the `file_id` from the response and stores it as a collection variable for future use:

    javascript

    Αντιγραφή κώδικα

    `var jsonData = JSON.parse(responseBody); pm.collectionVariables.set("file_id", jsonData.id);`


### 2\. \[Provider\] List Files

Retrieves the list of files stored on the provider connector.

*   **Method**: `GET`
*   **URL**: `{{provider_url}}:15588/api/files`

### 3\. \[Provider\] Get File by ID

Fetches the details of a specific file on the provider connector by `file_id`.

*   **Method**: `GET`
*   **URL**: `{{provider_url}}:15588/api/files/{{file_id}}`

### 4\. \[Provider\] Register Data Plane Instance

Registers a data plane instance on the provider connector.

*   **Method**: `POST`
*   **URL**: `{{provider_url}}:8003/management/v2/dataplanes`
*   **Request Body**: JSON

    json

    Αντιγραφή κώδικα

    `{   "@context": {     "@vocab": "https://w3id.org/edc/v0.0.1/ns/"   },   "edctype": "dataspaceconnector:dataplaneinstance",   "@id": "http-pull-provider-dataplane",   "url": "{{provider_url}}:8005/control/transfer",   "allowedSourceTypes": ["HttpData"],   "allowedDestTypes": ["HttpProxy", "HttpData"],   "properties": {     "publicApiUrl": "{{provider_url}}:8002/public/"   } }`


### 5\. \[Consumer\] Fetch Provider Catalog

Fetches the provider’s catalog to negotiate a contract.

*   **Method**: `POST`
*   **URL**: `{{consumer_url}}:9003/management/v2/catalog/request/`
*   **Request Body**: JSON

    json

    Αντιγραφή κώδικα

    `{   "@context": { "@vocab": "https://w3id.org/edc/v0.0.1/ns/" },   "counterPartyAddress": "{{provider_url}}:8004/protocol",   "protocol": "dataspace-protocol-http",   "querySpec": {     "filterExpression": [{       "operandLeft": "id",       "operator": "=",       "operandRight": "asset-id"     }]   } }`


### 6\. \[Consumer\] Negotiate Contract

Initiates the negotiation of a contract to retrieve assets from the provider connector.

*   **Method**: `POST`
*   **URL**: `{{consumer_url}}:9003/management/v2/contractnegotiations`
*   **Request Body**: JSON

    json

    Αντιγραφή κώδικα

    `{   "@context": { "@vocab": "https://w3id.org/edc/v0.0.1/ns/" },   "connectorAddress": "{{provider_url}}:8004/protocol",   "protocol": "dataspace-protocol-http",   "offerId": "{{offer_id}}:artifact",   "assetId": "{{asset_id}}",   "policy": {     "permissions": [],     "prohibitions": [],     "obligations": []   } }`


### 7\. \[Consumer\] Check Contract Negotiation Status

Retrieves the status of the contract negotiation.

*   **Method**: `GET`
*   **URL**: `{{consumer_url}}:9003/management/v2/contractnegotiations/{{contract-negotiation-id}}`

### 8\. \[Consumer\] Initiate File Transfer (Pull from Provider)

Once the contract is agreed upon, this endpoint is used to start the transfer from the provider to the consumer.

*   **Method**: `POST`
*   **URL**: `{{consumer_url}}:9003/management/v2/transferprocesses`
*   **Request Body**: JSON

    json

    Αντιγραφή κώδικα

    `{   "assetId": "{{asset_id}}",   "connectorAddress": "{{provider_url}}:8004/protocol",   "contractId": "{{contract_id}}",   "dataDestination": {     "type": "HttpProxy"   },   "managedResources": false,   "protocol": "dataspace-protocol-http" }`


### 9\. \[Consumer\] Check Transfer Status

Checks the status of the data transfer from the provider to the consumer.

*   **Method**: `GET`
*   **URL**: `{{consumer_url}}:9003/management/v2/transferprocesses/{{transfer-process-id}}`

### 10\. \[Consumer\] Retrieve the File

After the transfer is completed, the consumer can pull the file from the provider.

*   **Method**: `GET`
*   **URL**: `{{consumer_url}}:9003/management/v2/transferresult/{{transfer-process-id}}`
*   **Description**: Pulls the file from the provider's storage based on the completed transfer process ID.

### 11\. \[Provider\] Create Contract Definition

Defines a contract to govern access to specific assets.

*   **Method**: `POST`
*   **URL**: `{{provider_url}}:8003/management/v2/contractdefinitions`
*   **Request Body**: JSON

    json

    Αντιγραφή κώδικα

    `{   "@context": { "@vocab": "https://w3id.org/edc/v0.0.1/ns/" },   "@id": "1",   "accessPolicyId": "access-policy",   "contractPolicyId": "contract-policy",   "assetsSelector": [] }`


Variables
---------

*   **`provider_url`**: URL of the provider connector.
*   **`consumer_url`**: URL of the consumer connector.
*   **`file_id`**: ID of the file uploaded to the provider connector.
*   **`contract-negotiation-id`**: ID of the contract negotiation.
*   **`contract-id`**: ID of the finalized contract.
*   **`transfer-process-id`**: ID of the transfer process after initiation.
*   **`offer_id`**: ID of the data offer in the provider's catalog.
*   **`asset_id`**: ID of the asset to be transferred.

Complete Workflow
-----------------

1.  **Upload a File to Provider**: Start by uploading a file using the `upload` endpoint of the provider. The response will contain a `file_id`.

2.  **Fetch Provider's Catalog**: Use the `Fetch Provider Catalog` endpoint on the consumer connector to retrieve the assets the provider is offering.

3.  **Negotiate Contract**: Initiate the negotiation process for the asset using the `Negotiate Contract` endpoint. This generates a contract.

4.  **Check Contract Negotiation Status**: Use the `Check Contract Negotiation Status` endpoint to confirm that the contract is finalized.

5.  **Initiate File Transfer (Pull)**: Start the file transfer process from the provider to the consumer using the `Initiate File Transfer` endpoint.

6.  **Check Transfer Status**: Monitor the status of the file transfer using the `Check Transfer Status` endpoint.

7.  **Retrieve the File**: Once the transfer is complete, use the `Retrieve the File` endpoint to pull the file from the provider and complete the process.


* * *

This documentation provides a comprehensive guide for transferring files between connectors using WeForming Connector, from uploading files to the provider to pulling them from the consumer.
