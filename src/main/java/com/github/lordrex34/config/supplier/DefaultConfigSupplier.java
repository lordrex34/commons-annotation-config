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

import com.github.lordrex34.config.ConfigManager;
import com.github.lordrex34.config.annotation.ConfigField;
import com.github.lordrex34.config.converter.ConfigConverters;
import com.github.lordrex34.config.converter.IConfigConverter;
import com.github.lordrex34.config.lang.FieldParser.FieldParserException;
import com.github.lordrex34.config.util.PropertiesParser;

/**
 * This is the configuration value supplier used by {@link ConfigField} annotation by default.
 * @author lord_rex
 */
public class DefaultConfigSupplier implements IConfigValueSupplier<Object>
{
	@Override
	public Object supply(Field field, ConfigField configField, PropertiesParser properties, PropertiesParser overridenProperties)
	{
		final String propertyKey = configField.name();
		final String propertyValue = configField.value();
		
		final String configProperty = ConfigManager.getProperty(properties, overridenProperties, propertyKey, propertyValue);
		final IConfigConverter converter = ConfigConverters.get(configField.converter());
		
		Object value;
		try
		{
			value = converter.convertFromString(field, field.getType(), configProperty);
		}
		catch (FieldParserException e)
		{
			throw new FieldParserException("Property '" + propertyKey + "' has incorrect syntax! Please check!");
		}
		
		return value;
	}
}
