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

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.time.Instant;

import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import at.syntaxerror.json5.JSONArray;
import at.syntaxerror.json5.JSONObject;
import at.syntaxerror.json5.JSONOptions;
import at.syntaxerror.json5.JSONParser;

/**
 * @author SyntaxError404
 * 
 */
class UnitTests {
	
	@BeforeAll
	static void setUpBeforeClass() throws Exception {
		// compile regex patterns
		JSONParser.class.toString();
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
		json.set("g", 123e+45);
		json.set("h", (float) -123e45);
		json.set("i", 123L);
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
		assertTrue(
			parse("{a:1338150759534}")
				.isInstant("a")
		);
		
		assertEquals(
			parse("{a:1338150759534}")
				.getLong("a"),
			1338150759534L
		);
		
		assertEquals(
			parse("{a:'2001-09-09T01:46:40Z'}")
				.getString("a"),
			"2001-09-09T01:46:40Z"
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

}
