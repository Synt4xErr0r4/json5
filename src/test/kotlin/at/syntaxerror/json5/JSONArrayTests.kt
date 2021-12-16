package at.syntaxerror.json5

import io.kotest.assertions.assertSoftly
import io.kotest.assertions.throwables.shouldThrow
import io.kotest.core.spec.style.BehaviorSpec
import io.kotest.matchers.string.shouldContain
import io.kotest.property.Arb
import io.kotest.property.arbitrary.Codepoint
import io.kotest.property.arbitrary.codepoints
import io.kotest.property.arbitrary.filter
import io.kotest.property.checkAll

class JSONArrayTests : BehaviorSpec({

  Given("an invalid JSON5 array starting character") {
    val validArrayStarter = '['
    val invalidArrayStarterArb = Arb.codepoints().filter { it != Codepoint(validArrayStarter.code) }

    When("an array with an invalid starting character is parsed") {
      Then("Expect an exception") {
        checkAll(invalidArrayStarterArb) { invalidArrayStarter ->

          val invalid = """
                          $invalidArrayStarter
                            "key": "value"
                          ]
                        """.trimIndent()

          val thrown = shouldThrow<JSONException.SyntaxError> {
            JSONArray(invalid)
          }

          assertSoftly(thrown.message) {
            shouldContain("must begin with")
            shouldContain("$validArrayStarter")
          }
        }
      }
    }
  }
})
