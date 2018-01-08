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

import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import com.github.lordrex34.config.annotation.ConfigClass;
import com.github.lordrex34.config.annotation.ConfigField;

/**
 * @author UnAfraid
 */
public class EnvironmentTest
{
	@Before
	public void before()
	{
		ConfigManager configManager = new ConfigManager();
		configManager.setOverrideSystemAllowed(false);
		configManager.load(EnvironmentTest.class.getPackage().getName());
	}
	
	
	@Test
	public void testEnv()
	{
		Assert.assertNotNull(EnvTest.VALUE);
		Assert.assertNotEquals(EnvTest.VALUE, "empty");
	}

	@ConfigClass(pathNames = "build", fileName = "test/EnvTest")
	public static class EnvTest 
	{
		@ConfigField(name = "Value", value = "empty")
		public static String VALUE;
	}
}
