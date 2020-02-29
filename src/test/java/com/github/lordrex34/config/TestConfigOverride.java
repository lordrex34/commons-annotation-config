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

import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertNotEquals;
import static org.junit.Assert.assertThat;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;

import org.junit.Before;
import org.junit.Test;

import com.github.lordrex34.config.annotation.ConfigClass;
import com.github.lordrex34.config.annotation.ConfigField;

/**
 * @author lord_rex
 */
public class TestConfigOverride extends AbstractConfigTest
{
	private static final int OVERRIDDEN_INT = 300;
	private static final int OVERRIDDEN_INT_ALT = 333;
	private static final String OVERRIDDEN_STRING = "My overridden string. :)";
	private static final String OVERRIDDEN_STRING_ALT = "My alternative overridden string. :)";
	
	private boolean _alternative;
	
	@Override
	@Before
	public void before() throws IOException, IllegalArgumentException, IllegalAccessException, InstantiationException
	{
		clearAll(ITestConfigMarker.class.getPackage().getName());
		_configManager = new ConfigManager(this::overrideInputStream);
		_configManager.load(ITestConfigMarker.class.getPackage().getName());
	}
	
	private InputStream overrideInputStream()
	{
		final StringBuffer buffer = new StringBuffer();
		buffer.append("TestOverrideInt = ").append(_alternative ? OVERRIDDEN_INT_ALT : OVERRIDDEN_INT).append(System.lineSeparator());
		buffer.append("TestOverrideString = ").append(_alternative ? OVERRIDDEN_STRING_ALT : OVERRIDDEN_STRING).append(System.lineSeparator());
		return new ByteArrayInputStream(buffer.toString().getBytes());
	}
	
	@Test
	public void test()
	{
		assertNotEquals(_configManager.getConfigRegistrySize(), 0);
		assertNotEquals(_configManager.getOverriddenProperties().size(), 0);
		assertThat(ConfigOverrideTest.TEST_OVERRIDE_INT, is(OVERRIDDEN_INT));
		assertThat(ConfigOverrideTest.TEST_OVERRIDE_STRING, is(OVERRIDDEN_STRING));
	}
	
	@Test
	public void testReload() throws IllegalAccessException, IOException, InstantiationException
	{
		assertNotEquals(_configManager.getConfigRegistrySize(), 0);
		assertNotEquals(_configManager.getOverriddenProperties().size(), 0);
		assertThat(ConfigOverrideTest.TEST_OVERRIDE_INT, is(OVERRIDDEN_INT));
		assertThat(ConfigOverrideTest.TEST_OVERRIDE_STRING, is(OVERRIDDEN_STRING));
		
		_configManager.reload(ITestConfigMarker.class.getPackage().getName());
		
		assertNotEquals(_configManager.getConfigRegistrySize(), 0);
		assertNotEquals(_configManager.getOverriddenProperties().size(), 0);
		assertThat(ConfigOverrideTest.TEST_OVERRIDE_INT, is(OVERRIDDEN_INT));
		assertThat(ConfigOverrideTest.TEST_OVERRIDE_STRING, is(OVERRIDDEN_STRING));
		
		_alternative = true;
		_configManager.reload(ITestConfigMarker.class.getPackage().getName());
		
		assertNotEquals(_configManager.getConfigRegistrySize(), 0);
		assertNotEquals(_configManager.getOverriddenProperties().size(), 0);
		assertThat(ConfigOverrideTest.TEST_OVERRIDE_INT, is(OVERRIDDEN_INT_ALT));
		assertThat(ConfigOverrideTest.TEST_OVERRIDE_STRING, is(OVERRIDDEN_STRING_ALT));
	}
	
	@ConfigClass(fileName = "override_test")
	public static class ConfigOverrideTest
	{
		@ConfigField(name = "TestOverrideInt", value = "1")
		public static int TEST_OVERRIDE_INT;
		
		@ConfigField(name = "TestOverrideString", value = "These configuration will be overridden.")
		public static String TEST_OVERRIDE_STRING;
	}
}
