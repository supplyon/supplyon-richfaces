package org.ajax4jsf.cache;


/**
 * CacheException is a generic exception, which indicates
 * a cache error has occurred. All the other cache exceptions are the
 * subclass of this class. All the methods in the cache package only
 * throw CacheException or the sub class of it.
 * <P>
 *
 */
public class CacheException extends Exception
{
    /**
	 * 
	 */
	private static final long serialVersionUID = 6712594794189413065L;

	/**
     * Constructs a new CacheException.
     */
    public CacheException()
    {
        super();
    }

    /**
     * Constructs a new CacheException with a message string.
     */
    public CacheException(String s)
    {
        super(s);
    }

    /**
     * Constructs a CacheException with a message string, and
     * a base exception
     */
    public CacheException(String s, Throwable ex)
    {
        super(s, ex);
    }
}
