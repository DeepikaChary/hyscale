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
package io.hyscale.controller.validator.impl;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import io.hyscale.commons.exception.HyscaleException;
import io.hyscale.commons.logger.WorkflowLogger;
import io.hyscale.commons.models.Status;
import io.hyscale.commons.validator.Validator;
import io.hyscale.controller.activity.ValidatorActivity;
import io.hyscale.controller.model.WorkflowContext;
import io.hyscale.deployer.services.deployer.Deployer;

@Component
public class ClusterValidator implements Validator<WorkflowContext> {
    
	private static final Logger logger = LoggerFactory.getLogger(ClusterValidator.class);

	@Autowired
	private Deployer deployer;
	
	private boolean isClusterValidated = false;

	/**
	 * 1. It will try to connect to the cluster 
	 * 2. It will take cluster details which is provided by user
	 * 3. Try to fetch V1APIResourceList from cluster
	 * 4. If  V1APIResourceList null then it will return true otherwise false
	 */
	@Override
	public boolean validate(WorkflowContext context) throws HyscaleException {
	    if (isClusterValidated) {
	        return isClusterValidated;
	    }
	    WorkflowLogger.startActivity(ValidatorActivity.VALIDATING_CLUSTER);
		logger.debug("Starting K8s cluster validation");
		boolean isClusterValid = false;
		try {
		    isClusterValid = deployer.authenticate(context.getAuthConfig());
		} catch(HyscaleException ex) {
		    WorkflowLogger.endActivity(Status.FAILED);
		    throw ex;
		}
		if (isClusterValid) {
		    WorkflowLogger.endActivity(Status.DONE);
		    isClusterValidated = true;
		}
		if (!isClusterValid) {
		    WorkflowLogger.endActivity(Status.FAILED);
		}
		return isClusterValid;
	}
}
