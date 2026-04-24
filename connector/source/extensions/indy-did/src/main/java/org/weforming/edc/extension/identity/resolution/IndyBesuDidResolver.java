/*
 *  This file is part of the WeForming project.
 *
 *  (Reserved for license)
 */

package org.weforming.edc.extension.identity.resolution;

import com.fasterxml.jackson.databind.ObjectMapper;
import okhttp3.Request;
import okhttp3.OkHttpClient;
import org.eclipse.edc.iam.did.spi.document.DidDocument;
import org.eclipse.edc.iam.did.spi.resolution.DidResolver;
import org.eclipse.edc.spi.monitor.Monitor;
import org.eclipse.edc.spi.result.Result;
import org.jetbrains.annotations.NotNull;

import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;

import static java.lang.String.format;

/**
 * Resolves Indy-Besu DIds using the WeForming identity proxy
 */
public class IndyBesuDidResolver implements DidResolver {
    private static final String DID_METHOD = "indy";

    private final OkHttpClient httpClient;
    private final ObjectMapper mapper;
    private final Monitor monitor;
    private final String identityProxyUrl;

    private final String identityProxyApiToken;

    /**
     * Instantiates IndyBesuDidResolver
     *
     * @param identityProxyUrl URL of the identity proxy
     * @param mapper Object mapper instance
     * @param monitor Monitor instance
     */
    public IndyBesuDidResolver(String identityProxyUrl, String identityProxyApiToken, ObjectMapper mapper, Monitor monitor) {
        this.httpClient = new OkHttpClient();
        this.identityProxyUrl = identityProxyUrl;
        this.identityProxyApiToken = identityProxyApiToken;
        this.mapper = mapper;
        this.monitor = monitor;
    }

    @Override
    public @NotNull String getMethod() {
        return DID_METHOD;
    }

    /**
     * From the given base URL of the identity proxy, resolves
     * the API endpoint that must be called in order
     * to resolve the DID document from the given DID
     *
     * @param didKey The DID
     * @return The URL of the endpoint that must be called
     * @throws URISyntaxException If the base URL is misconfigured (not a valid URL)
     */
    private String getResolutionEndpoint(String didKey) throws URISyntaxException {
        URI identityProxyUri = new URI(this.identityProxyUrl);
        URI resolvedUri = identityProxyUri.resolve("./api/v1/did/" + URLEncoder.encode(didKey, StandardCharsets.UTF_8) + "?adapt=edc");

        return resolvedUri.toString();
    }

    @Override
    @NotNull
    public Result<DidDocument> resolve(String didKey) {
        String url;
        try {
            url = this.getResolutionEndpoint(didKey);
        } catch (URISyntaxException e) {
            monitor.severe("Error resolving identity proxy endpoint. DID: " + didKey + " | Proxy base URL: " + this.identityProxyUrl, e);
            return Result.failure("Error resolving identity proxy endpoint: " + e.getMessage());
        }

        monitor.debug("Calling identity proxy endpoint: GET " + url);

        var request = new Request.Builder().url(url).get().header("x-api-token", this.identityProxyApiToken).build();
        try (var response = httpClient.newCall(request).execute()) {
            if (response.code() != 200) {
                monitor.debug(format("Error resolving DID: %s. HTTP Code was: %s", didKey, response.code()));
                return Result.failure(format("Error resolving DID: %s. HTTP Code was: %s", didKey, response.code()));
            }
            try (var body = response.body()) {
                if (body == null) {
                    return Result.failure("DID response contained an empty body: " + didKey);
                }
                var didDocument = mapper.readValue(body.string(), DidDocument.class);
                return Result.success(didDocument);
            }
        } catch (IOException e) {
            monitor.severe("Error resolving DID: " + didKey, e);
            return Result.failure("Error resolving DID: " + e.getMessage());
        }
    }
}
