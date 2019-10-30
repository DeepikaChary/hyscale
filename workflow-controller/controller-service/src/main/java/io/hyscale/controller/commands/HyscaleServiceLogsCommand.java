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
package io.hyscale.controller.commands;

import javax.validation.constraints.Max;
import javax.validation.constraints.Min;

import io.hyscale.controller.activity.ControllerActivity;
import io.hyscale.controller.constants.WorkflowConstants;
import io.hyscale.controller.model.WorkflowContext;
import io.hyscale.controller.util.CommandUtil;
import io.hyscale.controller.util.LoggerUtility;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import io.hyscale.commons.logger.WorkflowLogger;
import picocli.CommandLine.Command;
import picocli.CommandLine.Option;

/**
 *  This class executes the 'hyscale get service logs' command
 *  It is a sub-command of the 'hyscale get service' command
 *  @see HyscaleGetServiceCommand .
 *  Every command/sub-command has to implement the Runnable so tha
 *  whenever the command is executed the {@link #run()}
 *  method will be invoked
 *
 * @option serviceName name of the service
 * @option namespace  namespace in which the app is deployed
 * @option appName   name of the app
 * @option tail  enable this option to tail the logs
 * @option line  last 'n' number of lines are retrieved from the service
 *
 * Eg: hyscale get service logs -s s1 -n dev -a sample
 *
 * Fetches the pod logs from the given cluster
 *
 */

@Command(name = "logs", aliases = { "log" }, description = "Displays the service logs")
@Component
public class HyscaleServiceLogsCommand implements Runnable {

	private static final Logger LOGGER = LoggerFactory.getLogger(HyscaleServiceLogsCommand.class);

	@Option(names = { "-h", "--help" }, usageHelp = true, description = "Displays help information for the specified command")
	private boolean helpRequested = false;

	@Option(names = { "-n", "--namespace", "-ns" }, required = true, description = "Namespace of the service")
	private String namespace;

	@Option(names = { "-a", "--app" }, required = true, description = "Application name")
	private String appName;

	@Option(names = { "-s", "--service" }, required = true, description = "Service name")
	private String serviceName;

	@Option(names = { "-t", "--tail" }, required = false, description = "Tail output of the service logs")
	private boolean tail = false;

	@Min(value = 1, message = "Logs Lines must not be less than 1")
	@Max(value = 500, message = "Logs Lines must not be more than 500")
	@Option(names = { "-l", "--line" }, required = false, description = "Number of lines of logs")
	private Integer line = 100;

	@Autowired
	private LoggerUtility loggerUtility;

	@Override
	public void run() {
		if (!CommandUtil.isInputValid(this)) {
			System.exit(1);
		}
		
		WorkflowContext workflowContext = new WorkflowContext();
		WorkflowLogger.header(ControllerActivity.SERVICE_NAME, serviceName);
		workflowContext.setAppName(appName.trim());
		workflowContext.setNamespace(namespace.trim());
		workflowContext.setServiceName(serviceName);
		workflowContext.addAttribute(WorkflowConstants.TAIL_LOGS, tail);
		workflowContext.addAttribute(WorkflowConstants.LINES, line);

		loggerUtility.getLogs(workflowContext);

	}

}
