package main

import (
	"encoding/base64"
	"encoding/json"
	"errors"
	"fmt"
	"os"
	"time"

	"github.com/AgustinSRG/glog"
)

func initializeIdentityHub(config *LauncherConfig, logger *glog.Logger) {
	done := false
	var clientSecret string
	var err error

	// Create participant in identity hub

	for !done {
		time.Sleep(1 * time.Second)

		clientSecret, err = createIdentityHubParticipant(config, logger)

		if err != nil {
			time.Sleep(1 * time.Second)
			continue
		}

		done = true
	}

	// Set secret in the connector

	done = false

	for !done {
		time.Sleep(1 * time.Second)

		err = setParticipantClientSecret(config, logger, clientSecret, false)

		if err != nil {
			time.Sleep(1 * time.Second)
			continue
		}

		done = true
	}

	logger.Info("Done: Successfully initialized Identity Hub")
}

type IdentityHubParticipantServiceEndpoint struct {
	Type            string `json:"type"`
	ServiceEndpoint string `json:"serviceEndpoint"`
	Id              string `json:"id"`
}

type IdentityHubParticipantKey struct {
	KeyId           string `json:"keyId"`
	PrivateKeyAlias string `json:"privateKeyAlias"`
	PublicKeyPem    string `json:"publicKeyPem"`
}

type IdentityHubParticipant struct {
	Roles            []string                                `json:"roles"`
	ServiceEndpoints []IdentityHubParticipantServiceEndpoint `json:"serviceEndpoints"`
	Active           bool                                    `json:"active"`
	ParticipantId    string                                  `json:"participantId"`
	Did              string                                  `json:"did"`
	Key              IdentityHubParticipantKey               `json:"key"`
}

type IdentityHubParticipantResponse struct {
	ClientSecret string `json:"clientSecret"`
}

func createIdentityHubParticipant(config *LauncherConfig, logger *glog.Logger) (clientSecret string, err error) {
	logger.Infof("Creating participant in Identity Hub...")

	identityHubSuperUserSecret := base64.StdEncoding.EncodeToString([]byte(config.identityHubSuperUser.user)) +
		"." +
		base64.StdEncoding.EncodeToString([]byte(config.identityHubSuperUser.secret))

	publicKeyBytes, err := os.ReadFile(config.identity.publicKey)

	if err != nil {
		logger.Errorf("Error loading public key: %v", err)
		return "", err
	}

	publicKeyPem := string(publicKeyBytes)

	logger.Debugf("Loaded public key: \n%v", publicKeyPem)

	credentialServiceEndpoint := concatUrl(config.externalUrls.credentialServiceUrl, "/api/credentials/v1/participants/"+base64.URLEncoding.EncodeToString([]byte(config.identity.participantId)))
	protocolEndpoint := concatUrl(config.externalUrls.protocolUrl, "/protocol")

	participant := IdentityHubParticipant{
		Roles: make([]string, 0),
		ServiceEndpoints: []IdentityHubParticipantServiceEndpoint{
			IdentityHubParticipantServiceEndpoint{
				Type:            "CredentialService",
				ServiceEndpoint: credentialServiceEndpoint,
				Id:              "provider-credentialservice-1",
			},
			IdentityHubParticipantServiceEndpoint{
				Type:            "ProtocolEndpoint",
				ServiceEndpoint: protocolEndpoint,
				Id:              "provider-dsp",
			},
		},
		Active:        true,
		ParticipantId: config.identity.participantId,
		Did:           config.identity.participantId,
		Key: IdentityHubParticipantKey{
			KeyId:           config.identity.participantId + "#key-1",
			PrivateKeyAlias: "private-key",
			PublicKeyPem:    publicKeyPem,
		},
	}

	requestBody, err := json.Marshal(participant)

	if err != nil {
		logger.Errorf("JSON serialization error: %v", err)
		return "", err
	}

	requestUrl := "http://127.0.0.1:" + fmt.Sprint(config.identityHubPorts.identity) + "/api/identity/v1alpha/participants/"

	requestBodyString := string(requestBody)

	logger.Debugf("POST %v %v", requestUrl, requestBodyString)

	s, responseBody, err := doPostRequest(requestUrl, map[string]string{
		"x-api-key":    identityHubSuperUserSecret,
		"Content-Type": "application/json",
	}, requestBodyString)

	if err != nil {
		logger.Errorf("Request error: %v", err)
		return "", err
	}

	if s != 200 {
		logger.Errorf("Request ended with status code: %d | %v", s, responseBody)
		return "", errors.New("Request ended with status code: " + fmt.Sprint(s))
	}

	var res IdentityHubParticipantResponse

	err = json.Unmarshal([]byte(responseBody), &res)

	if err != nil {
		logger.Errorf("JSON parsing error: %v", err)
		return "", err
	}

	return res.ClientSecret, nil
}

func setParticipantClientSecret(config *LauncherConfig, logger *glog.Logger, clientSecret string, exists bool) error {
	logger.Infof("Setting participant client secret...")

	secretObject := map[string]any{
		"@context": map[string]string{
			"edc": "https://w3id.org/edc/v0.0.1/ns/",
		},
		"@type":                                "https://w3id.org/edc/v0.0.1/ns/Secret",
		"@id":                                  config.identity.participantId + "-sts-client-secret",
		"https://w3id.org/edc/v0.0.1/ns/value": clientSecret,
	}

	requestBody, err := json.Marshal(secretObject)

	if err != nil {
		logger.Errorf("JSON serialization error: %v", err)
		return err
	}

	requestUrl := "http://127.0.0.1:" + fmt.Sprint(config.connectorPorts.management) + "/management/v3/secrets"

	requestBodyString := string(requestBody)

	if exists {
		logger.Debugf("PUT %v %v", requestUrl, requestBodyString)

		s, responseBody, err := doPutRequest(requestUrl, map[string]string{
			"Content-Type": "application/json",
		}, requestBodyString)

		if err != nil {
			logger.Errorf("Request error: %v", err)
			return err
		}

		if s == 404 {
			logger.Debug("Secret does not exist, creating it...")
			return setParticipantClientSecret(config, logger, clientSecret, false)
		}

		if s != 200 {
			logger.Errorf("Request ended with status code: %d | %v", s, responseBody)
			return errors.New("Request ended with status code: " + fmt.Sprint(s))
		}
	} else {
		logger.Debugf("POST %v %v", requestUrl, requestBodyString)

		s, responseBody, err := doPostRequest(requestUrl, map[string]string{
			"Content-Type": "application/json",
		}, requestBodyString)

		if err != nil {
			logger.Errorf("Request error: %v", err)
			return err
		}

		if s == 409 {
			logger.Debug("Secret already exists, updating it...")
			return setParticipantClientSecret(config, logger, clientSecret, true)
		}

		if s != 200 {
			logger.Errorf("Request ended with status code: %d | %v", s, responseBody)
			return errors.New("Request ended with status code: " + fmt.Sprint(s))
		}
	}

	return nil
}
