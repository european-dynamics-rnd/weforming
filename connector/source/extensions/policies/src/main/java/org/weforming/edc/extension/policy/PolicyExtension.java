/*
 *  This file is part of the WeForming project.
 *
 *  (Reserved for license)
 */

package org.weforming.edc.extension.policy;

import org.eclipse.edc.connector.controlplane.contract.spi.policy.ContractNegotiationPolicyContext;
import org.eclipse.edc.iam.identitytrust.spi.scope.ScopeExtractorRegistry;
import org.eclipse.edc.policy.context.request.spi.RequestCatalogPolicyContext;
import org.eclipse.edc.policy.context.request.spi.RequestContractNegotiationPolicyContext;
import org.eclipse.edc.policy.context.request.spi.RequestTransferProcessPolicyContext;
import org.eclipse.edc.policy.context.request.spi.RequestVersionPolicyContext;
import org.eclipse.edc.policy.engine.spi.PolicyEngine;
import org.eclipse.edc.policy.engine.spi.RuleBindingRegistry;
import org.eclipse.edc.policy.model.Duty;
import org.eclipse.edc.policy.model.Permission;
import org.eclipse.edc.runtime.metamodel.annotation.Extension;
import org.eclipse.edc.runtime.metamodel.annotation.Inject;
import org.eclipse.edc.spi.system.ServiceExtension;
import org.eclipse.edc.spi.system.ServiceExtensionContext;

import java.util.Set;

import static org.eclipse.edc.connector.controlplane.contract.spi.policy.ContractNegotiationPolicyContext.NEGOTIATION_SCOPE;
import static org.eclipse.edc.jsonld.spi.PropertyAndTypeNames.ODRL_USE_ACTION_ATTRIBUTE;
import static org.eclipse.edc.policy.engine.spi.PolicyEngine.ALL_SCOPES;
import static org.eclipse.edc.spi.constants.CoreConstants.EDC_NAMESPACE;


/**
 * This extension handles the policies for negotiations and access control
 */
@Extension(value = PolicyExtension.NAME)
public class PolicyExtension implements ServiceExtension {
    private static final String LOCATION_CONSTRAINT_KEY = EDC_NAMESPACE + "location";

    /**
     * Name of the extension
     */
    public static final String NAME = "Policies Extension";

    @Inject
    private RuleBindingRegistry ruleBindingRegistry;

    @Inject
    private PolicyEngine policyEngine;

    @Inject
    private ScopeExtractorRegistry scopeExtractorRegistry;

    @Override
    public String name() {
        return NAME;
    }

    @Override
    public void initialize(ServiceExtensionContext context) {
        var monitor = context.getMonitor();

        // Access control (Require MembershipCredential)

        var contextMappingFunction = new DefaultScopeMappingFunction(Set.of("org.eclipse.edc.vc.type:MembershipCredential:read"));

        policyEngine.registerPostValidator(RequestCatalogPolicyContext.class, contextMappingFunction::apply);
        policyEngine.registerPostValidator(RequestContractNegotiationPolicyContext.class, contextMappingFunction::apply);
        policyEngine.registerPostValidator(RequestTransferProcessPolicyContext.class, contextMappingFunction::apply);
        policyEngine.registerPostValidator(RequestVersionPolicyContext.class, contextMappingFunction::apply);

        // Scope extractors

        scopeExtractorRegistry.registerScopeExtractor(new DataAccessCredentialScopeExtractor());

        // Register rules

        ruleBindingRegistry.bind(ODRL_USE_ACTION_ATTRIBUTE, ALL_SCOPES);
        ruleBindingRegistry.bind(LOCATION_CONSTRAINT_KEY, NEGOTIATION_SCOPE);

        policyEngine.registerFunction(
                ContractNegotiationPolicyContext.class,
                Permission.class,
                LOCATION_CONSTRAINT_KEY,
                new LocationConstraintFunction(monitor)
        );

        ruleBindingRegistry.bind("DataAccess.level", NEGOTIATION_SCOPE);

        policyEngine.registerFunction(
                ContractNegotiationPolicyContext.class,
                Permission.class,
                "DataAccess.level",
                new DataAccessLevelConstraintFunction<>(monitor)
        );

        policyEngine.registerFunction(
                ContractNegotiationPolicyContext.class,
                Duty.class,
                "DataAccess.level",
                new DataAccessLevelConstraintFunction<>(monitor)
        );

        monitor.info("Loaded Policies Extension");
    }
}
