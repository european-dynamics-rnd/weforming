/*
 *  This file is part of the WeForming project.
 *
 *  (Reserved for license)
 */

package org.weforming.edc.extension.vc;
import org.eclipse.edc.identityhub.spi.verifiablecredentials.model.VerifiableCredentialResource;
import org.eclipse.edc.identityhub.spi.verifiablecredentials.store.CredentialStore;
import org.eclipse.edc.runtime.metamodel.annotation.Extension;
import org.eclipse.edc.runtime.metamodel.annotation.Inject;
import org.eclipse.edc.runtime.metamodel.annotation.Setting;
import org.eclipse.edc.spi.monitor.Monitor;
import org.eclipse.edc.spi.security.Vault;
import org.eclipse.edc.spi.system.ServiceExtension;
import org.eclipse.edc.spi.system.ServiceExtensionContext;
import org.eclipse.edc.spi.types.TypeManager;

import java.io.File;
import java.io.IOException;
import java.util.stream.Stream;

import static org.eclipse.edc.spi.constants.CoreConstants.JSON_LD;


/**
 * This extension loads the verifiable credentials
 */
@Extension(value = VcLoaderExtension.NAME)
public class VcLoaderExtension implements ServiceExtension {

    /**
     * Name of the extension
     */
    public static final String NAME = "VC Loader";

    @Inject
    private Vault vault;

    @Setting(key = "identity.hub.credentials.path", required = true, description = "The path to the verifiable credentials folder.")
    private String credentialsPath;

    @Inject
    private TypeManager typeManager;

    @Inject
    private CredentialStore store;

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
        // Load credentials
        try {
            seedCredentials(credentialsPath, monitor);
        } catch (IOException ex) {
            throw new RuntimeException(ex);
        }
    }

    private void seedCredentials(String credentialsSourceDirectory, Monitor monitor) throws IOException {
        var absPath = new File(credentialsSourceDirectory).getAbsoluteFile();

        if (!absPath.exists()) {
            monitor.warning("Path '%s' does not exist. It must be a resolvable path with read access. Will not add any VCs.".formatted(credentialsSourceDirectory));
            return;
        }
        var files = absPath.listFiles();
        if (files == null) {
            monitor.warning("No files found in directory '%s'. Will not add any VCs.".formatted(credentialsSourceDirectory));
            return;
        }

        var objectMapper = typeManager.getMapper(JSON_LD);
        // filtering for *.json files is advised, because on K8s there can be softlinks, if a directory is mapped via ConfigMap
        Stream.of(files).filter(f -> f.getName().endsWith(".json")).forEach(p -> {
            try {
                store.create(objectMapper.readValue(p, VerifiableCredentialResource.class));
                monitor.info("Loaded VC from file '%s'".formatted(p.getAbsolutePath()));
            } catch (IOException e) {
                monitor.severe("Error storing VC", e);
            }
        });
    }
}
