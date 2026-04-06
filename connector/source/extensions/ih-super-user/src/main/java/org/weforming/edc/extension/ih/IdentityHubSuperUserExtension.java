/*
 *  This file is part of the WeForming project.
 *
 *  (Reserved for license)
 */

package org.weforming.edc.extension.ih;

import org.eclipse.edc.identityhub.spi.authentication.ServicePrincipal;
import org.eclipse.edc.identityhub.spi.participantcontext.ParticipantContextService;
import org.eclipse.edc.identityhub.spi.participantcontext.model.KeyDescriptor;
import org.eclipse.edc.identityhub.spi.participantcontext.model.ParticipantManifest;
import org.eclipse.edc.runtime.metamodel.annotation.Extension;
import org.eclipse.edc.runtime.metamodel.annotation.Inject;
import org.eclipse.edc.runtime.metamodel.annotation.Setting;
import org.eclipse.edc.spi.EdcException;
import org.eclipse.edc.spi.monitor.Monitor;
import org.eclipse.edc.spi.security.Vault;
import org.eclipse.edc.spi.system.ServiceExtension;
import org.eclipse.edc.spi.system.ServiceExtensionContext;

import java.util.List;
import java.util.Map;

/**
 * This extension loads the verifiable credentials
 */
@Extension(value = IdentityHubSuperUserExtension.NAME)
public class IdentityHubSuperUserExtension implements ServiceExtension {

    /**
     * Name of the extension
     */
    public static final String NAME = "Identity Hub Superuser";

    @Setting(key = "edc.ih.client.id", required = false, description = "Client ID for the OAuth client.", defaultValue = "super-user")
    private String clientId;

    @Setting(key = "edc.ih.client.secret", required = true, description = "Client secret for the OAuth client.")
    private String clientSecret;

    @Inject
    private ParticipantContextService participantContextService;

    @Inject
    private Vault vault;

    @Override
    public String name() {
        return NAME;
    }

    private Monitor monitor;

    @Override
    public void initialize(ServiceExtensionContext context) {
        this.monitor = context.getMonitor();
    }

    @Override
    public void start() {
        if (participantContextService.getParticipantContext(clientId).succeeded()) { // already exists
            monitor.debug("super-user already exists with ID '%s', will not re-create".formatted(clientId));
            return;
        }

        participantContextService.createParticipantContext(ParticipantManifest.Builder.newInstance()
                        .participantId(clientId)
                        .did("did:web:" + clientId) // doesn't matter, not intended for resolution
                        .active(true)
                        .key(KeyDescriptor.Builder.newInstance()
                                .keyGeneratorParams(Map.of("algorithm", "EdDSA", "curve", "Ed25519"))
                                .keyId("private-key")
                                .privateKeyAlias("public-key")
                                .build())
                        .roles(List.of(ServicePrincipal.ROLE_ADMIN))
                        .build())
                .onSuccess(generatedKey -> {
                    participantContextService.getParticipantContext(clientId)
                            .onSuccess(pc -> vault.storeSecret(pc.getApiTokenAlias(), clientSecret)
                                    .onSuccess(u -> monitor.debug("Super-user key override successful"))
                                    .onFailure(f -> monitor.warning("Error storing API key in vault: %s".formatted(f.getFailureDetail()))))
                            .onFailure(f -> monitor.warning("Error overriding API key for '%s': %s".formatted(clientId, f.getFailureDetail())));
                })
                .orElseThrow(f -> new EdcException("Error creating Super-User: " + f.getFailureDetail()));
    }
}
