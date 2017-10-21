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

import java.lang.reflect.Field;

/**
 * @author NB4L1
 * @param <T> associated field type
 */
public abstract class TypedConfigConverter<T> implements IConfigConverter
{
	@Override
	public final Object convertFromString(Field field, Class<?> type, String value)
	{
		if (getRequiredType() != type)
		{
			throw new ClassCastException(getRequiredType() + " type required, but found: " + type + "!");
		}
		
		value = (String) MainConfigConverter.getInstance().convertFromString(field, String.class, value);
		
		return convertFromString(value);
	}
	
	@Override
	public final String convertToString(Field field, Class<?> type, Object obj)
	{
		if (getRequiredType() != type)
		{
			throw new ClassCastException(getRequiredType() + " type required, but found: " + type + "!");
		}
		
		if (obj == null)
		{
			return "";
		}
		
		if (!getRequiredType().isInstance(obj))
		{
			throw new ClassCastException(getRequiredType() + " value required, but found: '" + obj + "'!");
		}
		
		final String value = convertToString(getRequiredType().cast(obj));
		
		return MainConfigConverter.getInstance().convertToString(field, String.class, value);
	}
	
	protected abstract T convertFromString(String value);
	
	protected abstract String convertToString(T obj);
	
	protected abstract Class<T> getRequiredType();
}
