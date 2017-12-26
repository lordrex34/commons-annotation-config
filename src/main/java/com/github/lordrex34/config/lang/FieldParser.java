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

import java.lang.reflect.Array;

/**
 * This class's purpose is eventually to parse fields.
 * @author NB4L1
 */
public final class FieldParser
{
	@SuppressWarnings(
	{
		"unchecked",
		"rawtypes"
	})
	public static Object get(Class<?> type, String value)
	{
		if ((type == Boolean.class) || (type == Boolean.TYPE))
		{
			return FieldParser.getBoolean(value);
		}
		else if ((type == Long.class) || (type == Long.TYPE))
		{
			return FieldParser.getLong(value);
		}
		else if ((type == Integer.class) || (type == Integer.TYPE))
		{
			return FieldParser.getInteger(value);
		}
		else if ((type == Short.class) || (type == Short.TYPE))
		{
			return FieldParser.getShort(value);
		}
		else if ((type == Byte.class) || (type == Byte.TYPE))
		{
			return FieldParser.getByte(value);
		}
		else if ((type == Double.class) || (type == Double.TYPE))
		{
			return FieldParser.getDouble(value);
		}
		else if ((type == Float.class) || (type == Float.TYPE))
		{
			return FieldParser.getFloat(value);
		}
		else if (type == String.class)
		{
			return FieldParser.getString(value);
		}
		else if (type.isEnum())
		{
			return FieldParser.getEnum((Class<? extends Enum>) type, value);
		}
		else
		{
			throw new FieldParserException("Not covered type: " + type + "!");
		}
	}
	
	public static boolean getBoolean(String value)
	{
		if (value == null)
		{
			throw new FieldParserException(Boolean.class);
		}
		
		try
		{
			return Boolean.parseBoolean(value);
		}
		catch (RuntimeException e)
		{
			throw new FieldParserException(Boolean.class, value, e);
		}
	}
	
	public static byte getByte(String value)
	{
		if (value == null)
		{
			throw new FieldParserException(Byte.class);
		}
		
		try
		{
			return Byte.decode(value);
		}
		catch (RuntimeException e)
		{
			throw new FieldParserException(Byte.class, value, e);
		}
	}
	
	public static short getShort(String value)
	{
		if (value == null)
		{
			throw new FieldParserException(Short.class);
		}
		
		try
		{
			return Short.decode(value);
		}
		catch (RuntimeException e)
		{
			throw new FieldParserException(Short.class, value, e);
		}
	}
	
	public static int getInteger(String value)
	{
		if (value == null)
		{
			throw new FieldParserException(Integer.class);
		}
		
		try
		{
			return Integer.decode(value);
		}
		catch (RuntimeException e)
		{
			throw new FieldParserException(Integer.class, value, e);
		}
	}
	
	public static long getLong(String value)
	{
		if (value == null)
		{
			throw new FieldParserException(Long.class);
		}
		
		try
		{
			return Long.decode(value);
		}
		catch (RuntimeException e)
		{
			throw new FieldParserException(Long.class, value, e);
		}
	}
	
	public static float getFloat(String value)
	{
		if (value == null)
		{
			throw new FieldParserException(Float.class);
		}
		
		try
		{
			return Float.parseFloat(value);
		}
		catch (RuntimeException e)
		{
			throw new FieldParserException(Float.class, value, e);
		}
	}
	
	public static double getDouble(String value)
	{
		if (value == null)
		{
			throw new FieldParserException(Double.class);
		}
		
		try
		{
			return Double.parseDouble(value);
		}
		catch (RuntimeException e)
		{
			throw new FieldParserException(Double.class, value, e);
		}
	}
	
	public static String getString(String value)
	{
		if (value == null)
		{
			throw new FieldParserException(String.class);
		}
		
		return String.valueOf(value);
	}
	
	public static <T extends Enum<T>> T getEnum(Class<T> enumClass, String value)
	{
		if (value == null)
		{
			throw new FieldParserException(enumClass);
		}
		
		try
		{
			return Enum.valueOf(enumClass, value);
		}
		catch (RuntimeException e)
		{
			throw new FieldParserException(enumClass, value, e);
		}
	}
	
	public static Object getArray(Class<?> componentClass, String value, String regex)
	{
		final String[] values = value.split(regex);
		
		final Object array = Array.newInstance(componentClass, values.length);
		
		for (int i = 0; i < values.length; i++)
		{
			Array.set(array, i, get(componentClass, values[i]));
		}
		
		return array;
	}
	
	public static final class FieldParserException extends RuntimeException
	{
		private static final long serialVersionUID = 1839324679891385619L;
		
		public FieldParserException()
		{
			super();
		}
		
		public FieldParserException(String message)
		{
			super(message);
		}
		
		public FieldParserException(Class<?> requiredType)
		{
			super(requiredType + " value required, but not specified!");
		}
		
		public FieldParserException(Class<?> requiredType, String value, RuntimeException cause)
		{
			super(requiredType + " value required, but found: '" + value + "'!", cause);
		}
	}
}
