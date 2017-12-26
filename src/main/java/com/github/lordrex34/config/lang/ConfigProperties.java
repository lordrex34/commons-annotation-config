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
package com.github.lordrex34.config.lang;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.Reader;
import java.io.Serializable;
import java.lang.reflect.Array;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.Duration;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Properties;
import java.util.Set;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.w3c.dom.NamedNodeMap;
import org.w3c.dom.Node;

import com.github.lordrex34.config.util.TimeUtil;

/**
 * A simple properties loader designed for <b>{@link String} based</b> key-value based property loading.
 * @author Noctarius (original idea, basic concept)
 * @author NB4L1 (re-designed concept)
 * @author savormix (re-designed java 8 concept)
 * @author NosBit (the getters)
 */
public final class ConfigProperties implements Serializable
{
	private static final long serialVersionUID = -6418707730244047405L;
	
	private static final Logger LOGGER = LoggerFactory.getLogger(ConfigProperties.class);
	
	public static final ConfigProperties EMPTY = new ConfigProperties(Collections.emptyMap());
	
	private final Map<String, String> _map;
	private String _loggingPrefix = getClass().getSimpleName();
	
	// ===================================================================================
	// Default constructors
	
	public ConfigProperties(Map<String, String> map)
	{
		_map = map;
	}
	
	public ConfigProperties()
	{
		this(new HashMap<>());
	}
	
	// ===================================================================================
	// Special Constructors
	
	public ConfigProperties(File file) throws IOException
	{
		this();
		_loggingPrefix = file.toString();
		load(new FileInputStream(file));
	}
	
	public ConfigProperties(Path path) throws IOException
	{
		this();
		_loggingPrefix = path.toString();
		load(Files.newBufferedReader(path));
	}
	
	public ConfigProperties(String name) throws IOException
	{
		this(Paths.get(name));
		_loggingPrefix = name;
	}
	
	public ConfigProperties(InputStream inStream) throws IOException
	{
		this();
		load(inStream);
	}
	
	public ConfigProperties(Reader reader) throws IOException
	{
		this();
		load(reader);
	}
	
	public ConfigProperties(Node node)
	{
		this();
		load(node);
	}
	
	public ConfigProperties(Properties properties)
	{
		this();
		load(properties);
	}
	
	public ConfigProperties(ConfigProperties properties)
	{
		this();
		load(properties);
	}
	
	// ===================================================================================
	// Loaders
	
	public void load(String name) throws IOException
	{
		load(new FileInputStream(name));
	}
	
	public void load(File file) throws IOException
	{
		load(new FileInputStream(file));
	}
	
	public void load(InputStream inStream) throws IOException
	{
		final Properties prop = new Properties();
		
		try (InputStream in = inStream)
		{
			prop.load(in);
		}
		
		load(prop);
	}
	
	public void load(Reader reader) throws IOException
	{
		final Properties prop = new Properties();
		
		try (Reader in = reader)
		{
			prop.load(in);
		}
		
		load(prop);
	}
	
	public void load(Node node)
	{
		final NamedNodeMap attrs = node.getAttributes();
		
		for (int i = 0; i < attrs.getLength(); i++)
		{
			final Node attr = attrs.item(i);
			
			setProperty(attr.getNodeName(), attr.getNodeValue());
		}
	}
	
	public void load(Properties properties)
	{
		for (Map.Entry<Object, Object> entry : properties.entrySet())
		{
			setProperty(entry.getKey(), entry.getValue());
		}
	}
	
	public void load(ConfigProperties properties)
	{
		for (Map.Entry<String, String> entry : properties.entrySet())
		{
			setProperty(entry.getKey(), entry.getValue());
		}
	}
	
	// ===================================================================================
	// Wrapped map functions
	
	public String setProperty(String key, String value)
	{
		return _map.put(key, value);
	}
	
	public String setProperty(Object key, Object value)
	{
		return _map.put(String.valueOf(key), String.valueOf(value));
	}
	
	public void clear()
	{
		_map.clear();
	}
	
	public int size()
	{
		return _map.size();
	}
	
	public boolean containsKey(String key)
	{
		return _map.containsKey(key);
	}
	
	public Set<Entry<String, String>> entrySet()
	{
		return Collections.unmodifiableSet(_map.entrySet());
	}
	
	public String getProperty(String key)
	{
		final String value = _map.get(key);
		return value != null ? value.trim() : null;
	}
	
	// ===================================================================================
	// Parsers
	
	public String getProperty(String key, String defaultValue)
	{
		String value = getProperty(key);
		if (value == null)
		{
			LOGGER.warn("[{}] missing property for key: {} using default value: {}", _loggingPrefix, key, defaultValue);
			return defaultValue;
		}
		return value;
	}
	
	public boolean getBoolean(String key, boolean defaultValue)
	{
		String value = getProperty(key);
		if (value == null)
		{
			LOGGER.warn("[{}] missing property for key: {} using default value: {}", _loggingPrefix, key, defaultValue);
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
			LOGGER.warn("[{}] Invalid value specified for key: {} specified value: {} should be \"boolean\" using default value: {}", _loggingPrefix, key, value, defaultValue);
			return defaultValue;
		}
	}
	
	public byte getByte(String key, byte defaultValue)
	{
		String value = getProperty(key);
		if (value == null)
		{
			LOGGER.warn("[{}] missing property for key: {} using default value: {}", _loggingPrefix, key, defaultValue);
			return defaultValue;
		}
		
		try
		{
			return Byte.parseByte(value);
		}
		catch (NumberFormatException e)
		{
			LOGGER.warn("[{}] Invalid value specified for key: {} specified value: {} should be \"byte\" using default value: {}", _loggingPrefix, key, value, defaultValue);
			return defaultValue;
		}
	}
	
	public short getShort(String key, short defaultValue)
	{
		String value = getProperty(key);
		if (value == null)
		{
			LOGGER.warn("[{}] missing property for key: {} using default value: {}", _loggingPrefix, key, defaultValue);
			return defaultValue;
		}
		
		try
		{
			return Short.parseShort(value);
		}
		catch (NumberFormatException e)
		{
			LOGGER.warn("[{}] Invalid value specified for key: {} specified value: {} should be \"short\" using default value: {}", _loggingPrefix, key, value, defaultValue);
			return defaultValue;
		}
	}
	
	public int getInt(String key, int defaultValue)
	{
		String value = getProperty(key);
		if (value == null)
		{
			LOGGER.warn("[{}] missing property for key: {} using default value: {}", _loggingPrefix, key, defaultValue);
			return defaultValue;
		}
		
		try
		{
			return Integer.parseInt(value);
		}
		catch (NumberFormatException e)
		{
			LOGGER.warn("[{}] Invalid value specified for key: {} specified value: {} should be \"int\" using default value: {}", _loggingPrefix, key, value, defaultValue);
			return defaultValue;
		}
	}
	
	public long getLong(String key, long defaultValue)
	{
		String value = getProperty(key);
		if (value == null)
		{
			LOGGER.warn("[{}] missing property for key: {} using default value: {}", _loggingPrefix, key, defaultValue);
			return defaultValue;
		}
		
		try
		{
			return Long.parseLong(value);
		}
		catch (NumberFormatException e)
		{
			LOGGER.warn("[{}] Invalid value specified for key: {} specified value: {} should be \"long\" using default value: {}", _loggingPrefix, key, value, defaultValue);
			return defaultValue;
		}
	}
	
	public float getFloat(String key, float defaultValue)
	{
		String value = getProperty(key);
		if (value == null)
		{
			LOGGER.warn("[{}] missing property for key: {} using default value: {}", _loggingPrefix, key, defaultValue);
			return defaultValue;
		}
		
		try
		{
			return Float.parseFloat(value);
		}
		catch (NumberFormatException e)
		{
			LOGGER.warn("[{}] Invalid value specified for key: {} specified value: {} should be \"float\" using default value: {}", _loggingPrefix, key, value, defaultValue);
			return defaultValue;
		}
	}
	
	public double getDouble(String key, double defaultValue)
	{
		String value = getProperty(key);
		if (value == null)
		{
			LOGGER.warn("[{}] missing property for key: {} using default value: {}", _loggingPrefix, key, defaultValue);
			return defaultValue;
		}
		
		try
		{
			return Double.parseDouble(value);
		}
		catch (NumberFormatException e)
		{
			LOGGER.warn("[{}] Invalid value specified for key: {} specified value: {} should be \"double\" using default value: {}", _loggingPrefix, key, value, defaultValue);
			return defaultValue;
		}
	}
	
	public String getString(String key, String defaultValue)
	{
		return getProperty(key, defaultValue);
	}
	
	public <T extends Enum<T>> T getEnum(String key, Class<T> clazz, T defaultValue)
	{
		String value = getProperty(key);
		if (value == null)
		{
			LOGGER.warn("[{}] missing property for key: {} using default value: {}", _loggingPrefix, key, defaultValue);
			return defaultValue;
		}
		
		try
		{
			return Enum.valueOf(clazz, value);
		}
		catch (IllegalArgumentException e)
		{
			LOGGER.warn("[{}] Invalid value specified for key: {} specified value: {} should be enum value of \"{}\" using default value: {}", _loggingPrefix, key, value, clazz.getSimpleName(), defaultValue);
			return defaultValue;
		}
	}
	
	public Duration getDuration(String durationPattern, String defaultValue)
	{
		return getDuration(durationPattern, defaultValue, null);
	}
	
	public Duration getDuration(String durationPattern, String defaultValue, Duration defaultDuration)
	{
		final String value = getString(durationPattern, defaultValue);
		try
		{
			return TimeUtil.parseDuration(value);
		}
		catch (IllegalStateException e)
		{
			LOGGER.warn("[{}] Invalid value specified for key: {} specified value: {] should be time patttern using default value: {}", _loggingPrefix, durationPattern, value, defaultValue);
		}
		return defaultDuration;
	}
	
	public int[] getIntArray(String key, String separator, int... defaultValues)
	{
		final String value = getProperty(key);
		if (value == null)
		{
			LOGGER.warn("[{}] missing property for key: {} using default value: {}", _loggingPrefix, key, defaultValues);
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
			LOGGER.warn("[{}] Invalid value specified for key: {} specified value: {} should be array using default value: {}", _loggingPrefix, key, value, defaultValues);
			return defaultValues;
		}
	}
	
	@SafeVarargs
	public final <T extends Enum<T>> T[] getEnumArray(String key, String separator, Class<T> clazz, T... defaultValues)
	{
		final String value = getProperty(key);
		if (value == null)
		{
			LOGGER.warn("[{}] missing property for key: {} using default value: {}", _loggingPrefix, key, defaultValues);
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
			LOGGER.warn("[{}] Invalid value specified for key: {} specified value: {} should be array using default value: {}", _loggingPrefix, key, value, defaultValues);
			return defaultValues;
		}
	}
	
	@SafeVarargs
	public final <T extends Enum<T>> List<T> getEnumList(String key, String separator, Class<T> clazz, T... defaultValues)
	{
		String value = getProperty(key);
		if (value == null)
		{
			LOGGER.warn("[{}] missing property for key: {} using default value: {}", _loggingPrefix, key, defaultValues);
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
			LOGGER.warn("[{}] Invalid value specified for key: {} specified value: {} should be array using default value: {}", _loggingPrefix, key, value, defaultValues);
			return Arrays.asList(defaultValues);
		}
	}
}
