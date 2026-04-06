/*
 *  This file is part of the WeForming project.
 *
 *  (Reserved for license)
 */

package org.weforming.edc.extension.transformers;
import org.eclipse.edc.runtime.metamodel.annotation.Extension;
import org.eclipse.edc.runtime.metamodel.annotation.Inject;
import org.eclipse.edc.spi.system.ServiceExtension;
import org.eclipse.edc.spi.system.ServiceExtensionContext;
import org.eclipse.edc.spi.types.TypeManager;
import org.eclipse.edc.transform.spi.TypeTransformerRegistry;
import org.eclipse.edc.transform.transformer.edc.to.JsonValueToGenericTypeTransformer;

import static org.eclipse.edc.spi.constants.CoreConstants.JSON_LD;


/**
 * This extension registers a transformer for JSON-LD
 */
@Extension(value = JsonLdTransformerExtension.NAME)
public class JsonLdTransformerExtension implements ServiceExtension {

    /**
     * Name of the extension
     */
    public static final String NAME = "JSON-LD Transfromer";

    @Inject
    private TypeManager typeManager;

    @Inject
    private TypeTransformerRegistry typeTransformerRegistry;

    @Override
    public String name() {
        return NAME;
    }

    @Override
    public void initialize(ServiceExtensionContext context) {
        var monitor = context.getMonitor();

        typeTransformerRegistry.register(new JsonValueToGenericTypeTransformer(typeManager, JSON_LD));

        monitor.info("Registered JSON-LD transfromer");
    }
}
