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

import java.io.IOException;

import org.junit.Before;
import org.junit.Test;

import com.github.lordrex34.config.annotation.ConfigClass;
import com.github.lordrex34.config.annotation.ConfigField;

/**
 * @author lord_rex
 */
public class TestMultipleConfigLoad extends AbstractConfigTest
{
	@Override
	@Before
	public void before() throws SecurityException, IllegalArgumentException, IllegalAccessException, IOException
	{
		clearAll(ITestConfigMarker.class.getPackage().getName());
		_configManager = new ConfigManager();
	}
	
	@Test
	public void test() throws IllegalArgumentException, IllegalAccessException, InstantiationException, IOException
	{
		for (int i = 0; i < 3; i++)
		{
			_configManager.load(ITestConfigMarker.class.getPackage().getName());
		}
	}
	
	@ConfigClass(fileName = "test")
	public static class ConfigTest
	{
		@ConfigField(name = "TestFooString", value = "Foo")
		public static String TEST_FOO_STRING;
	}
}
