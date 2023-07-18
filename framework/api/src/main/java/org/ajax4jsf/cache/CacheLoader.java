package org.ajax4jsf.cache;


/**
 * User should implement this CacheLoader interface to
 * provide a loader object to load the objects into cache.
 */
public interface CacheLoader
{
    /**
     * loads an object. Application writers should implement this
     * method to customize the loading of cache object. This method is called
     * by the caching service when the requested object is not in the cache.
     * <P>
     *
     * @param key the key identifying the object being loaded
     *
     * @return The object that is to be stored in the cache.
     * @throws CacheException
     *
     */
    public Object load(Object key, Object context) throws CacheException;

}
