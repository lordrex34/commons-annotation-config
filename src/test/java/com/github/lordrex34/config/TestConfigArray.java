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

import org.junit.BeforeClass;
import org.junit.Test;

import com.github.lordrex34.config.impl.ConfigArrayTest;
import com.github.lordrex34.config.impl.ITestConfigMarker;

/**
 * @author lord_rex
 */
public class TestConfigArray
{
	@BeforeClass
	public static void beforeClass()
	{
		// clear to avoid conflict with other tests
		ConfigManager.getInstance().clear();
		
		// load configurations
		ConfigManager.getInstance().load(ITestConfigMarker.class.getPackage().getName());
	}
	
	@Test
	public void test()
	{
		assertNotEquals(ConfigManager.getInstance().getConfigRegistry().size(), 0);
		assertThat(ConfigArrayTest.TEST_STRING_ARRAY, is(ConfigArrayTest.TEST_STRING_ARRAY_VALUE.split(",")));
		assertThat(ConfigArrayTest.TEST_INT_ARRAY[1], is(Integer.valueOf(ConfigArrayTest.TEST_INT_ARRAY_VALUE.split(",")[1])));
	}
}
