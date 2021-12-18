package at.syntaxerror.json5

import io.kotest.assertions.assertSoftly
import io.kotest.assertions.throwables.shouldThrow
import io.kotest.core.spec.style.BehaviorSpec
import io.kotest.matchers.collections.shouldContainInOrder
import io.kotest.matchers.collections.shouldHaveSize
import io.kotest.matchers.string.shouldContain
import io.kotest.matchers.types.shouldBeInstanceOf
import java.math.BigInteger
import org.intellij.lang.annotations.Language

class JSONParserTests : BehaviorSpec({

  val testOptions = JSONOptions(
    parseInstants = false
  )

  Given("a valid json5 array") {
    @Language("JSON5")
    val valid = """
                  [
                    "I'm a string",
                    10,
                  ]
                """.trimIndent()
    When("the valid array is parsed as a stream") {

      val parser = JSONParser(valid.byteInputStream(), testOptions)
      val parsedValue = parser.nextValue()

      Then("expect the value is a JSON Array") {
        assertSoftly(parsedValue) {
          shouldBeInstanceOf<JSONArray>()
          shouldHaveSize(2)
          shouldContainInOrder("I'm a string",  BigInteger.valueOf(10))
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
})
