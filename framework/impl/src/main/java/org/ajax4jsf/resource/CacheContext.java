/**
 * 
 */
package org.ajax4jsf.resource;


/**
 * @author Nick - mailto:nbelaevski@exadel.com
 * created 01.05.2007
 * 
 */
public class CacheContext {
	private CachedResourceContext resourceContext;
    private InternetResource resource;
	public CacheContext(CachedResourceContext resourceContext,
			InternetResource resource) {
		super();
		this.resourceContext = resourceContext;
		this.resource = resource;
	}
	/**
	 * @return the resourceContext
	 */
	public CachedResourceContext getResourceContext() {
		return resourceContext;
	}
	public InternetResource getResource() {
		return resource;
	}
	
}
