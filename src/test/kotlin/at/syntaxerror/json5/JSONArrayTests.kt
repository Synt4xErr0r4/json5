package at.syntaxerror.json5

import io.kotest.assertions.assertSoftly
import io.kotest.assertions.throwables.shouldThrow
import io.kotest.assertions.withClue
import io.kotest.core.spec.style.BehaviorSpec
import io.kotest.matchers.collections.shouldContainInOrder
import io.kotest.matchers.collections.shouldHaveSize
import io.kotest.matchers.shouldBe
import io.kotest.matchers.string.shouldContain
import io.kotest.property.Arb
import io.kotest.property.arbitrary.Codepoint
import io.kotest.property.arbitrary.codepoints
import io.kotest.property.arbitrary.filter
import io.kotest.property.checkAll
import kotlinx.serialization.json.JsonPrimitive

class JSONArrayTests : BehaviorSpec({

  val j5 = Json5Module { }

  Given("a JSON5 array should start with '['") {
    val validArrayStarter = '['

    And("an array with a valid starting character") {
      When("the valid array is parsed") {

        val valid = """
                      $validArrayStarter
                        "I'm a string",
                        10,
                      ]
                    """.trimIndent()

        val result = j5.decodeArray(valid)

        Then("expect the array can be pretty-printed") {
          val pretty = j5.encodeToString(result)
          pretty shouldBe
              //language=JSON5
              """
                [
                  "I'm a string",
                  10
                ]
              """.trimIndent()
        }
        Then("expect the array can be compact-printed") {
          //language=JSON5
          result.toString() shouldBe """["I'm a string",10]"""
        }
        Then("expect the array matches an equivalent List") {

          assertSoftly(result) {
            withClue(joinToString { it.javaClass.simpleName }) {
              shouldHaveSize(2)
              shouldContainInOrder(JsonPrimitive("I'm a string"), JsonPrimitive(10))
            }
          }
        }
      }
    }

    And("an array with an invalid starting character") {
      val invalidArrayStarterArb =
        Arb.codepoints().filter { it != Codepoint(validArrayStarter.code) }
      When("the invalid array is parsed") {

        Then("expect a syntax exception") {
          checkAll(invalidArrayStarterArb) { invalidArrayStarter ->

            val invalid = """
                          $invalidArrayStarter
                            "key": "value"
                          ]
                        """.trimIndent()

            val thrown = shouldThrow<JSONException.SyntaxError> {
              j5.decodeArray(invalid)
            }

            assertSoftly(thrown.message) {
              shouldContain("must begin with")
              shouldContain("$validArrayStarter")
            }
          }
        }
      }
    }
  }
})
