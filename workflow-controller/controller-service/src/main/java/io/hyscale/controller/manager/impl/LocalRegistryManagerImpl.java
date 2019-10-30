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
package io.hyscale.controller.manager.impl;

import java.io.File;
import java.io.IOException;
import java.util.Base64;

import javax.annotation.PostConstruct;

import io.hyscale.commons.config.SetupConfig;
import io.hyscale.commons.logger.WorkflowLogger;
import io.hyscale.controller.activity.ControllerActivity;
import io.hyscale.controller.config.ControllerConfig;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;

import io.hyscale.commons.constants.ToolConstants;
import io.hyscale.commons.exception.HyscaleException;
import io.hyscale.commons.models.Auth;
import io.hyscale.commons.models.DockerConfig;
import io.hyscale.commons.models.ImageRegistry;
import io.hyscale.commons.utils.ObjectMapperFactory;
import io.hyscale.controller.core.exception.ControllerErrorCodes;
import io.hyscale.controller.manager.RegistryManager;

/**
 * Reads local docker registry config
 */
@Component
public class LocalRegistryManagerImpl implements RegistryManager {

    private static final Logger logger = LoggerFactory.getLogger(LocalRegistryManagerImpl.class);

    private static DockerConfig dockerConfig = new DockerConfig();

    @Autowired
    private ControllerConfig controllerConfig;

    @PostConstruct
    public void init() throws HyscaleException {
        ObjectMapper mapper = ObjectMapperFactory.jsonMapper();
        try {
            TypeReference<DockerConfig> dockerConfigTypeReference = new TypeReference<DockerConfig>() {
            };

            dockerConfig = mapper.readValue(new File(controllerConfig.getDefaultRegistryConf()),
                    dockerConfigTypeReference);

        } catch (IOException e) {
            String dockerConfPath = SetupConfig.getMountOfDockerConf(controllerConfig.getDefaultRegistryConf());
            WorkflowLogger.error(ControllerActivity.ERROR_WHILE_READING, dockerConfPath, e.getMessage());
            HyscaleException ex = new HyscaleException(e, ControllerErrorCodes.DOCKER_CONFIG_NOT_FOUND, dockerConfPath);
            logger.error("Error while deserializing image registries {}", ex.toString());
            throw ex;
        }
    }

    /**
     * gives image registry details based on registry name
     */
    @Override
    public ImageRegistry getImageRegistry(String registry) throws HyscaleException {
        if (StringUtils.isBlank(registry)) {
            logger.debug("Image push not required");
            return null;
        }
        Auth auth = dockerConfig.getAuths().get(registry);
        if (auth == null) {
            return null;
        }
        return getPrivateRegistry(auth, registry);
    }

    private ImageRegistry getPrivateRegistry(Auth auth, String url) {
        String encodedAuth = auth.getAuth();
        if (StringUtils.isBlank(encodedAuth)) {
            return null;
        }
        // Format username:password
        String decodedAuth = new String(Base64.getDecoder().decode(encodedAuth));
        String[] authArray = decodedAuth.split(ToolConstants.COLON);
        ImageRegistry imageRegistry = new ImageRegistry();
        imageRegistry.setUrl(url);
        imageRegistry.setUserName(authArray[0]);
        imageRegistry.setPassword(authArray[1]);

        return imageRegistry;
    }

}
