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

import java.lang.reflect.Field;
import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;

/**
 * @author lord_rex
 */
public final class GenericUtil
{
	private GenericUtil()
	{
		// utility class
	}
	
	public static Type[] getGenericTypes(final Field field)
	{
		final Type genType = field.getGenericType();
		if (!ParameterizedType.class.isInstance(genType))
		{
			return null;
		}
		
		final ParameterizedType pType = (ParameterizedType) genType;
		return pType.getActualTypeArguments();
	}
	
	public static Class<?> getFirstGenericTypeOfGenerizedField(final Field field)
	{
		final Type[] allGenTypes = getGenericTypes(field);
		if (allGenTypes == null)
		{
			return Object.class; // missing wildcard declaration
		}
		
		return (Class<?>) allGenTypes[0];
	}
}
