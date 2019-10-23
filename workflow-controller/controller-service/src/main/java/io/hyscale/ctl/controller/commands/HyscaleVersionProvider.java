package io.hyscale.ctl.controller.commands;

import org.apache.commons.lang.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.info.BuildProperties;
import org.springframework.stereotype.Component;

import io.hyscale.ctl.commons.constants.ToolConstants;
import picocli.CommandLine;

/**
 * Provides version when 'hyscale version' command
 * is executed
 */
@Component
public class HyscaleVersionProvider implements CommandLine.IVersionProvider {

    @Autowired
    BuildProperties buildProperties;

    @Override
    public String[] getVersion() throws Exception {
        StringBuilder sb = new StringBuilder();
        sb.append(ToolConstants.VERSION_KEY).append(buildProperties.getVersion());
        sb.append(ToolConstants.LINE_SEPARATOR);
        String buildDate = buildProperties.get(ToolConstants.HYSCALE_BUILD_TIME);
        sb.append(ToolConstants.BUILDDATE_KEY);
        if (StringUtils.isNotBlank(buildDate)) {
            sb.append(buildDate);
        } else {
            sb.append(buildProperties.getTime());
        }
        return new String[]{sb.toString()};
    }
}
