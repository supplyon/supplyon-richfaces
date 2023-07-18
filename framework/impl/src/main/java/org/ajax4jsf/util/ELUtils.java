/**
 * 
 */
package org.ajax4jsf.util;

/**
 * @author asmirnov
 *
 */
public class ELUtils {
	
	private ELUtils() {
		// Utility class with static methods only - do not instantiate.
	}

	/**
	 * Get EL-enabled value. Return same string, if not el-expression.
	 * Otherthise, return parsed and evaluated expression.
	 * 
	 * @param context -
	 *            current Faces Context.
	 * @param value -
	 *            string to parse.
	 * @return - interpreted el or unmodified value.
	 */
	public static boolean isValueReference(String value) {
		if (value == null)
			return false;

		int start = value.indexOf("#{");
		if (start >= 0) {
			int end = value.lastIndexOf('}');
			if (end >= 0 && start < end) {
				return true;
			}
		}
		return false;
	}

}
