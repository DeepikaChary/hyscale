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
package io.hyscale.controller.plugins;

import io.hyscale.commons.component.InvokerHook;
import io.hyscale.commons.exception.HyscaleException;
import io.hyscale.controller.core.exception.ControllerErrorCodes;
import io.hyscale.controller.model.WorkflowContext;
import io.hyscale.dockerfile.gen.services.exception.DockerfileErrorCodes;
import io.hyscale.servicespec.commons.fields.HyscaleSpecFields;
import io.hyscale.servicespec.commons.model.service.Artifact;
import io.hyscale.servicespec.commons.model.service.BuildSpec;
import io.hyscale.servicespec.commons.model.service.ServiceSpec;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import java.util.List;

/**
 * 	Hook to validate {@link BuildSpec} before docker file generation
 *
 */
@Component
public class BuildSpecValidatorHook implements InvokerHook<WorkflowContext> {

    private static final Logger logger = LoggerFactory.getLogger(BuildSpecValidatorHook.class);

    @Override
    public void preHook(WorkflowContext context) throws HyscaleException {
        logger.debug("Executing BuildSpecValidatorHook");
        ServiceSpec serviceSpec = context.getServiceSpec();
        if (serviceSpec == null) {
            logger.debug("Cannot service spec at BuildSpec validator plugin ");
            throw new HyscaleException(ControllerErrorCodes.SERVICE_SPEC_REQUIRED);
        }

        BuildSpec buildSpec = serviceSpec.get(
                HyscaleSpecFields.getPath(HyscaleSpecFields.image, HyscaleSpecFields.buildSpec), BuildSpec.class);
        if (buildSpec != null) {
            String stackImageName = buildSpec.getStackImage();
            if (StringUtils.isBlank(stackImageName)) {
                throw new HyscaleException(DockerfileErrorCodes.CANNOT_RESOLVE_STACK_IMAGE);
            }

            List<Artifact> artifactList = buildSpec.getArtifacts();

            boolean validate = true;
            if (artifactList != null && !artifactList.isEmpty()) {
                for (Artifact each : artifactList) {
                    validate = validate &&
                            StringUtils.isNotBlank(each.getName()) &&
                            StringUtils.isNotBlank(each.getSource()) &&
                            StringUtils.isNotBlank(each.getDestination());
                    if (!validate) {
                        throw new HyscaleException(DockerfileErrorCodes.ARTIFACTS_FOUND_INVALID_IN_SERVICE_SPEC, each.getName());
                    }
                }
            }
        }
    }

    @Override
    public void postHook(WorkflowContext context) throws HyscaleException {

    }

    @Override
    public void onError(WorkflowContext context, Throwable th) {
        context.setFailed(true);
    }
}
