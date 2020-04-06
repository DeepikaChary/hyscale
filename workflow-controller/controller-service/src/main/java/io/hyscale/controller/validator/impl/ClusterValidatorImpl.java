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

import io.hyscale.commons.exception.HyscaleException;
import io.hyscale.controller.builder.K8sAuthConfigBuilder;
import io.hyscale.controller.model.WorkflowContext;
import io.hyscale.controller.validator.ClusterValidator;

public class ClusterValidatorImpl implements ClusterValidator{
  private static final Logger logger = LoggerFactory.getLogger(ClusterValidatorImpl.class);

    @Autowired
    private K8sAuthConfigBuilder authConfigBuilder;

	@Override
	public boolean validate(WorkflowContext t) throws HyscaleException {
		  logger.debug("Starting K8s cluster validation");
	        authConfigBuilder.getAuthConfig();
		return false;
	}

}
