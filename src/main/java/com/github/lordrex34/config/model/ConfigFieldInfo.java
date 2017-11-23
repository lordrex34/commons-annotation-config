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
import java.util.Arrays;
import java.util.stream.Collectors;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.github.lordrex34.config.ConfigManager;
import com.github.lordrex34.config.annotation.ConfigField;
import com.github.lordrex34.config.annotation.ConfigGroupBeginning;
import com.github.lordrex34.config.annotation.ConfigGroupEnding;
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
	
	private final Class<?> _clazz;
	private final Field _field;
	private final ConfigField _configField;
	
	public ConfigFieldInfo(Class<?> clazz, Field field)
	{
		_clazz = clazz;
		_field = field;
		_configField = _field.getDeclaredAnnotation(ConfigField.class);
	}
	
	public void load(Path configPath, PropertiesParser properties, PropertiesParser overriddenProperties)
	{
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
			ConfigManager.getInstance().registerProperty(_clazz.getPackage().getName(), configPath, propertyKey);
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
			_field.set(null, _configField.valueSupplier().newInstance().supply(_field, _configField, properties, overriddenProperties));
			_field.setAccessible(wasAccessible);
			
			// post load hook event for field
			final ConfigPostLoadHook postLoadHook = _configField.postLoadHook().newInstance();
			if ((postLoadHook != null) && !(postLoadHook instanceof EmptyConfigPostLoadHook))
			{
				postLoadHook.load(properties, overriddenProperties);
			}
		}
		catch (InstantiationException | IllegalAccessException e)
		{
			LOGGER.warn("Failed to set field!", e);
		}
	}
	
	/**
	 * Generates the required information of the filed into the properties file.
	 * @param out the string builder used for the generation
	 */
	public void print(StringBuilder out)
	{
		if ((_field == null) || (_configField == null))
		{
			return;
		}
		
		final ConfigGroupBeginning beginningGroup = _field.getDeclaredAnnotation(ConfigGroupBeginning.class);
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
		
		for (String line : _configField.comment())
		{
			out.append("# ").append(line).append(System.lineSeparator());
		}
		
		if (!_configField.onlyComment())
		{
			out.append("# Default: ").append(_configField.value()).append(System.lineSeparator());
			if (_field.getType().isEnum())
			{
				out.append("# Available: ").append(Arrays.stream(_field.getType().getEnumConstants()).map(String::valueOf).collect(Collectors.joining("|"))).append(System.lineSeparator());
			}
			else if (_field.getType().isArray())
			{
				final Class<?> fieldComponentType = _field.getType().getComponentType();
				if (fieldComponentType.isEnum())
				{
					out.append("# Available: ").append(Arrays.stream(_field.getType().getEnumConstants()).map(String::valueOf).collect(Collectors.joining(","))).append(System.lineSeparator());
				}
			}
			out.append(_configField.name()).append(" = ").append(_configField.value()).append(System.lineSeparator());
			out.append(System.lineSeparator());
		}
		
		final ConfigGroupEnding endingGroup = _field.getDeclaredAnnotation(ConfigGroupEnding.class);
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
