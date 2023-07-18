/**
 * 
 */
package org.ajax4jsf.webapp;

/**
 * @author asmirnov
 *
 */
public class ConfigurableFilter extends BaseFilter {

    /**
     * 
     */
    public ConfigurableFilter() {
	xmlFilter = new ConfigurableXMLFilter();
	xmlFilter.setFilter(this);
    }

}
