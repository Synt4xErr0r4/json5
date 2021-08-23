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
	 * Whether or not instants should be parsed as such.
	 * If this is {@code false}, {@link #isParseStringInstants()} and {@link #isParseUnixInstants()}
	 * are ignored
	 * <p>
	 * Default: {@code true}
	 * <p>
	 * <i>This is a {@link JSONParser Parser}-only option</i>
	 * 
	 * @param parseInstants a boolean
	 * 
	 * @return whether or not instants should be parsed
	 * @since 1.1.0
	 */
	@Builder.Default
	boolean parseInstants = true;
	/**
	 * Whether or not string instants (according to 
	 * <a href="https://datatracker.ietf.org/doc/html/rfc3339#section-5.6">RFC 3339, Section 5.6</a>)
	 * should be parsed as such.
	 * Ignored if {@link #isParseInstants()} is {@code false}
	 * <p>
	 * Default: {@code true}
	 * <p>
	 * <i>This is a {@link JSONParser Parser}-only option</i>
	 * 
	 * @param parseStringInstants a boolean
	 * 
	 * @return whether or not string instants should be parsed
	 * @since 1.1.0
	 */
	@Builder.Default
	boolean parseStringInstants = true;
	/**
	 * Whether or not unix instants (integers) should be parsed as such.
	 * Ignored if {@link #isParseInstants()} is {@code false}
	 * <p>
	 * Default: {@code true}
	 * <p>
	 * <i>This is a {@link JSONParser Parser}-only option</i>
	 * 
	 * @param parseUnixInstants a boolean
	 * 
	 * @return whether or not unix instants should be parsed
	 * @since 1.1.0
	 */
	@Builder.Default
	boolean parseUnixInstants = true;
	
	/**
	 * Whether or not instants should be stringifyed as unix timestamps.
	 * If this is {@code false}, instants will be stringifyed as strings
	 * (according to <a href="https://datatracker.ietf.org/doc/html/rfc3339#section-5.6">RFC 3339, Section 5.6</a>).
	 * <p>
	 * Default: {@code false}
	 * <p>
	 * <i>This is a {@link JSONStringify Stringify}-only option</i>
	 * 
	 * @param stringifyUnixInstants a boolean
	 * 
	 * @return whether or not instants should be stringifyed as unix timestamps
	 * @since 1.1.0
	 */
	@Builder.Default
	boolean stringifyUnixInstants = false;
	
	/**
	 * Whether or not {@code NaN} should be allowed as a number
	 * <p>
	 * Default: {@code true}
	 * 
	 * @param allowNaN a boolean
	 * 
	 * @return whether or not {@code NaN} should be allowed
	 * @since 1.1.0
	 */
	@Builder.Default
	boolean allowNaN = true;
	
	/**
	 * Whether or not {@code Infinity} should be allowed as a number.
	 * This applies to both {@code +Infinity} and {@code -Infinity}
	 * <p>
	 * Default: {@code true}
	 * 
	 * @param allowInfinity a boolean
	 * 
	 * @return whether or not {@code Infinity} should be allowed
	 * @since 1.1.0
	 */
	@Builder.Default
	boolean allowInfinity = true;
	
	/**
	 * Whether or not invalid unicode surrogate pairs should be allowed
	 * <p>
	 * Default: {@code true}
	 * <p>
	 * <i>This is a {@link JSONParser Parser}-only option</i>
	 * 
	 * @param allowInvalidSurrogates a boolean
	 * 
	 * @return whether or not invalid unicode surrogate pairs should be allowed
	 * @since 1.1.0
	 */
	@Builder.Default
	boolean allowInvalidSurrogates = true;
	
	/**
	 * Whether or not string should be single-quoted ({@code '}) instead of double-quoted ({@code "}).
	 * This also includes a {@link JSONObject JSONObject's} member names
	 * <p>
	 * Default: {@code false}
	 * <p>
	 * <i>This is a {@link JSONStringify Stringify}-only option</i>
	 * 
	 * @param quoteSingle a boolean
	 * 
	 * @return whether or not string should be single-quoted
	 * @since 1.1.0
	 */
	@Builder.Default
	boolean quoteSingle = false;
	
}
