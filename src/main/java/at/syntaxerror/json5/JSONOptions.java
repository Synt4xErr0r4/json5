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


/**
 * This class used is used to customize the behaviour of {@link JSONParser parsing} and {@link JSONStringify stringifying}
 *
 * @author SyntaxError404
 * @since 1.1.0
 */
public class JSONOptions {

  /**
   * The default options for parsing and stringifying
   */
  public static JSONOptions defaultOptions = new JSONOptions();

  /**
   * Whether instants should be parsed as such.
   * If this is {@code false}, {@link #parseStringInstants} and {@link #parseUnixInstants}
   * are ignored
   * <p>
   * Default: {@code true}
   * <p>
   * <i>This is a {@link JSONParser Parser}-only option</i>
   */
  public boolean parseInstants = true;
  /**
   * Whether string instants (according to
   * <a href="https://datatracker.ietf.org/doc/html/rfc3339#section-5.6">RFC 3339, Section 5.6</a>)
   * should be parsed as such.
   * Ignored if {@link #parseInstants} is {@code false}
   * <p>
   * Default: {@code true}
   * <p>
   * <i>This is a {@link JSONParser Parser}-only option</i>
   */
  public boolean parseStringInstants = true;
  /**
   * Whether unix instants (integers) should be parsed as such.
   * Ignored if {@link #parseInstants} is {@code false}
   * <p>
   * Default: {@code true}
   * <p>
   * <i>This is a {@link JSONParser Parser}-only option</i>
   */
  public boolean parseUnixInstants = true;

  /**
   * Whether instants should be stringifyed as unix timestamps.
   * If this is {@code false}, instants will be stringifyed as strings
   * (according to <a href="https://datatracker.ietf.org/doc/html/rfc3339#section-5.6">RFC 3339, Section 5.6</a>).
   * <p>
   * Default: {@code false}
   * <p>
   * <i>This is a {@link JSONStringify Stringify}-only option</i>
   */
  public boolean stringifyUnixInstants = false;

  /**
   * Whether {@code NaN} should be allowed as a number
   * <p>
   * Default: {@code true}
   */
  public boolean allowNaN = true;

  /**
   * Whether {@code Infinity} should be allowed as a number.
   * This applies to both {@code +Infinity} and {@code -Infinity}
   * <p>
   * Default: {@code true}
   * */
  public boolean allowInfinity = true;

  /**
   * Whether invalid unicode surrogate pairs should be allowed
   * <p>
   * Default: {@code true}
   * <p>
   * <i>This is a {@link JSONParser Parser}-only option</i>
   */
  public boolean allowInvalidSurrogates = true;

  /**
   * Whether string should be single-quoted ({@code '}) instead of double-quoted ({@code "}).
   * This also includes a {@link JSONObject JSONObject's} member names
   * <p>
   * Default: {@code false}
   * <p>
   * <i>This is a {@link JSONStringify Stringify}-only option</i>
   */
  public boolean quoteSingle = false;

}
