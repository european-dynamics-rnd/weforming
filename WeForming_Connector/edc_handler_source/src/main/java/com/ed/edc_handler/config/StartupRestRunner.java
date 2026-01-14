package com.ed.edc_handler.config;

import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.*;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.nio.charset.StandardCharsets;
import java.util.Base64;
import java.util.List;
import java.util.Map;

@Service
@RequiredArgsConstructor
public class StartupRestRunner implements ApplicationRunner {

    @Value("${connector.url}")
    private String connectorUrl;

    @Value("${indy.url}")
    private String indyUrl;

    @Value("${registerParticipant.x-api-key}")
    private String xApiKey;

    @Value("${registerParticipant.id}")
    private String credentialServiceId;

    @Value("${registerParticipant.participantDid}")
    private String participantDid;

    @Value("${registerParticipant.participantId}")
    private String participantId;

    @Value("${registerParticipant.publicKeyPem}")
    private String publicKeyPem;

    @Value("${registerParticipant.privateKey}")
    private String privateKey;

    private final RestTemplate restTemplate;

    @Override
    public void run(ApplicationArguments args) {
        Map<String, String> registerParticipantResponse = registerParticipant();

        if (registerParticipantResponse == null) {
            throw new IllegalStateException("registerParticipant returned null response");
        }

        String clientSecret = registerParticipantResponse.get("clientSecret");

        if (clientSecret == null || clientSecret.isBlank()) {
            throw new IllegalStateException("clientSecret missing from registerParticipant response");
        }

        createSecret(clientSecret);

        registerIndyIdentity();
    }

    private Map<String, String> registerParticipant() {

        String participantIdBase64 =
                Base64.getEncoder()
                        .encodeToString(participantDid.getBytes(StandardCharsets.UTF_8));

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        headers.set("x-api-key", xApiKey);

        Map<String, Object> credentialServiceEndpoint = Map.of(
                "type", "CredentialService",
                "id", credentialServiceId,
                "serviceEndpoint",
                connectorUrl + ":7002/api/credentials/v1/participants/" + participantIdBase64
        );

        Map<String, Object> protocolEndpoint = Map.of(
                "type", "ProtocolEndpoint",
                "id", "consumer-dsp",
                "serviceEndpoint", connectorUrl + ":8004/protocol"
        );

        Map<String, Object> key = Map.of(
                "keyId", participantDid + "#key-1",
                "privateKeyAlias", "private-key",
                "publicKeyPem", publicKeyPem
        );

        Map<String, Object> requestBody = Map.of(
                "roles", List.of(),
                "serviceEndpoints", List.of(credentialServiceEndpoint, protocolEndpoint),
                "active", true,
                "participantId", participantDid,
                "did", participantDid,
                "key", key
        );

        HttpEntity<Map<String, Object>> entity =
                new HttpEntity<>(requestBody, headers);

        ResponseEntity<Map<String, String>> response =
                restTemplate.exchange(
                        connectorUrl + ":7003/api/identity/v1alpha/participants/",
                        HttpMethod.POST,
                        entity,
                        new ParameterizedTypeReference<>() {}
                );

        return response.getBody();
    }

    private Map<String, Object> createSecret(String clientSecret) {

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);

        Map<String, Object> context = Map.of(
                "edc", "https://w3id.org/edc/v0.0.1/ns/"
        );

        Map<String, Object> requestBody = Map.of(
                "@context", context,
                "@type", "https://w3id.org/edc/v0.0.1/ns/Secret",
                "@id", participantDid + "-sts-client-secret",
                "https://w3id.org/edc/v0.0.1/ns/value", clientSecret
        );

        HttpEntity<Map<String, Object>> entity =
                new HttpEntity<>(requestBody, headers);

        ResponseEntity<Map<String, Object>> response =
                restTemplate.exchange(
                        connectorUrl + ":8003/management/v3/secrets",
                        HttpMethod.POST,
                        entity,
                        new ParameterizedTypeReference<>() {}
                );

        return response.getBody();
    }

    private Map<String, Object> registerIndyIdentity() {

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        headers.set("x-api-token", "change_me_1"); // ιδανικά από properties

        Map<String, Object> purposes = Map.of(
                "authentication", true,
                "assertionMethod", true,
                "keyAgreement", true,
                "capabilityInvocation", true,
                "capabilityDelegation", true
        );

        Map<String, Object> publicKey = Map.of(
                "type", "Ed25519Signature2018",
                "base64", extractBase64FromPem(publicKeyPem),
                "purposes", purposes
        );

        String participantIdBase64 =
                Base64.getEncoder()
                        .encodeToString(participantDid.getBytes(StandardCharsets.UTF_8));

        Map<String, Object> credentialService = Map.of(
                "type", "CredentialService",
                "endpoint",
                connectorUrl + ":7002/api/credentials/v1/participants/" + participantIdBase64
        );

        Map<String, Object> protocolService = Map.of(
                "type", "ProtocolEndpoint",
                "endpoint", connectorUrl + ":8004/protocol"
        );

        Map<String, Object> configuration = Map.of(
                "publicKeys", List.of(publicKey),
                "services", List.of(credentialService, protocolService)
        );

        Map<String, Object> txSign = Map.of(
                "mode", "private_key",
                "privateKey", privateKey
        );

        Map<String, Object> requestBody = Map.of(
                "configuration", configuration,
                "txSign", txSign
        );

        HttpEntity<Map<String, Object>> entity =
                new HttpEntity<>(requestBody, headers);

        ResponseEntity<Map<String, Object>> response =
                restTemplate.exchange(
                        indyUrl + participantId,
                        HttpMethod.POST,
                        entity,
                        new ParameterizedTypeReference<>() {}
                );

        return response.getBody();
    }

    private String extractBase64FromPem(String publicKeyPem) {
        return publicKeyPem
                .replace("-----BEGIN PUBLIC KEY-----", "")
                .replace("-----END PUBLIC KEY-----", "")
                .replaceAll("\\s+", "")
                .trim();
    }
}
