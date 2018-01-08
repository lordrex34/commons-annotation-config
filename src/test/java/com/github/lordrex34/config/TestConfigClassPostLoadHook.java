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

import org.junit.Test;

import com.github.lordrex34.config.annotation.ConfigClass;
import com.github.lordrex34.config.annotation.ConfigField;
import com.github.lordrex34.config.postloadhooks.IConfigPostLoadClassHook;

/**
 * @author lord_rex
 */
public class TestConfigClassPostLoadHook extends AbstractConfigTest
{
	@Test
	public void test()
	{
		assertNotEquals(_configManager.getConfigRegistrySize(), 0);
		assertThat(ConfigClassPostLoadHookTest.TEST_POST_STRING, is(ConfigClassTestHook.POST_STRING_VALUE));
		assertThat(ConfigClassPostLoadHookTest.TEST_POST_INT, is(ConfigClassTestHook.POST_INT_VALUE));
	}
	
	@ConfigClass(fileName = "class_post_load_hook_test", postLoadHook = ConfigClassTestHook.class)
	public static class ConfigClassPostLoadHookTest
	{
		@ConfigField(name = "TestPostInt", value = "129834")
		public static int TEST_POST_INT;
		
		@ConfigField(name = "TestPostString", value = "Any string is good here.")
		public static String TEST_POST_STRING;
	}
	
	public static class ConfigClassTestHook implements IConfigPostLoadClassHook
	{
		public static final String POST_STRING_VALUE = "Value is post changed. (class)";
		public static final int POST_INT_VALUE = 4_000;
		
		@Override
		public void load()
		{
			ConfigClassPostLoadHookTest.TEST_POST_STRING = POST_STRING_VALUE;
			ConfigClassPostLoadHookTest.TEST_POST_INT = POST_INT_VALUE;
		}
	}
}
