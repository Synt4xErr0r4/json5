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

import java.time.Instant
import java.util.stream.Stream
import kotlinx.serialization.json.buildJsonObject
import kotlinx.serialization.json.contentOrNull
import kotlinx.serialization.json.doubleOrNull
import kotlinx.serialization.json.jsonPrimitive
import kotlinx.serialization.json.longOrNull
import kotlinx.serialization.json.put
import kotlinx.serialization.json.putJsonArray
import kotlinx.serialization.json.putJsonObject
import org.intellij.lang.annotations.Language
import org.junit.jupiter.api.Assertions.assertAll
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.DynamicTest
import org.junit.jupiter.api.DynamicTest.dynamicTest
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.TestFactory
import org.junit.jupiter.params.ParameterizedTest
import org.junit.jupiter.params.provider.CsvSource
import org.junit.jupiter.params.provider.ValueSource

/**
 * @author SyntaxError404
 */
internal class UnitTests {

  private val j5 = Json5Module()

  @ParameterizedTest(name = "{index}: {0}")
  @CsvSource(
    useHeadersInDisplayName = true,
    delimiter = '|',
    quoteCharacter = Char.MIN_VALUE, // prevent JUnit from interfering with our quotes
    textBlock =
    """
Title                                 |  Input value    |  Expected value
#-------------------------------------|-----------------|-----------------
double quote in double-quoted string  |  "Test \" 123"  |  Test " 123 
double quote in single-quoted string  |  'Test \" 123'  |  Test " 123 
single quote in double-quoted string  |  "Test \' 123"  |  Test ' 123 
single quote in single-quoted string  |  'Test \' 123'  |  Test ' 123 
"""
  )
  fun testDoubleQuoted(title: String, inputValue: String, expectedValue: String) {
    assertTrue(title.isNotEmpty(), "dummy var - used for test name")

    val parsedObject = j5.decodeObject("""{ a: $inputValue }""")

    assertTrue(parsedObject.containsKey("a"))
    assertEquals(expectedValue, parsedObject["a"]?.jsonPrimitive?.contentOrNull)
  }

  @Test
  fun testStringify() {
    val jsonObject = buildJsonObject {
      put("a", null as String?)
      put("b", false)
      put("c", true)
      putJsonObject("d") {}
      putJsonArray("e") {}
      put("f", Double.NaN)
      put("g", 123e+45)
      put("h", (-123e45).toFloat())
      put("i", 123L)
      put("j", "Lorem Ipsum")
      put("k", Instant.ofEpochSecond(1639908193).toString())
    }

    @Language("JSON5")
    val expected =
      """
        {
          "a": null,
          "b": false,
          "c": true,
          "d": {
          },
          "e": [
          ],
          "f": NaN,
          "g": 1.23E47,
          "h": -Infinity,
          "i": 123,
          "j": "Lorem Ipsum",
          "k": "2021-12-19T10:03:13Z"
        }
      """.trimIndent()
    // TODO set up Instant encode/decode
    //      "k": 1639908193
    assertAll(
      { assertEquals(expected, j5.encodeToString(jsonObject)) },
      {
        val parsedValue = j5.decodeObject(expected)
        assertEquals(expected, j5.encodeToString(parsedValue))
      },
    )
  }

  @TestFactory
  fun `test escaped characters`(): Stream<DynamicTest> {
    return listOf(
      """ \n     """ to '\n',
      """ \r     """ to '\r',
      """ \u000c """ to '\u000c',
      """ \b     """ to '\b',
      """ \t     """ to '\t',
      """ \v     """ to '\u000B',
      """ \0     """ to '\u0000',
      """ \u12Fa """ to '\u12Fa',
      """ \u007F """ to '\u007F',
    )
      .map { (input, expectedChar) -> input.trim() to expectedChar }
      .map { (input, expectedChar) ->
        dynamicTest("expect escaped char '$input is mapped to actual char value") {
          val parsedValue = j5.decodeObject("""{ a: "$input" }""")
          assertTrue(parsedValue.containsKey("a"))
          assertEquals(expectedChar.toString(), parsedValue["a"]?.jsonPrimitive?.contentOrNull)
        }
      }.stream()
  }

  @Test
  fun testEscapes() {

    val inputValue = """\n\r\u000c\b\t\v\0\u12Fa\x7F"""
    val expectedValue = "\n\r\u000c\b\t\u000B\u0000\u12Fa\u007F"

    val parsedValue = j5.decodeObject("""{ a: "$inputValue" }""")

    assertTrue(parsedValue.containsKey("a"))
    assertEquals(expectedValue, parsedValue["a"]?.jsonPrimitive?.contentOrNull)
  }

  @Test
  fun testMemberName() {
    // note: requires UTF-8

    val inputKey = "\$Lorem\\u0041_Ipsum123指事字"
    val expectedKey = "\$LoremA_Ipsum123指事字"

    val parsedValue = j5.decodeObject("{ $inputKey: 0 }")

    assertTrue(parsedValue.containsKey(expectedKey))
    assertEquals(0, parsedValue[expectedKey]?.jsonPrimitive?.longOrNull)
  }

  @ParameterizedTest
  @ValueSource(
    //language=JSON5
    strings = [
      """
        // test
        { // lorem ipsum
          a: 'b'
        // test
        }// test
      """,
      """
        /**/{
          /**/ a /**/: /**/'b'
          /**/
        }/**/
      """,
    ]
  )
  fun testComments(inputJson: String) {
    val parsedValue = j5.decodeObject(inputJson)

    assertTrue(parsedValue.containsKey("a"))
    assertEquals("b", parsedValue["a"]?.jsonPrimitive?.contentOrNull)
  }

  @Test
  fun testHex() {

    val parsedObject = j5.decodeObject("""{ a: 0xCAFEBABE }""")

    assertTrue(parsedObject.containsKey("a"))
    val actualValue = parsedObject["a"]?.jsonPrimitive?.longOrNull
    assertEquals(0xCAFEBABE, actualValue)
  }

  @ParameterizedTest
  @ValueSource(strings = ["NaN", "-NaN", "+NaN"])
  fun `expect NaN value is parsed to NaN Double`(nanValue: String) {

    val jsonString = """ { a: $nanValue } """
    val parsedObject = j5.decodeObject(jsonString)

    assertTrue(parsedObject.containsKey("a"))
    assertTrue(
      parsedObject["a"]?.jsonPrimitive?.doubleOrNull?.isNaN() == true
    )
  }

  @ParameterizedTest
  @ValueSource(strings = ["Infinity", "-Infinity", "+Infinity"])
  fun `expect Infinity value is parsed to Infinite Double`(nanValue: String) {

    val jsonString = """ { a: $nanValue } """
    val parsedObject = j5.decodeObject(jsonString)

    assertTrue(parsedObject.containsKey("a"))
    assertTrue(
      parsedObject["a"]?.jsonPrimitive?.doubleOrNull?.isInfinite() == true
    )
  }

}
