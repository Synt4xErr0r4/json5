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
 * A JSONArray is an array structure capable of holding multiple values, including other JSONArrays
 * and [JSONObjects][JSONObject]
 *
 * @author SyntaxError404
 */
class JSONArray(
  private val values: MutableList<Any?> = mutableListOf()
) : Iterable<Any?> by values {

  /** Constructs a new JSONArray from a string */
  constructor(source: String) : this(JSONParser(source))

  /** Constructs a new JSONArray from a JSONParser */
  constructor(parser: JSONParser) : this() {
    if (parser.nextClean() != '[') {
      throw parser.createSyntaxException("A JSONArray must begin with '['")
    }
    while (true) {
      var c: Char = parser.nextClean()
      when (c) {
        Char.MIN_VALUE -> throw parser.createSyntaxException("A JSONArray must end with ']'")
        ']'            -> return
        else           -> parser.back()
      }
      val value = parser.nextValue()
      values.add(value)
      c = parser.nextClean()
      if (c == ']') {
        // finish parsing this array
        return
      }
      if (c != ',') {
        throw parser.createSyntaxException("Expected ',' or ']' after value, got '$c' instead")
      }
    }
  }

  /**
   * Converts the JSONArray into a list. All JSONObjects and JSONArrays contained within this
   * JSONArray will be converted into their Map or List form as well
   */
  fun toList(): List<Any?> {
    return values.map { value ->
      when (value) {
        is JSONObject -> value.toMap()
        is JSONArray  -> value.toList()
        else          -> value
      }
    }
  }

  /**
   * Converts the JSONArray into its string representation. The indentation factor enables
   * pretty-printing and defines how many spaces (' ') should be placed before each value. A factor
   * of `< 1` disables pretty-printing and discards any optional whitespace characters.
   *
   * `indentFactor = 2` results in
   *
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
   * `indentFactor = 0` results in
   *
   * ```
   * ["value",{"nested":123},false]
   * ```
   *
   * @param indentFactor the indentation factor
   * @see JSONStringify.toString
   */
  fun toString(indentFactor: Int): String {
    return JSONStringify.toString(this, indentFactor)
  }

  /**
   * Converts the JSONArray into its compact string representation.
   *
   * @return the compact string representation
   */
  override fun toString(): String {
    return toString(0)
  }
}
