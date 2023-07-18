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

package org.ajax4jsf.webapp.tidy;

import java.io.IOException;
import java.io.InputStream;
import java.io.PrintWriter;
import java.io.Reader;
import java.io.Writer;
import java.util.Iterator;
import java.util.Properties;
import java.util.Set;

import org.ajax4jsf.Messages;
import org.ajax4jsf.application.AjaxViewHandler;
import org.ajax4jsf.org.w3c.tidy.Lexer;
import org.ajax4jsf.org.w3c.tidy.Node;
import org.ajax4jsf.org.w3c.tidy.Tidy;
import org.ajax4jsf.org.w3c.tidy.TidyMessage;
import org.ajax4jsf.org.w3c.tidy.TidyMessageListener;
import org.ajax4jsf.webapp.HtmlParser;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;

/**
 * @author asmirnov@exadel.com (latest modification by $Author: alexsmirnov $)
 * @version $Revision: 1.1.2.1 $ $Date: 2007/01/09 18:59:17 $
 * 
 */
public class TidyParser implements TidyMessageListener, HtmlParser {

	private static final Log log = LogFactory.getLog(TidyParser.class);

	private Tidy tidy;

	private Set<String> _scripts;

	private Set<String> _styles;

	private Set<String> _userStyles;

	private String _viewState;

	private String _encoding;

	private String _outputEncoding;

	private static final String[] _htmlTypes = { "text/html" };

	private static final String[] _xmlTypes = { "text/xml" };// "text/xml"};

	private static final String[] _xhtmlTypes = { "application/xhtml+xml" };

	private static final String TIDY_MARK = "[TIDY_MESSAGE]";

	/**
	 * 
	 */
	public TidyParser(Properties props) {
		tidy = new Tidy();
		tidy.setConfigurationFromProps(props);
		// tidy.setConfigurationFromProps(getProperties("tidy.properties"));
		PrintWriter errout = new PrintWriter(new ErrorWriter());
		tidy.setErrout(errout);
		tidy.setForceOutput(true);
		tidy.setHideEndTags(false);
		tidy.setMessageListener(this);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.ajax4jsf.webapp.HtmlParser#parseHtml(java.lang.Object,
	 *      java.io.OutputStream)
	 */
	public Document parseHtmlByTidy(Object input, Writer output)
			throws IOException {
		Document document = tidy.parseDOM(input, null);
		if (null != document) {
			Element documentElement = document.getDocumentElement();
			if (null != documentElement) {
				// Replace state elements with real stored.

				NodeList inputs = documentElement.getElementsByTagName("span");
				for (int i = 0; i < inputs.getLength(); i++) {
					Element node = (Element) inputs.item(i);
					if (AjaxViewHandler.STATE_MARKER_KEY.equals(node
							.getAttribute("id"))
							&& AjaxViewHandler.STATE_MARKER_KEY.equals(node
									.getAttribute("name"))) {
						// State marker - replace with real.
						org.w3c.dom.Node parentNode = node.getParentNode();
						if (null != _viewState) {
							parentNode.replaceChild(document
									.createCDATASection(_viewState), node);
						} else {
							// Remove marker element, but keep it content.
							if (node.hasChildNodes()) {
								org.w3c.dom.Node nextSibling = node
										.getNextSibling();
								NodeList childNodes = node.getChildNodes();
								// Copy all nodes by temporary array ( since
								// moving nodes in iteration
								// modify NodeList with side effects.
								org.w3c.dom.Node[] childArray = new org.w3c.dom.Node[childNodes
										.getLength()];
								for (int j = 0; j < childArray.length; j++) {
									childArray[j] = childNodes.item(j);
								}
								for (int j = 0; j < childArray.length; j++) {
									parentNode.insertBefore(childArray[j],
											nextSibling);
								}
							}
							parentNode.removeChild(node);
						}
					}
				}

				// Inserts scripts and styles to head.
				if (null != _scripts || null != _styles || null != _userStyles || null != _viewState) {
					// find head
					org.w3c.dom.Node head = documentElement
							.getElementsByTagName("head").item(0);
					// Insert empty if not found
					if (null == head) {
						head = document.createElement("head");
						documentElement.insertBefore(head, documentElement
								.getFirstChild());
					}
					NodeList headChildren = head.getChildNodes();
					org.w3c.dom.Node child = null;
					for (int i = 0; i < headChildren.getLength(); i++) {
						child = headChildren.item(i);
						if (child instanceof Element) {
							String nodeName = ((Element) child).getNodeName();
							if (!("title".equalsIgnoreCase(nodeName) || "base"
									.equalsIgnoreCase(nodeName))) {
								break;
							}
						}
						child = null;
					}
					if (null != _styles) {
						for (Iterator<String> iter = _styles.iterator(); iter.hasNext();) {
							String url = (String) iter.next();
							Element style = document.createElement("link");
							style.setAttribute("type", "text/css");
							style.setAttribute("rel", "stylesheet");
							style.setAttribute("href", url);
							style.setAttribute("class", COMPONENT_RESOURCE_LINK_CLASS);
							head.insertBefore(style, child);
						}
					}
					if (null != _userStyles) {
						for (Iterator<String> iter = _userStyles.iterator(); iter.hasNext();) {
							String url = (String) iter.next();
							Element style = document.createElement("link");
							style.setAttribute("type", "text/css");
							style.setAttribute("rel", "stylesheet");
							style.setAttribute("href", url);
							style.setAttribute("class", USER_RESOURCE_LINK_CLASS);
							head.insertBefore(style, child);
						}
					}
					// Scripts
					if (null != _scripts) {
						for (Iterator<String> iter = _scripts.iterator(); iter
								.hasNext();) {
							String url = (String) iter.next();
							Element script = document.createElement("script");

							script.setAttribute("type", "text/javascript");
							// script.setAttribute("defer", "defer");
							script.setAttribute("src", url);
							// script.appendChild(document.createTextNode(""));
							head.insertBefore(script, child);
						}
					}
				}
			}
			if (null != output) {
				tidy.pprint(document, output);
			}
		}
		return document;
	}

	public void parseHtml(InputStream input, Writer output) throws IOException {
		this.parseHtmlByTidy(input, output);

	}

	public void parseHtml(Reader input, Writer output) throws IOException {
		this.parseHtmlByTidy(input, output);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.w3c.tidy.TidyMessageListener#messageReceived(org.w3c.tidy.TidyMessage)
	 */
	public void messageReceived(TidyMessage message) {
		// TODO record messages for output.
		// TODO change signature for receive current node and append message in
		// tree.
		if (log.isDebugEnabled()) {
			log.debug(Messages.getMessage(Messages.MESSAGE_PARSING_INFO,
					message.getMessage()));
		}
		// TODO - configurable output for reeors in page.
		if (false) {
			Lexer lexer = message.getLexer();
			Node element = message.getElement();
			if (null == element) {
				element = lexer.getLastNode();
			} else if ("style".equalsIgnoreCase(element.getElement())
					|| "script".equalsIgnoreCase(element.getElement())) {
				element = lexer.getLastNode();
			}
			// Insert comment about error.
			if (null != element) {
				String messageText = message.getMessage();
				byte[] msg = (TIDY_MARK + messageText + TIDY_MARK).getBytes();
				Node comment = lexer.newNode(Node.COMMENT_TAG, msg, 0,
						msg.length);
				// TODO - detect style or script elements - not allow comment in
				// it.
				element.insertNodeAtEnd(comment);
			}
		}
	}

	/**
	 * 'null' writer for discard errors - since processed in message listener
	 * 
	 * @author shura (latest modification by $Author: alexsmirnov $)
	 * @version $Revision: 1.1.2.1 $ $Date: 2007/01/09 18:59:17 $
	 * 
	 */
	private static class ErrorWriter extends Writer {

		/*
		 * (non-Javadoc)
		 * 
		 * @see java.io.Writer#write(char[], int, int)
		 */
		public void write(char[] cbuf, int off, int len) throws IOException {
			// do nothing
		}

		/*
		 * (non-Javadoc)
		 * 
		 * @see java.io.Writer#flush()
		 */
		public void flush() throws IOException {
			// do nothing
		}

		/*
		 * (non-Javadoc)
		 * 
		 * @see java.io.Writer#close()
		 */
		public void close() throws IOException {
			// do nothing
		}

	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.ajax4jsf.webapp.HtmlParser#setEncoding(java.lang.String)
	 */
	public void setInputEncoding(String encoding) {
		if (null != encoding) {
			this._encoding = encoding;
			if (null != encoding) {
				this.tidy.setInputEncoding(encoding);
			}
			// this.tidy.setOutputEncoding(encoding);
		}
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.ajax4jsf.webapp.HtmlParser#setEncoding(java.lang.String)
	 */
	public void setOutputEncoding(String encoding) {
		if (null != encoding) {
			this._outputEncoding = encoding;
			// this.tidy.setInputEncoding(encoding);
			if (null != encoding) {
				this.tidy.setOutputEncoding(encoding);
			}
		}
	}

	/**
	 * Setup properly tidy output for given mime type. return {@link false } if
	 * this type not supported by tidy.
	 * 
	 * @param mimeType
	 * @return
	 */
	public boolean setMime(String mimeType) {
		if (null != mimeType) {
			for (int i = 0; i < _htmlTypes.length; i++) {
				String mime = _htmlTypes[i];
				if (mimeType.startsWith(mime)) {
					// setup html output
					tidy.setXHTML(false);
					tidy.setXmlOut(false);
					if (log.isDebugEnabled()) {
					    log.debug("Print output as ordinary HTML");
					}
					return true;
				}
			}
			for (int i = 0; i < _xhtmlTypes.length; i++) {
				String mime = _xhtmlTypes[i];
				if (mimeType.startsWith(mime)) {
					// setup xhtml output
					tidy.setXHTML(true);
					tidy.setXmlOut(false);
					if (log.isDebugEnabled()) {
					    log.debug("Print output as XHTML");
					}
					return true;
				}
			}
			for (int i = 0; i < _xmlTypes.length; i++) {
				String mime = _xmlTypes[i];
				if (mimeType.startsWith(mime)) {
					// setup html output
					tidy.setXHTML(true);
					tidy.setXmlOut(true);
					tidy.setXmlPi(true);
					tidy.setEscapeCdata(false);
					tidy.setNumEntities(true);
					if (log.isDebugEnabled()) {
					    log.debug("Print output as XML");
					}
					return true;
				}
			}
		}
		return false;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.ajax4jsf.webapp.HtmlParser#setMoveElements(boolean)
	 */
	public void setMoveElements(boolean move) {
		tidy.setMoveElements(move);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.ajax4jsf.webapp.HtmlParser#setScripts(java.util.Set)
	 */
	public void setScripts(Set<String> scripts) {
		this._scripts = scripts;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.ajax4jsf.webapp.HtmlParser#setStyles(java.util.Set)
	 */
	public void setStyles(Set<String> styles) {
		this._styles = styles;
	}
	
	public void setUserStyles(Set<String> styles) {
		this._userStyles = styles;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.ajax4jsf.webapp.HtmlParser#setDoctype(java.lang.String)
	 */
	public void setDoctype(String doctype) {
		 tidy.setDocType("omit");//doctype);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.ajax4jsf.webapp.HtmlParser#setViewState(java.lang.String)
	 */
	public void setViewState(String viewState) {
		_viewState = viewState;
	}
}
