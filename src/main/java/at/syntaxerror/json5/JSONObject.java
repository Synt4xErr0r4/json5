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
import java.util.Set;
import java.util.function.Function;
import java.util.function.Predicate;

/**
 * A JSONObject is a map (key-value) structure capable of holding multiple values,
 * including other {@link JSONArray JSONArrays} and JSONObjects
 * 
 * @author SyntaxError404
 * 
 */
public class JSONObject implements Iterable<Map.Entry<String, Object>> {

	private Map<String, Object> values;
	
	/**
	 * Constructs a new JSONObject
	 */
	public JSONObject() {
		values = new HashMap<>();
	}
	
	/**
	 * Constructs a new JSONObject from a string
	 * 
	 * @param source a string
	 */
	public JSONObject(String source) {
		this(new JSONParser(source));
	}
	
	/**
	 * Constructs a new JSONObject from a JSONParser
	 * 
	 * @param parser a JSONParser
	 */
	public JSONObject(JSONParser parser) {
		this();
		
		char c;
		String key;
		
		if(parser.nextClean() != '{')
			throw parser.syntaxError("A JSONObject must begin with '{'");
		
		while(true) {
			c = parser.nextClean();
			
			switch(c) {
			case 0:
				throw parser.syntaxError("A JSONObject must end with '}'");
			case '}':
				return;
			default:
				parser.back();
				key = parser.nextMemberName();
			}
			
			if(has(key))
				throw new JSONException("Duplicate key " + JSONStringify.quote(key));
			
			c = parser.nextClean();
			
			if(c != ':')
				throw parser.syntaxError("Expected ':' after a key, got '" + c + "' instead");
			
			Object value = parser.nextValue();
			
			values.put(key, value);
			
			c = parser.nextClean();
			
			if(c == '}')
				return;
			
			if(c != ',')
				throw parser.syntaxError("Expected ',' or '}' after value, got '" + c + "' instead");
		}
	}
	
	//
	
	/**
	 * Converts the JSONObject into a map. All JSONObjects and JSONArrays
	 * contained within this JSONObject will be converted into their
	 * Map or List form as well
	 * 
	 * @return a map of the entries of this object
	 */
	public Map<String, Object> toMap() {
		Map<String, Object> map = new HashMap<>();
		
		for(Entry<String, Object> entry : this) {
			Object value = entry.getValue();
			
			if(value instanceof JSONObject)
				value = ((JSONObject) value).toMap();
			
			else if(value instanceof JSONArray)
				value = ((JSONArray) value).toList();
			
			map.put(entry.getKey(), value);
		}
		
		return map;
	}

	/**
	 * Returns a set of keys of the JSONObject
	 * 
	 * @return a set of keys
	 * 
	 * @see Map#keySet()
	 */
	public Set<String> keySet() {
		return values.keySet();
	}
	
	/**
     * Returns a set of entries of the JSONObject. Modifying the set 
     * or an entry will modify the JSONObject
     *
     * Use with caution.
     *
     * @return a set of entries
     *
     * @see Map#entrySet()
     */
	public Set<Entry<String, Object>> entrySet() {
		return values.entrySet();
	}
	
	@Override
	public Iterator<Entry<String, Object>> iterator() {
		return values.entrySet().iterator();
	}
	
	/**
	 * Returns the number of entries in the JSONObject
	 * 
	 * @return the number of entries
	 */
	public int length() {
		return values.size();
	}
	
	/**
	 * Removes all values from this JSONObject
	 * 
	 * @since 1.2.0
	 */
	public void clear() {
		values.clear();
	}
	
	/**
	 * Removes a key from a JSONObject
	 * 
	 * @param key the key to be removed
	 * @since 1.2.0
	 * 
	 * @throws JSONException if the key does not exist
	 */
	public void remove(String key) {
		checkKey(key);
		values.remove(key);
	}
	
	// -- CHECK --
	
	/**
	 * Checks if a key exists within the JSONObject
	 * 
	 * @param key the key
	 * @return whether or not the key exists
	 */
	public boolean has(String key) {
		return values.containsKey(key);
	}

	/**
	 * Checks if the value with the specified key is {@code null}
	 * 
	 * @param key the key
	 * @return whether or not the value is {@code null}
	 * 
	 * @throws JSONException if the key does not exist
	 */
	public boolean isNull(String key) {
		return checkKey(key) == null;
	}

	/**
	 * Checks if the value with the specified key is a boolean
	 * 
	 * @param key the key
	 * @return whether or not the value is a boolean
	 * 
	 * @throws JSONException if the key does not exist
	 */
	public boolean isBoolean(String key) {
		return checkKey(key) instanceof Boolean;
	}

	/**
	 * Checks if the value with the specified key is a string
	 * 
	 * @param key the key
	 * @return whether or not the value is a string
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
	 * @param key the key
	 * @return whether or not the value is a number
	 * 
	 * @throws JSONException if the key does not exist
	 */
	public boolean isNumber(String key) {
		Object value = checkKey(key);
		return value instanceof Number || value instanceof Instant;
	}

	/**
	 * Checks if the value with the specified key is a JSONObject
	 * 
	 * @param key the key
	 * @return whether or not the value is a JSONObject
	 * 
	 * @throws JSONException if the key does not exist
	 */
	public boolean isObject(String key) {
		return checkKey(key) instanceof JSONObject;
	}

	/**
	 * Checks if the value with the specified key is a JSONArray
	 * 
	 * @param key the key
	 * @return whether or not the value is a JSONArray
	 * 
	 * @throws JSONException if the key does not exist
	 */
	public boolean isArray(String key) {
		return checkKey(key) instanceof JSONArray;
	}

	/**
	 * Checks if the value with the specified key is an Instant
	 * 
	 * @param key the key
	 * @return whether or not the value is an Instant
	 * @since 1.1.0
	 * 
	 * @throws JSONException if the key does not exist
	 */
	public boolean isInstant(String key) {
		return checkKey(key) instanceof Instant;
	}
	
	// -- GET --

	/**
	 * Returns the value for a given key
	 * 
	 * @param key the key
	 * @return the value
	 * 
	 * @throws JSONException if the key does not exist
	 */
	public Object get(String key) {
		checkKey(key);
		return values.get(key);
	}

	/**
	 * Returns the value as a boolean for a given key
	 * 
	 * @param key the key
	 * @return the boolean
	 * 
	 * @throws JSONException if the key does not exist, or if the value is not a boolean
	 */
	public boolean getBoolean(String key) {
		return checkType(this::isBoolean, key, "boolean");
	}

	/**
	 * Returns the value as a string for a given key
	 * 
	 * @param key the key
	 * @return the string
	 * 
	 * @throws JSONException if the key does not exist, or if the value is not a string
	 */
	public String getString(String key) {
		if(isInstant(key))
			return getInstant(key).toString();
		
		return checkType(this::isString, key, "string");
	}

	/**
	 * Returns the value as a number for a given key
	 * 
	 * @param key the key
	 * @return the number
	 * 
	 * @throws JSONException if the key does not exist, or if the value is not a number
	 */
	public Number getNumber(String key) {
		if(isInstant(key))
			return getInstant(key).getEpochSecond();
		
		return checkType(this::isNumber, key, "number");
	}

	/**
	 * Returns the value as a byte for a given key
	 * 
	 * @param key the key
	 * @return the byte
	 * 
	 * @throws JSONException if the key does not exist, or if the value is not a byte
	 */
	public byte getByte(String key) {
		return getNumber(key).byteValue();
	}
	/**
	 * Returns the value as a short for a given key
	 * 
	 * @param key the key
	 * @return the short
	 * 
	 * @throws JSONException if the key does not exist, or if the value is not a short
	 */
	public short getShort(String key) {
		return getNumber(key).shortValue();
	}
	/**
	 * Returns the value as an int for a given key
	 * 
	 * @param key the key
	 * @return the int
	 * 
	 * @throws JSONException if the key does not exist, or if the value is not an int
	 */
	public int getInt(String key) {
		return getNumber(key).intValue();
	}
	/**
	 * Returns the value as a long for a given key
	 * 
	 * @param key the key
	 * @return the long
	 * 
	 * @throws JSONException if the key does not exist, or if the value is not a long
	 */
	public long getLong(String key) {
		return getNumber(key).longValue();
	}

	/**
	 * Returns the value as a float for a given key
	 * 
	 * @param key the key
	 * @return the float
	 * 
	 * @throws JSONException if the key does not exist, or if the value is not a float
	 */
	public float getFloat(String key) {
		return getNumber(key).floatValue();
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
	
	private <T> T getNumberExact(String key, String type, Function<BigInteger, T> bigint, Function<BigDecimal, T> bigdec) {
		Number number = getNumber(key);
		
		try {
			if(number instanceof BigInteger)
				return bigint.apply((BigInteger) number);

			if(number instanceof BigDecimal)
				return bigdec.apply((BigDecimal) number);
			
		} catch (Exception e) { }
		
		throw mismatch(key, type);
	}

	/**
	 * Returns the exact value as a byte for a given key.
	 * This fails if the value does not fit into a byte
	 * 
	 * @param key the key
	 * @return the byte
	 * 
	 * @throws JSONException if the key does not exist, the value is not a byte, or if the value does not fit into a byte
	 */
	public byte getByteExact(String key) {
		return getNumberExact(key, "byte", BigInteger::byteValueExact, BigDecimal::byteValueExact);
	}
	/**
	 * Returns the exact value as a short for a given key.
	 * This fails if the value does not fit into a short
	 * 
	 * @param key the key
	 * @return the short
	 * 
	 * @throws JSONException if the key does not exist, the value is not a short, or if the value does not fit into a short
	 */
	public short getShortExact(String key) {
		return getNumberExact(key, "short", BigInteger::shortValueExact, BigDecimal::shortValueExact);
	}
	/**
	 * Returns the exact value as an int for a given key.
	 * This fails if the value does not fit into an int
	 * 
	 * @param key the key
	 * @return the int
	 * 
	 * @throws JSONException if the key does not exist, the value is not an int, or if the value does not fit into an int
	 */
	public int getIntExact(String key) {
		return getNumberExact(key, "int", BigInteger::intValueExact, BigDecimal::intValueExact);
	}
	/**
	 * Returns the exact value as a long for a given key.
	 * This fails if the value does not fit into a long
	 * 
	 * @param key the key
	 * @return the long
	 * 
	 * @throws JSONException if the key does not exist, the value is not a long, or if the value does not fit into a long
	 */
	public long getLongExact(String key) {
		return getNumberExact(key, "long", BigInteger::longValueExact, BigDecimal::longValueExact);
	}

	/**
	 * Returns the exact value as a float for a given key.
	 * This fails if the value does not fit into a float
	 * 
	 * @param key the key
	 * @return the float
	 * 
	 * @throws JSONException if the key does not exist, the value is not a float, or if the value does not fit into a float
	 */
	public float getFloatExact(String key) {
		Number num = getNumber(key);
		
		if(num instanceof Double) // NaN and Infinity
			return ((Double) num).floatValue();
		
		float f = num.floatValue();
		
		if(!Float.isFinite(f))
			throw mismatch(key, "float");
		
		return f;
	}
	/**
	 * Returns the exact value as a double for a given key.
	 * This fails if the value does not fit into a double
	 * 
	 * @param key the key
	 * @return the double
	 * 
	 * @throws JSONException if the key does not exist, the value is not a double, or if the value does not fit into a double
	 */
	public double getDoubleExact(String key) {
		Number num = getNumber(key);
		
		if(num instanceof Double) // NaN and Infinity
			return (Double) num;
		
		double d = num.doubleValue();
		
		if(!Double.isFinite(d))
			throw mismatch(key, "double");
		
		return d;
	}

	/**
	 * Returns the value as a JSONObject for a given key
	 * 
	 * @param key the key
	 * @return the JSONObject
	 * 
	 * @throws JSONException if the key does not exist, or if the value is not a JSONObject
	 */
	public JSONObject getObject(String key) {
		return checkType(this::isObject, key, "object");
	}

	/**
	 * Returns the value as a JSONArray for a given key
	 * 
	 * @param key the key
	 * @return the JSONArray
	 * 
	 * @throws JSONException if the key does not exist, or if the value is not a JSONArray
	 */
	public JSONArray getArray(String key) {
		return checkType(this::isArray, key, "array");
	}

	/**
	 * Returns the value as an Instant for a given key
	 * 
	 * @param key the key
	 * @return the Instant
	 * @since 1.1.0
	 * 
	 * @throws JSONException if the key does not exist, or if the value is not an Instant
	 */
	public Instant getInstant(String key) {
		return checkType(this::isInstant, key, "instant");
	}
	
	// -- OPTIONAL --
	
	private <T> T getOpt(String key, Function<String, T> supplier, T defaults) {
		try {
			return supplier.apply(key);
		} catch (Exception e) {
			return defaults;
		}
	}
	
	/**
	 * Returns the value for a given key, or the default value if the operation is not possible
	 * 
	 * @param key the key
	 * @param defaults the default value
	 * @return the value
	 */
	public Object get(String key, Object defaults) {
		return getOpt(key, this::get, defaults);
	}

	/**
	 * Returns the value as a boolean for a given key, or the default value if the operation is not possible
	 * 
	 * @param key the key
	 * @param defaults the default value
	 * @return the boolean
	 */
	public boolean getBoolean(String key, boolean defaults) {
		return getOpt(key, this::getBoolean, defaults);
	}

	/**
	 * Returns the value as a string for a given key, or the default value if the operation is not possible
	 * 
	 * @param key the key
	 * @param defaults the default value
	 * @return the string
	 */
	public String getString(String key, String defaults) {
		return getOpt(key, this::getString, defaults);
	}

	/**
	 * Returns the value as a number for a given key, or the default value if the operation is not possible
	 * 
	 * @param key the key
	 * @param defaults the default value
	 * @return the number
	 */
	public Number getNumber(String key, Number defaults) {
		return getOpt(key, this::getNumber, defaults);
	}

	/**
	 * Returns the value as a byte for a given key, or the default value if the operation is not possible
	 * 
	 * @param key the key
	 * @param defaults the default value
	 * @return the byte
	 */
	public byte getByte(String key, byte defaults) {
		return getOpt(key, this::getByte, defaults);
	}
	/**
	 * Returns the value as a short for a given key, or the default value if the operation is not possible
	 * 
	 * @param key the key
	 * @param defaults the default value
	 * @return the short
	 */
	public short getShort(String key, short defaults) {
		return getOpt(key, this::getShort, defaults);
	}
	/**
	 * Returns the value as an int for a given key, or the default value if the operation is not possible
	 * 
	 * @param key the key
	 * @param defaults the default value
	 * @return the int
	 */
	public int getInt(String key, int defaults) {
		return getOpt(key, this::getInt, defaults);
	}
	/**
	 * Returns the value as a long for a given key, or the default value if the operation is not possible
	 * 
	 * @param key the key
	 * @param defaults the default value
	 * @return the long
	 */
	public long getLong(String key, long defaults) {
		return getOpt(key, this::getLong, defaults);
	}

	/**
	 * Returns the value as a float for a given key, or the default value if the operation is not possible
	 * 
	 * @param key the key
	 * @param defaults the default value
	 * @return the float
	 */
	public float getFloat(String key, float defaults) {
		return getOpt(key, this::getFloat, defaults);
	}
	/**
	 * Returns the value as a double for a given key, or the default value if the operation is not possible
	 * 
	 * @param key the key
	 * @param defaults the default value
	 * @return the double
	 */
	public double getDouble(String key, double defaults) {
		return getOpt(key, this::getDouble, defaults);
	}

	/**
	 * Returns the exact value as a byte for a given key, or the default value if the operation is not possible
	 * 
	 * @param key the key
	 * @param defaults the default value
	 * @return the byte
	 */
	public byte getByteExact(String key, byte defaults) {
		return getOpt(key, this::getByteExact, defaults);
	}
	/**
	 * Returns the exact value as a short for a given key, or the default value if the operation is not possible
	 * 
	 * @param key the key
	 * @param defaults the default value
	 * @return the short
	 */
	public short getShortExact(String key, short defaults) {
		return getOpt(key, this::getShortExact, defaults);
	}
	/**
	 * Returns the exact value as an int for a given key, or the default value if the operation is not possible
	 * 
	 * @param key the key
	 * @param defaults the default value
	 * @return the int
	 */
	public int getIntExact(String key, int defaults) {
		return getOpt(key, this::getIntExact, defaults);
	}
	/**
	 * Returns the exact value as a long for a given key, or the default value if the operation is not possible
	 * 
	 * @param key the key
	 * @param defaults the default value
	 * @return the long
	 */
	public long getLongExact(String key, long defaults) {
		return getOpt(key, this::getLongExact, defaults);
	}

	/**
	 * Returns the exact value as a float for a given key, or the default value if the operation is not possible
	 * 
	 * @param key the key
	 * @param defaults the default value
	 * @return the float
	 */
	public float getFloatExact(String key, float defaults) {
		return getOpt(key, this::getFloatExact, defaults);
	}
	/**
	 * Returns the exact value as a double for a given key, or the default value if the operation is not possible
	 * 
	 * @param key the key
	 * @param defaults the default value
	 * @return the double
	 */
	public double getDoubleExact(String key, double defaults) {
		return getOpt(key, this::getDoubleExact, defaults);
	}

	/**
	 * Returns the value as a JSONObject for a given key, or the default value if the operation is not possible
	 * 
	 * @param key the key
	 * @param defaults the default value
	 * @return the JSONObject
	 */
	public JSONObject getObject(String key, JSONObject defaults) {
		return getOpt(key, this::getObject, defaults);
	}

	/**
	 * Returns the value as a JSONArray for a given key, or the default value if the operation is not possible
	 * 
	 * @param key the key
	 * @param defaults the default value
	 * @return the JSONArray
	 */
	public JSONArray getArray(String key, JSONArray defaults) {
		return getOpt(key, this::getArray, defaults);
	}
	
	/**
	 * Returns the value as an Instant for a given key, or the default value if the operation is not possible
	 * 
	 * @param key the key
	 * @param defaults the default value
	 * @return the Instant
	 * @since 1.1.0
	 */
	public Instant getInstant(String key, Instant defaults) {
		return getOpt(key, this::getInstant, defaults);
	}
	
	// -- SET --
	
	/**
	 * Sets the value at a given key
	 * 
	 * @param key the key
	 * @param value the new value
	 * @return this JSONObject
	 */
	public JSONObject set(String key, Object value) {
		values.put(key, sanitize(value));
		return this;
	}
	
	// -- STRINGIFY --
	
	/**
	 * Converts the JSONObject into its string representation.
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
	 * @param indentFactor the indentation factor
	 * @return the string representation
	 * 
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
		if(!values.containsKey(key))
			throw new JSONException("JSONObject[" + JSONStringify.quote(key) + "] does not exist");
		
		return values.get(key);
	}
	
	@SuppressWarnings("unchecked")
	private <T> T checkType(Predicate<String> predicate, String key, String type) {
		if(!predicate.test(key))
			throw mismatch(key, type);
		
		return (T) values.get(key);
	}
	
	//
	
	private static JSONException mismatch(String key, String type) {
		return new JSONException("JSONObject[" + JSONStringify.quote(key) +"] is not of type " + type);
	}
	
	/**
	 * Sanitizes an input value
	 * 
	 * @param value the value
	 * @return the sanitized value
	 * 
	 * @throws JSONException if the value is illegal
	 */
	static Object sanitize(Object value) {
		if(value == null)
			return null;
		
		if(value instanceof Boolean ||
			value instanceof String ||
			value instanceof JSONObject ||
			value instanceof JSONArray ||
			value instanceof Instant)
			return value;
		
		else if(value instanceof Number) {
			Number num = (Number) value;
			
			if(value instanceof Double) {
				double d = (Double) num;
				
				if(Double.isFinite(d))
					return BigDecimal.valueOf(d);
			}
			
			else if(value instanceof Float) {
				float f = (Float) num;
				
				if(Float.isFinite(f))
					return BigDecimal.valueOf(f);
				
				// NaN and Infinity
				return num.doubleValue();
			}
			
			else if(value instanceof Byte ||
					value instanceof Short ||
					value instanceof Integer ||
					value instanceof Long)
				return BigInteger.valueOf(num.longValue());
			
			else if(!(value instanceof BigDecimal ||
					value instanceof BigInteger))
				return BigDecimal.valueOf(num.doubleValue());
			
			return num;
		}
		
		else throw new JSONException("Illegal type '" + value.getClass() + "'");
	}
	
}
