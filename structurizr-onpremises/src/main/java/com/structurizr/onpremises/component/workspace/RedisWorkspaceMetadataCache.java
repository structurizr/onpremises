package com.structurizr.onpremises.component.workspace;

import com.structurizr.onpremises.configuration.Configuration;
import com.structurizr.onpremises.configuration.StructurizrProperties;
import com.structurizr.util.StringUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
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

    private static final Log log = LogFactory.getLog(RedisWorkspaceMetadataCache.class);

    private static final String CACHE_NAME = "structurizr.workspace.metadata";

    RedisWorkspaceMetadataCache() {
        String protocol = Configuration.getInstance().getProperty(StructurizrProperties.REDIS_PROTOCOL);
        String host = Configuration.getInstance().getProperty(StructurizrProperties.REDIS_HOST);
        int port = Integer.parseInt(Configuration.getInstance().getProperty(StructurizrProperties.REDIS_PORT));
        String password = Configuration.getInstance().getProperty(StructurizrProperties.REDIS_PASSWORD);
        int database = Integer.parseInt(Configuration.getInstance().getProperty(StructurizrProperties.REDIS_DATABASE));

        int expiryInMinutes = Integer.parseInt(StructurizrProperties.DEFAULT_CACHE_EXPIRY_IN_MINUTES);
        try {
            expiryInMinutes = Integer.parseInt(Configuration.getInstance().getProperty(StructurizrProperties.CACHE_EXPIRY_IN_MINUTES));
        } catch (NumberFormatException nfe) {
            log.warn(nfe);
        }

        log.debug("Creating cache for workspace metadata: implementation=redis; protocol=" + protocol + "; host=" + host + "; port=" + port + "; database=" + database + "; expiry=" + expiryInMinutes + " minute(s)");

        Config config = new Config();
        SingleServerConfig singleServerConfig = config.useSingleServer();
        singleServerConfig.setAddress(Configuration.getInstance().getProperty(StructurizrProperties.REDIS_ENDPOINT));
        singleServerConfig.setDatabase(database);
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