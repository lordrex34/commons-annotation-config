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
package com.github.lordrex34.config.util;

import java.io.File;
import java.io.InputStream;
import java.lang.reflect.Array;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.Duration;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map.Entry;
import java.util.Properties;
import java.util.Set;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Simplifies loading of property files and adds logging if a non-existing property is requested.
 * @author NosBit
 */
public final class PropertiesParser
{
	private static final Logger LOGGER = LoggerFactory.getLogger(PropertiesParser.class);
	
	private final Properties _properties = new Properties();
	private final Path _path;
	
	public PropertiesParser(String name)
	{
		this(Paths.get(name));
	}
	
	public PropertiesParser(Path path)
	{
		_path = path;
		
		try (InputStream is = Files.newInputStream(path))
		{
			_properties.load(is);
		}
		catch (Exception e)
		{
			LOGGER.warn("[{}] There was an error loading config", _path, e);
		}
	}
	
	public PropertiesParser(File file)
	{
		this(file.toPath());
	}
	
	public boolean containsKey(String key)
	{
		return _properties.containsKey(key);
	}
	
	public Set<Entry<Object, Object>> entrySet()
	{
		return _properties.entrySet();
	}
	
	public String getValue(String key)
	{
		String value = _properties.getProperty(key);
		return value != null ? value.trim() : null;
	}
	
	public boolean getBoolean(String key, boolean defaultValue)
	{
		String value = getValue(key);
		if (value == null)
		{
			LOGGER.warn("[{}] missing property for key: {} using default value: {}", _path, key, defaultValue);
			return defaultValue;
		}
		
		if (value.equalsIgnoreCase("true"))
		{
			return true;
		}
		else if (value.equalsIgnoreCase("false"))
		{
			return false;
		}
		else
		{
			LOGGER.warn("[{}] Invalid value specified for key: {} specified value: {} should be \"boolean\" using default value: {}", _path, key, value, defaultValue);
			return defaultValue;
		}
	}
	
	public byte getByte(String key, byte defaultValue)
	{
		String value = getValue(key);
		if (value == null)
		{
			LOGGER.warn("[{}] missing property for key: {} using default value: {}", _path, key, defaultValue);
			return defaultValue;
		}
		
		try
		{
			return Byte.parseByte(value);
		}
		catch (NumberFormatException e)
		{
			LOGGER.warn("[{}] Invalid value specified for key: {} specified value: {} should be \"byte\" using default value: {}", _path, key, value, defaultValue);
			return defaultValue;
		}
	}
	
	public short getShort(String key, short defaultValue)
	{
		String value = getValue(key);
		if (value == null)
		{
			LOGGER.warn("[{}] missing property for key: {} using default value: {}", _path, key, defaultValue);
			return defaultValue;
		}
		
		try
		{
			return Short.parseShort(value);
		}
		catch (NumberFormatException e)
		{
			LOGGER.warn("[{}] Invalid value specified for key: {} specified value: {} should be \"short\" using default value: {}", _path, key, value, defaultValue);
			return defaultValue;
		}
	}
	
	public int getInt(String key, int defaultValue)
	{
		String value = getValue(key);
		if (value == null)
		{
			LOGGER.warn("[{}] missing property for key: {} using default value: {}", _path, key, defaultValue);
			return defaultValue;
		}
		
		try
		{
			return Integer.parseInt(value);
		}
		catch (NumberFormatException e)
		{
			LOGGER.warn("[{}] Invalid value specified for key: {} specified value: {} should be \"int\" using default value: {}", _path, key, value, defaultValue);
			return defaultValue;
		}
	}
	
	public long getLong(String key, long defaultValue)
	{
		String value = getValue(key);
		if (value == null)
		{
			LOGGER.warn("[{}] missing property for key: {} using default value: {}", _path, key, defaultValue);
			return defaultValue;
		}
		
		try
		{
			return Long.parseLong(value);
		}
		catch (NumberFormatException e)
		{
			LOGGER.warn("[{}] Invalid value specified for key: {} specified value: {} should be \"long\" using default value: {}", _path, key, value, defaultValue);
			return defaultValue;
		}
	}
	
	public float getFloat(String key, float defaultValue)
	{
		String value = getValue(key);
		if (value == null)
		{
			LOGGER.warn("[{}] missing property for key: {} using default value: {}", _path, key, defaultValue);
			return defaultValue;
		}
		
		try
		{
			return Float.parseFloat(value);
		}
		catch (NumberFormatException e)
		{
			LOGGER.warn("[{}] Invalid value specified for key: {} specified value: {} should be \"float\" using default value: {}", _path, key, value, defaultValue);
			return defaultValue;
		}
	}
	
	public double getDouble(String key, double defaultValue)
	{
		String value = getValue(key);
		if (value == null)
		{
			LOGGER.warn("[{}] missing property for key: {} using default value: {}", _path, key, defaultValue);
			return defaultValue;
		}
		
		try
		{
			return Double.parseDouble(value);
		}
		catch (NumberFormatException e)
		{
			LOGGER.warn("[{}] Invalid value specified for key: {} specified value: {} should be \"double\" using default value: {}", _path, key, value, defaultValue);
			return defaultValue;
		}
	}
	
	public String getString(String key, String defaultValue)
	{
		String value = getValue(key);
		if (value == null)
		{
			LOGGER.warn("[{}] missing property for key: {} using default value: {}", _path, key, defaultValue);
			return defaultValue;
		}
		return value;
	}
	
	public <T extends Enum<T>> T getEnum(String key, Class<T> clazz, T defaultValue)
	{
		String value = getValue(key);
		if (value == null)
		{
			LOGGER.warn("[{}] missing property for key: {} using default value: {}", _path, key, defaultValue);
			return defaultValue;
		}
		
		try
		{
			return Enum.valueOf(clazz, value);
		}
		catch (IllegalArgumentException e)
		{
			LOGGER.warn("[{}] Invalid value specified for key: {} specified value: {} should be enum value of \"{}\" using default value: {}", _path, key, value, clazz.getSimpleName(), defaultValue);
			return defaultValue;
		}
	}
	
	/**
	 * @param durationPattern
	 * @param defaultValue
	 * @return {@link Duration} object by the durationPattern specified, {@code null} in case of malformed pattern.
	 */
	public Duration getDuration(String durationPattern, String defaultValue)
	{
		return getDuration(durationPattern, defaultValue, null);
	}
	
	/**
	 * @param durationPattern
	 * @param defaultValue
	 * @param defaultDuration
	 * @return {@link Duration} object by the durationPattern specified, the defaultDuration in case of malformed pattern.
	 */
	public Duration getDuration(String durationPattern, String defaultValue, Duration defaultDuration)
	{
		final String value = getString(durationPattern, defaultValue);
		try
		{
			return TimeUtil.parseDuration(value);
		}
		catch (IllegalStateException e)
		{
			LOGGER.warn("[{}] Invalid value specified for key: {} specified value: {] should be time patttern using default value: {}", _path, durationPattern, value, defaultValue);
		}
		return defaultDuration;
	}
	
	/**
	 * @param key
	 * @param separator
	 * @param defaultValues
	 * @return int array
	 */
	public int[] getIntArray(String key, String separator, int... defaultValues)
	{
		final String value = getValue(key);
		if (value == null)
		{
			LOGGER.warn("[{}] missing property for key: {} using default value: {}", _path, key, defaultValues);
			return defaultValues;
		}
		
		try
		{
			final String[] data = value.trim().split(separator);
			int[] result = new int[data.length];
			for (int i = 0; i < data.length; i++)
			{
				result[i] = Integer.decode(data[i].trim());
			}
			return result;
		}
		catch (Exception e)
		{
			LOGGER.warn("[{}] Invalid value specified for key: {} specified value: {} should be array using default value: {}", _path, key, value, defaultValues);
			return defaultValues;
		}
	}
	
	/**
	 * @param <T>
	 * @param key
	 * @param separator
	 * @param clazz
	 * @param defaultValues
	 * @return enum array
	 */
	@SafeVarargs
	public final <T extends Enum<T>> T[] getEnumArray(String key, String separator, Class<T> clazz, T... defaultValues)
	{
		final String value = getValue(key);
		if (value == null)
		{
			LOGGER.warn("[{}] missing property for key: {} using default value: {}", _path, key, defaultValues);
			return defaultValues;
		}
		
		try
		{
			final String[] data = value.trim().split(separator);
			@SuppressWarnings("unchecked")
			final T[] result = (T[]) Array.newInstance(clazz, data.length);
			for (int i = 0; i < data.length; i++)
			{
				result[i] = Enum.valueOf(clazz, data[i]);
			}
			return result;
		}
		catch (Exception e)
		{
			LOGGER.warn("[{}] Invalid value specified for key: {} specified value: {} should be array using default value: {}", _path, key, value, defaultValues);
			return defaultValues;
		}
	}
	
	/**
	 * @param <T>
	 * @param key
	 * @param separator
	 * @param clazz
	 * @param defaultValues
	 * @return list
	 */
	@SafeVarargs
	public final <T extends Enum<T>> List<T> getEnumList(String key, String separator, Class<T> clazz, T... defaultValues)
	{
		String value = getValue(key);
		if (value == null)
		{
			LOGGER.warn("[{}] missing property for key: {} using default value: {}", _path, key, defaultValues);
			return Arrays.asList(defaultValues);
		}
		
		try
		{
			final String[] data = value.trim().split(separator);
			final List<T> result = new ArrayList<>(data.length);
			for (String element : data)
			{
				result.add(Enum.valueOf(clazz, element));
			}
			return result;
		}
		catch (Exception e)
		{
			LOGGER.warn("[{}] Invalid value specified for key: {} specified value: {} should be array using default value: {}", _path, key, value, defaultValues);
			return Arrays.asList(defaultValues);
		}
	}
}
