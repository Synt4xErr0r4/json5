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
 * A utility class for serializing [JSONObjects][JSONObject] and [JSONArrays][JSONArray]
 * into their string representations
 *
 * @author SyntaxError404
 */
class JSONStringify private constructor() {
  init {
    throw UnsupportedOperationException("Utility class")
  }

  companion object {
    /**
     * Converts a JSONObject into its string representation. The indentation factor enables
     * pretty-printing and defines how many spaces (' ') should be placed before each key/value pair.
     * A factor of `< 1` disables pretty-printing and discards any optional whitespace
     * characters.
     *
     *
     * `indentFactor = 2`:
     * <pre>
     * {
     * "key0": "value0",
     * "key1": {
     * "nested": 123
     * },
     * "key2": false
     * }
    </pre> *
     *
     *
     * `indentFactor = 0`:
     * <pre>
     * {"key0":"value0","key1":{"nested":123},"key2":false}
    </pre> *
     */
    /**
     * Converts a JSONObject into its string representation. The indentation factor enables
     * pretty-printing and defines how many spaces (' ') should be placed before each key/value pair.
     * A factor of `< 1` disables pretty-printing and discards any optional whitespace
     * characters.
     *
     *
     * `indentFactor = 2`:
     * <pre>
     * {
     * "key0": "value0",
     * "key1": {
     * "nested": 123
     * },
     * "key2": false
     * }
    </pre> *
     *
     *
     * `indentFactor = 0`:
     * <pre>
     * {"key0":"value0","key1":{"nested":123},"key2":false}
    </pre> *
     * This uses the [default options][JSONOptions.defaultOptions]
     */
    @JvmOverloads
    fun toString(`object`: JSONObject, indentFactor: Int, options: JSONOptions? = null): String {
      return toString(
        `object`,
        "",
        Math.max(0, indentFactor),
        options ?: JSONOptions.defaultOptions
      )
    }
    /**
     * Converts a JSONArray into its string representation. The indentation factor enables
     * pretty-printing and defines how many spaces (' ') should be placed before each value. A factor
     * of `< 1` disables pretty-printing and discards any optional whitespace characters.
     *
     *
     * `indentFactor = 2`:
     * <pre>
     * [
     * "value",
     * {
     * "nested": 123
     * },
     * false
     * ]
    </pre> *
     *
     *
     * `indentFactor = 0`:
     * <pre>
     * ["value",{"nested":123},false]
    </pre> *
     */
    /**
     * Converts a JSONArray into its string representation. The indentation factor enables
     * pretty-printing and defines how many spaces (' ') should be placed before each value. A factor
     * of `< 1` disables pretty-printing and discards any optional whitespace characters.
     *
     *
     * `indentFactor = 2`:
     * <pre>
     * [
     * "value",
     * {
     * "nested": 123
     * },
     * false
     * ]
    </pre> *
     *
     *
     * `indentFactor = 0`:
     * <pre>
     * ["value",{"nested":123},false]
    </pre> *
     * This uses the [default options][JSONOptions.defaultOptions]
     */
    @JvmOverloads
    fun toString(array: JSONArray, indentFactor: Int, options: JSONOptions? = null): String {
      return toString(
        array,
        "",
        Math.max(0, indentFactor),
        options ?: JSONOptions.defaultOptions
      )
    }

    private fun toString(
      jsonObject: JSONObject, indent: String, indentFactor: Int,
      options: JSONOptions
    ): String {
      val sb = StringBuilder()
      val childIndent = indent + " ".repeat(indentFactor)
      sb.append('{')
      jsonObject.forEach { (key, value) ->
        if (sb.length != 1) {
          sb.append(',')
        }
        if (indentFactor > 0) {
          sb.append('\n').append(childIndent)
        }
        sb.append(quote(key, options))
          .append(':')
        if (indentFactor > 0) {
          sb.append(' ')
        }
        sb.append(toString(value, childIndent, indentFactor, options))
      }
      if (indentFactor > 0) {
        sb.append('\n').append(indent)
      }
      sb.append('}')
      return sb.toString()
    }

    private fun toString(
      array: JSONArray, indent: String, indentFactor: Int,
      options: JSONOptions
    ): String {
      val sb = StringBuilder()
      val childIndent = indent + " ".repeat(indentFactor)
      sb.append('[')
      for (value in array) {
        if (sb.length != 1) {
          sb.append(',')
        }
        if (indentFactor > 0) {
          sb.append('\n').append(childIndent)
        }
        sb.append(toString(value, childIndent, indentFactor, options))
      }
      if (indentFactor > 0) {
        sb.append('\n').append(indent)
      }
      sb.append(']')
      return sb.toString()
    }

    private fun toString(
      value: Any?, indent: String, indentFactor: Int,
      options: JSONOptions
    ): String {
      return when (value) {
        null          -> "null"
        is JSONObject -> toString(value, indent, indentFactor, options)
        is JSONArray  -> toString(value, indent, indentFactor, options)
        is String     -> quote(value as String?, options)
        is Instant    -> {
          if (options.stringifyUnixInstants) {
            value.epochSecond.toString()
          } else quote(value.toString(), options)
        }
        is Double     -> {
          when {
            !options.allowNaN && value.isNaN()           -> throw JSONException("Illegal NaN in JSON")
            !options.allowInfinity && value.isInfinite() -> throw JSONException("Illegal Infinity in JSON")
            else                                         -> value.toString()
          }
          value.toString()
        }
        else          -> value.toString()
      }
    }

    fun quote(string: String?): String {
      return quote(string)
    }

    private fun quote(string: String?, options: JSONOptions = JSONOptions.defaultOptions): String {
      if (string == null || string.isEmpty()) {
        return if (options.quoteSingle) "''" else "\"\""
      }
      val qt = if (options.quoteSingle) '\'' else '"'
      val quoted = StringBuilder(string.length + 2)
      quoted.append(qt)
      for (c in string.toCharArray()) {
        if (c == qt) {
          quoted.append('\\')
          quoted.append(c)
          continue
        }
        when (c) {
          '\\'             -> quoted.append("\\\\")
          '\b'             -> quoted.append("\\b")
          FormFeed.char    -> quoted.append(FormFeed.representation)
          '\n'             -> quoted.append("\\n")
          '\r'             -> quoted.append("\\r")
          '\t'             -> quoted.append("\\t")
          VerticalTab.char -> quoted.append(VerticalTab.representation)
          else             -> when (c.category) {
           CharCategory.FORMAT,
           CharCategory.LINE_SEPARATOR,
           CharCategory.PARAGRAPH_SEPARATOR,
           CharCategory.CONTROL,
           CharCategory.PRIVATE_USE,
           CharCategory.SURROGATE,
           CharCategory.UNASSIGNED -> {
              quoted.append("\\u")
              quoted.append(String.format("%04X", c))
            }
            else                                                                                                                                                           -> quoted.append(
              c
            )
          }
        }
      }
      quoted.append(qt)
      return quoted.toString()
    }
  }
}
