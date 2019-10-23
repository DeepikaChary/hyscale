package io.hyscale.controller.config;

import java.io.File;

import javax.annotation.PostConstruct;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.PropertySource;
import org.springframework.stereotype.Component;

import io.hyscale.commons.config.SetupConfig;
import io.hyscale.commons.exception.HyscaleErrorCode;
import io.hyscale.commons.exception.HyscaleException;
import io.hyscale.commons.logger.WorkflowLogger;
import io.hyscale.controller.activity.ControllerActivity;
import io.hyscale.controller.core.exception.ControllerErrorCodes;

/**
 * Controller level config details such as docker registry config, kube config
 *
 */
@PropertySource("classpath:config/controller-config.props")
@Component
public class ControllerConfig {

    private static final Logger logger = LoggerFactory.getLogger(ControllerConfig.class);

    @Value(("${io.hyscale.default.registry.conf}"))
    private String defaultRegistryConfAsString;

    @Value(("${io.hyscale.default.kube.conf}"))
    private String defaultKubeConfAsString;

    private String defaultRegistryConf;
    private String defaultKubeConf;

    @Autowired
    private SetupConfig setupConfig;

    @PostConstruct
    public void init() throws HyscaleException {
        this.defaultKubeConf = SetupConfig.USER_HOME_DIR + SetupConfig.FILE_SEPARATOR + defaultKubeConfAsString;
        this.defaultRegistryConf = SetupConfig.USER_HOME_DIR + SetupConfig.FILE_SEPARATOR + defaultRegistryConfAsString;
        validate(defaultRegistryConf, false, ControllerErrorCodes.DOCKER_CONFIG_NOT_FOUND);
        validate(defaultKubeConf, true, ControllerErrorCodes.KUBE_CONFIG_NOT_FOUND);
    }

    private void validate(String path, boolean kubeConf, HyscaleErrorCode hyscaleErrorCode) throws HyscaleException {
        File conffile = new File(path);
        if (conffile != null && !conffile.exists()) {
            String confpath = kubeConf ? SetupConfig.getMountPathOfKubeConf(path) : SetupConfig.getMountOfDockerConf(path);
            WorkflowLogger.error(ControllerActivity.CANNOT_FIND_FILE,
                    confpath);
            throw new HyscaleException(hyscaleErrorCode, confpath);
        }
    }

    public String getDefaultRegistryConf() {
        logger.debug("Using default Regsitry Config {}", defaultRegistryConf);
        return defaultRegistryConf;
    }

    public String getDefaultKubeConf() {
        logger.debug("Using kubeconfig from file {}", defaultKubeConf);
        return defaultKubeConf;
    }


}
