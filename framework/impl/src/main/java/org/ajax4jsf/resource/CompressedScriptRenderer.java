/**
 * 
 */
package org.ajax4jsf.resource;

/**
 * @author asmirnov
 *
 */
public class CompressedScriptRenderer extends OneTimeRenderer {

	protected String getTag() {
		// TODO Auto-generated method stub
		return "script";
	}

	protected String getHrefAttr() {
		// TODO Auto-generated method stub
		return "src";
	}

	protected String[][] getCommonAttrs() {
		// TODO Auto-generated method stub
		return new String[][]{{"type",getContentType()}};
	}

	public String getContentType() {
		// TODO - use configurable encoding ?
		return "text/javascript";
	}


}
