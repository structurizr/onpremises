package com.structurizr.onpremises.component.workspace;

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

    private static final String CACHE_NAME = "structurizr.workspace.metadata";

    LocalWorkspaceMetadataCache(int expiryInMinutes) {
        CachingProvider provider = Caching.getCachingProvider(org.ehcache.jsr107.EhcacheCachingProvider.class.getName());
        cacheManager = provider.getCacheManager();

        MutableConfiguration<Long, WorkspaceMetaData> configuration = new MutableConfiguration<Long, WorkspaceMetaData>()
                .setTypes(Long.class, WorkspaceMetaData.class)
                .setStoreByValue(false)
                .setExpiryPolicyFactory(CreatedExpiryPolicy.factoryOf(new Duration(TimeUnit.MINUTES, expiryInMinutes)));
        cache = cacheManager.createCache(CACHE_NAME, configuration);
    }

}