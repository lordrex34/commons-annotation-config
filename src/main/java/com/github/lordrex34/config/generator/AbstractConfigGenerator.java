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
package com.github.lordrex34.config.generator;

import java.io.IOException;

import com.github.lordrex34.config.annotation.ConfigClass;
import com.github.lordrex34.config.model.ConfigClassInfo;
import com.github.lordrex34.config.util.ClassPathUtil;

/**
 * @author NB4L1
 * @author _dev_
 * @author lord_rex
 */
public abstract class AbstractConfigGenerator
{
	/**
	 * Constructs {@link AbstractConfigGenerator}, and also starts the properties file generation process.
	 * @param classLoader the class loader that is used for the process
	 * @throws IOException
	 */
	public AbstractConfigGenerator(ClassLoader classLoader) throws IOException
	{
		for (Class<?> clazz : ClassPathUtil.getAllClassesAnnotatedWith(classLoader, getPackageName(), ConfigClass.class))
		{
			final ConfigClassInfo configClassInfo = new ConfigClassInfo(clazz);
			configClassInfo.generate();
		}
	}
	
	/**
	 * Same as {@link #AbstractConfigGenerator(ClassLoader)}, using {@link ClassLoader#getSystemClassLoader()} as the classLoader parameter.
	 * @throws IOException
	 */
	public AbstractConfigGenerator() throws IOException
	{
		this(ClassLoader.getSystemClassLoader());
	}
	
	/**
	 * A simple static method to simplify properties file generation, in case you don't want to create your own generator implementation.
	 * @param classLoader the class loader that is used for the process
	 * @param packageName the package where configuration related classes are stored
	 * @throws IOException
	 */
	public static void generateAll(ClassLoader classLoader, String packageName) throws IOException
	{
		new AbstractConfigGenerator(classLoader)
		{
			@Override
			protected String getPackageName()
			{
				return packageName;
			}
		};
	}
	
	/**
	 * Same as {@link #generateAll(ClassLoader, String)}, using {@link ClassLoader#getSystemClassLoader()} as the classLoader parameter.
	 * @param packageName
	 * @throws IOException
	 */
	public static void generateAll(String packageName) throws IOException
	{
		generateAll(ClassLoader.getSystemClassLoader(), packageName);
	}
	
	/**
	 * Gets the package name that is to be scanned for properties file generation.
	 * @return the package name
	 */
	protected abstract String getPackageName();
}
