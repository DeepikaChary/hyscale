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
package io.hyscale.generator.services.plugins;

import com.fasterxml.jackson.core.JsonProcessingException;
import io.hyscale.plugin.framework.annotation.ManifestPlugin;
import io.hyscale.commons.config.SetupConfig;
import io.hyscale.commons.constants.ToolConstants;
import io.hyscale.commons.exception.HyscaleException;
import io.hyscale.commons.models.ManifestContext;
import io.hyscale.commons.utils.HyscaleFilesUtil;
import io.hyscale.generator.services.model.ManifestResource;
import io.hyscale.generator.services.model.AppMetaData;
import io.hyscale.generator.services.predicates.ManifestPredicates;
import io.hyscale.generator.services.provider.PropsProvider;
import io.hyscale.plugin.framework.handler.ManifestHandler;
import io.hyscale.plugin.framework.models.ManifestSnippet;
import io.hyscale.servicespec.commons.fields.HyscaleSpecFields;
import io.hyscale.servicespec.commons.model.PropType;
import io.hyscale.servicespec.commons.model.service.Props;
import io.hyscale.servicespec.commons.model.service.ServiceSpec;
import io.hyscale.plugin.framework.util.JsonSnippetConvertor;
import org.apache.commons.codec.binary.Base64;
import org.apache.commons.io.IOUtils;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.*;

@Component
@ManifestPlugin(name = "ConfigMapDataHandler")
public class ConfigMapDataHandler implements ManifestHandler {

    private static final Logger logger = LoggerFactory.getLogger(ConfigMapDataHandler.class);

    @Autowired
    private HyscaleFilesUtil filesUtil;

    @Override
    public List<ManifestSnippet> handle(ServiceSpec serviceSpec, ManifestContext manifestContext) throws HyscaleException {
        Props props = PropsProvider.getProps(serviceSpec);
        if (!ManifestPredicates.getPropsPredicate().test(serviceSpec)) {
            logger.debug("Props found to be empty while processing ConfigMap data.");
            return null;
        }
        AppMetaData appMetaData = new AppMetaData();
        appMetaData.setAppName(manifestContext.getAppName());
        appMetaData.setEnvName(manifestContext.getEnvName());
        appMetaData.setServiceName(serviceSpec.get(HyscaleSpecFields.name, String.class));

        String propsVolumePath = serviceSpec.get(HyscaleSpecFields.propsVolumePath, String.class);

        List<ManifestSnippet> manifestSnippetList = new ArrayList<>();
        try {
            manifestSnippetList.addAll(getConfigMapData(props, propsVolumePath, appMetaData));
            logger.debug("Added ConfigMap map data to the manifest snippet list");
        } catch (JsonProcessingException e) {
            logger.error("Error while generating manifest for props of service {}", appMetaData.getServiceName(), e);
        }
        return manifestSnippetList;
    }

    private List<ManifestSnippet> getConfigMapData(Props props, String propsVolumePath, AppMetaData metaDataContext)
            throws JsonProcessingException, HyscaleException {
        List<ManifestSnippet> manifestSnippets = new LinkedList<>();
        Map<String, String> configProps = new HashMap<>();
        StringBuilder sb = new StringBuilder();
        props.getProps().entrySet().stream().forEach(each -> {
            String value = each.getValue();
            if (PropType.FILE.getPatternMatcher().matcher(value).matches()) {
                String fileContent = null;
                try (InputStream is = new FileInputStream(SetupConfig.getAbsolutePath(PropType.FILE.extractPropValue(value)))) {
                    fileContent = IOUtils.toString(is, ToolConstants.CHARACTER_ENCODING);
                    logger.debug(" Adding file {} to config props.", value);
                    configProps.put(each.getKey(), Base64.encodeBase64String(fileContent.getBytes()));
                } catch (IOException e) {
                    logger.error("Error while reading file content of config prop {}", each.getKey(), e);
                }
            } else if (PropType.ENDPOINT.getPatternMatcher().matcher(value).matches()) {
                String propValue = PropType.ENDPOINT.extractPropValue(each.getValue());
                configProps.put(each.getKey(), propValue);
                logger.debug(" Adding endpoint {} to config props.", value);
                sb.append(each.getKey()).append("=").append(propValue).append("\n");
            } else {
                String propValue = PropType.STRING.extractPropValue(each.getValue());
                configProps.put(each.getKey(), propValue);
                logger.debug(" Adding prop {} to config props.", value);
                sb.append(each.getKey()).append("=").append(propValue).append("\n");
            }
        });

        String fileData = sb.toString();
        if (StringUtils.isNotBlank(fileData) && StringUtils.isNotBlank(propsVolumePath)) {
            logger.debug("Processing props file data.");
            configProps.put(filesUtil.getFileName(propsVolumePath), fileData);
        }

        ManifestSnippet configMapDataSnippet = new ManifestSnippet();
        configMapDataSnippet.setKind(ManifestResource.CONFIG_MAP.getKind());
        configMapDataSnippet.setPath("data");
        configMapDataSnippet.setSnippet(JsonSnippetConvertor.serialize(configProps));
        manifestSnippets.add(configMapDataSnippet);
        return manifestSnippets;
    }

}
