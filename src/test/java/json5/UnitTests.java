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
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE
 * SOFTWARE.
 */
package json5;

import static org.junit.jupiter.api.Assertions.*;

import java.time.Instant;

import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import at.syntaxerror.json5.JSONArray;
import at.syntaxerror.json5.JSONException;
import at.syntaxerror.json5.JSONObject;
import at.syntaxerror.json5.JSONOptions;
import at.syntaxerror.json5.JSONOptions.DuplicateBehavior;
import at.syntaxerror.json5.JSONParser;
import at.syntaxerror.json5.JSONStringify;

/**
 * @author SyntaxError404
 * 
 */
class UnitTests {
	
	@BeforeAll
	static void setUpBeforeClass() throws Exception {
	}
	
	JSONOptions allowAllFeatures() {
		JSONOptions opts = JSONOptions.builder()
			.allowBinaryLiterals(true)
			.allowOctalLiterals(true)
			.allowHexFloatingLiterals(true)
			.allowCDigitSeparators(true)
			.allowJavaDigitSeparators(true)
			.allowLongUnicodeEscapes(true)
			.build();
		
		JSONOptions.setDefaultOptions(opts);
		
		return opts;
	}
	
	@Test
	void testDoubleQuoted() {
		assertTrue(
			parse("{ a: \"Test \\\" 123\" }")
				.getString("a")
				.equals("Test \" 123")
		);
	}

	@Test
	void testSingleQuoted() {
		assertTrue(
			parse("{ a: 'Test \\' 123\' }")
				.getString("a")
				.equals("Test ' 123")
		);
	}

	@Test
	void testMixedQuoted() {
		assertTrue(
			parse("{ a: \"Test \\' 123\" }")
				.getString("a")
				.equals("Test ' 123")
		);
	}
	
	@Test
	void testStringify() {
		JSONOptions.setDefaultOptions(
			JSONOptions.builder()
				.stringifyUnixInstants(true)
				.build()
		);
		
		JSONObject json = new JSONObject();
		
		json.set("a", (Object) null);
		json.set("b", false);
		json.set("c", true);
		json.set("d", new JSONObject());
		json.set("e", new JSONArray());
		json.set("f", Double.NaN);
		json.set("g", -123e+45);
		json.set("h", (float) -123e45);
		json.set("i", -123L);
		json.set("j", "Lorem Ipsum");
		json.set("k", Instant.now());
		
		assertEquals(
			json.toString(),
			parse(json.toString()).toString()
		);
	}
	
	@Test
	void testEscapes() {
		assertTrue(
			parse("{ a: \"\\n\\r\\f\\b\\t\\v\\0\\u12Fa\\x7F\" }")
				.getString("a")
				.equals("\n\r\f\b\t\u000B\0\u12Fa\u007F")
		);
	}
	
	@Test
	void testMemberName() {
		assertTrue(
			parse("{ $Lorem\\u0041_Ipsum123指事字: 0 }")
				.has("$LoremA_Ipsum123指事字")
		);
	}
	
	@Test
	void testMultiComments() {
		assertTrue(
			parse("/**/{/**/a/**/:/**/'b'/**/}/**/")
				.has("a")
		);
	}
	
	@Test
	void testSingleComments() {
		assertTrue(
			parse("// test\n{ // lorem ipsum\n a: 'b'\n// test\n}// test")
				.has("a")
		);
	}
	
	/** @since 1.1.0 */
	@Test
	void testInstant() {
		assertFalse(
			parse("{a: -9223372036854775808}")
				.isInstant("a")
		);
		
		assertTrue(
			parse("{a: 1338150759534}")
				.isInstant("a")
		);
		
		assertTrue(
			parse("{a: '2001-09-09T01:46:40Z'}")
				.isInstant("a")
		);
	}
	
	/** @since 1.1.0 */
	@Test
	void testHex() {
		assertEquals(
			0xCAFEBABEL,
			parse("{a: 0xCAFEBABE}")
				.getLong("a")
		);
	}
	
	@Test
	void testSpecial() {
		assertTrue(
			Double.isNaN(
				parse("{a: +NaN}")
					.getDouble("a")
			)
		);
		
		assertTrue(
			Double.isInfinite(
				parse("{a: -Infinity}")
					.getDouble("a")
			)
		);
	}
	
	JSONObject parse(String str) {
		return new JSONObject(new JSONParser(str));
	}
	
	/** @since 1.2.1 */
	@Test
	void testStringifyUnicode() {
		assertEquals(
			"{'a':'\\uD800'}",
			JSONStringify.toString(
				new JSONObject().set("a", "\uD800"), // U+D800 is a surrogate character and should therefore be escaped
				0,
				JSONOptions.builder()
					.quoteSingle(true)
					.build()
			)
		);
	}
	
	@Test
	void testParseInvalidSurrogate() {
		assertThrows(
			JSONException.class,
			() -> new JSONObject(
				new JSONParser(
					"{a: 'A\uD800'}", // invalid surrogate sequence (non-surrogate + high surrogate)
					JSONOptions.builder()
						.allowInvalidSurrogates(false)
						.build()
				)
			)
		);
	}
	
	/** @since 1.3.0 */
	@Test
	void testDuplicateUnique() {
		assertThrows(
			JSONException.class,
			() -> new JSONObject(
				new JSONParser(
					"{ a: 123, b: 456, a: 789 }"
				)
			)
		);
	}

	@Test
	void testDuplicateLastValueWins() {
		assertEquals(
			new JSONObject(
				new JSONParser(
					"{ a: 123, b: 456, a: 789 }",
					JSONOptions.builder()
						.duplicateBehaviour(DuplicateBehavior.LAST_WINS)
						.build()
				)
			).getInt("a"),
			789
		);
	}

	@Test
	void testDuplicateDuplicate() {
		assertEquals(
			new JSONObject(
				new JSONParser(
					"{ a: 123, b: 456, a: 789, c: false, a: 'test123' }",
					JSONOptions.builder()
						.duplicateBehaviour(DuplicateBehavior.DUPLICATE)
						.build()
				)
			).getArray("a")
				.toString(),
			new JSONArray()
				.add(123)
				.add(789)
				.add("test123")
				.toString()
		);
	}

	/** @since 2.0.0 */
	@Test
	void testParseLiterals() {
		allowAllFeatures();
		
		JSONObject obj = new JSONObject("{bin: 0B10101010101, oct: 0o1234567, hex: 0x12cafeBEEF, fhex: 0xAA.BBp+10, sep: 123_456'789}");
		
		assertEquals(obj.getLong("bin"), 0b10101010101);
		assertEquals(obj.getLong("oct"), 01234567);
		assertEquals(obj.getLong("hex"), 0x12cafeBEEFL);
		assertEquals(obj.getDouble("fhex"), 0xAA.BBp+10);
		assertEquals(obj.getLong("sep"), 123456789L);
	}

	/** @since 2.0.0 */
	@Test
	void testParseInvalidLiterals() {
		allowAllFeatures();
		
		assertThrows(
			JSONException.class,
			() -> new JSONObject("{a: 0b1010102}") // invalid binary digit '2'
		);
		
		assertThrows(
			JSONException.class,
			() -> new JSONObject("{a: 0o12345678}") // invalid octal digit '8'
		);
		
		assertThrows(
			JSONException.class,
			() -> new JSONObject("{a: 0xabcdefg}") // invalid hex digit 'g'
		);
		
		assertThrows(
			JSONException.class,
			() -> new JSONObject("{a: 12345abc}") // trailing junk
		);
		
		assertThrows(
			JSONException.class,
			() -> new JSONObject("{a: 0x0.0e0}") // wrong exponent char for hexadecimal floating-point literals
		);
		
		assertThrows(
			JSONException.class,
			() -> new JSONObject("{a: 0x0.0}") // missing exponent for hexadecimal floating-point literals
		);
		
		assertThrows(
			JSONException.class,
			() -> new JSONObject("{a: 0o_123}") // illegal digit sep at start
		);
		
		assertThrows(
			JSONException.class,
			() -> new JSONObject("{a: _123}") // illegal digit sep at start
		);
		
		assertThrows(
			JSONException.class,
			() -> new JSONObject("{a: 123_}") // illegal digit sep at end
		);
		
		assertThrows(
			JSONException.class,
			() -> new JSONObject("{a: 123._123}") // illegal digit sep in the middle
		);
	}

	/** @since 2.0.0 */
	@Test
	void testUTF32Escapes() {
		allowAllFeatures();
		
		assertTrue(
			parse("{ a: '\\U0001F642\\U0001F41B' }")
				.getString("a")
				.equals("🙂🐛")
		);
	}

	/** @since 2.0.0 */
	@Test
	void testLineContinuation() {
		allowAllFeatures();
		
		assertTrue(
			parse("{ a: 'abcdef\\\nghijklmn' }")
				.getString("a")
				.equals("abcdefghijklmn")
		);
		
		assertTrue(
			parse("{ a: 'abcdef\\\r\nghijklmn' }")
				.getString("a")
				.equals("abcdefghijklmn")
		);
		
		assertTrue(
			parse("{ a: 'abcdef\\\rghijklmn' }")
				.getString("a")
				.equals("abcdefghijklmn")
		);
		
		assertTrue(
			parse("{ a: 'abcdef\\\n\\\n\\\nghijklmn' }")
				.getString("a")
				.equals("abcdefghijklmn")
		);
		
		assertTrue(
			parse("{ a: 'abcdef\\\n\\\r\n\\\r\\\r\\\r\n\\\n\\\rghijklmn' }")
				.getString("a")
				.equals("abcdefghijklmn")
		);
	}

}
