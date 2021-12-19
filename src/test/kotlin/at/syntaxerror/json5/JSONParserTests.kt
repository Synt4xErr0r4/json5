package at.syntaxerror.json5

import io.kotest.assertions.assertSoftly
import io.kotest.assertions.throwables.shouldThrow
import io.kotest.core.spec.style.BehaviorSpec
import io.kotest.matchers.collections.shouldContainInOrder
import io.kotest.matchers.collections.shouldHaveSize
import io.kotest.matchers.comparables.shouldBeEqualComparingTo
import io.kotest.matchers.nulls.shouldNotBeNull
import io.kotest.matchers.string.shouldContain
import io.kotest.matchers.string.shouldMatch
import io.kotest.matchers.types.shouldBeInstanceOf
import io.kotest.property.Arb
import io.kotest.property.Exhaustive
import io.kotest.property.arbitrary.bind
import io.kotest.property.arbitrary.positiveLong
import io.kotest.property.checkAll
import io.kotest.property.exhaustive.collection
import java.math.BigInteger
import kotlinx.serialization.json.JsonArray
import kotlinx.serialization.json.JsonPrimitive
import kotlinx.serialization.json.jsonPrimitive
import kotlinx.serialization.json.longOrNull
import org.intellij.lang.annotations.Language

class JSONParserTests : BehaviorSpec({

  val j5 = Json5Module()

  Given("a valid json5 array") {
    @Language("JSON5")
    val valid = """
                  [
                    "I'm a string",
                    10,
                  ]
                """.trimIndent()
    When("JSONParser parses the array as a stream") {

      val parser = JSONParser(valid.reader(), j5)
      val parsedValue = parser.nextValue()

      Then("expect the value is a JSON Array") {
        assertSoftly(parsedValue) {
          shouldBeInstanceOf<JsonArray>()
          shouldHaveSize(2)
          shouldContainInOrder(JsonPrimitive("I'm a string"), JsonPrimitive(10))
        }
      }
      Then("expect there are no more values") {
        val thrown = shouldThrow<JSONException.SyntaxError> {
          parser.nextValue()
        }
        assertSoftly(thrown.message) {
          shouldContain("Unexpected end of data")
        }
      }
    }
  }

  Given("an initial number encoded as a hexadecimal value") {

    When("JSONParser parsers an Json5 array that contains the hexadecimal value") {

      Then("expect the parsed value equals the initial number") {

        val arbHex = Arb.bind(
          Exhaustive.collection(setOf("+", "", "-")),
          Exhaustive.collection(setOf("0x", "0X")),
          Arb.positiveLong(),
        ) { sign, prefix, long ->
          val hex = long.toString(16)
          val expected = BigInteger.valueOf(long).run {
            if (sign == "-") negate() else abs()
          }
          expected.longValueExact() to (sign + prefix + hex)
        }

        checkAll(arbHex) { (expectedNumber, hexString) ->
          hexString shouldMatch Regex("[+-]?0[xX][0-9a-fA-F]+")

          val json5Array = """ [ $hexString ] """

          val parser = JSONParser(json5Array.reader(), j5)
          val parsedValue = parser.nextValue()

          assertSoftly(parsedValue) {
            shouldBeInstanceOf<JsonArray>()
            shouldHaveSize(1)
            val parsedHex = elementAt(0)
            parsedHex.shouldBeInstanceOf<JsonPrimitive>()
            assertSoftly(parsedHex.jsonPrimitive.longOrNull) {
              shouldNotBeNull()
              shouldBeEqualComparingTo(expectedNumber)
            }

          }
        }
      }
    }
  }
})
