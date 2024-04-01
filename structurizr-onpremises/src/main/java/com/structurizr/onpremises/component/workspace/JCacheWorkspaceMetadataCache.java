package com.structurizr.onpremises.component.workspace;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import javax.cache.Cache;
import javax.cache.CacheManager;

/**
 * Workspace metadata cache implementation that uses an in-memory Ehcache cache, via the JCache API.
 */
abstract class JCacheWorkspaceMetadataCache implements WorkspaceMetadataCache {

    private static final Log log = LogFactory.getLog(JCacheWorkspaceMetadataCache.class);

    protected CacheManager cacheManager;
    protected Cache<Long, WorkspaceMetaData> cache;

    public final WorkspaceMetaData get(long workspaceId) {
        WorkspaceMetaData wmd = cache.get(workspaceId);
        if (wmd != null) {
            log.debug("Cache hit: " + workspaceId);
        } else {
            log.debug("Cache miss: " + workspaceId);
        }

        return wmd;
    }

    public final void put(WorkspaceMetaData workspaceMetaData) {
        if (workspaceMetaData != null) {
            cache.put(workspaceMetaData.getId(), workspaceMetaData);
        }
    }

    @Override
    public final void stop() {
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