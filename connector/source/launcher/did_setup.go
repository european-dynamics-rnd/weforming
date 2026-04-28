package main

import (
	"encoding/base64"
	"encoding/json"
	"net/url"
	"os"

	"github.com/AgustinSRG/glog"
)

func setupParticipantDid(config *LauncherConfig, logger *glog.Logger) {
	// Check identity configuration

	if len(config.identity.participantId) == 0 {
		logger.Error("Missing required configuration value: PARTICIPANT_ID")
		os.Exit(1)
	}

	if len(config.identity.publicKey) == 0 {
		logger.Error("Missing required configuration value: PUBLIC_KEY")
		os.Exit(1)
	}

	if len(config.identity.privateKey) == 0 {
		logger.Error("Missing required configuration value: PRIVATE_KEY")
		os.Exit(1)
	}

	if len(config.identity.identityProxyUrl) == 0 {
		logger.Error("Missing required configuration value: IDENTITY_PROXY_URL")
		os.Exit(1)
	}

	if len(config.identity.vcFolder) == 0 {
		logger.Error("Missing required configuration value: VC_FOLDER")
		os.Exit(1)
	}

	if len(config.identity.didPublicKeyType) == 0 {
		logger.Error("Missing required configuration value: DID_PUBLIC_KEY_TYPE")
		os.Exit(1)
	}

	// Check DID

	done := false

	for !done {
		didCorrect := checkDidDocument(config, logger)

		if didCorrect {
			logger.Info("DID document up to date!")
			return
		}

		hasRole := checkRole(config, logger)

		if !hasRole {
			logger.Error("Participant does not have the necessary role. Ask the TRUSTEE to give you the STEWARD role to join the data space.")
			os.Exit(1)
		}

		updateDidDocument(config, logger)
	}
}

type IndyDidConfigPublicKeyPurposes struct {
	Authentication       bool `json:"authentication"`
	AssertionMethod      bool `json:"assertionMethod"`
	KeyAgreement         bool `json:"keyAgreement"`
	CapabilityInvocation bool `json:"capabilityInvocation"`
	CapabilityDelegation bool `json:"capabilityDelegation"`
}

type IndyDidConfigPublicKey struct {
	Type     string                         `json:"type"`
	Base64   string                         `json:"base64"`
	Purposes IndyDidConfigPublicKeyPurposes `json:"purposes"`
}

type IndyDidConfigService struct {
	Type     string `json:"type"`
	Endpoint string `json:"endpoint"`
}

type IndyDidConfig struct {
	PublicKeys []IndyDidConfigPublicKey `json:"publicKeys"`
	Services   []IndyDidConfigService   `json:"services"`
}

type IndyDidResponse struct {
	Configuration IndyDidConfig `json:"configuration"`
}

func checkDidDocument(config *LauncherConfig, logger *glog.Logger) bool {
	publicKeyBytes, err := os.ReadFile(config.identity.publicKey)

	if err != nil {
		logger.Errorf("Error loading public key: %v", err)
		os.Exit(1)
	}

	publicKeyPem := string(publicKeyBytes)
	publicKeyBase64 := pemToBase64(publicKeyPem)

	logger.Debugf("Loaded public key: %v", publicKeyBase64)

	address := addressFromDid(config.identity.participantId)

	logger.Infof("Checking DID document configuration for %v", address)

	requestUrl := concatUrl(config.identity.identityProxyUrl, "/api/v1/indy/"+url.PathEscape(address))

	logger.Debugf("GET %v", requestUrl)

	s, responseBody, err := doGetRequest(requestUrl, map[string]string{
		"x-api-token": config.identity.identityProxyToken,
	})

	if err != nil {
		logger.Errorf("Request error: %v", err)
		os.Exit(1)
	}

	if s == 404 {
		logger.Errorf("No DID document found for %v", address)
		return false
	}

	if s != 200 {
		logger.Errorf("Request ended with status code: %d | %v", s, responseBody)
		os.Exit(1)
	}

	logger.Debugf("Response: %v", responseBody)

	var res IndyDidResponse

	err = json.Unmarshal([]byte(responseBody), &res)

	if err != nil {
		logger.Errorf("JSON parsing error: %v", err)
		return false
	}

	hasPublicKey := false

	if len(res.Configuration.PublicKeys) > 0 {
		pk := res.Configuration.PublicKeys[0]

		if pk.Base64 == publicKeyBase64 && pk.Type == config.identity.didPublicKeyType && pk.Purposes.Authentication {
			hasPublicKey = true
		}
	}

	if !hasPublicKey {
		logger.Info("DID document does not contain the public key")
		return false
	}

	hasCredentialService := false
	credentialServiceEndpoint := concatUrl(config.externalUrls.credentialServiceUrl, "/api/credentials/v1/participants/"+base64.URLEncoding.EncodeToString([]byte(config.identity.participantId)))

	for _, s := range res.Configuration.Services {
		if s.Type == "CredentialService" && s.Endpoint == credentialServiceEndpoint {
			hasCredentialService = true
			break
		}
	}

	if !hasCredentialService {
		logger.Info("DID document does not contain the service 'CredentialService'")
		return false
	}

	hasProtocolEndpoint := false
	protocolEndpoint := concatUrl(config.externalUrls.protocolUrl, "/protocol")

	for _, s := range res.Configuration.Services {
		if s.Type == "ProtocolEndpoint" && s.Endpoint == protocolEndpoint {
			hasProtocolEndpoint = true
			break
		}
	}

	if !hasProtocolEndpoint {
		logger.Info("DID document does not contain the service 'ProtocolEndpoint'")
		return false
	}

	return true
}

type RoleResponse struct {
	Address string `json:"address"`
	Role    string `json:"role"`
}

func checkRole(config *LauncherConfig, logger *glog.Logger) bool {
	address := addressFromDid(config.identity.participantId)

	logger.Infof("Checking the current ROLE for %v", address)

	requestUrl := concatUrl(config.identity.identityProxyUrl, "/api/v1/roles/account/"+url.PathEscape(address))

	logger.Debugf("GET %v", requestUrl)

	s, responseBody, err := doGetRequest(requestUrl, map[string]string{
		"x-api-token": config.identity.identityProxyToken,
	})

	if err != nil {
		logger.Errorf("Request error: %v", err)
		return false
	}

	if s != 200 {
		logger.Errorf("Request ended with status code: %d | %v", s, responseBody)
		return false
	}

	var res RoleResponse

	err = json.Unmarshal([]byte(responseBody), &res)

	if err != nil {
		logger.Errorf("JSON parsing error: %v", err)
		return false
	}

	logger.Infof("Current role for %v is %v", address, res.Role)

	return res.Role != "NONE"
}

type DidUpdateTxSign struct {
	Mode       string `json:"mode"`
	PrivateKey string `json:"privateKey"`
}

type DidUpdateBody struct {
	Configuration IndyDidConfig   `json:"configuration"`
	TxSign        DidUpdateTxSign `json:"txSign"`
}

type TxSendResult struct {
	Success bool   `json:"success"`
	Error   string `json:"error"`
	TxHash  string `json:"txHash"`
}

type DidUpdateResponse struct {
	SendResult *TxSendResult `json:"sendResult"`
	Success    bool          `json:"success"`
	Error      string        `json:"error"`
	TxHash     string        `json:"txHash"`
}

func updateDidDocument(config *LauncherConfig, logger *glog.Logger) bool {
	didOwnerPrivateKey := config.identity.didOwnerPrivateKey

	if len(didOwnerPrivateKey) == 0 {
		logger.Errorf("DID owner private key not provided. Cannot update DID automatically.")
		os.Exit(1)
	}

	publicKeyBytes, err := os.ReadFile(config.identity.publicKey)

	if err != nil {
		logger.Errorf("Error loading public key: %v", err)
		os.Exit(1)
	}

	publicKeyPem := string(publicKeyBytes)
	publicKeyBase64 := pemToBase64(publicKeyPem)

	logger.Debugf("Loaded public key: %v", publicKeyBase64)

	address := addressFromDid(config.identity.participantId)

	logger.Infof("Updating DID document for %v", address)

	requestUrl := concatUrl(config.identity.identityProxyUrl, "/api/v1/indy/"+url.PathEscape(address))

	credentialServiceEndpoint := concatUrl(config.externalUrls.credentialServiceUrl, "/api/credentials/v1/participants/"+base64.URLEncoding.EncodeToString([]byte(config.identity.participantId)))
	protocolEndpoint := concatUrl(config.externalUrls.protocolUrl, "/protocol")

	didDocument := DidUpdateBody{
		Configuration: IndyDidConfig{
			PublicKeys: []IndyDidConfigPublicKey{
				IndyDidConfigPublicKey{
					Type:   config.identity.didPublicKeyType,
					Base64: publicKeyBase64,
					Purposes: IndyDidConfigPublicKeyPurposes{
						Authentication:       true,
						AssertionMethod:      true,
						KeyAgreement:         true,
						CapabilityInvocation: true,
						CapabilityDelegation: true,
					},
				},
			},
			Services: []IndyDidConfigService{
				IndyDidConfigService{
					Type:     "CredentialService",
					Endpoint: credentialServiceEndpoint,
				},
				IndyDidConfigService{
					Type:     "ProtocolEndpoint",
					Endpoint: protocolEndpoint,
				},
			},
		},
		TxSign: DidUpdateTxSign{
			Mode:       "private_key",
			PrivateKey: didOwnerPrivateKey,
		},
	}

	requestBody, err := json.Marshal(didDocument)

	if err != nil {
		logger.Errorf("JSON serialization error: %v", err)
		os.Exit(1)
	}

	requestBodyString := string(requestBody)

	logger.Debugf("POST %v %v", requestUrl, requestBodyString)

	s, responseBody, err := doPostRequest(requestUrl, map[string]string{
		"x-api-token":  config.identity.identityProxyToken,
		"Content-Type": "application/json",
	}, requestBodyString)

	if err != nil {
		logger.Errorf("Request error: %v", err)
		return false
	}

	if s != 200 {
		logger.Errorf("Request ended with status code: %d | %v", s, responseBody)
		return false
	}

	var res DidUpdateResponse

	err = json.Unmarshal([]byte(responseBody), &res)

	if err != nil {
		logger.Errorf("JSON parsing error: %v", err)
		return false
	}

	if (res.SendResult == nil || !res.SendResult.Success) && !res.Success {
		if res.SendResult != nil {
			logger.Errorf("Error sending translation: %v", res.SendResult.Error)
		} else {
			logger.Errorf("Error sending translation: %v", res.Error)
		}

		return false
	}

	if res.SendResult != nil {
		logger.Infof("DID document updated for %v. Transaction hash: %v", address, res.SendResult.TxHash)
	} else {
		logger.Infof("DID document updated for %v. Transaction hash: %v", address, res.TxHash)
	}

	return true
}
