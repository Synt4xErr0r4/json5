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
import org.intellij.lang.annotations.Language
import org.junit.jupiter.api.Assertions.assertAll
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.Test
import org.junit.jupiter.params.ParameterizedTest
import org.junit.jupiter.params.provider.ValueSource

/**
 * @author SyntaxError404
 */
internal class UnitTests {

  @Test
  fun testDoubleQuoted() {
    assertEquals(
      "Test \" 123",

      parse(
        //language=JSON5
        """{ a: "Test \" 123" }"""
      ).getString("a")
    )
  }

  @Test
  fun testSingleQuoted() {
    assertEquals(
      "Test ' 123",
      parse(
        //language=JSON5
        """{ a: 'Test \' 123' }"""
      ).getString("a")
    )
  }

  @Test
  fun testMixedQuoted() {
    assertEquals(
      "Test ' 123",
      parse(
        //language=JSON5
        """{ a: "Test \' 123" }"""
      ).getString("a")
    )
  }

  @Test
  fun testStringify() {
    val testOptions = JSONOptions.defaultOptions
    testOptions.stringifyUnixInstants = true
    val json = DecodeJson5Object()
    json["a"] = null
    json["b"] = false
    json["c"] = true
    json["d"] = DecodeJson5Object()
    json["e"] = DecodeJson5Array()
    json["f"] = Double.NaN
    json["g"] = 123e+45
    json["h"] = (-123e45).toFloat()
    json["i"] = 123L
    json["j"] = "Lorem Ipsum"
    json["k"] = Instant.ofEpochSecond(1639908193)

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
          "g": 1.23E+47,
          "h": -Infinity,
          "i": 123,
          "j": "Lorem Ipsum",
          "k": 1639908193
        }
      """.trimIndent()

    assertAll(
      { assertEquals(expected, json.toString(2u)) },
      {
        val parsedValue = parse(json.toString(2u))
        assertEquals(expected, parsedValue.toString(2u))
      },
    )
  }

  @Test
  fun testEscapes() {
    assertEquals(
      "\n\r\u000c\b\t\u000B\u0000\u12Fa\u007F",
      parse(
        //language=JSON5
        """{ a: "\n\r\u000c\b\t\v\0\u12Fa\x7F" }"""
      ).getString("a")
    )
  }

  @Test
  fun testMemberName() {
    // note: requires UTF-8
    assertTrue(
      parse(
        //language=JSON5
        "{ \$Lorem\\u0041_Ipsum123指事字: 0 }"
      )
        .has("\$LoremA_Ipsum123指事字")
    )
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

    val parsedValue = parse(inputJson)

    assertAll(
      { assertTrue(parsedValue.has("a")) },
      { assertTrue(parsedValue.isString("a")) },
      { assertEquals("b", parsedValue.getString("a")) },
    )
  }

  @Test
  fun testInstant() {

    JSONOptions.defaultOptions.parseInstants = true

    assertTrue(
      parse("{a:1338150759534}")
        .isInstant("a")
    )
    assertEquals(
      parse("{a:1338150759534}")
        .getLong("a"),
      1338150759534L
    )
    assertEquals(
      parse("{a:'2001-09-09T01:46:40Z'}")
        .getString("a"),
      "2001-09-09T01:46:40Z"
    )
  }

  @Test
  fun testHex() {
    assertEquals(
      0xCAFEBABEL,
      parse("{a: 0xCAFEBABE}")
        .getLong("a")
    )
  }

  @Test
  fun testSpecial() {
    assertTrue(
      parse("{a: +NaN}")
        .getDouble("a")
        .isNaN()

    )
    assertTrue(
      parse("{a: -Infinity}")
        .getDouble("a")
        .isInfinite()
    )
  }

  private fun parse(str: String): DecodeJson5Object {
    return DecodeJson5Object(JSONParser(str))
  }

}
