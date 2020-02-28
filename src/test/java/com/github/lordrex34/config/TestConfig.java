/*
 * Copyright (c) 2017 Reginald Ravenhorst <lordrex34@gmail.com>
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
package com.github.lordrex34.config;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotEquals;
import static org.junit.Assert.assertTrue;

import org.junit.Test;

import com.github.lordrex34.config.annotation.ConfigClass;
import com.github.lordrex34.config.annotation.ConfigField;
import com.github.lordrex34.config.annotation.ConfigGroupBeginning;
import com.github.lordrex34.config.annotation.ConfigGroupEnding;

import java.io.IOException;

/**
 * @author lord_rex
 */
public class TestConfig extends AbstractConfigTest
{
	@Test
	public void test()
	{
		assertNotEquals(_configManager.getConfigRegistrySize(), 0);
		assertTrue(ConfigTest.TEST_BOOLEAN);
		assertEquals(ConfigTest.TEST_STRING, ConfigTest.TEST_STRING_VALUE);
	}
	
	@Test
	public void testReload() throws IllegalAccessException, IOException, InstantiationException
	{
		assertNotEquals(_configManager.getConfigRegistrySize(), 0);
		assertTrue(ConfigTest.TEST_BOOLEAN);
		assertEquals(ConfigTest.TEST_STRING, ConfigTest.TEST_STRING_VALUE);
		
		reload();
		
		assertNotEquals(_configManager.getConfigRegistrySize(), 0);
		assertTrue(ConfigTest.TEST_BOOLEAN);
		assertEquals(ConfigTest.TEST_STRING, ConfigTest.TEST_STRING_VALUE);
	}
	
	@ConfigClass(fileName = "test")
	public static class ConfigTest
	{
		@ConfigGroupBeginning(name = "Booleans")
		@ConfigField(name = "TestBoolean", value = "true")
		@ConfigGroupEnding(name = "Booleans")
		public static boolean TEST_BOOLEAN;
		
		@ConfigGroupBeginning(name = "Numbers")
		@ConfigField(name = "TestByte", value = "120")
		public static byte TEST_BYTE;
		
		@ConfigField(name = "TestShort", value = "9870")
		public static short TEST_SHORT;
		
		@ConfigField(name = "TestInt", value = "129834")
		public static int TEST_INT;
		
		@ConfigField(name = "TestLong", value = "712983235535234")
		public static long TEST_LONG;
		
		@ConfigField(name = "TestFloat", value = "1234.")
		public static float TEST_FLOAT;
		
		@ConfigGroupEnding(name = "Numbers")
		@ConfigField(name = "TestDouble", value = "1234.14")
		public static double TEST_DOUBLE;
		
		public static final String TEST_STRING_VALUE = "Any string is good here.";
		
		@ConfigGroupBeginning(name = "Strings")
		@ConfigField(name = "TestString", value = TEST_STRING_VALUE)
		@ConfigGroupEnding(name = "Strings")
		public static String TEST_STRING;
		
		@ConfigGroupBeginning(name = "Enums")
		@ConfigField(name = "TestEnum", value = "TEST_1")
		@ConfigGroupEnding(name = "Enums")
		public static EnumForConfig TEST_ENUM;
	}
}
