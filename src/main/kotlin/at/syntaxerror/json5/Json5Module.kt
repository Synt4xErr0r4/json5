package at.syntaxerror.json5


import java.io.InputStream
import java.io.InputStreamReader
import java.io.Reader
import kotlinx.serialization.json.JsonArray
import kotlinx.serialization.json.JsonObject

class Json5Module(
  configure: JSONOptions.() -> Unit = {}
) {
  private val options: JSONOptions = JSONOptions()
  private val stringify: JSONStringify = JSONStringify(options)

  init {
    options.configure()
  }

  fun decodeObject(string: String): JsonObject = decodeObject(string.reader())
  fun decodeObject(stream: InputStream): JsonObject = decodeObject(InputStreamReader(stream))

  fun decodeObject(reader: Reader): JsonObject {
    return reader.use { r ->
      val parser = JSONParser(r, options)
      DecodeJson5Object.decode(parser)
    }
  }

  fun decodeArray(string: String): JsonArray = decodeArray(string.reader())
  fun decodeArray(stream: InputStream): JsonArray = decodeArray(InputStreamReader(stream))

  fun decodeArray(reader: Reader): JsonArray {
    return reader.use { r ->
      val parser = JSONParser(r, options)
      DecodeJson5Array.decode(parser)
    }
  }

  fun encodeToString(array: JsonArray) = stringify.encodeArray(array, 2u)
  fun encodeToString(jsonObject: JsonObject) = stringify.encodeObject(jsonObject, 2u)

}
