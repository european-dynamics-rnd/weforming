/*
 *  This file is part of the WeForming project.
 *
 *  (Reserved for license)
 */

package org.weforming.edc.extension.keys;
import org.eclipse.edc.runtime.metamodel.annotation.Extension;
import org.eclipse.edc.runtime.metamodel.annotation.Inject;
import org.eclipse.edc.runtime.metamodel.annotation.Setting;
import org.eclipse.edc.spi.security.Vault;
import org.eclipse.edc.spi.system.ServiceExtension;
import org.eclipse.edc.spi.system.ServiceExtensionContext;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Paths;


/**
 * This extension loads the private and public keys
 * from PEM files
 */
@Extension(value = KeysLoaderExtension.NAME)
public class KeysLoaderExtension implements ServiceExtension {

    /**
     * Name of the extension
     */
    public static final String NAME = "Keys Loader";

    @Inject
    private Vault vault;

    @Setting(key = "connector.keys.public.path", required = true, description = "The path to the public key of the connector. PEM format.")
    private String publicKeyPath;

    @Setting(key = "connector.keys.private.path", required = true, description = "The path to the private key of the connector. PEM format.")
    private String privateKeyPath;

    @Override
    public String name() {
        return NAME;
    }

    @Override
    public void initialize(ServiceExtensionContext context) {
        var monitor = context.getMonitor();

        // Read the keys from the filesystem

        String publicKeyPem = "";
        String privateKeyPem = "";

        try {
            byte[] publicKeyBytes = Files.readAllBytes(Paths.get(publicKeyPath));
            publicKeyPem = new String(publicKeyBytes, StandardCharsets.UTF_8);

            byte[] privateKeyBytes = Files.readAllBytes(Paths.get(privateKeyPath));
            privateKeyPem = new String(privateKeyBytes, StandardCharsets.UTF_8);
        } catch (IOException ex) {
            monitor.severe("Could not load connector keys: " + ex.getMessage());
            return;
        }

        // Store keys in the vault

        vault.storeSecret("public-key", publicKeyPem);
        vault.storeSecret("private-key", privateKeyPem);

        monitor.info("Loaded connector keys (Public + Private)");
    }
}
