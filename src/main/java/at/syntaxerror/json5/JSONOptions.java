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

import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NonNull;
import lombok.Setter;
import lombok.experimental.FieldDefaults;

/**
 * This class used is used to customize the behaviour of {@link JSONParser parsing} and {@link JSONStringify stringifying}
 * 
 * @author SyntaxError404
 * @since 1.1.0
 */
@AllArgsConstructor(access = AccessLevel.PRIVATE)
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
@Getter
@Builder(toBuilder = true)
public class JSONOptions {
	
	/**
	 * -- GETTER --
	 * Returns the default options for parsing and stringifying
	 * 
	 * @return the default options
	 * @since 1.1.0
	 * 
	 * -- SETTER --
	 * Sets the default options for parsing and stringifying.
	 * Must not be {@code null}
	 * 
	 * @param defaultOptions the new default options
	 * @since 1.1.0
	 */
	@Getter
	@Setter
	@NonNull
	private static JSONOptions defaultOptions = builder().build();
	
	/**
	 * Whether instants should be stringifyed as unix timestamps.
	 * If this is {@code false}, instants will be stringifyed as strings
	 * (according to <a href="https://datatracker.ietf.org/doc/html/rfc3339#section-5.6">RFC 3339, Section 5.6</a>).
	 * <p>
	 * Default: {@code false}
	 * <p>
	 * <i>This is a {@link JSONStringify Stringify}-only option</i>
	 * 
	 * @param stringifyUnixInstants a boolean
	 * 
	 * @return whether instants should be stringifyed as unix timestamps
	 * @since 1.1.0
	 */
	@Builder.Default
	boolean stringifyUnixInstants = false;
	
	/**
	 * Whether stringifying should only yield ASCII strings.
	 * All non-ASCII characters will be converted to their
	 * Unicode escape sequence (<code>&#92;uXXXX</code>).
	 * <p>
	 * Default: {@code false}
	 * <p>
	 * <i>This is a {@link JSONStringify Stringify}-only option</i>
	 * 
	 * @param stringifyAscii a boolean
	 * 
	 * @return whether stringifying should only yield ASCII strings
	 * @since 2.0.0
	 */
	@Builder.Default
	boolean stringifyAscii = false;
	
	/**
	 * Whether {@code NaN} should be allowed as a number
	 * <p>
	 * Default: {@code true}
	 * 
	 * @param allowNaN a boolean
	 * 
	 * @return whether {@code NaN} should be allowed
	 * @since 1.1.0
	 */
	@Builder.Default
	boolean allowNaN = true;
	
	/**
	 * Whether {@code Infinity} should be allowed as a number.
	 * This applies to both {@code +Infinity} and {@code -Infinity}
	 * <p>
	 * Default: {@code true}
	 * 
	 * @param allowInfinity a boolean
	 * 
	 * @return whether {@code Infinity} should be allowed
	 * @since 1.1.0
	 */
	@Builder.Default
	boolean allowInfinity = true;
	
	/**
	 * Whether invalid unicode surrogate pairs should be allowed
	 * <p>
	 * Default: {@code true}
	 * <p>
	 * <i>This is a {@link JSONParser Parser}-only option</i>
	 * 
	 * @param allowInvalidSurrogates a boolean
	 * 
	 * @return whether invalid unicode surrogate pairs should be allowed
	 * @since 1.1.0
	 */
	@Builder.Default
	boolean allowInvalidSurrogates = true;
	
	/**
	 * Whether strings should be single-quoted ({@code '}) instead of double-quoted ({@code "}).
	 * This also includes a {@link JSONObject JSONObject's} member names
	 * <p>
	 * Default: {@code false}
	 * <p>
	 * <i>This is a {@link JSONStringify Stringify}-only option</i>
	 * 
	 * @param quoteSingle a boolean
	 * 
	 * @return whether strings should be single-quoted
	 * @since 1.1.0
	 */
	@Builder.Default
	boolean quoteSingle = false;
	
	/**
	 * Whether binary literals ({@code 0b10101...}) should be allowed
	 * <p>
	 * Default: {@code false}
	 * <p>
	 * <i>This is a {@link JSONParser Parser}-only option</i>
	 * 
	 * @param allowBinaryLiterals a boolean
	 * 
	 * @return whether binary literals should be allowed
	 * @since 2.0.0
	 */
	@Builder.Default
	boolean allowBinaryLiterals = false;
	
	/**
	 * Whether octal literals ({@code 0o567...}) should be allowed
	 * <p>
	 * Default: {@code false}
	 * <p>
	 * <i>This is a {@link JSONParser Parser}-only option</i>
	 * 
	 * @param allowOctalLiterals a boolean
	 * 
	 * @return whether octal literals should be allowed
	 * @since 2.0.0
	 */
	@Builder.Default
	boolean allowOctalLiterals = false;
	
	/**
	 * Whether hexadecimal floating-point literals (e.g. {@code 0xA.BCp+12}) should be allowed
	 * <p>
	 * Default: {@code false}
	 * <p>
	 * <i>This is a {@link JSONParser Parser}-only option</i>
	 * 
	 * @param allowHexFloatingLiterals a boolean
	 * 
	 * @return whether octal literals should be allowed
	 * @since 2.0.0
	 */
	@Builder.Default
	boolean allowHexFloatingLiterals = false;
	
	/**
	 * Whether Java-style digit separators ({@code 123_456}) should be allowed
	 * <p>
	 * Default: {@code false}
	 * <p>
	 * <i>This is a {@link JSONParser Parser}-only option</i>
	 * 
	 * @param allowJavaDigitSeparators a boolean
	 * 
	 * @return whether Java-style digit separators should be allowed
	 * @since 2.0.0
	 */
	@Builder.Default
	boolean allowJavaDigitSeparators = false;
	
	/**
	 * Whether C-style digit separators ({@code 123'456}) should be allowed
	 * <p>
	 * Default: {@code false}
	 * <p>
	 * <i>This is a {@link JSONParser Parser}-only option</i>
	 * 
	 * @param allowCDigitSeparators a boolean
	 * 
	 * @return whether C-style digit separators should be allowed
	 * @since 2.0.0
	 */
	@Builder.Default
	boolean allowCDigitSeparators = false;
	
	/**
	 * Whether 32-bit unicode escape sequences ({@code \U00123456}) should be allowed
	 * <p>
	 * Default: {@code false}
	 * <p>
	 * <i>This is a {@link JSONParser Parser}-only option</i>
	 * 
	 * @param allowLongUnicodeEscapes a boolean
	 * 
	 * @return whether 32-bit unicode escape sequences should be allowed
	 * @since 2.0.0
	 */
	@Builder.Default
	boolean allowLongUnicodeEscapes = false;
	
	/**
	 * Specifies the behavior when the same key is encountered multiple times within the same {@link JSONObject}
	 * <p>
	 * Default: {@link DuplicateBehavior#UNIQUE UNIQUE}
	 * <p>
	 * <i>This is a {@link JSONParser Parser}-only option</i>
	 * 
	 * @param duplicateBehaviour the behavior
	 * 
	 * @return the behavior when encountering duplicate keys
	 * @since 1.3.0
	 */
	@Builder.Default
	DuplicateBehavior duplicateBehaviour = DuplicateBehavior.UNIQUE;
	
	/**
	 * Specifies whether trailing data should be allowed.<br>
	 * If {@code false}, parsing the following will produce an error
	 * due to the trailing {@code abc}:
	 * 
	 * <pre><code>{ }abc</code></pre>
	 * 
	 * If {@code true}, however, this will be interpreted as an empty
	 * {@link JSONObject} and any trailing will be ignored.
	 * <p>
	 * Whitespace never counts as trailing data.
	 * <p>
	 * Default: {@code false}
	 * <p>
	 * <i>This is a {@link JSONParser Parser}-only option</i>
	 * 
	 * @param allowTrailingData a boolean
	 * 
	 * @return whether trailing data should be allowed
	 * @since 2.1.0
	 */
	@Builder.Default
	boolean allowTrailingData = false;
	
	/**
	 * An enum containing all supported behaviors for duplicate keys
	 * 
	 * @since 1.3.0
	 */
	public static enum DuplicateBehavior {
		
		/**
		 * Throws an {@link JSONException exception} when a key
		 * is encountered multiple times within the same object
		 */
		UNIQUE,
		
		/**
		 * Only the last encountered value is significant,
		 * all previous occurrences are silently discarded
		 */
		LAST_WINS,
		
		/**
		 * Wraps duplicate values inside an {@link JSONArray array},
		 * effectively treating them as if they were declared as one
		 */
		DUPLICATE
		
	}
	
}
