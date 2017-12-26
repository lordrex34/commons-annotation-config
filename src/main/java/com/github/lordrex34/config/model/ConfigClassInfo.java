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
package com.github.lordrex34.config.model;

import java.io.BufferedWriter;
import java.io.IOException;
import java.lang.reflect.Field;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.github.lordrex34.config.annotation.ConfigClass;
import com.github.lordrex34.config.annotation.ConfigField;
import com.github.lordrex34.config.component.ConfigComponents;
import com.github.lordrex34.config.context.ConfigClassLoadingContext;
import com.github.lordrex34.config.context.ConfigFieldLoadingContext;
import com.github.lordrex34.config.lang.ConfigProperties;

/**
 * @author NB4L1 (original concept)
 * @author lord_rex
 */
public final class ConfigClassInfo
{
	private static final Logger LOGGER = LoggerFactory.getLogger(ConfigClassInfo.class);
	
	/** The class that is being scanned for the annotations. */
	private final Class<?> _clazz;
	
	/** The configuration class annotation. */
	private final ConfigClass _configClass;
	
	/** A list of the {@link ConfigFieldInfo} classes contained by this info container class. */
	private final List<ConfigFieldInfo> _fieldInfoClasses = new ArrayList<>();
	
	/**
	 * Constructs a new info container class.
	 * @param clazz the configuration class itself
	 */
	public ConfigClassInfo(Class<?> clazz)
	{
		_clazz = clazz;
		_configClass = Objects.requireNonNull(_clazz.getDeclaredAnnotation(ConfigClass.class), () -> "Class " + _clazz + " doesn't have @ConfigClass annotation!");
		
		for (Field field : _clazz.getDeclaredFields())
		{
			if (field.getDeclaredAnnotation(ConfigField.class) == null)
			{
				// skip fields not using ConfigField annotation
				continue;
			}
			
			_fieldInfoClasses.add(new ConfigFieldInfo(_clazz, field));
		}
	}
	
	/**
	 * Gets all registered field info classes inside this class.
	 * @return the fieldInfoClasses
	 */
	public List<ConfigFieldInfo> getFieldInfoClasses()
	{
		return _fieldInfoClasses;
	}
	
	/**
	 * Loads the configuration class that is being managed by this information container.
	 * @param classLoadingContext the context of the actual loading
	 * @throws IOException
	 * @throws InstantiationException
	 * @throws IllegalAccessException
	 * @throws IllegalArgumentException
	 */
	public void load(ConfigClassLoadingContext classLoadingContext) throws IOException, IllegalArgumentException, IllegalAccessException, InstantiationException
	{
		final Path configPath = Paths.get("", _configClass.pathNames()).resolve(_configClass.fileName() + _configClass.fileExtension());
		if (Files.notExists(configPath))
		{
			LOGGER.warn("Config File {} doesn't exist! Generating ...", configPath);
			
			generate();
		}
		
		final ConfigProperties overriddenProperties = classLoadingContext.getOverriddenProperties();
		final Boolean isReloading = classLoadingContext.isReloading();
		Objects.requireNonNull(overriddenProperties, "Overridden properties is null!");
		Objects.requireNonNull(isReloading, "isReloading boolean is null in the loading context!");
		
		final ConfigFieldLoadingContext fieldLoadingContext = new ConfigFieldLoadingContext();
		fieldLoadingContext.setConfigPath(configPath);
		fieldLoadingContext.setProperties(ConfigProperties.of(new ConfigProperties(configPath), overriddenProperties));
		fieldLoadingContext.setReloading(isReloading);
		for (ConfigFieldInfo configFieldInfo : _fieldInfoClasses)
		{
			configFieldInfo.load(fieldLoadingContext);
		}
		
		ConfigComponents.get(_configClass.postLoadHook()).load();
		
		LOGGER.info("loaded '{}'", configPath);
	}
	
	/**
	 * Prints the necessary class information into a {@link StringBuilder}.
	 * @param out the {@link StringBuilder} that receives the output
	 */
	public void print(StringBuilder out)
	{
		// Header.
		out.append("################################################################################\r\n");
		out.append("## ").append(_configClass.fileName().replace("_", " ")).append(" Settings").append(System.lineSeparator());
		out.append("################################################################################\r\n");
		
		out.append(System.lineSeparator()); // separator
		
		// File comment if exists.
		if ((_configClass.comment() != null) && (_configClass.comment().length > 0))
		{
			for (String line : _configClass.comment())
			{
				out.append("# ").append(line).append(System.lineSeparator());
			}
			out.append(System.lineSeparator());
		}
		
		_fieldInfoClasses.forEach(configFieldInfo -> configFieldInfo.print(out));
	}
	
	/**
	 * Generates a properties file based on the annotation input from the configuration class and its fields.
	 * @throws IOException
	 */
	public void generate() throws IOException
	{
		final StringBuilder out = new StringBuilder();
		
		print(out);
		
		final Path configPath = Paths.get("", _configClass.pathNames()).resolve(_configClass.fileName() + _configClass.fileExtension());
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
	
	@Override
	public int hashCode()
	{
		final int prime = 31;
		int result = 1;
		result = (prime * result) + ((_clazz == null) ? 0 : _clazz.hashCode());
		return result;
	}
	
	@Override
	public boolean equals(Object obj)
	{
		if (this == obj)
		{
			return true;
		}
		if (obj == null)
		{
			return false;
		}
		if (!(obj instanceof ConfigClassInfo))
		{
			return false;
		}
		ConfigClassInfo other = (ConfigClassInfo) obj;
		if (_clazz == null)
		{
			if (other._clazz != null)
			{
				return false;
			}
		}
		else if (!_clazz.equals(other._clazz))
		{
			return false;
		}
		return true;
	}
}
