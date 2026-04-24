/*
 *  This file is part of the WeForming project.
 *
 *  (Reserved for license)
 */

package org.weforming.edc.extension.policy;

import org.eclipse.edc.connector.controlplane.contract.spi.policy.ContractNegotiationPolicyContext;
import org.eclipse.edc.policy.engine.spi.AtomicConstraintRuleFunction;
import org.eclipse.edc.policy.model.Operator;
import org.eclipse.edc.policy.model.Permission;
import org.eclipse.edc.spi.monitor.Monitor;

import java.util.Collection;
import java.util.Objects;

import static java.lang.String.format;

public class LocationConstraintFunction implements AtomicConstraintRuleFunction<Permission, ContractNegotiationPolicyContext> {
    
    private final Monitor monitor;
    
    public LocationConstraintFunction(Monitor monitor) {
        this.monitor = monitor;
    }
    
    @Override
    public boolean evaluate(Operator operator, Object rightValue, Permission rule, ContractNegotiationPolicyContext context) {
        var region = context.participantAgent().getClaims().get("region");
        
        monitor.debug(format("Evaluating constraint: location %s %s", operator, rightValue.toString()));
        
        return switch (operator) {
            case EQ -> Objects.equals(region, rightValue);
            case NEQ -> !Objects.equals(region, rightValue);
            case IN -> ((Collection<?>) rightValue).contains(region);
            default -> false;
        };
    }
}
