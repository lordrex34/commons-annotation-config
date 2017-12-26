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

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotEquals;
import static org.junit.Assert.assertNotNull;

import java.io.IOException;
import java.util.Set;

import org.junit.Before;
import org.junit.Test;

import com.github.lordrex34.config.annotation.ConfigClass;
import com.github.lordrex34.config.annotation.ConfigField;

/**
 * @author lord_rex
 */
public class TestConfigSet
{
	private ConfigManager _configManager;
	
	@Before
	public void before() throws IOException, IllegalArgumentException, IllegalAccessException, InstantiationException
	{
		_configManager = new ConfigManager();
		_configManager.load(ITestConfigMarker.class.getPackage().getName());
	}
	
	@Test
	public void test()
	{
		assertNotEquals(_configManager.getConfigRegistrySize(), 0);
		
		assertNotNull(ConfigSetTest.TEST_STRING_SET);
		assertFalse(ConfigSetTest.TEST_STRING_SET.isEmpty());
		
		assertNotNull(ConfigSetTest.TEST_INT_SET);
		assertFalse(ConfigSetTest.TEST_INT_SET.isEmpty());
	}
	
	@ConfigClass(fileName = "set_test")
	public static class ConfigSetTest
	{
		@ConfigField(name = "TestBooleanSet", value = "true,false,true")
		public static Set<Boolean> TEST_BOOLEAN_SET;
		
		@ConfigField(name = "TestByteSet", value = "1,2,3")
		public static Set<Byte> TEST_BYTE_SET;
		
		@ConfigField(name = "TestShortSet", value = "111,221,443")
		public static Set<Short> TEST_SHORT_SET;
		
		@ConfigField(name = "TestIntSet", value = "1222,4442,9993")
		public static Set<Integer> TEST_INT_SET;
		
		@ConfigField(name = "TestLongSet", value = "12252353252352,443252353253242,993252362673293")
		public static Set<Long> TEST_LONG_SET;
		
		@ConfigField(name = "TestFloatSet", value = "1.,3.2,5.")
		public static Set<Float> TEST_FLOAT_SET;
		
		@ConfigField(name = "TestDoubleSet", value = "4.1,2.3,9.7")
		public static Set<Double> TEST_DOUBLE_SET;
		
		@ConfigField(name = "TestStringSet", value = "This,is,a,string,array,test.")
		public static Set<String> TEST_STRING_SET;
		
		@ConfigField(name = "TestEnumSet", value = "TEST_1,TEST_2")
		public static Set<EnumForConfig> TEST_ENUM_SET;
	}
	
}
