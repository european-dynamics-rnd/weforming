/*
 *  This file is part of the WeForming project.
 *
 *  (Reserved for license)
 */

package org.weforming.edc.extension.policy;

import org.eclipse.edc.connector.controlplane.contract.spi.policy.ContractNegotiationPolicyContext;
import org.eclipse.edc.iam.verifiablecredentials.spi.model.VerifiableCredential;
import org.eclipse.edc.participant.spi.ParticipantAgent;
import org.eclipse.edc.policy.engine.spi.AtomicConstraintRuleFunction;
import org.eclipse.edc.policy.model.Operator;
import org.eclipse.edc.policy.model.Rule;
import org.eclipse.edc.spi.monitor.Monitor;
import org.eclipse.edc.spi.result.Result;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

import static java.lang.String.format;

public class DataAccessLevelConstraintFunction<R extends Rule> implements AtomicConstraintRuleFunction<R, ContractNegotiationPolicyContext> {
    private static final String VC_CLAIM = "vc";
    protected static final String MVD_NAMESPACE = "https://w3id.org/mvd/credentials/";
    private static final String DATAPROCESSOR_CRED_TYPE = "DataProcessorCredential";

    private final Monitor monitor;

    public DataAccessLevelConstraintFunction(Monitor monitor) {
        this.monitor = monitor;
    }

    protected Result<List<VerifiableCredential>> getCredentialList(ParticipantAgent agent) {
        var vcListClaim = agent.getClaims().get(VC_CLAIM);

        if (vcListClaim == null) {
            return Result.failure("ParticipantAgent did not contain a '%s' claim.".formatted(VC_CLAIM));
        }
        if (!(vcListClaim instanceof List)) {
            return Result.failure("ParticipantAgent contains a '%s' claim, but the type is incorrect. Expected %s, received %s.".formatted(VC_CLAIM, List.class.getName(), vcListClaim.getClass().getName()));
        }

        var vcList = new ArrayList<VerifiableCredential>();

        for (Object o : (List<?>) vcListClaim) {
            if (o instanceof VerifiableCredential) {
                vcList.add((VerifiableCredential) o);
            }
        }

        if (vcList.isEmpty()) {
            return Result.failure("ParticipantAgent contains a '%s' claim but it did not contain any VerifiableCredentials.".formatted(VC_CLAIM));
        }

        return Result.success(vcList);
    }

    @Override
    public boolean evaluate(Operator operator, Object rightValue, R rule, ContractNegotiationPolicyContext context) {
        monitor.debug(format("Evaluating constraint: DataAccess.level %s %s", operator, rightValue.toString()));

        if (!operator.equals(Operator.EQ)) {
            context.reportProblem("Cannot evaluate operator %s, only %s is supported".formatted(operator, Operator.EQ));
            return false;
        }
        var pa = context.participantAgent();
        if (pa == null) {
            context.reportProblem("ParticipantAgent not found on PolicyContext");
            return false;
        }

        var credentialResult = getCredentialList(pa);
        if (credentialResult.failed()) {
            context.reportProblem(credentialResult.getFailureDetail());
            return false;
        }

        return credentialResult.getContent()
                .stream()
                .filter(vc -> vc.getType().stream().anyMatch(t -> t.endsWith(DATAPROCESSOR_CRED_TYPE)))
                .flatMap(credential -> credential.getCredentialSubject().stream())
                .anyMatch(credentialSubject -> {
                    var version = credentialSubject.getClaim(MVD_NAMESPACE, "contractVersion");
                    var level = credentialSubject.getClaim(MVD_NAMESPACE, "level");

                    return version != null && Objects.equals(level, rightValue);
                });
    }
}
