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
package io.hyscale.controller.directive;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.fail;

import java.io.IOException;

import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import com.fasterxml.jackson.databind.node.ObjectNode;

import io.hyscale.commons.exception.HyscaleException;
import io.hyscale.commons.utils.WindowsUtil;
import io.hyscale.controller.directive.impl.DockerfileJsonHandler;
import io.hyscale.controller.util.ServiceSpecTestUtil;
import io.hyscale.servicespec.commons.fields.HyscaleSpecFields;
import io.hyscale.servicespec.commons.model.service.Dockerfile;
import io.hyscale.servicespec.commons.model.service.ServiceSpec;

public class DockerfileJsonHandlerTest {

    private static final String FILEPATH = "/servicespecs/dockerfile_update.hspec.yaml";

    private static final String JSON_PATH = HyscaleSpecFields.getPath(HyscaleSpecFields.image,
            HyscaleSpecFields.dockerfile);

    private static DockerfileJsonHandler dockerfileHandler = new DockerfileJsonHandler();

    private static ObjectNode serviceSpecNode = null;

    private static ServiceSpec oldServiceSpec = null;

    private static ServiceSpec updatedServiceSpec = null;

    @BeforeAll
    public static void beforeAll() {
        try {
            oldServiceSpec = ServiceSpecTestUtil.getServiceSpec(FILEPATH);
            serviceSpecNode = (ObjectNode) ServiceSpecTestUtil.getServiceSpecJsonNode(FILEPATH);
            dockerfileHandler.update(serviceSpecNode);
            updatedServiceSpec = new ServiceSpec(serviceSpecNode);
        } catch (IOException | HyscaleException e) {
            fail();
        }
    }

    @Test
    public void updateDockerfile() {
        Dockerfile oldDockerfile = null;
        Dockerfile updatedDockerfile = null;
        try {
            oldDockerfile = oldServiceSpec.get(JSON_PATH, Dockerfile.class);
            updatedDockerfile = updatedServiceSpec.get(JSON_PATH, Dockerfile.class);
        } catch (HyscaleException e) {
            fail();
        }

        assertEquals(WindowsUtil.updateToUnixFileSeparator(oldDockerfile.getPath()), updatedDockerfile.getPath());
        assertEquals(WindowsUtil.updateToUnixFileSeparator(oldDockerfile.getDockerfilePath()),
                updatedDockerfile.getDockerfilePath());
    }
}
