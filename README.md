# json5 [![javadoc](https://img.shields.io/endpoint?label=javadoc&url=https%3A%2F%2Fjavadoc.syntaxerror.at%2Fjson5%2F%3Fbadge%3Dtrue%26version%3Dlatest)](https://javadoc.syntaxerror.at/json5/latest) ![GitHub Workflow Status](https://img.shields.io/github/actions/workflow/status/Synt4xErr0r4/json5/maven.yml)

A JSON5 Library for Java (11+)  

## Overview

The [JSON5 Standard](https://json5.org/) tries to make JSON more human-readable  

This is a reference implementation, capable of parsing JSON5 data according to the [specification](https://spec.json5.org/).

## Getting started

In order to use the code, you can either [download the jar](https://github.com/Synt4xErr0r4/json5/releases/download/2.0.0/json5-2.0.0.jar), or use the Maven dependency:

*Note:* Starting with version 2.0.0, the Maven dependency is now also available from [Maven Central](https://central.sonatype.com/artifact/at.syntaxerror/json5/2.0.0).

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
  <version>2.0.0</version>
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
    //...
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

```json5
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

```json5
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
    Whether instants should be parsed as such.  
    If this is `false`, `parseStringInstants` and `parseUnixInstants` are ignored
- `parseStringInstants`: (default `true`, *Parser-only*) ([proposed here](https://github.com/json5/json5-spec/issues/4))  
    Whether string instants (according to [RFC 3339, Section 5.6](https://datatracker.ietf.org/doc/html/rfc3339#section-5.6)) should be parsed as such.  
    Ignored if `parseInstants` is `false`
- `parseUnixInstants`: (default `true`, *Parser-only*) ([proposed here](https://github.com/json5/json5-spec/issues/4))  
    Whether unix instants (integers) should be parsed as such.  
    Ignored if `parseInstants` is `false`
- `stringifyUnixInstants`: (default `false`, *Stringify-only*) ([proposed here](https://github.com/json5/json5-spec/issues/4))  
    Whether instants should be stringifyed as unix timestamps (integers).  
    If this is `false`, instants will be stringifyed as strings (according to [RFC 3339, Section 5.6](https://datatracker.ietf.org/doc/html/rfc3339#section-5.6))
- `allowNaN`: (default `true`, *Parser-only*) ([proposed here](https://github.com/json5/json5-spec/issues/24))  
    Whether `NaN` should be allowed as a number
- `allowInfinity`: (default `true`, *Parser-only*) ([proposed here](https://github.com/json5/json5-spec/issues/24))  
    Whether `Infinity` should be allowed as a number. This applies to both `+Infinity` and `-Infinity`
- `allowInvalidSurrogates`: (default `true`, *Parser-only*) ([proposed here](https://github.com/json5/json5-spec/issues/12))  
    Whether invalid unicode surrogate pairs should be allowed
- `quoteSingle`: (default `false`, *Stringify-only*)  
    Whether strings should be single-quoted (`'`) instead of double-quoted (`"`). This also includes a JSONObject's member names

### v1.2.0

- added `clear()` method:  
  removes all values from an object/array
- added `remove(String key)` and `remove(int index)` methods:  
  remove a certain key/index from an object/array

### v1.2.1

- fixed a bug where stringifying non-printable unicode characters would throw a ClassCastException
- fixed a bug where checking for invalid unicode surrogate pairs would not work as intended

### v1.3.0

- added option `duplicateBehavior` (*Parser-only*) for different duplicate behaviors to `JSONOptions` ([proposed here](https://github.com/json5/json5-spec/issues/38)). The default behavior is `UNIQUE`. The enum `JSONOptions.DuplicateBehavior` defines the following behaviors:
  - `UNIQUE`: Throws an exception when a key is encountered multiple times within the same object
  - `LAST_WINS`: Only the last encountered value is significant, all previous occurrences are silently discarded
  - `DUPLICATE`: Wraps duplicate values inside an array, effectively treating them as if they were declared as one

Example:

```json
{
  "a": true,
  "a": 123
}
```

`UNIQUE` throws a `JSONException`, `LAST_WINS` declares `a` as `123`.  

When the behavior is `DUPLICATE`, the snippet above is effectively equal to the following:

```json
{
  "a": [
    true,
    123
  ]
}
```

### v2.0.0

- the `JSONParser` no longer uses regular expressions for parsing
- removed options `parseInstants`, `parseStringInstants` and `parseUnixInstants` from `JSONOptions`.
  - you can still use `getInstant(...)` in `JSONObject` and `JSONArray`, but the instant will
    now be parsed dynamically rather than when the full JSON is parsed.
  - furthermore, you can still add `Instant`s to `JSONObject`s and `JSONArray`s and use the `stringifyUnixInstants` option
- added options to `JSONOptions`:
  - `allowBinaryLiterals`: (default `false`, *Parser-only*)  
    Allows the use of binary literals (prefixed with `0b` or `0B`) for integers
  - `allowOctalLiterals`: (default `false`, *Parser-only*)
    Allows the use of octal literals (prefixed with `0o` or `0O`) for integers
  - `allowHexFloatingLiterals`: (default `false`, *Parser-only*)  
    Allows the use of hexadecimal floating-point notationi (e.g. `0xA.BCp+12`) for floating-point numbers
  - `allowJavaDigitSeparators`: (default `false`, *Parser-only*)  
    Allows the use of Java's `_` digit separators (e.g. `123_456` for `123456`) for integers and floating-point numbers.
  - `allowCDigitSeparators`: (default `false`, *Parser-only*)  
    Allows the use of C23's `'` digit separators (e.g. `123'456` for `123456`) for integers and floating-point numbers.
  - `allowLongUnicodeEscapes`: (default `false`, *Parser-only*)  
    Allows the use of 32-bit unicode escape sequences (e.g. `\U0001F642` for `🙂`)
  - `stringifyAscii`: (default `false`, *Stringify-only*)  
    Ensures that the stringifyed JSON is always valid ASCII by replacing non-ASCII characters with their UTF-16 unicode escape sequence.
- added methods to create shallow (`copy`) and deep copies (`deepCopy`) of `JSONObject`s and `JSONArray`s
- added an additional `forEach` method to `JSONObject`, which takes a `BiConsumer<String, Object>` rather than a `Consumer<Map.Entry<String, Object>>`
- added an additional `forEach` method to `JSONArray`, which takes a `BiConsumer<Integer, Object>` rather than a `Consumer<Object>`
- added `removeIf` methods to `JSONObject` and `JSONArray`, which take a `BiPredicate<String, Object>` or `BiPredicate<Integer, Object>` and remove all
  the entries where the predicate returns `true`.
- added `retainIf` methods to `JSONObject` and `JSONArray`, which take a `BiPredicate<String, Object>` or `BiPredicate<Integer, Object>` and retain only
  the entries where the predicate returns `true`.
- added `removeIf(String, Predicate<Object>)` and `removeXIf(String, Predicate<X>)` methods to `JSONObject`, which removes the value associated with the
  given key if the predicate returns `true`.
- added `retainIf(String, Predicate<Object>)` and `retainXIf(String, Predicate<X>)` methods to `JSONObject`, which retains the value associated with the
  given key only if the predicate returns `true`.
- added `removeKeys(JSONObject)` method to `JSONObject`, which removes every key that also exists in the given `JSONObject`
- added `retainKeys(JSONObject)` method to `JSONObject`, which retains only the keys that also exist in the given `JSONObject`
- added `putAll(JSONObject)` method to `JSONObject`, which copies (shallow copy) all values of the given `JSONObject` to the target object
- added `putAllDeep(JSONObject)` method to `JSONObject`, which copies (deep copy) all values of the given `JSONObject` to the target object
- added `setIfAbsent(String, Object)` and `setIfPresent(String, Object)` methods to `JSONObject`, which sets the value only if the key is either absent or present, respectively.
- added `compute`, `computeX`, `computeIfAbsent`, `computeXIfAbsent`, `computeIfPresent`, `computeXIfPresent` methods to `JSONObject`, which sets the
  value returned by the (re)mapping function either unconditionally, if the key is absent, or if the key is present, respectively.
- added `sublist` and `deepSublist` methods to `JSONArray`, which copies (shallow or deep copy, respectively) part of the `JSONArray` to a new `JSONArray`.
- added `removeAll` and `retainAll` methods to `JSONArray`, which remove all or retain only common values of the `JSONArray`s.
- added `addAll(JSONArray)` method to `JSONArray`, which copies (shallow copy) all values of the given `JSONArray` to the target array.
- added `addAllDeep(JSONArray)` method to `JSONArray`, which copies (deep copy) all values of the given `JSONArray` to the target array.

Note regarding digit separators: Digit separators may neither occur next to each other, nor at the beginning nor the end of a literal. They can also be used within binary/octal/hexadecimal and hexadecimal floating-point literals.

## Documentation

The JavaDoc for the latest version can be found [here](https://javadoc.syntaxerror.at/json5/latest).

## Credits

This project is partly based on stleary's [JSON-Java](https://github.com/stleary/JSON-java) library.

## License

This project is licensed under the [MIT License](https://github.com/Synt4xErr0r4/json5/blob/main/LICENSE)
