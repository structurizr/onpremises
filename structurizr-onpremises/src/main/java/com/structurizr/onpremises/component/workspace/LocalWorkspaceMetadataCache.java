package com.structurizr.onpremises.component.workspace;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import javax.cache.Cache;
import javax.cache.CacheManager;
import javax.cache.Caching;
import javax.cache.configuration.MutableConfiguration;
import javax.cache.expiry.CreatedExpiryPolicy;
import javax.cache.expiry.Duration;
import javax.cache.spi.CachingProvider;
import java.util.concurrent.TimeUnit;

/**
 * Workspace metadata cache implementation that uses an in-memory Ehcache cache, via the JCache API.
 */
class LocalWorkspaceMetadataCache implements WorkspaceMetadataCache {

    private static final Log log = LogFactory.getLog(LocalWorkspaceMetadataCache.class);

    private static final String CACHE_NAME = "structurizr.workspace.metadata";

    private final CacheManager cacheManager;
    private final Cache<Long, WorkspaceMetaData> cache;

    LocalWorkspaceMetadataCache(int expiryInMinutes) {
        CachingProvider provider = Caching.getCachingProvider();
        cacheManager = provider.getCacheManager();

        MutableConfiguration<Long, WorkspaceMetaData> configuration = new MutableConfiguration<Long, WorkspaceMetaData>()
                .setTypes(Long.class, WorkspaceMetaData.class)
                .setStoreByValue(false)
                .setExpiryPolicyFactory(CreatedExpiryPolicy.factoryOf(new Duration(TimeUnit.MINUTES, expiryInMinutes)));
        cache = cacheManager.createCache(CACHE_NAME, configuration);
    }

    public WorkspaceMetaData get(long workspaceId) {
        WorkspaceMetaData wmd = cache.get(workspaceId);
        if (wmd != null) {
            log.debug("Cache hit: " + workspaceId);
        } else {
            log.debug("Cache miss: " + workspaceId);
        }

        return wmd;
    }

    public void put(WorkspaceMetaData workspaceMetaData) {
        if (workspaceMetaData != null) {
            cache.put(workspaceMetaData.getId(), workspaceMetaData);
        }
    }

    @Override
    public void stop() {
        if (cacheManager != null) {
            try {
                cacheManager.close();
            } catch (Exception e) {
                log.warn(e);
            }
        }

        if (cache != null) {
            try {
                cache.close();
            } catch (Exception e) {
                log.warn(e);
            }
        }
    }

}