/*
 *  This file is part of the WeForming project.
 *
 *  (Reserved for license)
 */

package org.weforming.edc.extension.policy;

import org.eclipse.edc.policy.context.request.spi.RequestPolicyContext;
import org.eclipse.edc.policy.engine.spi.PolicyValidatorRule;
import org.eclipse.edc.policy.model.Policy;

import java.util.HashSet;
import java.util.Set;

/**
 * Scope mapping function (from MVD)
 */
public class DefaultScopeMappingFunction implements PolicyValidatorRule<RequestPolicyContext> {
    private final Set<String> defaultScopes;

    /**
     * Constructor
     *
     * @param defaultScopes The list of scopes
     */
    public DefaultScopeMappingFunction(Set<String> defaultScopes) {
        this.defaultScopes = defaultScopes;
    }

    @Override
    public Boolean apply(Policy policy, RequestPolicyContext requestPolicyContext) {
        var requestScopeBuilder = requestPolicyContext.requestScopeBuilder();
        var rq = requestScopeBuilder.build();
        var existingScope = rq.getScopes();
        var newScopes = new HashSet<>(defaultScopes);
        newScopes.addAll(existingScope);
        requestScopeBuilder.scopes(newScopes);
        return true;
    }
}
