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

import java.io.BufferedReader
import java.io.InputStream
import java.io.InputStreamReader
import java.io.Reader
import java.io.StringReader
import java.math.BigDecimal
import java.math.BigInteger
import java.time.Instant
import java.util.regex.Pattern

/**
 * A JSONParser is used to convert a source string into tokens, which then are used to construct
 * [JSONObjects][JSONObject] and [JSONArrays][JSONArray]
 *
 * @author SyntaxError404
 */
class JSONParser @JvmOverloads constructor(reader: Reader, options: JSONOptions? = null) {
  private val reader: Reader
  private val options: JSONOptions
  /**
   * whether the end of the file has been reached
   */
  private var eof: Boolean
  /**
   * whether the current character should be re-read
   */
  private var back: Boolean
  /**
   * the absolute position in the string
   */
  private var index: Long
  /**
   * the relative position in the line
   */
  private var character: Long
  /**
   * the line number
   */
  private var line: Long
  /**
   * the previous character
   */
  private var previous: Char
  /**
   * the current character
   */
  private var current: Char
  /**
   * Constructs a new JSONParser from a Reader. The reader is not [closed][Reader.close]
   *
   * @param reader  a reader
   * @param options the options for parsing
   * @since 1.1.0
   */
  /**
   * Constructs a new JSONParser from a Reader. The reader is not [closed][Reader.close].
   * This uses the [default options][JSONOptions.defaultOptions]
   */
  init {
    this.reader = if (reader.markSupported()) reader else BufferedReader(reader)
    this.options = options ?: JSONOptions.defaultOptions
    eof = false
    back = false
    index = -1
    character = 0
    line = 1
    previous = 0.toChar()
    current = 0.toChar()
  }
  /**
   * Constructs a new JSONParser from a string
   *
   * @param source  a string
   * @param options the options for parsing
   * @since 1.1.0
   */
  /**
   * Constructs a new JSONParser from a string. This uses the [ default options][JSONOptions.defaultOptions]
   */
  @JvmOverloads
  constructor(source: String?, options: JSONOptions? = null) : this(StringReader(source), options) {
  }
  /**
   * Constructs a new JSONParser from an InputStream. The stream is not [ closed][InputStream.close]
   */
  /**
   * Constructs a new JSONParser from an InputStream. The stream is not [ closed][InputStream.close]. This uses the [default options][JSONOptions.defaultOptions]
   */
  @JvmOverloads
  constructor(stream: InputStream?, options: JSONOptions? = null) : this(
    InputStreamReader(stream),
    options
  ) {
  }

  private fun more(): Boolean {
    return if (back || eof) {
      back && !eof
    } else peek().code > 0
  }
  /**
   * Forces the parser to re-read the last character
   */
  fun back() {
    back = true
  }

  private fun peek(): Char {
    if (eof) {
      return 0
    }
    val c: Int
    try {
      reader.mark(1)
      c = reader.read()
      reader.reset()
    } catch (e: Exception) {
      throw syntaxError("Could not peek from source", e)
    }
    return if (c == -1) 0 else c.toChar()
  }

  private operator fun next(): Char {
    if (back) {
      back = false
      return current
    }
    val c: Int
    c = try {
      reader.read()
    } catch (e: Exception) {
      throw syntaxError("Could not read from source", e)
    }
    if (c < 0) {
      eof = true
      return 0
    }
    previous = current
    current = c.toChar()
    index++
    if (isLineTerminator(current) && (current != '\n' || current == '\n' && previous != '\r')) {
      line++
      character = 0
    } else {
      character++
    }
    return current
  }
  // https://262.ecma-international.org/5.1/#sec-7.3
  private fun isLineTerminator(c: Char): Boolean {
    return when (c) {
      '\n', '\r', 0x2028, 0x2029 -> true
      else                       -> false
    }
  }
  // https://spec.json5.org/#white-space
  private fun isWhitespace(c: Char): Boolean {
    return when (c) {
      '\t', '\n', 0x0B, '\f', '\r', ' ', 0xA0, 0x2028, 0x2029, 0xFEFF -> true
      else                                                            ->         // Unicode category "Zs" (space separators)
        Character.getType(c) == Character.SPACE_SEPARATOR.toInt()
    }
  }
  // https://262.ecma-international.org/5.1/#sec-9.3.1
  private fun isDecimalDigit(c: Char): Boolean {
    return c >= '0' && c <= '9'
  }

  private fun nextMultiLineComment() {
    while (true) {
      val n = next()
      if (n == '*' && peek() == '/') {
        next()
        return
      }
    }
  }

  private fun nextSingleLineComment() {
    while (true) {
      val n = next()
      if (isLineTerminator(n) || n.code == 0) {
        return
      }
    }
  }
  /**
   * Reads until encountering a character that is not a whitespace according to the
   * [JSON5 Specification](https://spec.json5.org/#white-space)
   *
   * @return a non-whitespace character, or `0` if the end of the stream has been reached
   */
  fun nextClean(): Char {
    while (true) {
      if (!more()) {
        throw syntaxError("Unexpected end of data")
      }
      val n = next()
      if (n == '/') {
        val p = peek()
        if (p == '*') {
          next()
          nextMultiLineComment()
        } else if (p == '/') {
          next()
          nextSingleLineComment()
        } else {
          return n
        }
      } else if (!isWhitespace(n)) {
        return n
      }
    }
  }

  private fun nextCleanTo(delimiters: String): String {
    val result = StringBuilder()
    while (true) {
      if (!more()) {
        throw syntaxError("Unexpected end of data")
      }
      val n = nextClean()
      if (delimiters.indexOf(n) > -1 || isWhitespace(n)) {
        back()
        break
      }
      result.append(n)
    }
    return result.toString()
  }

  private fun dehex(c: Char): Int {
    if (c >= '0' && c <= '9') {
      return c - '0'
    }
    if (c >= 'a' && c <= 'f') {
      return c - 'a' + 0xA
    }
    return if (c >= 'A' && c <= 'F') {
      c - 'A' + 0xA
    } else -1
  }

  private fun unicodeEscape(member: Boolean, part: Boolean): Char {
    val where = if (member) "key" else "string"
    var value = ""
    var codepoint = 0
    for (i in 0..3) {
      val n = next()
      value += n
      val hex = dehex(n)
      if (hex == -1) {
        throw syntaxError("Illegal unicode escape sequence '\\u$value' in $where")
      }
      codepoint = codepoint or (hex shl (3 - i shl 2))
    }
    if (member && !isMemberNameChar(codepoint.toChar(), part)) {
      throw syntaxError("Illegal unicode escape sequence '\\u$value' in key")
    }
    return codepoint.toChar()
  }

  private fun checkSurrogate(hi: Char, lo: Char) {
    if (options.allowInvalidSurrogates) {
      return
    }
    if (!Character.isHighSurrogate(hi) || !Character.isLowSurrogate(lo)) {
      return
    }
    if (!Character.isSurrogatePair(hi, lo)) {
      throw syntaxError(
        String.format(
          "Invalid surrogate pair: U+%04X and U+%04X",
          hi, lo
        )
      )
    }
  }
  // https://spec.json5.org/#prod-JSON5String
  private fun nextString(quote: Char): String {
    val result = StringBuilder()
    var value: String
    var codepoint: Int
    var n = 0.toChar()
    var prev: Char
    while (true) {
      if (!more()) {
        throw syntaxError("Unexpected end of data")
      }
      prev = n
      n = next()
      if (n == quote) {
        break
      }
      if (isLineTerminator(n) && n.code != 0x2028 && n.code != 0x2029) {
        throw syntaxError("Unescaped line terminator in string")
      }
      if (n == '\\') {
        n = next()
        if (isLineTerminator(n)) {
          if (n == '\r' && peek() == '\n') {
            next()
          }

          // escaped line terminator/ line continuation
          continue
        } else {
          when (n) {
            '\'', '"', '\\' -> {
              result.append(n)
              continue
            }
            'b'             -> {
              result.append('\b')
              continue
            }
            'f'             -> {
              result.append('\f')
              continue
            }
            'n'             -> {
              result.append('\n')
              continue
            }
            'r'             -> {
              result.append('\r')
              continue
            }
            't'             -> {
              result.append('\t')
              continue
            }
            'v'             -> {
              result.append(0x0B.toChar())
              continue
            }
            '0'             -> {
              val p = peek()
              if (isDecimalDigit(p)) {
                throw syntaxError("Illegal escape sequence '\\0$p'")
              }
              result.append(0.toChar())
              continue
            }
            'x'             -> {
              value = ""
              codepoint = 0
              var i = 0
              while (i < 2) {
                n = next()
                value += n
                val hex = dehex(n)
                if (hex == -1) {
                  throw syntaxError("Illegal hex escape sequence '\\x$value' in string")
                }
                codepoint = codepoint or (hex shl (1 - i shl 2))
                ++i
              }
              n = codepoint.toChar()
            }
            'u'             -> n = unicodeEscape(false, false)
            else            -> if (isDecimalDigit(n)) {
              throw syntaxError("Illegal escape sequence '\\$n'")
            }
          }
        }
      }
      checkSurrogate(prev, n)
      result.append(n)
    }
    return result.toString()
  }

  private fun isMemberNameChar(n: Char, part: Boolean): Boolean {
    if (n == '$' || n == '_' || n.code == 0x200C || n.code == 0x200D) {
      return true
    }
    val type = Character.getType(n)
    when (type) {
      Character.UPPERCASE_LETTER, Character.LOWERCASE_LETTER, Character.TITLECASE_LETTER, Character.MODIFIER_LETTER, Character.OTHER_LETTER, Character.LETTER_NUMBER -> return true
      Character.NON_SPACING_MARK, Character.COMBINING_SPACING_MARK, Character.DECIMAL_DIGIT_NUMBER, Character.CONNECTOR_PUNCTUATION                                  -> if (part) {
        return true
      }
    }
    return false
  }
  /**
   * Reads a member name from the source according to the
   * [JSON5 Specification](https://spec.json5.org/#prod-JSON5MemberName)
   *
   * @return an member name
   */
  fun nextMemberName(): String {
    val result = StringBuilder()
    var prev: Char
    var n = next()
    if (n == '"' || n == '\'') {
      return nextString(n)
    }
    back()
    n = 0.toChar()
    while (true) {
      if (!more()) {
        throw syntaxError("Unexpected end of data")
      }
      val part = result.length > 0
      prev = n
      n = next()
      if (n == '\\') { // unicode escape sequence
        n = next()
        if (n != 'u') {
          throw syntaxError("Illegal escape sequence '\\$n' in key")
        }
        n = unicodeEscape(true, part)
      } else if (!isMemberNameChar(n, part)) {
        back()
        break
      }
      checkSurrogate(prev, n)
      result.append(n)
    }
    if (result.length == 0) {
      throw syntaxError("Empty key")
    }
    return result.toString()
  }
  /**
   * Reads a value from the source according to the
   * [JSON5 Specification](https://spec.json5.org/#prod-JSON5Value)
   *
   * @return an member name
   */
  fun nextValue(): Any? {
    val n = nextClean()
    when (n) {
      '"', '\'' -> {
        val string = nextString(n)
        if (options.parseInstants && options.parseStringInstants) {
          try {
            return Instant.parse(string)
          } catch (ignored: Exception) {
          }
        }
        return string
      }
      '{'       -> {
        back()
        return JSONObject(this)
      }
      '['       -> {
        back()
        return JSONArray(this)
      }
    }
    back()
    val string = nextCleanTo(",]}")
    if (string == "null") {
      return null
    }
    if (PATTERN_BOOLEAN.matcher(string).matches()) {
      return string == "true"
    }
    if (PATTERN_NUMBER_INTEGER.matcher(string).matches()) {
      val bigint = BigInteger(string)
      if (options.parseInstants && options.parseUnixInstants) {
        try {
          val unix = bigint.longValueExact()
          return Instant.ofEpochSecond(unix)
        } catch (ignored: Exception) {
        }
      }
      return bigint
    }
    if (PATTERN_NUMBER_FLOAT.matcher(string).matches()) {
      return BigDecimal(string)
    }
    if (PATTERN_NUMBER_SPECIAL.matcher(string).matches()) {
      val special: String
      val factor: Int
      var d = 0.0
      when (string[0]) {
        '+'  -> {
          special = string.substring(1) // +
          factor = 1
        }
        '-'  -> {
          special = string.substring(1) // -
          factor = -1
        }
        else -> {
          special = string
          factor = 1
        }
      }
      when (special) {
        "NaN"      -> {
          if (!options.allowNaN) {
            throw syntaxError("NaN is not allowed")
          }
          d = Double.NaN
        }
        "Infinity" -> {
          if (!options.allowInfinity) {
            throw syntaxError("Infinity is not allowed")
          }
          d = Double.POSITIVE_INFINITY
        }
      }
      return factor * d
    }
    if (PATTERN_NUMBER_HEX.matcher(string).matches()) {
      val hex: String
      val factor: Int
      when (string[0]) {
        '+'  -> {
          hex = string.substring(3) // +0x
          factor = 1
        }
        '-'  -> {
          hex = string.substring(3) // -0x
          factor = -1
        }
        else -> {
          hex = string.substring(2) // 0x
          factor = 1
        }
      }
      val bigint = BigInteger(hex, 16)
      return if (factor == -1) {
        bigint.negate()
      } else bigint
    }
    throw JSONException("Illegal value '$string'")
  }

  fun syntaxError(message: String, cause: Throwable?): JSONException {
    return JSONException(message + this, cause)
  }

  fun syntaxError(message: String): JSONException {
    return JSONException(message + this)
  }

  override fun toString(): String {
    return " at index $index [character $character in line $line]"
  }

  companion object {
    private val PATTERN_BOOLEAN = Pattern.compile(
      "true|false"
    )
    private val PATTERN_NUMBER_FLOAT = Pattern.compile(
      "[+-]?((0|[1-9]\\d*)(\\.\\d*)?|\\.\\d+)([eE][+-]?\\d+)?"
    )
    private val PATTERN_NUMBER_INTEGER = Pattern.compile(
      "[+-]?(0|[1-9]\\d*)"
    )
    private val PATTERN_NUMBER_HEX = Pattern.compile(
      "[+-]?0[xX][0-9a-fA-F]+"
    )
    private val PATTERN_NUMBER_SPECIAL = Pattern.compile(
      "[+-]?(Infinity|NaN)"
    )
  }
}
