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

import java.io.IOException;
import java.lang.reflect.Field;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.github.lordrex34.config.ConfigManager;
import com.github.lordrex34.config.annotation.ConfigClass;
import com.github.lordrex34.config.generator.AbstractConfigGenerator;
import com.github.lordrex34.config.postloadhooks.ConfigPostLoadHook;
import com.github.lordrex34.config.postloadhooks.EmptyConfigPostLoadHook;
import com.github.lordrex34.config.util.PropertiesParser;

/**
 * @author NB4L1 (original concept)
 * @author lord_rex
 */
public class ConfigClassInfo
{
	private static final Logger LOGGER = LoggerFactory.getLogger(ConfigClassInfo.class);
	private final Class<?> _clazz;
	private final ConfigClass _configClass;
	
	/**
	 * Constructs a new info holder class.
	 * @param clazz the configuration class itself
	 */
	public ConfigClassInfo(Class<?> clazz)
	{
		_clazz = clazz;
		_configClass = _clazz.getDeclaredAnnotation(ConfigClass.class);
	}
	
	/**
	 * Loads a configuration class.
	 */
	public void load()
	{
		final PropertiesParser overridenProperties = ConfigManager.getInstance().getOverriddenProperties();
		if (overridenProperties == null)
		{
			throw new NullPointerException("Override properties is missing!");
		}
		
		if (_configClass == null)
		{
			LOGGER.warn("Class {} doesn't have @ConfigClass annotation!", _clazz);
			return;
		}
		
		final Path configPath = Paths.get("", _configClass.pathNames()).resolve(_configClass.fileName() + _configClass.fileExtension());
		if (Files.notExists(configPath))
		{
			LOGGER.warn("Config File {} doesn't exist! Generating ...", configPath);
			
			try
			{
				AbstractConfigGenerator.printConfigClass(_clazz);
			}
			catch (IOException e)
			{
				LOGGER.warn("Failed to generate config!", e);
			}
		}
		
		final PropertiesParser properties = new PropertiesParser(configPath);
		for (Field field : _clazz.getDeclaredFields())
		{
			final ConfigFieldInfo configFieldInfo = new ConfigFieldInfo(configPath, _clazz, field, properties, overridenProperties);
			configFieldInfo.load();
		}
		
		try
		{
			// post load hook event for class
			final ConfigPostLoadHook postLoadHook = _configClass.postLoadHook().newInstance();
			if ((postLoadHook != null) && !(postLoadHook instanceof EmptyConfigPostLoadHook))
			{
				postLoadHook.load(properties, overridenProperties);
			}
		}
		catch (InstantiationException | IllegalAccessException e)
		{
			LOGGER.warn("Failed to load post load hook!", e);
		}
		
		LOGGER.debug("loaded '{}'", configPath);
	}
}
