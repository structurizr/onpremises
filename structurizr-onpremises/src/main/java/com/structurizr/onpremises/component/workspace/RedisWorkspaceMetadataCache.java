package com.structurizr.onpremises.component.workspace;

import com.structurizr.util.StringUtils;
import org.redisson.Redisson;
import org.redisson.api.RedissonClient;
import org.redisson.config.Config;
import org.redisson.config.SingleServerConfig;
import org.redisson.jcache.JCacheManager;

import javax.cache.configuration.MutableConfiguration;
import javax.cache.expiry.CreatedExpiryPolicy;
import javax.cache.expiry.Duration;
import java.util.concurrent.TimeUnit;

/**
 * Workspace metadata cache implementation that uses an out-of-process Redis cache, via the JCache API.
 */
public class RedisWorkspaceMetadataCache extends JCacheWorkspaceMetadataCache {

    private static final String CACHE_NAME = "structurizr.workspace.metadata";

    RedisWorkspaceMetadataCache(String host, int port, String password, int expiryInMinutes) {
        Config config = new Config();
        SingleServerConfig singleServerConfig = config.useSingleServer();
        singleServerConfig.setAddress(String.format("redis://%s:%s", host, port));
        if (!StringUtils.isNullOrEmpty(password)) {
            singleServerConfig.setPassword(password);
        }
        RedissonClient redisson = Redisson.create(config);

        cacheManager = new JCacheManager((Redisson) redisson, JCacheManager.class.getClassLoader(), null, null, null);

        MutableConfiguration<Long, WorkspaceMetaData> configuration = new MutableConfiguration<Long, WorkspaceMetaData>()
                .setTypes(Long.class, WorkspaceMetaData.class)
                .setStoreByValue(false)
                .setExpiryPolicyFactory(CreatedExpiryPolicy.factoryOf(new Duration(TimeUnit.MINUTES, expiryInMinutes)));
        cache = cacheManager.createCache(CACHE_NAME, configuration);
    }

}