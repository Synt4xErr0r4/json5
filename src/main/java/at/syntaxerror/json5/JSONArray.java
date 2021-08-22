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
import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;
import java.util.List;
import java.util.function.Function;
import java.util.function.Predicate;

/**
 * A JSONArray is an array structure capable of holding multiple values,
 * including other JSONArrays and {@link JSONObject JSONObjects}
 * 
 * @author SyntaxError404
 * 
 */
public class JSONArray implements Iterable<Object> {

	private List<Object> values;
	
	/**
	 * Constructs a new JSONArray
	 */
	public JSONArray() {
		values = new ArrayList<>();
	}
	
	/**
	 * Constructs a new JSONArray from a string
	 * 
	 * @param source a string
	 */
	public JSONArray(String source) {
		this(new JSONParser(source));
	}
	
	/**
	 * Constructs a new JSONArray from a JSONParser
	 * 
	 * @param parser a JSONParser
	 */
	public JSONArray(JSONParser parser) {
		this();
		
		char c;
		
		if(parser.nextClean() != '[')
			throw parser.syntaxError("A JSONArray must begin with '['");
		
		while(true) {
			c = parser.nextClean();
			
			switch(c) {
			case 0:
				throw parser.syntaxError("A JSONArray must end with ']'");
			case ']':
				return;
			default:
				parser.back();
			}

			Object value = parser.nextValue();
			
			values.add(value);
			
			c = parser.nextClean();
			
			if(c == ']')
				return;
			
			if(c != ',')
				throw parser.syntaxError("Expected ',' or ']' after value, got '" + c + "' instead");
		}
	}
	
	//
	
	/**
	 * Converts the JSONArray into a list. All JSONObjects and JSONArrays
	 * contained within this JSONArray will be converted into their
	 * Map or List form as well
	 * 
	 * @return a list of the values of this array
	 */
	public List<Object> toList() {
		List<Object> list = new ArrayList<>();
		
		for(Object value : this) {
			if(value instanceof JSONObject)
				value = ((JSONObject) value).toMap();
			
			else if(value instanceof JSONArray)
				value = ((JSONArray) value).toList();
			
			list.add(value);
		}
		
		return list;
	}
	
	/**
     * Returns a collection of values of the JSONArray.
     * Modifying the collection will modify the JSONArray
     *
     * Use with caution.
     *
     * @return a set of entries
     */
	public Collection<Object> entrySet() {
		return values;
	}
	
	@Override
	public Iterator<Object> iterator() {
		return values.iterator();
	}
	
	/**
	 * Returns the number of values in the JSONArray
	 * 
	 * @return the number of values
	 */
	public int length() {
		return values.size();
	}
	
	// -- CHECK --

	/**
	 * Checks if the value with the specified index is {@code null}
	 * 
	 * @param index the index
	 * @return whether or not the value is {@code null}
	 * 
	 * @throws JSONException if the index does not exist
	 */
	public boolean isNull(int index) {
		return checkIndex(index) == null;
	}

	/**
	 * Checks if the value with the specified index is a boolean
	 * 
	 * @param index the index
	 * @return whether or not the value is a boolean
	 * 
	 * @throws JSONException if the index does not exist
	 */
	public boolean isBoolean(int index) {
		return checkIndex(index) instanceof Boolean;
	}

	/**
	 * Checks if the value with the specified index is a string
	 * 
	 * @param index the index
	 * @return whether or not the value is a string
	 * 
	 * @throws JSONException if the index does not exist
	 */
	public boolean isString(int index) {
		return checkIndex(index) instanceof String;
	}

	/**
	 * Checks if the value with the specified index is a number
	 * 
	 * @param index the index
	 * @return whether or not the value is a number
	 * 
	 * @throws JSONException if the index does not exist
	 */
	public boolean isNumber(int index) {
		return checkIndex(index) instanceof Number;
	}

	/**
	 * Checks if the value with the specified index is a JSONObject
	 * 
	 * @param index the index
	 * @return whether or not the value is a JSONObject
	 * 
	 * @throws JSONException if the index does not exist
	 */
	public boolean isObject(int index) {
		return checkIndex(index) instanceof JSONObject;
	}

	/**
	 * Checks if the value with the specified index is a JSONArray
	 * 
	 * @param index the index
	 * @return whether or not the value is a JSONArray
	 * 
	 * @throws JSONException if the index does not exist
	 */
	public boolean isArray(int index) {
		return checkIndex(index) instanceof JSONArray;
	}
	
	// -- GET --

	/**
	 * Returns the value for a given index
	 * 
	 * @param index the index
	 * @return the value
	 * 
	 * @throws JSONException if the index does not exist
	 */
	public Object get(int index) {
		checkIndex(index);
		return values.get(index);
	}

	/**
	 * Returns the value as a boolean for a given index
	 * 
	 * @param index the index
	 * @return the boolean
	 * 
	 * @throws JSONException if the index does not exist, or if the value is not a boolean
	 */
	public boolean getBoolean(int index) {
		return checkType(this::isBoolean, index, "boolean");
	}

	/**
	 * Returns the value as a string for a given index
	 * 
	 * @param index the index
	 * @return the string
	 * 
	 * @throws JSONException if the index does not exist, or if the value is not a string
	 */
	public String getString(int index) {
		return checkType(this::isString, index, "string");
	}

	/**
	 * Returns the value as a number for a given index
	 * 
	 * @param index the index
	 * @return the number
	 * 
	 * @throws JSONException if the index does not exist, or if the value is not a number
	 */
	public Number getNumber(int index) {
		return checkType(this::isNumber, index, "number");
	}

	/**
	 * Returns the value as a byte for a given index
	 * 
	 * @param index the index
	 * @return the byte
	 * 
	 * @throws JSONException if the index does not exist, or if the value is not a byte
	 */
	public byte getByte(int index) {
		return getNumber(index).byteValue();
	}
	/**
	 * Returns the value as a short for a given index
	 * 
	 * @param index the index
	 * @return the short
	 * 
	 * @throws JSONException if the index does not exist, or if the value is not a short
	 */
	public short getShort(int index) {
		return getNumber(index).shortValue();
	}
	/**
	 * Returns the value as an int for a given index
	 * 
	 * @param index the index
	 * @return the int
	 * 
	 * @throws JSONException if the index does not exist, or if the value is not an int
	 */
	public int getInt(int index) {
		return getNumber(index).intValue();
	}
	/**
	 * Returns the value as a long for a given index
	 * 
	 * @param index the index
	 * @return the long
	 * 
	 * @throws JSONException if the index does not exist, or if the value is not a long
	 */
	public long getLong(int index) {
		return getNumber(index).longValue();
	}

	/**
	 * Returns the value as a float for a given index
	 * 
	 * @param index the index
	 * @return the float
	 * 
	 * @throws JSONException if the index does not exist, or if the value is not a float
	 */
	public float getFloat(int index) {
		return getNumber(index).floatValue();
	}
	/**
	 * Returns the value as a double for a given index
	 * 
	 * @param index the index
	 * @return the double
	 * 
	 * @throws JSONException if the index does not exist, or if the value is not a double
	 */
	public double getDouble(int index) {
		return getNumber(index).doubleValue();
	}
	
	private <T> T getNumberExact(int index, String type, Function<BigInteger, T> bigint, Function<BigDecimal, T> bigdec) {
		Number number = getNumber(index);
		
		try {
			
			if(number instanceof BigInteger)
				return bigint.apply((BigInteger) number);

			if(number instanceof BigDecimal)
				return bigdec.apply((BigDecimal) number);
			
		} catch (Exception e) { }
		
		throw mismatch(index, type);
	}

	/**
	 * Returns the exact value as a byte for a given index.
	 * This fails if the value does not fit into a byte
	 * 
	 * @param index the index
	 * @return the byte
	 * 
	 * @throws JSONException if the index does not exist, the value is not a byte, or if the value does not fit into a byte
	 */
	public byte getByteExact(int index) {
		return getNumberExact(index, "byte", BigInteger::byteValueExact, BigDecimal::byteValueExact);
	}
	/**
	 * Returns the exact value as a short for a given index.
	 * This fails if the value does not fit into a short
	 * 
	 * @param index the index
	 * @return the short
	 * 
	 * @throws JSONException if the index does not exist, the value is not a short, or if the value does not fit into a short
	 */
	public short getShortExact(int index) {
		return getNumberExact(index, "short", BigInteger::shortValueExact, BigDecimal::shortValueExact);
	}
	/**
	 * Returns the exact value as an int for a given index.
	 * This fails if the value does not fit into an int
	 * 
	 * @param index the index
	 * @return the int
	 * 
	 * @throws JSONException if the index does not exist, the value is not an int, or if the value does not fit into an int
	 */
	public int getIntExact(int index) {
		return getNumberExact(index, "int", BigInteger::intValueExact, BigDecimal::intValueExact);
	}
	/**
	 * Returns the exact value as a long for a given index.
	 * This fails if the value does not fit into a long
	 * 
	 * @param index the index
	 * @return the long
	 * 
	 * @throws JSONException if the index does not exist, the value is not a long, or if the value does not fit into a long
	 */
	public long getLongExact(int index) {
		return getNumberExact(index, "long", BigInteger::longValueExact, BigDecimal::longValueExact);
	}

	/**
	 * Returns the exact value as a float for a given index.
	 * This fails if the value does not fit into a float
	 * 
	 * @param index the index
	 * @return the float
	 * 
	 * @throws JSONException if the index does not exist, the value is not a float, or if the value does not fit into a float
	 */
	public float getFloatExact(int index) {
		Number num = getNumber(index);
		
		if(num instanceof Double) // NaN and Infinity
			return ((Double) num).floatValue();
		
		float f = num.floatValue();
		
		if(!Float.isFinite(f))
			throw mismatch(index, "float");
		
		return f;
	}
	/**
	 * Returns the exact value as a double for a given index.
	 * This fails if the value does not fit into a double
	 * 
	 * @param index the index
	 * @return the double
	 * 
	 * @throws JSONException if the index does not exist, the value is not a double, or if the value does not fit into a double
	 */
	public double getDoubleExact(int index) {
		Number num = getNumber(index);
		
		if(num instanceof Double) // NaN and Infinity
			return (Double) num;
		
		double d = num.doubleValue();
		
		if(!Double.isFinite(d))
			throw mismatch(index, "double");
		
		return d;
	}

	/**
	 * Returns the value as a JSONObject for a given index
	 * 
	 * @param index the index
	 * @return the JSONObject
	 * 
	 * @throws JSONException if the index does not exist, or if the value is not a JSONObject
	 */
	public JSONObject getObject(int index) {
		return checkType(this::isObject, index, "object");
	}

	/**
	 * Returns the value as a JSONArray for a given index
	 * 
	 * @param index the index
	 * @return the JSONArray
	 * 
	 * @throws JSONException if the index does not exist, or if the value is not a JSONArray
	 */
	public JSONArray getArray(int index) {
		return checkType(this::isArray, index, "array");
	}
	
	// -- OPTIONAL --
	
	private <T> T getOpt(int index, Function<Integer, T> supplier, T defaults) {
		try {
			return supplier.apply(index);
		} catch (Exception e) {
			return defaults;
		}
	}
	
	/**
	 * Returns the value for a given index, or the default value if the operation is not possible
	 * 
	 * @param index the index
	 * @param defaults the default value
	 * @return the value
	 */
	public Object get(int index, Object defaults) {
		return getOpt(index, this::get, defaults);
	}

	/**
	 * Returns the value as a boolean for a given index, or the default value if the operation is not possible
	 * 
	 * @param index the index
	 * @param defaults the default value
	 * @return the boolean
	 */
	public boolean getBoolean(int index, boolean defaults) {
		return getOpt(index, this::getBoolean, defaults);
	}

	/**
	 * Returns the value as a string for a given index, or the default value if the operation is not possible
	 * 
	 * @param index the index
	 * @param defaults the default value
	 * @return the string
	 */
	public String getString(int index, String defaults) {
		return getOpt(index, this::getString, defaults);
	}

	/**
	 * Returns the value as a number for a given index, or the default value if the operation is not possible
	 * 
	 * @param index the index
	 * @param defaults the default value
	 * @return the number
	 */
	public Number getNumber(int index, Number defaults) {
		return getOpt(index, this::getNumber, defaults);
	}

	/**
	 * Returns the value as a byte for a given index, or the default value if the operation is not possible
	 * 
	 * @param index the index
	 * @param defaults the default value
	 * @return the byte
	 */
	public byte getByte(int index, byte defaults) {
		return getOpt(index, this::getByte, defaults);
	}
	/**
	 * Returns the value as a short for a given index, or the default value if the operation is not possible
	 * 
	 * @param index the index
	 * @param defaults the default value
	 * @return the short
	 */
	public short getShort(int index, short defaults) {
		return getOpt(index, this::getShort, defaults);
	}
	/**
	 * Returns the value as an int for a given index, or the default value if the operation is not possible
	 * 
	 * @param index the index
	 * @param defaults the default value
	 * @return the int
	 */
	public int getInt(int index, int defaults) {
		return getOpt(index, this::getInt, defaults);
	}
	/**
	 * Returns the value as a long for a given index, or the default value if the operation is not possible
	 * 
	 * @param index the index
	 * @param defaults the default value
	 * @return the long
	 */
	public long getLong(int index, long defaults) {
		return getOpt(index, this::getLong, defaults);
	}

	/**
	 * Returns the value as a float for a given index, or the default value if the operation is not possible
	 * 
	 * @param index the index
	 * @param defaults the default value
	 * @return the float
	 */
	public float getFloat(int index, float defaults) {
		return getOpt(index, this::getFloat, defaults);
	}
	/**
	 * Returns the value as a double for a given index, or the default value if the operation is not possible
	 * 
	 * @param index the index
	 * @param defaults the default value
	 * @return the double
	 */
	public double getDouble(int index, double defaults) {
		return getOpt(index, this::getDouble, defaults);
	}

	/**
	 * Returns the exact value as a byte for a given index, or the default value if the operation is not possible
	 * 
	 * @param index the index
	 * @param defaults the default value
	 * @return the byte
	 */
	public byte getByteExact(int index, byte defaults) {
		return getOpt(index, this::getByteExact, defaults);
	}
	/**
	 * Returns the exact value as a short for a given index, or the default value if the operation is not possible
	 * 
	 * @param index the index
	 * @param defaults the default value
	 * @return the short
	 */
	public short getShortExact(int index, short defaults) {
		return getOpt(index, this::getShortExact, defaults);
	}
	/**
	 * Returns the exact value as an int for a given index, or the default value if the operation is not possible
	 * 
	 * @param index the index
	 * @param defaults the default value
	 * @return the int
	 */
	public int getIntExact(int index, int defaults) {
		return getOpt(index, this::getIntExact, defaults);
	}
	/**
	 * Returns the exact value as a long for a given index, or the default value if the operation is not possible
	 * 
	 * @param index the index
	 * @param defaults the default value
	 * @return the long
	 */
	public long getLongExact(int index, long defaults) {
		return getOpt(index, this::getLongExact, defaults);
	}

	/**
	 * Returns the exact value as a float for a given index, or the default value if the operation is not possible
	 * 
	 * @param index the index
	 * @param defaults the default value
	 * @return the float
	 */
	public float getFloatExact(int index, float defaults) {
		return getOpt(index, this::getFloatExact, defaults);
	}
	/**
	 * Returns the exact value as a double for a given index, or the default value if the operation is not possible
	 * 
	 * @param index the index
	 * @param defaults the default value
	 * @return the double
	 */
	public double getDoubleExact(int index, double defaults) {
		return getOpt(index, this::getDoubleExact, defaults);
	}

	/**
	 * Returns the exact value as a JSONObject for a given index, or the default value if the operation is not possible
	 * 
	 * @param index the index
	 * @param defaults the default value
	 * @return the JSONObject
	 */
	public JSONObject getObject(int index, JSONObject defaults) {
		return getOpt(index, this::getObject, defaults);
	}

	/**
	 * Returns the exact value as a JSONArray for a given index, or the default value if the operation is not possible
	 * 
	 * @param index the index
	 * @param defaults the default value
	 * @return the JSONArray
	 */
	public JSONArray getArray(int index, JSONArray defaults) {
		return getOpt(index, this::getArray, defaults);
	}
	
	// -- ADD --
	
	/**
	 * Adds a value to the JSONArray
	 *
	 * @param value the new value
	 */
	public void add(Object value) {
		values.add(JSONObject.sanitize(value));
	}
	
	private void addCheck(Object value) {
		values.add(JSONObject.checkNull(value));
	}

	/**
	 * Adds a value to the JSONArray
	 *
	 * @param value the new value
	 */
	public void add(boolean value) {
		addCheck(value);
	}

	/**
	 * Adds a value to the JSONArray
	 *
	 * @param value the new value
	 */
	public void add(String value) {
		addCheck(value);
	}

	/**
	 * Adds a value to the JSONArray
	 *
	 * @param value the new value
	 */
	public void add(Number value) {
		addCheck(value);
	}

	/**
	 * Adds a value to the JSONArray
	 *
	 * @param value the new value
	 */
	public void add(byte value) {
		addCheck(value);
	}
	/**
	 * Adds a value to the JSONArray
	 *
	 * @param value the new value
	 */
	public void add(short value) {
		addCheck(value);
	}
	/**
	 * Adds a value to the JSONArray
	 *
	 * @param value the new value
	 */
	public void add(int value) {
		addCheck(value);
	}
	/**
	 * Adds a value to the JSONArray
	 *
	 * @param value the new value
	 */
	public void add(long value) {
		addCheck(value);
	}

	/**
	 * Adds a value to the JSONArray
	 *
	 * @param value the new value
	 */
	public void add(float value) {
		addCheck(value);
	}
	/**
	 * Adds a value to the JSONArray
	 *
	 * @param value the new value
	 */
	public void add(double value) {
		addCheck(value);
	}

	/**
	 * Adds a value to the JSONArray
	 *
	 * @param value the new value
	 */
	public void add(JSONObject value) {
		addCheck(value);
	}

	/**
	 * Adds a value to the JSONArray
	 *
	 * @param value the new value
	 */
	public void add(JSONArray value) {
		addCheck(value);
	}
	
	// -- SET --
	
	/**
	 * Sets the value at a given index
	 * 
	 * @param index the index
	 * @param value the new value
	 */
	public void set(int index, Object value) {
		checkIndex(index);
		values.set(index, JSONObject.sanitize(value));
	}

	private void setCheck(int index, Object value) {
		checkIndex(index);
		values.set(index, JSONObject.checkNull(value));
	}

	/**
	 * Sets the value at a given index
	 * 
	 * @param index the index
	 * @param value the new value
	 */
	public void set(int index, boolean value) {
		setCheck(index, value);
	}

	/**
	 * Sets the value at a given index
	 * 
	 * @param index the index
	 * @param value the new value
	 */
	public void set(int index, String value) {
		setCheck(index, value);
	}

	/**
	 * Sets the value at a given index
	 * 
	 * @param index the index
	 * @param value the new value
	 */
	public void set(int index, Number value) {
		setCheck(index, value);
	}

	/**
	 * Sets the value at a given index
	 * 
	 * @param index the index
	 * @param value the new value
	 */
	public void set(int index, byte value) {
		setCheck(index, value);
	}
	/**
	 * Sets the value at a given index
	 * 
	 * @param index the index
	 * @param value the new value
	 */
	public void set(int index, short value) {
		setCheck(index, value);
	}
	/**
	 * Sets the value at a given index
	 * 
	 * @param index the index
	 * @param value the new value
	 */
	public void set(int index, int value) {
		setCheck(index, value);
	}
	/**
	 * Sets the value at a given index
	 * 
	 * @param index the index
	 * @param value the new value
	 */
	public void set(int index, long value) {
		setCheck(index, value);
	}

	/**
	 * Sets the value at a given index
	 * 
	 * @param index the index
	 * @param value the new value
	 */
	public void set(int index, float value) {
		setCheck(index, value);
	}
	/**
	 * Sets the value at a given index
	 * 
	 * @param index the index
	 * @param value the new value
	 */
	public void set(int index, double value) {
		setCheck(index, value);
	}

	/**
	 * Sets the value at a given index
	 * 
	 * @param index the index
	 * @param value the new value
	 */
	public void set(int index, JSONObject value) {
		setCheck(index, value);
	}

	/**
	 * Sets the value at a given index
	 * 
	 * @param index the index
	 * @param value the new value
	 */
	public void set(int index, JSONArray value) {
		setCheck(index, value);
	}
	
	// -- STRINGIFY --
	
	/**
	 * Converts the JSONArray into its string representation.
	 * The indentation factor enables pretty-printing and defines
	 * how many spaces (' ') should be placed before each index/value pair.
	 * A factor of {@code < 1} disables pretty-printing and discards
	 * any optional whitespace characters.
	 * <p>
	 * {@code indentFactor = 2}:
	 * <pre>
	 * [
	 *   "value",
	 *   {
	 *     "nested": 123
	 *   },
	 *   false
	 * }
	 * </pre>
	 * <p>
	 * {@code indentFactor = 0}:
	 * <pre>
	 * ["value",{"nested":123},false]
	 * </pre>
	 * 
	 * @param indentFactor the indentation factor
	 * @return the string representation
	 * 
	 * @see JSONStringify#toString(JSONArray, int)
	 */
	public String toString(int indentFactor) {
		return JSONStringify.toString(this, indentFactor);
	}

	/**
	 * Converts the JSONArray into its compact string representation.
	 * 
	 * @return the compact string representation
	 */
	@Override
	public String toString() {
		return toString(0);
	}
	
	// -- MISCELLANEOUS --
	
	private Object checkIndex(int index) {
		if(index < 0 || index >= length())
			throw new JSONException("JSONArray[" + index + "] does not exist");
		
		return values.get(index);
	}
	
	@SuppressWarnings("unchecked")
	private <T> T checkType(Predicate<Integer> predicate, int index, String type) {
		if(!predicate.test(index))
			throw mismatch(index, type);
		
		return (T) values.get(index);
	}
	
	//
	
	private static JSONException mismatch(int index, String type) {
		return new JSONException("JSONArray[" + index +"] is not of type " + type);
	}
	
}
