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
import java.util.Collection;
import java.util.EnumSet;
import java.util.Iterator;
import java.util.Set;
import java.util.TreeSet;

import com.github.lordrex34.config.util.GenericUtil;

/**
 * @author _dev_
 */
@SuppressWarnings(
{
	"unchecked",
	"rawtypes"
})
public abstract class CollectionConfigConverter implements IConfigConverter
{
	@Override
	public Object convertFromString(Field field, Class<?> type, String value)
	{
		if (value.isEmpty())
		{
			return emptyCollection();
		}
		
		Collection<Object> result = null;
		final String[] splitted = value.split(",");
		
		final Class<?> elementType = GenericUtil.getFirstGenericTypeOfGenerizedField(field);
		if (type == Set.class)
		{
			// for enums, impose enum order
			if (elementType.isEnum())
			{
				final Class<? extends Enum> c = elementType.asSubclass(Enum.class);
				if ("*".equals(value))
				{
					return EnumSet.allOf(c);
				}
				
				final EnumSet set = EnumSet.noneOf(c);
				result = set;
			}
			// otherwise, impose natural order (if applicable)
			else if (Comparable.class.isAssignableFrom(elementType))
			{
				result = new TreeSet<>();
			}
		}
		
		if (result == null)
		{
			result = createCollection(splitted.length);
		}
		
		for (final String e : splitted)
		{
			result.add(getElementConverter().convertFromString(null, elementType, e));
		}
		return result;
	}
	
	@Override
	public String convertToString(Field field, Class<?> type, Object obj)
	{
		if (obj == null)
		{
			return "";
		}
		
		final Collection<?> col = (Collection<?>) obj;
		if (col.isEmpty())
		{
			return "";
		}
		
		final Class<?> elementType = GenericUtil.getFirstGenericTypeOfGenerizedField(field);
		if (elementType.isEnum() && (type == Set.class))
		{
			final Class<? extends Enum> c = elementType.asSubclass(Enum.class);
			if (col.equals(EnumSet.allOf(c)))
			{
				return "*";
			}
		}
		
		final Iterator<?> it = col.iterator();
		final StringBuilder sb = new StringBuilder().append(getElementConverter().convertToString(null, elementType, it.next()));
		while (it.hasNext())
		{
			sb.append(',').append(getElementConverter().convertToString(null, elementType, it.next()));
		}
		return sb.toString();
	}
	
	protected abstract Collection<Object> createCollection(int size);
	
	protected abstract <E> Collection<E> emptyCollection();
	
	protected IConfigConverter getElementConverter()
	{
		return MainConfigConverter.getInstance();
	}
}
