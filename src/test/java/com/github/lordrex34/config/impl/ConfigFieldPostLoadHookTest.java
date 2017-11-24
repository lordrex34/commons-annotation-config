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
package com.github.lordrex34.config.impl;

import com.github.lordrex34.config.annotation.ConfigClass;
import com.github.lordrex34.config.annotation.ConfigField;
import com.github.lordrex34.config.postloadhooks.IConfigPostLoadHook;
import com.github.lordrex34.config.util.PropertiesParser;

/**
 * @author lord_rex
 */
@ConfigClass(fileName = "field_post_load_hook_test")
public class ConfigFieldPostLoadHookTest
{
	@ConfigField(name = "TestPostString", value = "Any string is good here.", postLoadHook = MyStringPostLoadHook.class)
	public static String TEST_POST_STRING;
	
	public static final String POST_STRING_VALUE = "Value is post changed. (field)";
	
	public static final class MyStringPostLoadHook implements IConfigPostLoadHook
	{
		@Override
		public void load(PropertiesParser properties, PropertiesParser override)
		{
			TEST_POST_STRING = POST_STRING_VALUE;
		}
	}
	
	public static final int POST_INT_VALUE = 9_000;
	
	public static final class MyIntPostLoadHook implements IConfigPostLoadHook
	{
		@Override
		public void load(PropertiesParser properties, PropertiesParser override)
		{
			TEST_POST_INT = POST_INT_VALUE;
		}
	}
	
	@ConfigField(name = "TestPostInt", value = "129834", postLoadHook = MyIntPostLoadHook.class)
	public static int TEST_POST_INT;
}