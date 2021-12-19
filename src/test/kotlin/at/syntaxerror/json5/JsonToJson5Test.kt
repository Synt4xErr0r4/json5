package at.syntaxerror.json5

import io.kotest.assertions.json.shouldEqualJson
import io.kotest.core.spec.style.BehaviorSpec
import io.kotest.matchers.shouldBe
import kotlinx.serialization.json.JsonObject
import kotlinx.serialization.json.add
import kotlinx.serialization.json.buildJsonObject
import kotlinx.serialization.json.put
import kotlinx.serialization.json.putJsonArray

class JsonToJson5Test : BehaviorSpec({

  val j5 = Json5Module()

  Given("A string-encoded JSON object") {

    //language=JSON
    val json = """
        {
          "widget": {
            "debug": "on",
            "window": {
              "title": "Sample Konfabulator Widget",
              "name": "main_window",
              "width": 500,
              "height": 500
            },
            "image": {
              "src": "Images/Sun.png",
              "name": "sun1",
              "hOffset": 250,
              "vOffset": 250,
              "alignment": "center"
            },
            "text": {
              "data": "Click Here",
              "size": 36,
              "style": "bold",
              "name": "text1",
              "hOffset": 250,
              "vOffset": 100,
              "alignment": "center",
              "onMouseUp": "sun1.opacity = (sun1.opacity / 100) * 90;"
            }
          }
        } 
      """.trimIndent()

    Then("expect it can be converted to a JSON5 string") {
      val jsonObject = j5.decodeObject(json)
      val json5String = j5.encodeToString(jsonObject)
      json5String shouldEqualJson json
    }
  }


  Given("A string-encoded JSON5 object") {

    //language=JSON5
    val json5 = """
        {
          // comments
          unquoted: 'and you can quote me on that',
          singleQuotes: 'I can use "double quotes" here',
          lineBreaks: "Look, Mom! \
        No \\n's!",
          hexadecimal: 0xdecaf,
          leadingDecimalPoint: .8675309,
          andTrailing: 8675309.,
          positiveSign: +1,
          trailingComma: 'in objects',
          andIn: [
            'arrays',
          ],
          "backwardsCompatible": "with JSON",
        }
      """.trimIndent()

    val jsonObject: JsonObject = j5.decodeObject(json5)

    Then("expect it can be parsed to a JsonObject") {
      jsonObject shouldBe buildJsonObject {
        put("unquoted", "and you can quote me on that")
        put("singleQuotes", """I can use "double quotes" here""")
        put("lineBreaks", """Look, Mom! No \n's!""")
        put("hexadecimal", 912559)
        put("leadingDecimalPoint", 0.8675309)
        put("andTrailing", 8675309.0)
        put("positiveSign", 1)
        put("trailingComma", "in objects")
        putJsonArray("andIn") { add("arrays") }
        put("backwardsCompatible", "with JSON")
      }
    }

    Then("expect it can be converted to a JSON string") {

      val jsonString = j5.encodeToString(jsonObject)
      //language=JSON5
      jsonString shouldBe """
        {
          "unquoted": "and you can quote me on that",
          "singleQuotes": "I can use \"double quotes\" here",
          "lineBreaks": "Look, Mom! No \\n's!",
          "hexadecimal": 912559,
          "leadingDecimalPoint": 0.8675309,
          "andTrailing": 8675309.0,
          "positiveSign": 1,
          "trailingComma": "in objects",
          "andIn": [
            "arrays"
          ],
          "backwardsCompatible": "with JSON"
        }
      """.trimIndent()
    }
  }
})
