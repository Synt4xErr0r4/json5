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
class JSONObject() : Iterable<Map.Entry<String?, Any?>?> {
  private val values: MutableMap<String, Any?> = HashMap()


  constructor(source: String?) : this(JSONParser(source))
  constructor(parser: JSONParser) : this() {
    var c: Char
    var key: String
    if (parser.nextClean() != '{') {
      throw parser.syntaxError("A JSONObject must begin with '{'")
    }
    while (true) {
      c = parser.nextClean()
      key = when (c) {
        0    -> throw parser.syntaxError("A JSONObject must end with '}'")
        '}'  -> return
        else -> {
          parser.back()
          parser.nextMemberName()
        }
      }
      if (has(key)) {
        throw JSONException("Duplicate key " + JSONStringify.quote(key))
      }
      c = parser.nextClean()
      if (c != ':') {
        throw parser.syntaxError("Expected ':' after a key, got '$c' instead")
      }
      val value = parser.nextValue()
      values[key] = value
      c = parser.nextClean()
      if (c == '}') {
        return
      }
      if (c != ',') {
        throw parser.syntaxError("Expected ',' or '}' after value, got '$c' instead")
      }
    }
  }
  /**
   * Converts the JSONObject into a map. All JSONObjects and JSONArrays contained within this
   * JSONObject will be converted into their Map or List form as well
   */
  fun toMap(): Map<String, Any> {
    val map: MutableMap<String, Any> = HashMap()
    for (entry in this) {
      var value = entry.value
      if (value is JSONObject) {
        value = value.toMap()
      } else if (value is JSONArray) {
        value = value.toList()
      }
      map[entry.key] = value
    }
    return map
  }

  override fun iterator(): Iterator<Map.Entry<String, Any?>> {
    return values.entries.iterator()
  }
  /**
   * Checks if a key exists within the JSONObject
   */
  fun has(key: String): Boolean {
    return values.containsKey(key)
  }
  /**
   * Checks if the value with the specified key is a string
   *
   * @throws JSONException if the key does not exist
   */
  fun isString(key: String): Boolean {
    val value = checkKey(key)
    return value is String || value is Instant
  }
  /**
   * Checks if the value with the specified key is a number
   *
   * @throws JSONException if the key does not exist
   */
  fun isNumber(key: String): Boolean {
    val value = checkKey(key)
    return value is Number || value is Instant
  }
  /**
   * Checks if the value with the specified key is an Instant
   * @throws JSONException if the key does not exist
   * @since 1.1.0
   */
  fun isInstant(key: String): Boolean {
    return checkKey(key) is Instant
  }
  // -- GET --
  /**
   * Returns the value as a string for a given key
   *
   * @param key the key
   * @return the string
   * @throws JSONException if the key does not exist, or if the value is not a string
   */
  fun getString(key: String): String {
    return if (isInstant(key)) {
      getInstant(key).toString()
    } else checkType<String>({ key: String ->
      isString(
        key
      )
    }, key, "string")!!
  }
  /**
   * Returns the value as a number for a given key
   *
   * @param key the key
   * @return the number
   * @throws JSONException if the key does not exist, or if the value is not a number
   */
  fun getNumber(key: String): Number {
    return if (isInstant(key)) {
      getInstant(key).epochSecond
    } else checkType<Number>({ key: String ->
      isNumber(
        key
      )
    }, key, "number")!!
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
   * @param key the key
   * @return the double
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
    return checkType<Instant>({ key: String ->
      isInstant(
        key
      )
    }, key, "instant")!!
  }
  /**
   * Sets the value at a given key
   *
   * @param key   the key
   * @param value the new value
   * @return this JSONObject
   */
  operator fun set(key: String, value: Any?): JSONObject {
    values[key] = sanitize(value)
    return this
  }
  // -- STRINGIFY --
  /**
   * Converts the JSONObject into its string representation. The indentation factor enables
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
   *
   * @param indentFactor the indentation factor
   * @return the string representation
   * @see JSONStringify.toString
   */
  fun toString(indentFactor: Int): String {
    return JSONStringify.toString(this, indentFactor)
  }
  /**
   * Converts the JSONObject into its compact string representation.
   *
   * @return the compact string representation
   */
  override fun toString(): String {
    return toString(0)
  }
  // -- MISCELLANEOUS --
  private fun checkKey(key: String): Any? {
    if (!values.containsKey(key)) {
      throw JSONException("JSONObject[" + JSONStringify.quote(key) + "] does not exist")
    }
    return values[key]
  }

  private fun <T> checkType(predicate: Predicate<String>, key: String, type: String): T? {
    if (!predicate.test(key)) {
      throw mismatch(key, type)
    }
    return values[key] as T?
  }

  companion object {
    private fun mismatch(key: String, type: String): JSONException {
      return JSONException("JSONObject[" + JSONStringify.quote(key) + "] is not of type " + type)
    }
    /**
     * Sanitizes an input value
     *
     * @throws JSONException if the value is illegal
     */
    fun sanitize(value: Any?): Any? {
      if (value == null) {
        return null
      }
      return if (value is Boolean ||
        value is String ||
        value is JSONObject ||
        value is JSONArray ||
        value is Instant
      ) {
        value
      } else if (value is Number) {
        val num = value
        if (value is Double) {
          val d = num as Double
          if (java.lang.Double.isFinite(d)) {
            return BigDecimal.valueOf(d)
          }
        } else if (value is Float) {
          val f = num as Float
          return if (java.lang.Float.isFinite(f)) {
            BigDecimal.valueOf(f.toDouble())
          } else num.toDouble()

          // NaN and Infinity
        } else if (value is Byte ||
          value is Short ||
          value is Int ||
          value is Long
        ) {
          return BigInteger.valueOf(num.toLong())
        } else if (!(value is BigDecimal ||
              value is BigInteger)
        ) {
          return BigDecimal.valueOf(num.toDouble())
        }
        num
      } else {
        throw JSONException("Illegal type '" + value.javaClass + "'")
      }
    }
  }
}
