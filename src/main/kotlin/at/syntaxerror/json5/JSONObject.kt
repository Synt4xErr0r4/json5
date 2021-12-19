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

import java.math.BigDecimal
import java.math.BigInteger
import java.time.Instant
import java.util.function.Predicate

/**
 * A JSONObject is a map (key-value) structure capable of holding multiple values, including other
 * [JSONArrays][JSONArray] and JSONObjects
 *
 * @author SyntaxError404
 */
class JSONObject(
  private val values: MutableMap<String, Any?> = mutableMapOf()
) : Iterable<Map.Entry<String, Any?>> by values.asIterable() {

  private val stringify: JSONStringify = JSONStringify()

  constructor(
    source: String,
    options: JSONOptions = JSONOptions.defaultOptions
  ) : this(parser = JSONParser(source))

  constructor(parser: JSONParser) : this() {
    var c: Char
    var key: String
    if (parser.nextClean() != '{') {
      throw parser.createSyntaxException("A JSONObject must begin with '{'")
    }
    while (true) {
      c = parser.nextClean()
      key = when (c) {
        Char.MIN_VALUE -> throw parser.createSyntaxException("A JSONObject must end with '}'")
        '}'            -> return
        else           -> {
          parser.back()
          parser.nextMemberName()
        }
      }
      if (has(key)) {
        throw JSONException("Duplicate key ${stringify.encodeString(key)}")
      }
      c = parser.nextClean()
      if (c != ':') {
        throw parser.createSyntaxException("Expected ':' after a key, got '$c' instead")
      }
      val value = parser.nextValue()
      values[key] = value
      c = parser.nextClean()
      when {
        c == '}' -> return // finish parsing this array
        c != ',' -> throw parser.createSyntaxException("Expected ',' or '}' after value, got '$c' instead")
      }
    }
  }

  /**
   * Converts the JSONObject into a map. All JSONObjects and JSONArrays contained within this
   * JSONObject will be converted into their Map or List form as well
   */
  fun toMap(): Map<String, Any?> {
    return values.mapValues { (_, value) ->
      when (value) {
        is JSONObject -> value.toMap()
        is JSONArray  -> value.toList()
        else          -> value
      }
    }
  }

  /**
   * Checks if a key exists within the [JSONObject]
   */
  fun has(key: String): Boolean {
    return values.containsKey(key)
  }
  /**
   * Checks if the value with the specified key is a [String] or an [Instant]
   *
   * @throws JSONException if the key does not exist
   */
  fun isString(key: String): Boolean {
    return when (checkKey(key)) {
      is String, is Instant -> true
      else                  -> false
    }
  }
  /**
   * Checks if the value with the specified key is a [Number] or an [Instant]
   *
   * @throws JSONException if the key does not exist
   */
  fun isNumber(key: String): Boolean {
    return when (checkKey(key)) {
      is Number, is Instant -> true
      else                  -> false
    }
  }
  /**
   * Checks if the value with the specified key is an Instant
   *
   * @throws JSONException if the key does not exist
   */
  fun isInstant(key: String): Boolean {
    return checkKey(key) is Instant
  }

  /**
   * Returns the value as a string for a given key
   *
   * @throws JSONException if the key does not exist, or if the value is not a string
   */
  fun getString(key: String): String {
    return if (isInstant(key)) {
      getInstant(key).toString()
    } else checkType<String>(::isString, key, "string")!!
  }
  /**
   * Returns the value as a number for a given key
   *
   * @throws JSONException if the key does not exist, or if the value is not a number
   */
  fun getNumber(key: String): Number {
    return if (isInstant(key)) {
      getInstant(key).epochSecond
    } else {
      checkType<Number>(::isNumber, key, "number")!!
    }
  }
  /**
   * Returns the value as a long for a given key
   *
   * @throws JSONException if the key does not exist, or if the value is not a long
   */
  fun getLong(key: String): Long {
    return getNumber(key).toLong()
  }
  /**
   * Returns the value as a double for a given key
   *
   * @throws JSONException if the key does not exist, or if the value is not a double
   */
  fun getDouble(key: String): Double {
    return getNumber(key).toDouble()
  }
  /**
   * Returns the value as an Instant for a given key
   *
   * @throws JSONException if the key does not exist, or if the value is not an Instant
   */
  fun getInstant(key: String): Instant {
    return checkType<Instant>(::isInstant, key, "instant")!!
  }
  /**
   * Sets the value at a given key
   */
  operator fun set(key: String, value: Any?): JSONObject {
    values[key] = sanitize(value)
    return this
  }

  /**
   * Converts the JSONObject into its string representation. The indentation factor enables
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
   * ```
   * {"key0":"value0","key1":{"nested":123},"key2":false}
   * ```
   *
   * @param indentFactor the indentation factor
   * @see JSONStringify.encodeObject
   */
  fun toString(indentFactor: UInt): String {
    return stringify.encodeObject(this, indentFactor)
  }

  /**
   * Converts the JSONObject into its compact string representation.
   */
  override fun toString(): String {
    return toString(0u)
  }

  private fun checkKey(key: String): Any? {
    if (!values.containsKey(key)) {
      throw JSONException("JSONObject[" + stringify.encodeString(key) + "] does not exist")
    }
    return values[key]
  }

  private inline fun <reified T> checkType(
    predicate: Predicate<String>,
    key: String,
    type: String
  ): T? {
    if (!predicate.test(key)) {
      throw JSONException("JSONObject[${stringify.encodeString(key)}] is not of type $type")
    }
    return values[key] as? T
  }

  companion object {

    /**
     * Sanitizes an input value
     *
     * @throws JSONException if the value is illegal
     */
    fun sanitize(value: Any?): Any? {
      if (value == null) {
        return null
      }
      return when (value) {
        is Boolean, is String, is JSONObject, is JSONArray, is Instant -> {
          value
        }
        is Number                                                      -> {
          if (value is Double) {
            if (value.isFinite()) {
              return BigDecimal.valueOf(value)
            }
          } else if (value is Float) {
            return if (value.isFinite()) {
              BigDecimal.valueOf(value.toDouble())
            } else value.toDouble()

            // NaN and Infinity
          } else if (value is Byte ||
            value is Short ||
            value is Int ||
            value is Long
          ) {
            return BigInteger.valueOf(value.toLong())
          } else if (!(value is BigDecimal ||
                value is BigInteger)
          ) {
            return BigDecimal.valueOf(value.toDouble())
          }
          value
        }
        else                                                           -> {
          throw JSONException("Illegal type '" + value.javaClass + "'")
        }
      }
    }
  }
}
