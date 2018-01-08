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

import java.lang.reflect.Field;

import org.junit.Test;

import com.github.lordrex34.config.annotation.ConfigClass;
import com.github.lordrex34.config.annotation.ConfigField;
import com.github.lordrex34.config.lang.ConfigProperties;
import com.github.lordrex34.config.supplier.IConfigValueSupplier;

/**
 * @author lord_rex
 */
public class TestConfigSupplier extends AbstractConfigTest
{
	@Test
	public void test()
	{
		assertNotEquals(_configManager.getConfigRegistrySize(), 0);
		assertThat(ConfigSupplierTest.TEST_STRING_SUPPLIER, is(ConfigSupplierTest.SUPPLIED_STRING_VALUE));
		assertThat(ConfigSupplierTest.TEST_INTEGER_SUPPLIER, is(ConfigSupplierTest.SUPPLIED_INTEGER_VALUE));
	}
	
	@ConfigClass(fileName = "supplier_test")
	public static class ConfigSupplierTest
	{
		public static final String SUPPLIED_STRING_VALUE = "My name is Lord Rex.";
		
		@ConfigField(name = "TestStringSupplier", valueSupplier = MyStringConfigValueSupplier.class, value = "Value is not loaded from here, but from the supplier.")
		public static String TEST_STRING_SUPPLIER;
		
		public static final class MyStringConfigValueSupplier implements IConfigValueSupplier<String>
		{
			@Override
			public String supply(Class<?> clazz, Field field, ConfigField configField, ConfigProperties properties)
			{
				return SUPPLIED_STRING_VALUE;
			}
		}
		
		public static final int SUPPLIED_INTEGER_VALUE = 1234;
		
		@ConfigField(name = "TestIntegerSupplier", valueSupplier = MyIntegerConfigValueSupplier.class, value = "Value is not loaded from here, but from the supplier.")
		public static int TEST_INTEGER_SUPPLIER;
		
		public static final class MyIntegerConfigValueSupplier implements IConfigValueSupplier<Integer>
		{
			@Override
			public Integer supply(Class<?> clazz, Field field, ConfigField configField, ConfigProperties properties)
			{
				return SUPPLIED_INTEGER_VALUE;
			}
		}
	}
	
}
