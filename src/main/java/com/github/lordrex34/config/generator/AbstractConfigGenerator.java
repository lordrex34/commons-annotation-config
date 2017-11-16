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

import java.io.BufferedWriter;
import java.io.IOException;
import java.lang.reflect.Field;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Arrays;
import java.util.stream.Collectors;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.github.lordrex34.config.annotation.ConfigClass;
import com.github.lordrex34.config.annotation.ConfigField;
import com.github.lordrex34.config.annotation.ConfigGroupBeginning;
import com.github.lordrex34.config.annotation.ConfigGroupEnding;
import com.github.lordrex34.config.util.ClassPathUtil;

/**
 * @author NB4L1
 * @author _dev_
 * @author lord_rex
 */
public abstract class AbstractConfigGenerator
{
	private static final Logger LOGGER = LoggerFactory.getLogger(AbstractConfigGenerator.class);
	
	/**
	 * Constructs {@link AbstractConfigGenerator}, and also starts the properties file generation process.
	 * @param classLoader the class loader that is used for the process
	 */
	public AbstractConfigGenerator(ClassLoader classLoader)
	{
		try
		{
			ClassPathUtil.getAllClassesAnnotatedWith(classLoader, getPackageName(), ConfigClass.class).forEach(clazz ->
			{
				try
				{
					printConfigClass(clazz);
				}
				catch (IOException e)
				{
					LOGGER.warn("Failed to generate config.", e);
				}
			});
		}
		catch (IOException e)
		{
			LOGGER.warn("Failed to scan for configs", e);
		}
	}
	
	/**
	 * Same as {@link #AbstractConfigGenerator(ClassLoader)}, using {@link ClassLoader#getSystemClassLoader()} as the classLoader parameter.
	 */
	public AbstractConfigGenerator()
	{
		this(ClassLoader.getSystemClassLoader());
	}
	
	/**
	 * A simple static method to simplify properties file generation, in case you don't want to create your own generator implementation.
	 * @param classLoader the class loader that is used for the process
	 * @param packageName the package where configuration related classes are stored
	 */
	public static void generateAll(ClassLoader classLoader, String packageName)
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
	 */
	public static void generateAll(String packageName)
	{
		generateAll(ClassLoader.getSystemClassLoader(), packageName);
	}
	
	/**
	 * Gets the package name that is to be scanned for properties file generation.
	 * @return the package name
	 */
	protected abstract String getPackageName();
	
	/**
	 * Generates a properties file based on the annotation input from the configuration class.
	 * @param clazz the configuration class itself
	 * @throws IOException
	 */
	public static void printConfigClass(Class<?> clazz) throws IOException
	{
		final StringBuilder out = new StringBuilder();
		
		final ConfigClass configClass = clazz.getDeclaredAnnotation(ConfigClass.class);
		if (configClass == null)
		{
			return;
		}
		
		// Header.
		out.append("################################################################################\r\n");
		out.append("## ").append(configClass.fileName().replace("_", " ")).append(" Settings").append(System.lineSeparator());
		out.append("################################################################################\r\n");
		
		out.append(System.lineSeparator()); // separator
		
		// File comment if exists.
		if ((configClass.comment() != null) && (configClass.comment().length > 0))
		{
			for (String line : configClass.comment())
			{
				out.append("# ").append(line).append(System.lineSeparator());
			}
			out.append(System.lineSeparator());
		}
		
		for (Field field : clazz.getDeclaredFields())
		{
			printConfigField(out, field);
		}
		
		final Path configPath = Paths.get("", configClass.pathNames()).resolve(configClass.fileName() + configClass.fileExtension());
		final Path configPathParent = configPath.getParent();
		if (configPathParent != null)
		{
			Files.createDirectories(configPathParent);
		}
		
		try (BufferedWriter bw = Files.newBufferedWriter(configPath))
		{
			bw.append(out.toString());
		}
		
		LOGGER.info("Generated: '{}'", configPath);
	}
	
	/**
	 * Generates the required information of the filed into the properties file.
	 * @param out the string builder used for the generation
	 * @param field the field that has the required information
	 */
	private static void printConfigField(StringBuilder out, Field field)
	{
		final ConfigField configField = field.getDeclaredAnnotation(ConfigField.class);
		
		if (configField == null)
		{
			return;
		}
		
		final ConfigGroupBeginning beginningGroup = field.getDeclaredAnnotation(ConfigGroupBeginning.class);
		if (beginningGroup != null)
		{
			out.append("########################################").append(System.lineSeparator());
			out.append("## Section BEGIN: ").append(beginningGroup.name()).append(System.lineSeparator());
			
			for (String line : beginningGroup.comment())
			{
				out.append("# ").append(line).append(System.lineSeparator());
			}
			
			out.append(System.lineSeparator());
		}
		
		for (String line : configField.comment())
		{
			out.append("# ").append(line).append(System.lineSeparator());
		}
		
		if (!configField.onlyComment())
		{
			out.append("# Default: ").append(configField.value()).append(System.lineSeparator());
			if (field.getType().isEnum())
			{
				out.append("# Available: ").append(Arrays.stream(field.getType().getEnumConstants()).map(String::valueOf).collect(Collectors.joining("|"))).append(System.lineSeparator());
			}
			else if (field.getType().isArray())
			{
				final Class<?> fieldComponentType = field.getType().getComponentType();
				if (fieldComponentType.isEnum())
				{
					out.append("# Available: ").append(Arrays.stream(field.getType().getEnumConstants()).map(String::valueOf).collect(Collectors.joining(","))).append(System.lineSeparator());
				}
			}
			out.append(configField.name()).append(" = ").append(configField.value()).append(System.lineSeparator());
			out.append(System.lineSeparator());
		}
		
		final ConfigGroupEnding endingGroup = field.getDeclaredAnnotation(ConfigGroupEnding.class);
		if (endingGroup != null)
		{
			for (String line : endingGroup.comment())
			{
				out.append("# ").append(line).append(System.lineSeparator());
			}
			
			out.append("## Section END: ").append(endingGroup.name()).append(System.lineSeparator());
			out.append("########################################").append(System.lineSeparator());
			
			out.append(System.lineSeparator());
		}
	}
}
