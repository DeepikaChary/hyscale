/**
 * Copyright 2019 Pramati Prism, Inc.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package io.hyscale.troubleshooting.integration.conditions;

import io.hyscale.commons.exception.HyscaleException;
import io.hyscale.deployer.core.model.ResourceKind;
import io.hyscale.deployer.services.model.PodStatus;
import io.hyscale.deployer.services.util.K8sPodUtil;
import io.hyscale.troubleshooting.integration.actions.FixCrashingApplication;
import io.hyscale.troubleshooting.integration.errors.TroubleshootErrorCodes;
import io.hyscale.troubleshooting.integration.models.*;
import io.hyscale.troubleshooting.integration.actions.FixHealthCheckAction;
import io.hyscale.troubleshooting.integration.util.ConditionUtil;
import io.kubernetes.client.models.V1Event;
import io.kubernetes.client.models.V1Pod;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import javax.annotation.PostConstruct;
import java.util.ArrayList;
import java.util.List;
import java.util.function.Predicate;
import java.util.stream.Collectors;

@Component
public class IsPodsReadinessFailing extends ConditionNode<TroubleshootingContext> {

    private static final Logger logger = LoggerFactory.getLogger(IsPodsReadinessFailing.class);

    private static final String UNHEALTHY_REASON = "Unhealthy";

    @Autowired
    private FixHealthCheckAction fixHealthCheckAction;

    @Autowired
    private FixCrashingApplication fixCrashingApplication;

    @Override
    public boolean decide(TroubleshootingContext context) throws HyscaleException {
        List<TroubleshootingContext.ResourceInfo> resourceInfos = context.getResourceInfos().get(ResourceKind.POD.getKind());
        DiagnosisReport report = new DiagnosisReport();
        if (resourceInfos == null || resourceInfos.isEmpty()) {
            report.setReason(AbstractedErrorMessage.SERVICE_NOT_DEPLOYED.formatReason(context.getServiceInfo().getServiceName()));
            report.setRecommendedFix(AbstractedErrorMessage.SERVICE_NOT_DEPLOYED.getMessage());
            context.addReport(report);
            throw new HyscaleException(TroubleshootErrorCodes.SERVICE_IS_NOT_DEPLOYED, context.getServiceInfo().getServiceName());
        }

        List<V1Event> v1Events = null;
        Object obj = context.getAttribute(FailedResourceKey.UNREADY_POD);
        v1Events = new ArrayList<>();
        V1Pod unhealthyPod = null;
        if (obj != null) {
            unhealthyPod = (V1Pod) FailedResourceKey.UNREADY_POD.getKlazz().cast(obj);
        }
        // Get all the events of the unhealthy pod from previous conditionNode or fetch it from the existing
        // set of pods
        for (TroubleshootingContext.ResourceInfo resourceInfo : resourceInfos) {
            if (resourceInfo != null && resourceInfo.getResource() != null && resourceInfo.getResource() instanceof V1Pod) {
                V1Pod pod = (V1Pod) resourceInfo.getResource();
                if (unhealthyPod == null) {
                    PodStatus status = PodStatus.get(K8sPodUtil.getAggregatedStatusOfContainersForPod(pod));
                    if (status != null || !status.isFailed()) {
                        v1Events.addAll(resourceInfo.getEvents());
                    }
                } else {
                    if (unhealthyPod.getMetadata().getName().equals(pod.getMetadata().getName())) {
                        v1Events.addAll(resourceInfo.getEvents());
                    }
                }
            }
        }

        if (v1Events == null && v1Events.isEmpty()) {
            report.setReason(AbstractedErrorMessage.CANNOT_FIND_EVENTS.formatReason(context.getServiceInfo().getServiceName()));
            report.setRecommendedFix(AbstractedErrorMessage.CANNOT_FIND_EVENTS.getMessage());
            context.addReport(report);
            return false;
        }
        return v1Events.stream().
                anyMatch(each -> {
                    if (UNHEALTHY_REASON.equals(each.getReason())) {
                        context.addAttribute(FailedResourceKey.UNHEALTHY_POD_EVENT, each);
                        return true;
                    }
                    return false;
                });
    }

    @Override
    public Node<TroubleshootingContext> onSuccess() {
        return fixHealthCheckAction;
    }

    @Override
    public Node<TroubleshootingContext> onFailure() {
        return fixCrashingApplication;
    }

    @Override
    public String describe() {
        return "Readiness failing ?";
    }

}
