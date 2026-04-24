/*
 *  This file is part of the WeForming project.
 *
 *  (Reserved for license)
 */

package org.weforming.edc.extension.trust;
import org.eclipse.edc.iam.verifiablecredentials.spi.model.Issuer;
import org.eclipse.edc.iam.verifiablecredentials.spi.validation.TrustedIssuerRegistry;
import org.eclipse.edc.runtime.metamodel.annotation.Extension;
import org.eclipse.edc.runtime.metamodel.annotation.Inject;
import org.eclipse.edc.runtime.metamodel.annotation.Setting;
import org.eclipse.edc.spi.system.ServiceExtension;
import org.eclipse.edc.spi.system.ServiceExtensionContext;

import java.util.Map;

import static org.eclipse.edc.iam.verifiablecredentials.spi.validation.TrustedIssuerRegistry.WILDCARD;


/**
 * This extension sets the list of trusted issuers
 */
@Extension(value = TrustedIssuersExtension.NAME)
public class TrustedIssuersExtension implements ServiceExtension {

    /**
     * Name of the extension
     */
    public static final String NAME = "Trusted Issuers";

    @Inject
    private TrustedIssuerRegistry trustedIssuerRegistry;

    @Setting(key = "connector.trusted.issuers", required = true, description = "List of DIDs of trusted issuers, separated by commas.")
    private String trustedIssuers;

    @Override
    public String name() {
        return NAME;
    }

    @Override
    public void initialize(ServiceExtensionContext context) {
        var monitor = context.getMonitor();

        String[] trustedIssuersSpl = trustedIssuers.split(",");

        for (String trustedIssuer : trustedIssuersSpl) {
            monitor.info("Trusted issuer: " + trustedIssuer);

            trustedIssuerRegistry.register(new Issuer(trustedIssuer, Map.of()), WILDCARD);
        }

        monitor.info("Loaded trusted issuers");
    }
}
