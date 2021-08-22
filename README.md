# json5 [![javadoc](https://img.shields.io/endpoint?label=javadoc&url=https%3A%2F%2Fjavadoc.syntaxerror.at%2Fjson5%2F%3Fbadge%3Dtrue)](https://javadoc.syntaxerror.at/json5/latest) ![GitHub Workflow Status](https://img.shields.io/github/workflow/status/Synt4xErr0r4/json5/Java%20CI%20with%20Maven)

A JSON5 Library for Java (11+)

## Overview

The [JSON5 Standard](https://json5.org/) tries to make JSON more human-readable  

This is a reference implementation, capable of parsing JSON5 data according to the [specification](https://spec.json5.org/).

## Getting started

In order to use the code, you can either [download the jar](), or use the Maven dependency:
```xml
<!-- Repository -->

<repository>
  <id>syntaxerror.at</id>
  <url>https://maven.syntaxerror.at</url>
</repository>

<!-- Dependency -->

<dependency>
  <groupId>at.syntaxerror</groupId>
  <artifactId>json5</artifactId>
  <version>1.0.0</version>
</dependency>
```

The library itself is located in the module `json5`.

## Usage

### Deserializing (Parsing)

To parse a JSON object (`{ ... }`), all you need to do is:
```java
import at.syntaxerror.json5.JSONObject;

//...

JSONObject jsonObject = new JSONObject("{ ... }");
```

Or if you want to read directly from a `Reader` or `InputStream`:
```java
import java.io.InputStream;
import at.syntaxerror.json5.JSONObject;

//...

try(InputStream stream = ...) {
    JSONObject jsonObject = new JSONObject(new JSONParser(stream));
    // ...
} catch (Exception e) {
    //...
}
```

Just replace `JSONObject` with `JSONArray` to read list-like data (`[ ... ]`).  

### Serializing (Stringifying)

Both the `JSONObject` and `JSONArray` class contain two methods for serialization:
- `toString()` and
- `toString(int indentFactor)`

The normal `toString()` method will return the compact string representation, without any optional whitespaces.  
The `indentFactor` of the `toString(int indentFactor)` method will enable pretty-printing if `> 0`.
Any value `< 1` will disable pretty-printing. The indent factor indicates the number of spaces before each key-value pair/ value:

`indentFactor = 2`
```json
{
  "key0": "value0",
  "key1": {
    "nested": 123
  },
  "key2": false
}

[
  "value",
  {
    "nested": 123
  },
  false
]
```

`indentFactor = 0`
```json
{"key0":"value0","key1":{"nested":123},"key2":false}

["value",{"nested":123},false]
```

Calling `json.toString(indentFactor)` is the same as `JSONStringify.toString(json, indentFactor)`.

### Working with JSONObjects and JSONArrays

The `getXXX` methods are used to read values from the JSON object/ array.  
The `set` methods are used to override or set values in the JSON object/ array.  
The `add` methods are used to add values to a JSON array.  

Supported data types are:
- `boolean`
- `byte`
- `short`
- `int`
- `float`
- `double`
- `String`
- `JSONObject`
- `JSONArray`

The normal `getXXX(String key)` and `getXXX(int index)` methods will throw an exception if the specified key or index does not exist, but the
`getXXX(String key, XXX defaults)` and `getXXX(int index, XXX defaults)` methods will return the default value (parameter `defaults`) instead.  
  
The `set(int index, XXX value)` method will also throw an exception if the index does not exist. You can use `add(XXX value)` instead to append a value to the list.

The getter-methods for numbers always return a rounded or truncated result.
If the actual number is too large to fit into the requested type, the upper bits are truncated (e.g. `int` to `byte` truncates the upper 24 bits).  
If the actual number is a decimal number (e.g. `123.456`), and the requested type is not (e.g. `long`), the decimal places are discarded.  
To check if a number can fit into a type, you can use the `getXXXExact` methods, which will throw an exception if the conversion is not possible without altering the result.  

Numbers are internally always stored as either a `java.math.BigInteger`, `java.math.BigDecimal`, or `double` (`double` is used for `Infinity` and `NaN` only). Therefore, any method
returning raw `java.lang.Object`s will return numbers as one of those types. The same behaviour applies to the `getNumber` methods.

## Documentation

The JavaDoc for the latest version can be found [here](https://javadoc.syntaxerror.at/json5/latest).

## Credits

This project is partly based on stleary's [JSON-Java](https://github.com/stleary/JSON-java) library.

## License

This project is licensed under the [MIT License](https://github.com/Synt4xErr0r4/json5/blob/main/LICENSE)