package com.structurizr.onpremises.component.workspace;

import com.structurizr.onpremises.configuration.Configuration;
import com.structurizr.onpremises.configuration.StructurizrProperties;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import javax.cache.Caching;
import javax.cache.configuration.MutableConfiguration;
import javax.cache.expiry.CreatedExpiryPolicy;
import javax.cache.expiry.Duration;
import javax.cache.spi.CachingProvider;
import java.util.concurrent.TimeUnit;

/**
 * Workspace metadata cache implementation that uses an in-memory Ehcache cache, via the JCache API.
 */
class LocalWorkspaceMetadataCache extends JCacheWorkspaceMetadataCache {

    private static final Log log = LogFactory.getLog(LocalWorkspaceMetadataCache.class);

    private static final String CACHE_NAME = "structurizr.workspace.metadata";

    LocalWorkspaceMetadataCache() {
        int expiryInMinutes = Integer.parseInt(StructurizrProperties.DEFAULT_CACHE_EXPIRY_IN_MINUTES);
        try {
            expiryInMinutes = Integer.parseInt(Configuration.getInstance().getProperty(StructurizrProperties.CACHE_EXPIRY_IN_MINUTES));
        } catch (NumberFormatException nfe) {
            log.warn(nfe);
        }

        log.debug("Creating cache for workspace metadata: implementation=local; expiry=" + expiryInMinutes + " minute(s)");

        CachingProvider provider = Caching.getCachingProvider(org.ehcache.jsr107.EhcacheCachingProvider.class.getName());
        cacheManager = provider.getCacheManager();

        MutableConfiguration<Long, WorkspaceMetaData> configuration = new MutableConfiguration<Long, WorkspaceMetaData>()
                .setTypes(Long.class, WorkspaceMetaData.class)
                .setStoreByValue(false)
                .setExpiryPolicyFactory(CreatedExpiryPolicy.factoryOf(new Duration(TimeUnit.MINUTES, expiryInMinutes)));
        cache = cacheManager.createCache(CACHE_NAME, configuration);
    }

}