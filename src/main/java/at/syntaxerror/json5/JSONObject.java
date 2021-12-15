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

import java.math.BigDecimal;
import java.math.BigInteger;
import java.time.Instant;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.Map.Entry;
import java.util.function.Predicate;

/**
 * A JSONObject is a map (key-value) structure capable of holding multiple values, including other
 * {@link JSONArray JSONArrays} and JSONObjects
 *
 * @author SyntaxError404
 */
public class JSONObject implements Iterable<Map.Entry<String, Object>> {

  private final Map<String, Object> values;

  public JSONObject() {
    values = new HashMap<>();
  }

  public JSONObject(String source) {
    this(new JSONParser(source));
  }

  public JSONObject(JSONParser parser) {
    this();

    char c;
    String key;

    if (parser.nextClean() != '{') {
      throw parser.syntaxError("A JSONObject must begin with '{'");
    }

    while (true) {
      c = parser.nextClean();

      switch (c) {
        case 0:
          throw parser.syntaxError("A JSONObject must end with '}'");
        case '}':
          return;
        default:
          parser.back();
          key = parser.nextMemberName();
      }

      if (has(key)) {
        throw new JSONException("Duplicate key " + JSONStringify.quote(key));
      }

      c = parser.nextClean();

      if (c != ':') {
        throw parser.syntaxError("Expected ':' after a key, got '" + c + "' instead");
      }

      Object value = parser.nextValue();

      values.put(key, value);

      c = parser.nextClean();

      if (c == '}') {
        return;
      }

      if (c != ',') {
        throw parser.syntaxError("Expected ',' or '}' after value, got '" + c + "' instead");
      }
    }
  }

  /**
   * Converts the JSONObject into a map. All JSONObjects and JSONArrays contained within this
   * JSONObject will be converted into their Map or List form as well
   */
  public Map<String, Object> toMap() {
    Map<String, Object> map = new HashMap<>();

    for (Entry<String, Object> entry : this) {
      Object value = entry.getValue();

      if (value instanceof JSONObject) {
        value = ((JSONObject) value).toMap();
      } else if (value instanceof JSONArray) {
        value = ((JSONArray) value).toList();
      }

      map.put(entry.getKey(), value);
    }

    return map;
  }

  @Override
  public Iterator<Entry<String, Object>> iterator() {
    return values.entrySet().iterator();
  }

  /**
   * Checks if a key exists within the JSONObject
   */
  public boolean has(String key) {
    return values.containsKey(key);
  }

  /**
   * Checks if the value with the specified key is a string
   *
   * @throws JSONException if the key does not exist
   */
  public boolean isString(String key) {
    Object value = checkKey(key);
    return value instanceof String || value instanceof Instant;
  }

  /**
   * Checks if the value with the specified key is a number
   *
   * @throws JSONException if the key does not exist
   */
  public boolean isNumber(String key) {
    Object value = checkKey(key);
    return value instanceof Number || value instanceof Instant;
  }

  /**
   * Checks if the value with the specified key is an Instant
   * @throws JSONException if the key does not exist
   * @since 1.1.0
   */
  public boolean isInstant(String key) {
    return checkKey(key) instanceof Instant;
  }

  // -- GET --

  /**
   * Returns the value as a string for a given key
   *
   * @param key the key
   * @return the string
   * @throws JSONException if the key does not exist, or if the value is not a string
   */
  public String getString(String key) {
    if (isInstant(key)) {
      return getInstant(key).toString();
    }

    return checkType(this::isString, key, "string");
  }

  /**
   * Returns the value as a number for a given key
   *
   * @param key the key
   * @return the number
   * @throws JSONException if the key does not exist, or if the value is not a number
   */
  public Number getNumber(String key) {
    if (isInstant(key)) {
      return getInstant(key).getEpochSecond();
    }

    return checkType(this::isNumber, key, "number");
  }

  /**
   * Returns the value as a long for a given key
   *
   * @throws JSONException if the key does not exist, or if the value is not a long
   */
  public long getLong(String key) {
    return getNumber(key).longValue();
  }
  /**
   * Returns the value as a double for a given key
   *
   * @param key the key
   * @return the double
   *
   * @throws JSONException if the key does not exist, or if the value is not a double
   */
  public double getDouble(String key) {
    return getNumber(key).doubleValue();
  }

  /**
   * Returns the value as an Instant for a given key
   *
   * @throws JSONException if the key does not exist, or if the value is not an Instant
   */
  public Instant getInstant(String key) {
    return checkType(this::isInstant, key, "instant");
  }

  /**
   * Sets the value at a given key
   *
   * @param key   the key
   * @param value the new value
   * @return this JSONObject
   */
  public JSONObject set(String key, Object value) {
    values.put(key, sanitize(value));
    return this;
  }

  // -- STRINGIFY --

  /**
   * Converts the JSONObject into its string representation. The indentation factor enables
   * pretty-printing and defines how many spaces (' ') should be placed before each key/value pair.
   * A factor of {@code < 1} disables pretty-printing and discards any optional whitespace
   * characters.
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
   * @param indentFactor the indentation factor
   * @return the string representation
   * @see JSONStringify#toString(JSONObject, int)
   */
  public String toString(int indentFactor) {
    return JSONStringify.toString(this, indentFactor);
  }

  /**
   * Converts the JSONObject into its compact string representation.
   *
   * @return the compact string representation
   */
  @Override
  public String toString() {
    return toString(0);
  }

  // -- MISCELLANEOUS --

  private Object checkKey(String key) {
    if (!values.containsKey(key)) {
      throw new JSONException("JSONObject[" + JSONStringify.quote(key) + "] does not exist");
    }

    return values.get(key);
  }

  @SuppressWarnings("unchecked")
  private <T> T checkType(Predicate<String> predicate, String key, String type) {
    if (!predicate.test(key)) {
      throw mismatch(key, type);
    }

    return (T) values.get(key);
  }

  private static JSONException mismatch(String key, String type) {
    return new JSONException("JSONObject[" + JSONStringify.quote(key) + "] is not of type " + type);
  }

  /**
   * Sanitizes an input value
   *
   * @throws JSONException if the value is illegal
   */
  static Object sanitize(Object value) {
    if (value == null) {
      return null;
    }

    if (value instanceof Boolean ||
        value instanceof String ||
        value instanceof JSONObject ||
        value instanceof JSONArray ||
        value instanceof Instant) {
      return value;
    } else if (value instanceof Number) {
      Number num = (Number) value;

      if (value instanceof Double) {
        double d = (Double) num;

        if (Double.isFinite(d)) {
          return BigDecimal.valueOf(d);
        }
      } else if (value instanceof Float) {
        float f = (Float) num;

        if (Float.isFinite(f)) {
          return BigDecimal.valueOf(f);
        }

        // NaN and Infinity
        return num.doubleValue();
      } else if (value instanceof Byte ||
          value instanceof Short ||
          value instanceof Integer ||
          value instanceof Long) {
        return BigInteger.valueOf(num.longValue());
      } else if (!(value instanceof BigDecimal ||
          value instanceof BigInteger)) {
        return BigDecimal.valueOf(num.doubleValue());
      }

      return num;
    } else {
      throw new JSONException("Illegal type '" + value.getClass() + "'");
    }
  }

}
