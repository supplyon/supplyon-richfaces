/**
 * 
 */
package org.ajax4jsf.webapp;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

abstract class ParserConfig {

	private static final Pattern[] ALL_VIEWS_PATTERN = { Pattern.compile(".*") };

	private static final Pattern SEPARATOR_PATTERN = Pattern
			.compile("\\s*,\\s*");

	private Pattern[] patterns = ALL_VIEWS_PATTERN;

	private ParserConfig next;

	protected abstract HtmlParser createParser(String mimetype);

	boolean storeParser(HtmlParser parser) {
		return false;
	}

	HtmlParser getParser(String viewId, String mimetype) {
		HtmlParser result = null;
		if (null != viewId) {
			for (int i = 0; i < patterns.length && null == result; i++) {
				Matcher matcher = patterns[i].matcher(viewId);
				if (matcher.matches()) {
					result = createParser(mimetype);
				}
			}

		}
		if (null == result) {
			if (null != next) {
				result = next.getParser(viewId, mimetype);
			} else {
				result = createParser(mimetype);
			}
		}
		return result;
	}

	public void reuseParser(HtmlParser parser) {
		if (!storeParser(parser) && null != next) {
			next.reuseParser(parser);
		}
	}

	public ParserConfig getNext() {
		return next;
	}

	public void setNext(ParserConfig next) {
		this.next = next;
	}

	public void setPatterns(String patternsString) {
		if (null != patternsString) {
			String[] split = SEPARATOR_PATTERN.split(patternsString);
			patterns = new Pattern[split.length];
			for (int i = 0; i < split.length; i++) {
				patterns[i] = Pattern.compile(split[i].trim());
			}
		}
	}

	public Pattern[] getPatterns() {
		return patterns;
	}

}