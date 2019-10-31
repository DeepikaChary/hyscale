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
package io.hyscale.schema.validator;

import com.fasterxml.jackson.databind.JsonNode;
import com.github.fge.jackson.JsonLoader;
import com.github.fge.jsonschema.core.exceptions.ProcessingException;
import com.github.fge.jsonschema.core.report.ProcessingReport;
import com.github.fge.jsonschema.main.JsonSchema;
import com.github.fge.jsonschema.main.JsonSchemaFactory;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import java.io.IOException;

/**
 * Created by vijays on 18/9/19.
 */
@Component
public class ServiceSpecValidator {

    private static final Logger LOGGER = LoggerFactory.getLogger(ServiceSpecValidator.class);
    private final JsonSchemaFactory factory = JsonSchemaFactory.byDefault();

    private static final String SERVICE_SCHEMA_FILE = "/v1.0.0-standalone/service-spec.json";

    public ProcessingReport validate(String inputServiceSpec) throws IOException, ProcessingException {

        final JsonNode serviceSpecSchemaJsonNode = JsonLoader.fromResource(SERVICE_SCHEMA_FILE);
        final JsonSchema schema = factory.getJsonSchema(serviceSpecSchemaJsonNode);

        final JsonNode inputServiceSpecJsonNode = JsonLoader.fromString(inputServiceSpec);
        return schema.validate(inputServiceSpecJsonNode);
    }

    public ProcessingReport validate(JsonNode inputServiceSchema) throws IOException, ProcessingException {

        final JsonNode serviceSpecSchemaJsonNode = JsonLoader.fromResource(SERVICE_SCHEMA_FILE);
        final JsonSchema schema = factory.getJsonSchema(serviceSpecSchemaJsonNode);

        return schema.validate(inputServiceSchema);
    }

}
