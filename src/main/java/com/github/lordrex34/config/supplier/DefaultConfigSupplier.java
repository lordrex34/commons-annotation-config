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
import java.util.HashMap;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.github.lordrex34.config.ConfigManager;
import com.github.lordrex34.config.annotation.ConfigField;
import com.github.lordrex34.config.converter.IConfigConverter;
import com.github.lordrex34.config.lang.FieldParser.FieldParserException;
import com.github.lordrex34.config.util.PropertiesParser;

/**
 * This is the configuration value supplier used by {@link ConfigField} annotation by default.
 * @author lord_rex
 */
public class DefaultConfigSupplier implements IConfigValueSupplier<Object>
{
	private static final Logger LOGGER = LoggerFactory.getLogger(DefaultConfigSupplier.class);
	
	/** To avoid creating the same converter thousand times. */
	private static final Map<String, IConfigConverter> CONVERTERS = new HashMap<>();
	
	@Override
	public Object supply(Field field, ConfigField configField, PropertiesParser properties, PropertiesParser overridenProperties) throws InstantiationException, IllegalAccessException
	{
		final String propertyKey = configField.name();
		final String propertyValue = configField.value();
		
		final String configProperty = ConfigManager.getProperty(properties, overridenProperties, propertyKey, propertyValue);
		
		final Class<? extends IConfigConverter> converterClass = configField.converter();
		final String converterClassName = converterClass.getName();
		IConfigConverter converter = CONVERTERS.get(converterClassName);
		if (converter == null)
		{
			converter = converterClass.newInstance();
			CONVERTERS.put(converterClassName, converter);
		}
		
		Object value;
		try
		{
			value = converter.convertFromString(field, field.getType(), configProperty);
		}
		catch (FieldParserException e)
		{
			value = converter.convertFromString(field, field.getType(), propertyValue);
			LOGGER.warn("Property '{}' has incorrect syntax! Using default value instead: {}", propertyKey, propertyValue);
		}
		
		return value;
	}
}
