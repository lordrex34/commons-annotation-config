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

import java.lang.reflect.Field;
import java.lang.reflect.Modifier;
import java.nio.file.Path;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.github.lordrex34.config.ConfigManager;
import com.github.lordrex34.config.annotation.ConfigField;
import com.github.lordrex34.config.postloadhooks.ConfigPostLoadHook;
import com.github.lordrex34.config.postloadhooks.EmptyConfigPostLoadHook;
import com.github.lordrex34.config.util.PropertiesParser;

/**
 * @author NB4L1 (original concept)
 * @author lord_rex
 */
public class ConfigFieldInfo
{
	private static final Logger LOGGER = LoggerFactory.getLogger(ConfigFieldInfo.class);
	
	private final Path _configPath;
	private final Class<?> _clazz;
	private final Field _field;
	private final PropertiesParser _properties;
	private final PropertiesParser _overriddenProperties;
	private final ConfigField _configField;
	
	public ConfigFieldInfo(Path configPath, Class<?> clazz, Field field, PropertiesParser properties, PropertiesParser overriddenProperties)
	{
		_configPath = configPath;
		_clazz = clazz;
		_field = field;
		_properties = properties;
		_overriddenProperties = overriddenProperties;
		_configField = _field.getDeclaredAnnotation(ConfigField.class);
	}
	
	public void load()
	{
		// Safety check.
		if (_field == null)
		{
			return;
		}
		
		// Skip inappropriate fields.
		if (!Modifier.isStatic(_field.getModifiers()) || Modifier.isFinal(_field.getModifiers()))
		{
			LOGGER.debug("Skipping non static or final field: {}#{}", _clazz.getSimpleName(), _field.getName());
			return;
		}
		
		// Get the annotation.
		if (_configField == null)
		{
			return;
		}
		
		// If field is just a comment holder, then do not try to load it.
		if (_configField.onlyComment())
		{
			return;
		}
		
		try
		{
			final String propertyKey = _configField.name();
			ConfigManager.getInstance().registerProperty(_clazz.getPackage().getName(), _configPath, propertyKey);
			if (!_configField.reloadable() && ConfigManager.getInstance().isReloading())
			{
				LOGGER.debug("Property '{}' retained with its previous value!", propertyKey);
				return;
			}
			
			final boolean wasAccessible = _field.isAccessible();
			if (!wasAccessible)
			{
				_field.setAccessible(true);
			}
			_field.set(null, _configField.valueSupplier().newInstance().supply(_field, _configField, _properties, _overriddenProperties));
			_field.setAccessible(wasAccessible);
			
			// post load hook event for field
			final ConfigPostLoadHook postLoadHook = _configField.postLoadHook().newInstance();
			if ((postLoadHook != null) && !(postLoadHook instanceof EmptyConfigPostLoadHook))
			{
				postLoadHook.load(_properties, _overriddenProperties);
			}
		}
		catch (InstantiationException | IllegalAccessException e)
		{
			LOGGER.warn("Failed to set field!", e);
		}
	}
}
