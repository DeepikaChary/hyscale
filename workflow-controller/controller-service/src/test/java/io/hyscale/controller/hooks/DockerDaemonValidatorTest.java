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
package io.hyscale.controller.hooks;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.junit.jupiter.api.Assertions.fail;

import java.util.stream.Stream;

import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import org.mockito.Mockito;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;

import io.hyscale.commons.exception.HyscaleException;
import io.hyscale.controller.model.WorkflowContext;
import io.hyscale.controller.validator.impl.DockerDaemonValidator;

@SpringBootTest
public class DockerDaemonValidatorTest {

    @MockBean
	private DockerDaemonValidator dockerDaemonValidator;
    
    @MockBean
    private WorkflowContext context;

	public static Stream<Arguments> input() {
		WorkflowContext context = new WorkflowContext();
		return Stream.of(Arguments.of(context));
	}
	

	@ParameterizedTest
	@MethodSource(value = "input")
	void testValidateForTrue(WorkflowContext context) {
		try {
			Mockito.when(dockerDaemonValidator.validate(context)).thenReturn(true);
			assertTrue(dockerDaemonValidator.validate(context));
		} catch (HyscaleException e) {
			fail();
		}
		
	}

	@ParameterizedTest
	@MethodSource(value = "input")
	void testValidateForFalse(WorkflowContext context) {
		try {
			Mockito.when(dockerDaemonValidator.validate(context)).thenReturn(false);
			assertFalse(dockerDaemonValidator.validate(context));
		} catch (HyscaleException e) {
			fail();
		}
	}

}
