/**
 * License Agreement.
 *
 * Rich Faces - Natural Ajax for Java Server Faces (JSF)
 *
 * Copyright (C) 2007 Exadel, Inc.
 *
 * This library is free software; you can redistribute it and/or
 * modify it under the terms of the GNU Lesser General Public
 * License version 2.1 as published by the Free Software Foundation.
 *
 * This library is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
 * Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public
 * License along with this library; if not, write to the Free Software
 * Foundation, Inc., 51 Franklin Street, Fifth Floor, Boston, MA 02110-1301  USA
 */

package org.ajax4jsf.io.parser;

import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.Reader;
import java.io.Writer;
import java.lang.reflect.Field;
import java.util.Iterator;
import java.util.Set;

import org.ajax4jsf.io.FastBufferReader;
import org.ajax4jsf.webapp.HtmlParser;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

/**
 * Simplified and fast HTML parsed - for find insertion point of <html><head>
 * there can be inserted <script><style> and <meta> tags.
 * 
 * @author shura
 * 
 */
public class FastHtmlParser implements HtmlParser {

	private static final Log log = LogFactory.getLog(FastHtmlParser.class);

	private Set<String> scripts;

	private Set<String> styles;

	private Set<String> userStyles;

	private String encoding;

	private String doctype;

	private String viewState;

	private String mimeType;

	public void parse(Reader in, Writer out) throws IOException {
		boolean haveHtml = false;
		boolean haveHead = false;
		boolean closingElement = false;
// Always parse content, see http://jira.jboss.com/jira/browse/RF-3577
//		if (null != scripts || null != styles || null != userStyles) {
			ParsingStateManager stateManager = ParsingStateManager
					.getInstance();
			ParserState state = stateManager.getInitialState();
			ParsingContext context = new ParsingContext(out);
			int nextChar;
			while ((nextChar = in.read()) > 0) {
				char c = (char)nextChar;
				state = state.getNextState(c, context);
				if (log.isDebugEnabled()) {
					// Find state name.
					Class<? extends ParsingStateManager> stateMgrClass = stateManager.getClass();
					Field[] fields = stateMgrClass.getDeclaredFields();
					for (int i = 0; i < fields.length; i++) {
						Field field = fields[i];
						try {
							if (field.get(stateManager) == state) {
								log
										.debug("Parser reached state is StateManager."
												+ field.getName());
								break;
							}
						} catch (Exception e) {
							// ignore ...
						}
					}
				}
				state.send(c, context);
				if (state == stateManager.LT) {
					closingElement = false;
				} else if (state == stateManager.CLOSINGELEMENT) {
					closingElement = true;
				} else if (state == stateManager.ENDELEMENT) {
					if (context.getLastMatched() == stateManager.HTML) {
						haveHtml = true;
						//if xmlns is not set on HTML element, append xmlns attribute
						if (!context.contains("xmlns=")) {
							context.insert(" xmlns=\"http://www.w3.org/1999/xhtml\"");
						}
						
						context.send();
						if (log.isDebugEnabled()) {
							log.debug("Found <html> element");
						}
					} else if (context.getLastMatched() == stateManager.HEAD) {
						haveHead = true;
						if (log.isDebugEnabled()) {
							log.debug("Found <head> element");
						}
						if (closingElement) {
							writeToHead(out, haveHtml, haveHead);
							break;
						} else {
							context.send();							
						}
					} else if (context.getLastMatched() == stateManager.TITLE) {
						context.send();
						haveHead = true;
						haveHtml = true;
						if (log.isDebugEnabled()) {
							log.debug("Found <title> element");
						}
					} else if (context.getLastMatched() == stateManager.BASE) {
						context.send();
						haveHead = true;
						haveHtml = true;
						if (log.isDebugEnabled()) {
							log.debug("Found <base> element");
						}
					} else {
						if (log.isDebugEnabled()) {
							log
									.debug("non <html> or <head><title>|<base> element");
						}
						writeToHead(out, haveHtml, haveHead);
						break;
					}
				} else if (state == stateManager.ELEMENT) {
					writeToHead(out, haveHtml, haveHead);
					break;
				}
			}
			context.send();
//		} else {
//			haveHtml = true;
//		}
		// Send rest of input.
		if (in instanceof FastBufferReader) {
			FastBufferReader fastIn = (FastBufferReader) in;
			fastIn.writeTo(out);

		} else {
			char[] buffer = new char[1024];
			int buffersCount = -1;
			int length;
			for (length = in.read(buffer); length > 0; length = in.read(buffer)) {
				out.write(buffer, 0, length);
				buffersCount++;
			}
		}
		if (!haveHtml) {
			out.write("</html>");
		}

	}

	private void writeToHead(Writer out, boolean haveHtml, boolean haveHead)
			throws IOException {
		if (!haveHead && !haveHtml) {
			out.write("<html  xmlns=\"http://www.w3.org/1999/xhtml\">");
		}
		if (!haveHead) {
			out.write("<head>");
		}
		if (null != styles) {
			for (Iterator<String> iter = styles.iterator(); iter.hasNext();) {
				String style = (String) iter.next();
				out.write("<link rel='stylesheet' class='" + COMPONENT_RESOURCE_LINK_CLASS + "'" +
						" type='text/css' href='");
				out.write(style);
				out.write("' />");
			}
		}
		if (null != userStyles) {
			for (Iterator<String> iter = userStyles.iterator(); iter.hasNext();) {
				String style = (String) iter.next();
				out.write("<link rel='stylesheet' class='" + USER_RESOURCE_LINK_CLASS + "'" +
						" type='text/css' href='");
				out.write(style);
				out.write("' />");
			}
		}
		if (null != scripts) {
			for (Iterator<String> iter = scripts.iterator(); iter.hasNext();) {
				String script = (String) iter.next();
				out.write("<script type='text/javascript' src='");
				out.write(script);
				out.write("'></script>");
			}
		}
		if (!haveHead) {
			out.write("</head>");
		}

	}

	/**
	 * @return Returns the scripts.
	 */
	public Set<String> getScripts() {
		return scripts;
	}

	/**
	 * @param scripts
	 *            The scripts to set.
	 */
	public void setScripts(Set<String> scripts) {
		this.scripts = scripts;
	}

	/**
	 * @return Returns the styles.
	 */
	public Set<String> getStyles() {
		return styles;
	}

	/**
	 * @param styles
	 *            The styles to set.
	 */
	public void setStyles(Set<String> styles) {
		this.styles = styles;
	}

	/**
	 * @return Returns the user styles.
	 */
	public Set<String> getUserStyles() {
		return userStyles;
	}

	/**
	 * @param styles
	 *            The user styles to set.
	 */
	public void setUserStyles(Set<String> userStyles) {
		this.userStyles = userStyles;
	}

	public void parseHtml(InputStream input, Writer output) throws IOException {
		parse(new InputStreamReader(input, encoding), output);

	}

	public void parseHtml(Reader input, Writer output) throws IOException {
		parse(input, output);

	}

	public void setInputEncoding(String encoding) {
		this.encoding = encoding;

	}

	public void setOutputEncoding(String encoding) {
		// TODO Auto-generated method stub

	}

	public void setMoveElements(boolean move) {
	}

	public void setDoctype(String doctype) {
		this.doctype = doctype;

	}

	public void setViewState(String viewState) {
		this.viewState = viewState;

	}

	public boolean setMime(String mimeType) {
	    this.mimeType=mimeType;
	    return true;
	}
}
