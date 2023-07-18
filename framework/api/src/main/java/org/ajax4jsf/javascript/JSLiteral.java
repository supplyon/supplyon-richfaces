/*
 * JSLiteral.java		Date created: 29.01.2008
 * Last modified by: $Author$
 * $Revision$	$Date$
 */

package org.ajax4jsf.javascript;

/**
 * Class provides creation of simple literal javascript to be set in ajax response data
 * @author Andrey Markavtsov
 *
 */
public class JSLiteral extends ScriptStringBase {
    
    /** Javascript literal text */
    private String literal;
    

    /**
     * Default constructor
     */
    public JSLiteral() {
	super();
    }

    /**
     * Constructor using literal parameter
     * @param literal
     */
    public JSLiteral(String literal) {
	super();
	this.literal = literal;
    }

    /* (non-Javadoc)
     * @see org.ajax4jsf.javascript.ScriptString#appendScript(java.lang.StringBuffer)
     */
    public void appendScript(StringBuffer jsString) {
	jsString.append(literal);

    }

    /**
     * @return the literal
     */
    public String getLiteral() {
        return literal;
    }

    /**
     * @param literal the literal to set
     */
    public void setLiteral(String literal) {
        this.literal = literal;
    }

}
