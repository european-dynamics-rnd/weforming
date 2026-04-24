/*
 *  This file is part of the WeForming project.
 *
 *  (Reserved for license)
 */

package org.weforming.edc.extension.jws;
import org.eclipse.edc.iam.identitytrust.spi.verification.SignatureSuiteRegistry;
import org.eclipse.edc.iam.verifiablecredentials.spi.VcConstants;
import org.eclipse.edc.runtime.metamodel.annotation.Extension;
import org.eclipse.edc.runtime.metamodel.annotation.Inject;
import org.eclipse.edc.security.signature.jws2020.Jws2020SignatureSuite;
import org.eclipse.edc.spi.system.ServiceExtension;
import org.eclipse.edc.spi.system.ServiceExtensionContext;
import org.eclipse.edc.spi.types.TypeManager;

import static org.eclipse.edc.spi.constants.CoreConstants.JSON_LD;


/**
 * This extension registers the JWS suite
 */
@Extension(value = JwsSuiteExtension.NAME)
public class JwsSuiteExtension implements ServiceExtension {

    /**
     * Name of the extension
     */
    public static final String NAME = "JWS Suite";

    @Inject
    private SignatureSuiteRegistry signatureSuiteRegistry;

    @Inject
    private TypeManager typeManager;

    @Override
    public String name() {
        return NAME;
    }

    @Override
    public void initialize(ServiceExtensionContext context) {
        var monitor = context.getMonitor();

        var suite = new Jws2020SignatureSuite(typeManager.getMapper(JSON_LD));
        signatureSuiteRegistry.register(VcConstants.JWS_2020_SIGNATURE_SUITE, suite);

        monitor.info("Loaded Jws2020SignatureSuite");
    }
}
