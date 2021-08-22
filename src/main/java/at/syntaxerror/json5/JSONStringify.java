/*
 * MIT License
 * 
 * Copyright (c) 2021 SyntaxError404
 * 
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 * 
 * The above copyright notice and this permission notice shall be included in all
 * copies or substantial portions of the Software.
 * 
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE
 * SOFTWARE.
 */
package at.syntaxerror.json5;

import java.util.Map;

/**
 * A utility class for serializing {@link JSONObject JSONObjects} and
 * {@link JSONArray JSONArrays} into their string representations
 * 
 * @author SyntaxError404
 * 
 */
public class JSONStringify {

	private JSONStringify() {
		throw new UnsupportedOperationException("Utility class");
	}

	/**
	 * Converts a JSONObject into its string representation.
	 * The indentation factor enables pretty-printing and defines
	 * how many spaces (' ') should be placed before each key/value pair.
	 * A factor of {@code < 1} disables pretty-printing and discards
	 * any optional whitespace characters.
	 * <p>
	 * {@code indentFactor = 2}:
	 * <pre>
	 * {
	 *   "key0": "value0",
	 *   "key1": {
	 *     "nested": 123
	 *   },
	 *   "key2": false
	 * }
	 * </pre>
	 * <p>
	 * {@code indentFactor = 0}:
	 * <pre>
	 * {"key0":"value0","key1":{"nested":123},"key2":false}
	 * </pre>
	 * 
	 * @param object the JSONObject
	 * @param indentFactor the indentation factor
	 * @return the string representation
	 * 
	 * @see JSONStringify#toString(JSONObject, int)
	 */
	public static String toString(JSONObject object, int indentFactor) {
		return toString(object, "", Math.max(0, indentFactor));
	}

	/**
	 * Converts a JSONArray into its string representation.
	 * The indentation factor enables pretty-printing and defines
	 * how many spaces (' ') should be placed before each index/value pair.
	 * A factor of {@code < 1} disables pretty-printing and discards
	 * any optional whitespace characters.
	 * <p>
	 * {@code indentFactor = 2}:
	 * <pre>
	 * [
	 *   "value",
	 *   {
	 *     "nested": 123
	 *   },
	 *   false
	 * }
	 * </pre>
	 * <p>
	 * {@code indentFactor = 0}:
	 * <pre>
	 * ["value",{"nested":123},false]
	 * </pre>
	 * 
	 * @param array the JSONArray
	 * @param indentFactor the indentation factor
	 * @return the string representation
	 */
	public static String toString(JSONArray array, int indentFactor) {
		return toString(array, "", Math.max(0, indentFactor));
	}
	
	private static String toString(JSONObject object, String indent, int indentFactor) {
		StringBuilder sb = new StringBuilder();
		
		String childIndent = indent + " ".repeat(indentFactor);
		
		sb.append('{');
		
		for(Map.Entry<String, Object> entry : object) {
			if(sb.length() != 1)
				sb.append(',');
			
			if(indentFactor > 0)
				sb.append('\n').append(childIndent);
			
			sb.append(quote(entry.getKey()))
				.append(':');
			
			if(indentFactor > 0)
				sb.append(' ');
			
			sb.append(toString(entry.getValue(), childIndent, indentFactor));
		}

		if(indentFactor > 0)
			sb.append('\n').append(indent);
		
		sb.append('}');
		
		return sb.toString();
	}
	private static String toString(JSONArray array, String indent, int indentFactor) {
		StringBuilder sb = new StringBuilder();
		
		String childIndent = indent + " ".repeat(indentFactor);
		
		sb.append('[');
		
		for(Object value : array) {
			if(sb.length() != 1)
				sb.append(',');
			
			if(indentFactor > 0)
				sb.append('\n').append(childIndent);
			
			sb.append(toString(value, childIndent, indentFactor));
		}

		if(indentFactor > 0)
			sb.append('\n').append(indent);
		
		sb.append(']');
		
		return sb.toString();
	}
	
	private static String toString(Object value, String indent, int indentFactor) {
		if(value == null)
			return "null";
		
		if(value instanceof JSONObject)
			return toString((JSONObject) value, indent, indentFactor);

		if(value instanceof JSONArray)
			return toString((JSONArray) value, indent, indentFactor);
		
		if(value instanceof String)
			return quote((String) value);
		
		return String.valueOf(value);
	}
	
	/**
	 * Converts a string into a valid JSON-string, enclosed by double quotes ({@code "})
	 * 
	 * @param string the string
	 * @return the quoted string
	 */
	static String quote(String string) {
		if(string == null || string.isEmpty())
			return "\"\"";
		
		StringBuilder quoted = new StringBuilder(string.length() + 2);
		
		quoted.append('"');
		
		for(char c : string.toCharArray()) {
			switch(c) {
			case '\\':
			case '"':
				quoted.append('\\');
				quoted.append(c);
				break;
			case '\b':
				quoted.append("\\b");
				break;
			case '\f':
				quoted.append("\\f");
				break;
			case '\n':
				quoted.append("\\n");
				break;
			case '\r':
				quoted.append("\\r");
				break;
			case '\t':
				quoted.append("\\t");
				break;
			case 0x0B: // Vertical Tab
				quoted.append("\\v");
				break;
			default:
				 // escape non-graphical characters (https://www.unicode.org/versions/Unicode13.0.0/ch02.pdf#G286941)
				switch(Character.getType(c)) {
				case Character.FORMAT:
				case Character.LINE_SEPARATOR:
				case Character.PARAGRAPH_SEPARATOR:
				case Character.CONTROL:
				case Character.PRIVATE_USE:
				case Character.SURROGATE:
				case Character.UNASSIGNED:
					quoted.append("\\u");
					quoted.append(String.format("%04X", c));
					break;
				default:
					quoted.append((char) c);
					break;
				}
			}
		}
		
		quoted.append('"');
		
		return quoted.toString();
	}
	
}
