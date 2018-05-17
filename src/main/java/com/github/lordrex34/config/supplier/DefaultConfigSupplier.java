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
package com.github.lordrex34.config.supplier;

import java.lang.reflect.Field;
import java.util.Collection;

import com.github.lordrex34.config.annotation.ConfigField;
import com.github.lordrex34.config.component.ConfigComponents;
import com.github.lordrex34.config.converter.IConfigConverter;
import com.github.lordrex34.config.lang.ConfigProperties;
import com.github.lordrex34.config.lang.FieldParser.FieldParserException;

/**
 * This is the configuration value supplier used by {@link ConfigField} annotation by default.
 * @author lord_rex
 */
public class DefaultConfigSupplier implements IConfigValueSupplier<Object>
{
	@Override
	public Object supply(Class<?> clazz, Field field, ConfigField configField, ConfigProperties properties, boolean generating)
	{
		final String propertyKey = configField.name();
		final String propertyValue = configField.value();
		
		final String configProperty = getProperty(clazz, field, propertyKey, propertyValue, properties);
		final IConfigConverter converter = ConfigComponents.get(configField.converter());
		
		try
		{
			if (generating && (field.getType().isArray() || Collection.class.isAssignableFrom(field.getType())))
			{
				return propertyValue;
			}
			return converter.convertFromString(field, field.getType(), configProperty);
		}
		catch (FieldParserException e)
		{
			throw new FieldParserException("Property '" + propertyKey + "' has incorrect syntax! Please check!");
		}
	}
	
	/**
	 * This method provides property value
	 * <ul>
	 * <li>using environment variable with syntax: <code>(clazz.getSimpleName() + "_" + field.getName()).toUpperCase()</code></li>
	 * <li>using system variable with syntax: <code>(clazz.getSimpleName() + "." + field.getName()</code></li>
	 * <li>"override.properties"</li>
	 * <li>&lt;config name&gt;.properties</li>
	 * </ul>
	 * @param clazz
	 * @param field
	 * @param propertyKey
	 * @param propertyValue
	 * @param properties
	 * @return the value either environment variable, system property or value specified by the properties files
	 */
	private String getProperty(Class<?> clazz, Field field, String propertyKey, String propertyValue, ConfigProperties properties)
	{
		String configProperty = System.getenv((clazz.getSimpleName() + "_" + field.getName()).toUpperCase());
		if (configProperty == null)
		{
			configProperty = System.getProperty(clazz.getSimpleName() + "." + field.getName());
			if (configProperty == null)
			{
				configProperty = properties.getProperty(propertyKey, propertyValue);
			}
		}
		return configProperty;
	}
}
