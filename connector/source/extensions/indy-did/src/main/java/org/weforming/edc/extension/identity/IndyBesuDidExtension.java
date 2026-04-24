/*
 *  This file is part of the WeForming project.
 *
 *  (Reserved for license)
 */

package org.weforming.edc.extension.identity;
import org.eclipse.edc.iam.did.spi.resolution.DidResolverRegistry;
import org.eclipse.edc.runtime.metamodel.annotation.Extension;
import org.eclipse.edc.runtime.metamodel.annotation.Inject;
import org.eclipse.edc.runtime.metamodel.annotation.Setting;
import org.eclipse.edc.spi.system.ServiceExtension;
import org.eclipse.edc.spi.system.ServiceExtensionContext;
import org.eclipse.edc.spi.types.TypeManager;
import org.eclipse.edc.web.spi.WebService;
import org.weforming.edc.extension.identity.api.DidResolutionApiController;
import org.weforming.edc.extension.identity.resolution.IndyBesuDidResolver;


/**
 * This extension enables support for Indy-Besu DID resolution
 * The identity proxy is required for this extension to work
 */
@Extension(value = IndyBesuDidExtension.NAME)
public class IndyBesuDidExtension implements ServiceExtension {

    /**
     * Name of the extension
     */
    public static final String NAME = "Indy-Besu DID";

    /**
     * Set the URL of the identity proxy, in order to make requests to it
     */
    @Setting
    public static final String IDENTITY_PROXY_URL = "weforming.identity.proxy.url";

    /**
     * Set the API token to authenticate to the identity proxy
     */
    @Setting
    public static final String IDENTITY_PROXY_TOKEN = "weforming.identity.proxy.token";

    /**
     * Set it to 'true' to enable debug endpoints
     */
    @Setting
    public static final String IDENTITY_DEBUG = "weforming.identity.debug";
    
    @Inject
    private DidResolverRegistry resolverRegistry;

    @Inject
    WebService webService;

    @Inject
    private TypeManager typeManager;

    @Override
    public String name() {
        return NAME;
    }

    @Override
    public void initialize(ServiceExtensionContext context) {
        var mapper = typeManager.getMapper();
        var monitor = context.getMonitor();

        // Register Indy-Besu DID resolver

        var identityProxyUrl = context.getSetting(IDENTITY_PROXY_URL, "http://127.0.0.1:80/");
        var identityProxyToken = context.getSetting(IDENTITY_PROXY_TOKEN, "");

        var resolver = new IndyBesuDidResolver(identityProxyUrl, identityProxyToken, mapper, monitor);

        resolverRegistry.register(resolver);

        // Register debug endpoints

        var isDebug = context.getSetting(IDENTITY_DEBUG, "false").equalsIgnoreCase("true");

        if (isDebug) {
            webService.registerResource(new DidResolutionApiController(resolver, context.getMonitor()));
        }

        monitor.debug("Registered Indy-Besu DID extension");
    }
}
