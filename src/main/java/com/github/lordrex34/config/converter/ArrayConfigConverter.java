/*
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
package com.github.lordrex34.config.converter;

import java.lang.reflect.Array;
import java.lang.reflect.Field;
import java.util.Arrays;
import java.util.regex.Pattern;

public class ArrayConfigConverter implements IConfigConverter
{
	@Override
	public Object convertFromString(Field field, Class<?> type, String value)
	{
		final Class<?> componentType = type.getComponentType();
		
		if (value.isEmpty())
		{
			return Array.newInstance(componentType, 0);
		}
		
		final String[] splitted = Pattern.compile(getElementDelimiter(), Pattern.LITERAL).split(value);
		final Object array = Array.newInstance(componentType, splitted.length);
		
		for (int i = 0; i < splitted.length; i++)
		{
			Array.set(array, i, getElementConverter().convertFromString(field, componentType, splitted[i]));
		}
		
		if (Comparable.class.isAssignableFrom(componentType))
		{
			Arrays.sort((Comparable[]) array);
		}
		
		return array;
	}
	
	@Override
	public String convertToString(Field field, Class<?> type, Object obj)
	{
		final Class<?> componentType = type.getComponentType();
		
		if (obj == null)
		{
			return "";
		}
		
		final int length = Array.getLength(obj);
		if (length < 1)
		{
			return "";
		}
		
		if (Comparable.class.isAssignableFrom(componentType))
		{
			Arrays.sort((Comparable[]) obj);
		}
		
		final StringBuilder sb = new StringBuilder();
		
		for (int i = 0; i < length; i++)
		{
			if (i > 0)
			{
				sb.append(getElementDelimiter());
			}
			
			sb.append(getElementConverter().convertToString(field, componentType, Array.get(obj, i)));
		}
		
		return sb.toString();
	}
	
	protected String getElementDelimiter()
	{
		return ",";
	}
	
	protected IConfigConverter getElementConverter()
	{
		return MainConfigConverter.getInstance();
	}
	
	private static final class SingletonHolder
	{
		static final ArrayConfigConverter INSTANCE = new ArrayConfigConverter();
	}
	
	public static ArrayConfigConverter getInstance()
	{
		return SingletonHolder.INSTANCE;
	}
}
