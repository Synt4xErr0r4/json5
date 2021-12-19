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

import at.syntaxerror.json5.UnicodeCharacter.FormFeed
import at.syntaxerror.json5.UnicodeCharacter.VerticalTab
import java.time.Instant

/**
 * A utility class for serializing [JSONObjects][DecodeJson5Object] and [JSONArrays][DecodeJson5Array]
 * into their string representations
 *
 * @author SyntaxError404
 */
class JSONStringify(
  private val options: JSONOptions = JSONOptions.defaultOptions
) {

  private val quoteToken = if (options.quoteSingle) '\'' else '"'
  private val emptyString = "$quoteToken$quoteToken"

  /**
   * Converts a JSONObject into its string representation. The indentation factor enables
   * pretty-printing and defines how many spaces (' ') should be placed before each key/value pair.
   * A factor of `< 1` disables pretty-printing and discards any optional whitespace
   * characters.
   *
   *
   * `indentFactor = 2`:
   * ```
   * {
   *   "key0": "value0",
   *   "key1": {
   *     "nested": 123
   *   },
   *   "key2": false
   * }
   * ```
   *
   * `indentFactor = 0`:
   *
   * ```
   * {"key0":"value0","key1":{"nested":123},"key2":false}
   * ```
   */
  fun encodeObject(
    jsonObject: DecodeJson5Object,
    indentFactor: UInt,
    indent: String = "",
  ): String {
    val childIndent = indent + " ".repeat(indentFactor.toInt())
    val isIndented = indentFactor > 0u

    val sb = StringBuilder()
    sb.append('{')
    jsonObject.forEach { (key, value) ->
      if (sb.length != 1) {
        sb.append(',')
      }
      if (isIndented) {
        sb.append('\n').append(childIndent)
      }
      sb.append(encodeString(key)).append(':')
      if (isIndented) {
        sb.append(' ')
      }
      sb.append(encode(value, childIndent, indentFactor))
    }
    if (isIndented) {
      sb.append('\n').append(indent)
    }
    sb.append('}')
    return sb.toString()
  }

  /**
   * Converts a JSONArray into its string representation. The indentation factor enables
   * pretty-printing and defines how many spaces (' ') should be placed before each value. A factor
   * of `< 1` disables pretty-printing and discards any optional whitespace characters.
   *
   *
   * `indentFactor = 2`:
   * ```
   * [
   *   "value",
   *   {
   *     "nested": 123
   *   },
   *   false
   * ]
   * ```
   *
   * `indentFactor = 0`:
   * ```
   * ["value",{"nested":123},false]
   * ```
   */
  fun encodeArray(
    array: DecodeJson5Array,
    indentFactor: UInt,
    indent: String = "",
  ): String {
    val childIndent = indent + " ".repeat(indentFactor.toInt())
    val isIndented = indentFactor > 0u

    val sb = StringBuilder()
    sb.append('[')
    for (value in array) {
      if (sb.length != 1) {
        sb.append(',')
      }
      if (isIndented) {
        sb.append('\n').append(childIndent)
      }
      sb.append(encode(value, childIndent, indentFactor))
    }
    if (isIndented) {
      sb.append('\n').append(indent)
    }
    sb.append(']')
    return sb.toString()
  }

  private fun encode(
    value: Any?,
    indent: String,
    indentFactor: UInt,
  ): String {
    return when (value) {
      null                 -> "null"
      is DecodeJson5Object -> encodeObject(value, indentFactor, indent)
      is DecodeJson5Array  -> encodeArray(value, indentFactor, indent)
      is String            -> encodeString(value)
      is Instant    -> {
        if (options.stringifyUnixInstants) {
          value.epochSecond.toString()
        } else encodeString(value.toString())
      }
      is Double     -> {
        when {
          !options.allowNaN && value.isNaN()           -> throw JSONException("Illegal NaN in JSON")
          !options.allowInfinity && value.isInfinite() -> throw JSONException("Illegal Infinity in JSON")
          else                                         -> value.toString()
        }
      }
      else          -> value.toString()
    }
  }

  fun encodeString(string: String?): String {
    return if (string.isNullOrEmpty()) {
      emptyString
    } else {
      string
        .asSequence()
        .joinToString(
          separator = "",
          prefix = quoteToken.toString(),
          postfix = quoteToken.toString()
        ) { c: Char ->
          when (c) {
            quoteToken       -> "\\$c"
            '\\'             -> "\\\\"
            '\b'             -> "\\b"
            FormFeed.char    -> FormFeed.representation
            '\n'             -> "\\n"
            '\r'             -> "\\r"
            '\t'             -> "\\t"
            VerticalTab.char -> VerticalTab.representation
            else             -> when (c.category) {
              CharCategory.FORMAT,
              CharCategory.LINE_SEPARATOR,
              CharCategory.PARAGRAPH_SEPARATOR,
              CharCategory.CONTROL,
              CharCategory.PRIVATE_USE,
              CharCategory.SURROGATE,
              CharCategory.UNASSIGNED -> String.format("\\u%04X", c)
              else                    -> c.toString()
            }
          }
        }
    }
  }
}
