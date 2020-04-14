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

import java.util.HashSet;
import java.util.Set;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import io.hyscale.commons.exception.HyscaleException;
import io.hyscale.commons.logger.WorkflowLogger;
import io.hyscale.commons.validator.Validator;
import io.hyscale.controller.activity.ValidatorActivity;
import io.hyscale.controller.manager.RegistryManager;
import io.hyscale.controller.model.WorkflowContext;
import io.hyscale.servicespec.commons.fields.HyscaleSpecFields;
import io.hyscale.servicespec.commons.model.service.Image;
import io.hyscale.servicespec.commons.util.ImageUtil;

@Component
public class RegistryValidator implements Validator<WorkflowContext> {
	private static final Logger logger = LoggerFactory.getLogger(ClusterValidator.class);

	@Autowired
	private RegistryManager registryManager;
	
	private Set<String> registries=new HashSet<String>();


	/**
	 * 1. It will check that spec has buildspec or dockerfile 
	 * 2. If both is not then it will return true
	 * 3. If any one is there then 
	 *    3.1  It will fetch registry details
	 *    3.2  Then it will verify the registry
	 *    3.3  if registry is exist which is provided by user then return true else false
	 */
	@Override
	public boolean validate(WorkflowContext context) throws HyscaleException {
		logger.debug("Starting registry validation");
		if (!ImageUtil.isImageBuildPushRequired(context.getServiceSpec())) {
			return true;
		}
		Image image = context.getServiceSpec().get(HyscaleSpecFields.image, Image.class);
		boolean isRegistryAvailable = registries.contains(image.getRegistry());
		if (isRegistryAvailable) {
			return true;
		} else {
			isRegistryAvailable = registryManager.getImageRegistry(image.getRegistry()) != null ? true : false;
		}
		if (isRegistryAvailable) {
			registries.add(image.getRegistry());
			return true;
		} else {
			WorkflowLogger.persistError(ValidatorActivity.REGISTRY_VALIDATION, "Registry validation failed");
			return false;
		}
	}
}
