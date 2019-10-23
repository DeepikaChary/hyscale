package io.hyscale.generator.services.plugins;

import com.fasterxml.jackson.core.JsonProcessingException;
import io.hyscale.plugin.framework.annotation.ManifestPlugin;
import io.hyscale.commons.exception.HyscaleException;
import io.hyscale.commons.models.ManifestContext;
import io.hyscale.generator.services.model.ManifestResource;
import io.hyscale.generator.services.model.MetaDataContext;
import io.hyscale.generator.services.generator.MetadatManifestSnippetGenerator;
import io.hyscale.plugin.framework.handler.ManifestHandler;
import io.hyscale.plugin.framework.models.ManifestSnippet;
import io.hyscale.servicespec.commons.fields.HyscaleSpecFields;
import io.hyscale.servicespec.commons.model.service.ServiceSpec;
import io.kubernetes.client.models.V1ObjectMeta;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;

@Component
@ManifestPlugin(name = "MetaDataHandler")
public class MetaDataHandler implements ManifestHandler {

    private static final Logger logger = LoggerFactory.getLogger(MetaDataHandler.class);

    @Override
    public List<ManifestSnippet> handle(ServiceSpec serviceSpec, ManifestContext context) throws HyscaleException {
        MetaDataContext metaDataContext = new MetaDataContext();
        metaDataContext.setAppName(context.getAppName());
        metaDataContext.setEnvName(context.getEnvName());
        metaDataContext.setServiceName(serviceSpec.get(HyscaleSpecFields.name, String.class));
        List<ManifestSnippet> snippetList = new ArrayList<>();
        try {
            for (ManifestResource manifestResource : ManifestResource.values()) {

                if (manifestResource.getPredicate().test(serviceSpec)) {
                    logger.debug("Creating metadata for resource {} ",manifestResource.getKind());
                    /* Snippet for kind for each manifest */
                    snippetList.add(MetadatManifestSnippetGenerator.getKind(manifestResource));

                    /* Snippet for apiVersion for each manifest */
                    snippetList.add(MetadatManifestSnippetGenerator.getApiVersion(manifestResource, metaDataContext));

                    /* Snippet for metadata for each manifest */
                    snippetList.add(MetadatManifestSnippetGenerator.getMetaData(manifestResource, metaDataContext));
                }

            }
        } catch (JsonProcessingException e) {
            logger.error("Error while serializing metadata snippet ", e);
        }
        return snippetList;
    }

    public V1ObjectMeta getMetaData(ManifestResource manifestResource, MetaDataContext metaDataContext) {
        V1ObjectMeta v1ObjectMeta = new V1ObjectMeta();
        v1ObjectMeta.setName(manifestResource.getName(metaDataContext));
        v1ObjectMeta.setLabels(manifestResource.getLabels(metaDataContext));
        return v1ObjectMeta;
    }


}
