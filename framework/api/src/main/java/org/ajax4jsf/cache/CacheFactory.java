package org.ajax4jsf.cache;

import java.util.Map;

/**
 * CacheFactory is a service provider specific interface.
 * Service provider should implement CacheFactory to provide
 * the functionality to create a new implementation specific Cache object.
 */
public interface CacheFactory
{
    /**
     * creates a new implementation specific Cache object using the env parameters.
     * @param env implementation specific environment parameters passed to the
     * CacheFactory.
     * @param cacheLoader implementation of the {@link CacheLoader} to use
     * @param cacheConfigurationloader TODO
     * @return an implementation specific Cache object.
     * @throws CacheException if any error occurs.
     */
    public Cache createCache(Map env, CacheLoader cacheLoader, CacheConfigurationLoader cacheConfigurationloader) throws CacheException;
}
