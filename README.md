# json5 [![javadoc](https://img.shields.io/endpoint?label=javadoc&url=https%3A%2F%2Fjavadoc.syntaxerror.at%2Fjson5%2F%3Fbadge%3Dtrue%26version%3Dlatest)](https://javadoc.syntaxerror.at/json5/latest) ![GitHub Workflow Status](https://img.shields.io/github/workflow/status/Synt4xErr0r4/json5/Java%20CI%20with%20Maven)

A JSON5 Library for Java (11+)  

## Overview

The [JSON5 Standard](https://json5.org/) tries to make JSON more human-readable  

This is a reference implementation, capable of parsing JSON5 data according to the [specification](https://spec.json5.org/).

## Getting started

In order to use the code, you can either [download the jar](https://github.com/Synt4xErr0r4/json5/releases/download/1.2.0/json5-1.2.0.jar), or use the Maven dependency:
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
  <version>1.2.0</version>
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
The `add` and `insert` methods are used to add values to a JSON array.  

Supported data types are:
- `boolean`
- `byte`
- `short`
- `int`
- `float`
- `double`
- `Number` (any sub-class)
- `String`
- `JSONObject`
- `JSONArray`
- `Instants` (since `1.1.0`, see below)

The normal `getXXX(String key)` and `getXXX(int index)` methods will throw an exception if the specified key or index does not exist, but the
`getXXX(String key, XXX defaults)` and `getXXX(int index, XXX defaults)` methods will return the default value (parameter `defaults`) instead.  
  
The `set(int index, Object value)` method will also throw an exception if the index does not exist. You can use `add(Object value)` instead to append a value to the list.

The getter-methods for numbers always return a rounded or truncated result.
If the actual number is too large to fit into the requested type, the upper bits are truncated (e.g. `int` to `byte` truncates the upper 24 bits).  
If the actual number is a decimal number (e.g. `123.456`), and the requested type is not (e.g. `long`), the decimal places are discarded.  
To check if a number can fit into a type, you can use the `getXXXExact` methods, which will throw an exception if the conversion is not possible without altering the result.  

Numbers are internally always stored as either a `java.math.BigInteger`, `java.math.BigDecimal`, or `double` (`double` is used for `Infinity` and `NaN` only). Therefore, any method
returning raw `java.lang.Object`s will return numbers as one of those types. The same behaviour applies to the `getNumber` methods.

## Changelog
### v1.1.0

Instants (date and time) are now supported. This option can be toggled via the options listed below.

The `JSONOptions` class allows you to customize the behaviour of the parser and stringifyer. It can be created via the builder subclass.  
You can also set the default options used if the supplied options are `null`, by using the method `setDefaultOptions(JSONOptions)`. The default options must not be `null`.

The following options are currently implemented:

- `parseInstants`: (default `true`, *Parser-only*) ([proposed here](https://github.com/json5/json5-spec/issues/4))  
    Whether or not instants should be parsed as such.  
    If this is `false`, `parseStringInstants` and `parseUnixInstants` are ignored
- `parseStringInstants`: (default `true`, *Parser-only*) ([proposed here](https://github.com/json5/json5-spec/issues/4))  
    Whether or not string instants (according to [RFC 3339, Section 5.6](https://datatracker.ietf.org/doc/html/rfc3339#section-5.6)) should be parsed as such.  
    Ignored if `parseInstants` is `false`
- `parseUnixInstants`: (default `true`, *Parser-only*) ([proposed here](https://github.com/json5/json5-spec/issues/4))  
    Whether or not unix instants (integers) should be parsed as such.  
    Ignored if `parseInstants` is `false`
- `stringifyUnixInstants`: (default `false`, *Stringify-only*) ([proposed here](https://github.com/json5/json5-spec/issues/4))  
    Whether or not instants should be stringifyed as unix timestamps (integers).  
    If this is `false`, instants will be stringifyed as strings (according to [RFC 3339, Section 5.6](https://datatracker.ietf.org/doc/html/rfc3339#section-5.6))
- `allowNaN`: (default `true`, *Parser-only*) ([proposed here](https://github.com/json5/json5-spec/issues/24))  
    Whether or not `NaN` should be allowed as a number
- `allowInfinity`: (default `true`, *Parser-only*) ([proposed here](https://github.com/json5/json5-spec/issues/24))  
    Whether or not `Infinity` should be allowed as a number. This applies to both `+Infinity` and `-Infinity`
- `allowInvalidSurrogates`: (default `true`, *Parser-only*) ([proposed here](https://github.com/json5/json5-spec/issues/12))  
    Whether or not invalid unicode surrogate pairs should be allowed
- `quoteSingle`: (default `false`, *Stringify-only*)  
    Whether or not string should be single-quoted (`'`) instead of double-quoted (`"`). This also includes a JSONObject's member names

### v1.2.0

- added `clear()` method 
	removes all values from an object/array
- added `remove(String key)` and `remove(int index)` methods
	remove a certain key/index from an object/array

## Documentation

The JavaDoc for the latest version can be found [here](https://javadoc.syntaxerror.at/json5/latest).

## Credits

This project is partly based on stleary's [JSON-Java](https://github.com/stleary/JSON-java) library.

## License

This project is licensed under the [MIT License](https://github.com/Synt4xErr0r4/json5/blob/main/LICENSE)
