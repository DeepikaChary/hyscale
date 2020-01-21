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

import java.io.File;
import java.util.concurrent.Callable;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import io.hyscale.controller.constants.WorkflowConstants;
import io.hyscale.controller.model.WorkflowContext;
import io.hyscale.controller.util.CommandUtil;
import io.hyscale.controller.util.ServiceProfileUtil;
import io.hyscale.controller.util.ServiceSpecMapper;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import io.hyscale.commons.config.SetupConfig;
import io.hyscale.commons.constants.ToolConstants;
import io.hyscale.commons.constants.ValidationConstants;
import io.hyscale.commons.exception.HyscaleException;
import io.hyscale.commons.logger.WorkflowLogger;
import io.hyscale.controller.activity.ControllerActivity;
import io.hyscale.controller.invoker.ManifestGeneratorComponentInvoker;
import io.hyscale.servicespec.commons.model.service.ServiceSpec;
import picocli.CommandLine;

import javax.annotation.PreDestroy;
import javax.validation.constraints.Pattern;

/**
 * This class executes 'hyscale generate service manifests' command
 * It is a sub-command of the 'hyscale generate service' command
 * @see HyscaleGenerateServiceCommand
 * Every command/sub-command has to implement the Runnable so that
 * whenever the command is executed the {@link #run()}
 * method will be invoked
 *
 * @option appName  name of the app
 * @option serviceSpecs  list of service specs
 * @option profiles  list of profiles for services
 *
 * Eg: hyscale generate service manifests -f s1.hspec.yaml -f s2.hspec.yaml -p p1-s1.hprof.yaml -a sample
 *
 * Generates the manifests from the given hspec and writes the manifests
 * to <USER.HOME/hyscale/apps/[<appName]/[serviceName]/generated-files/manifests/
 *
 *
 */
@CommandLine.Command(name = "manifests", aliases = {"manifest"},
        description = {"Generates manifests from the given service specs"})
@Component
public class HyscaleGenerateServiceManifestsCommand implements Callable<Integer> {
    
    private static final Logger logger = LoggerFactory.getLogger(HyscaleGenerateServiceManifestsCommand.class);

    @CommandLine.Option(names = {"-h", "--help"}, usageHelp = true, description = "Display help message about the specified command")
    private boolean helpRequested = false;

    @Pattern(regexp = ValidationConstants.APP_NAME_REGEX, message = ValidationConstants.INVALID_APP_NAME_MSG)
    @CommandLine.Option(names = {"-a", "--app"}, required = true, description = "Application name")
    private String appName;

    @CommandLine.Option(names = {"-f", "--files"}, required = true, description = "Service specs files.", split = ",")
    private List<
    @Pattern(regexp = ValidationConstants.SERVICE_SPEC_NAME_REGEX, message = ValidationConstants.INVALID_SERVICE_SPEC_NAME_MSG)
    String> serviceSpecs;
    
    @CommandLine.Option(names = {"-p", "--profile"}, required = false, description = "Profile for service.", split = ",")
    private List<
    @Pattern(regexp = ValidationConstants.PROFILE_FILENAME_REGEX, message = ValidationConstants.INVALID_PROFILE_FILE_NAME_MSG)
    String> profiles;

    @Autowired
    private ManifestGeneratorComponentInvoker manifestGeneratorComponentInvoker;
    
    @Autowired
    private ServiceSpecMapper serviceSpecMapper;

    @Override
    public Integer call() throws Exception {
        if (!CommandUtil.isInputValid(this)) {
            return ToolConstants.INVALID_INPUT_ERROR_CODE;
        }
        
        Map<String, String> serviceProfileMap = new HashMap<String, String>();
        try {
            serviceProfileMap = ServiceProfileUtil.getServiceProfileMap(profiles);
        } catch (HyscaleException e) {
            WorkflowLogger.error(ControllerActivity.CANNOT_PROCESS_SERVICE_PROFILE, e.getMessage());
            throw e;
        }
        boolean isFailed = false;
        for (String serviceSpecPath : serviceSpecs) {

            WorkflowContext workflowContext = new WorkflowContext();
            String serviceName = ServiceProfileUtil.getServiceName(serviceSpecPath);
            String profilePath = serviceProfileMap.remove(serviceName);
            try {
                File serviceSpecFile = new File(serviceSpecPath);
                File profileFile = null;
                if (profilePath != null) {
                    profileFile = new File(profilePath);
                }
                ServiceSpec serviceSpec = serviceSpecMapper.from(serviceSpecFile, profileFile);
                workflowContext.setServiceSpec(serviceSpec);
                workflowContext.setServiceName(serviceName);

                SetupConfig.clearAbsolutePath();
                SetupConfig.setAbsolutePath(serviceSpecFile.getAbsoluteFile().getParent());

            } catch (HyscaleException e) {
                WorkflowLogger.error(ControllerActivity.CANNOT_PROCESS_SERVICE_SPEC, e.getMessage());
                throw e;
            }

            workflowContext.setAppName(appName.trim());
            workflowContext.setEnvName(CommandUtil.getEnvName(profilePath, appName.trim()));

            try {
                manifestGeneratorComponentInvoker.execute(workflowContext);
            } catch (HyscaleException e) {
                logger.error("Error while generating manifest for app: {}, service: {}", appName, serviceName, e);
                isFailed = true;
            }
            if (workflowContext.isFailed()) {
                isFailed = true;
            }
            WorkflowLogger.footer();
            WorkflowLogger.logPersistedActivities();
            CommandUtil.logMetaInfo(SetupConfig.getMountPathOf((String) workflowContext.getAttribute(WorkflowConstants.MANIFESTS_PATH)),
                    ControllerActivity.MANIFESTS_GENERATION_PATH);
        }

        if (!serviceProfileMap.isEmpty()) {
            // unused profiles
            String services = serviceProfileMap.keySet().toString();
            WorkflowLogger.footer();
            WorkflowLogger.warn(ControllerActivity.NO_SERVICE_FOUND_FOR_PROFILE, services);
            WorkflowLogger.footer();
        }
        return isFailed ? ToolConstants.HYSCALE_ERROR_CODE : 0;
    }

    @PreDestroy
    public void clear() {
        SetupConfig.clearAbsolutePath();
    }
}
