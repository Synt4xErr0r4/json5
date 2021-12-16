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
 * FITNESS FOR A PARTICULAR PURPOSE AND NON-INFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE
 * SOFTWARE.
 */
package at.syntaxerror.json5

/**
 * This class used is used to customize the behaviour of [parsing][JSONParser] and [stringifying][JSONStringify]
 *
 * @author SyntaxError404
 * @since 1.1.0
 */
class JSONOptions {
  /**
   * Whether instants should be parsed as such.
   * If this is `false`, [.parseStringInstants] and [.parseUnixInstants]
   * are ignored
   *
   *
   * Default: `true`
   *
   *
   * *This is a [Parser][JSONParser]-only option*
   */
  var parseInstants = true
  /**
   * Whether string instants (according to
   * [RFC 3339, Section 5.6](https://datatracker.ietf.org/doc/html/rfc3339#section-5.6))
   * should be parsed as such.
   * Ignored if [.parseInstants] is `false`
   *
   *
   * Default: `true`
   *
   *
   * *This is a [Parser][JSONParser]-only option*
   */
  var parseStringInstants = true
  /**
   * Whether unix instants (integers) should be parsed as such.
   * Ignored if [.parseInstants] is `false`
   *
   *
   * Default: `true`
   *
   *
   * *This is a [Parser][JSONParser]-only option*
   */
  var parseUnixInstants = true
  /**
   * Whether instants should be stringifyed as unix timestamps.
   * If this is `false`, instants will be stringifyed as strings
   * (according to [RFC 3339, Section 5.6](https://datatracker.ietf.org/doc/html/rfc3339#section-5.6)).
   *
   *
   * Default: `false`
   *
   *
   * *This is a [Stringify][JSONStringify]-only option*
   */
  var stringifyUnixInstants = false
  /**
   * Whether `NaN` should be allowed as a number
   *
   *
   * Default: `true`
   */
  var allowNaN = true
  /**
   * Whether `Infinity` should be allowed as a number.
   * This applies to both `+Infinity` and `-Infinity`
   *
   *
   * Default: `true`
   */
  var allowInfinity = true
  /**
   * Whether invalid unicode surrogate pairs should be allowed
   *
   *
   * Default: `true`
   *
   *
   * *This is a [Parser][JSONParser]-only option*
   */
  var allowInvalidSurrogates = true
  /**
   * Whether string should be single-quoted (`'`) instead of double-quoted (`"`).
   * This also includes a [JSONObject&#39;s][JSONObject] member names
   *
   *
   * Default: `false`
   *
   *
   * *This is a [Stringify][JSONStringify]-only option*
   */
  var quoteSingle = false

  companion object {
    /**
     * The default options for parsing and stringifying
     */
    var defaultOptions = JSONOptions()
  }
}
