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
import java.lang.reflect.Field;
import java.lang.reflect.Modifier;

import org.junit.Before;

import com.github.lordrex34.config.annotation.ConfigClass;
import com.github.lordrex34.config.util.ConfigPropertyRegistry;
import com.github.lordrex34.reflection.util.ClassPathUtil;

/**
 * @author lord_rex
 */
public abstract class AbstractConfigTest
{
	protected ConfigManager _configManager;
	
	@Before
	public void before() throws IOException, IllegalArgumentException, IllegalAccessException, InstantiationException
	{
		clearAll(ITestConfigMarker.class.getPackage().getName());
		_configManager = new ConfigManager();
		_configManager.load(ITestConfigMarker.class.getPackage().getName());
	}
	
	/**
	 * Clears everything from the manager. Clears also the values of the fields. Usable only for tests.
	 * @param packageName the package where configuration related classes are stored
	 * @throws IOException
	 * @throws IllegalAccessException
	 * @throws IllegalArgumentException
	 * @throws SecurityException
	 */
	static void clearAll(String packageName) throws SecurityException, IllegalArgumentException, IllegalAccessException, IOException
	{
		for (Class<?> configClass : ClassPathUtil.getAllClassesAnnotatedWith(ClassLoader.getSystemClassLoader(), packageName, ConfigClass.class))
		{
			for (Field field : configClass.getDeclaredFields())
			{
				if (!Modifier.isStatic(field.getModifiers()) || Modifier.isFinal(field.getModifiers()))
				{
					// Skip inappropriate fields.
					continue;
				}
				
				// private field support
				final boolean wasAccessible = field.isAccessible(); /*field.canAccess(null);*/
				try
				{
					if (!wasAccessible)
					{
						field.setAccessible(true);
					}
					
					final Class<?> type = field.getType();
					if ((type == Boolean.class) || (type == Boolean.TYPE))
					{
						field.setBoolean(null, false);
					}
					else if ((type == Long.class) || (type == Long.TYPE))
					{
						field.setLong(null, 0);
					}
					else if ((type == Integer.class) || (type == Integer.TYPE))
					{
						field.setInt(null, 0);
					}
					else if ((type == Short.class) || (type == Short.TYPE))
					{
						field.setShort(null, (short) 0);
					}
					else if ((type == Byte.class) || (type == Byte.TYPE))
					{
						field.setByte(null, (byte) 0);
					}
					else if ((type == Double.class) || (type == Double.TYPE))
					{
						field.setDouble(null, 0);
					}
					else if ((type == Float.class) || (type == Float.TYPE))
					{
						field.setFloat(null, 0);
					}
					else
					{
						field.set(null, null);
					}
				}
				finally
				{
					// restore field's visibility to the original
					field.setAccessible(wasAccessible);
				}
			}
		}
		
		ConfigPropertyRegistry.clearAll();
	}
}
