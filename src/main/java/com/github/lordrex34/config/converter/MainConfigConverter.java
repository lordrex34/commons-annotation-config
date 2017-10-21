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

import java.io.File;
import java.lang.reflect.Field;
import java.nio.file.Path;
import java.time.Duration;
import java.util.List;
import java.util.Set;
import java.util.regex.Pattern;

import com.github.lordrex34.config.lang.FieldParser;

public class MainConfigConverter implements IConfigConverter
{
	@Override
	public Object convertFromString(Field field, Class<?> type, String value)
	{
		if (type.isArray())
		{
			return getArrayConverter().convertFromString(field, type, value);
		}
		
		if (type == List.class)
		{
			return getListConverter().convertFromString(field, type, value);
		}
		
		if (type == Set.class)
		{
			return getSetConverter().convertFromString(field, type, value);
		}
		
		if (type == Path.class)
		{
			return getPathConverter().convertFromString(field, type, value);
		}
		
		if (type == File.class)
		{
			return getFileConverter().convertFromString(field, type, value);
		}
		
		if (type == Pattern.class)
		{
			return getPatternConverter().convertFromString(field, type, value);
		}
		
		if (type == Duration.class)
		{
			return getDurationConverter().convertFromString(field, type, value);
		}
		
		return FieldParser.get(type, value);
	}
	
	@Override
	public String convertToString(Field field, Class<?> type, Object obj)
	{
		if (type.isArray())
		{
			return getArrayConverter().convertToString(field, type, obj);
		}
		
		if (type == List.class)
		{
			return getListConverter().convertToString(field, type, obj);
		}
		
		if (type == Set.class)
		{
			return getSetConverter().convertToString(field, type, obj);
		}
		
		if (type == Path.class)
		{
			return getPathConverter().convertToString(field, type, obj);
		}
		
		if (type == File.class)
		{
			return getFileConverter().convertToString(field, type, obj);
		}
		
		if (type == Pattern.class)
		{
			return getPatternConverter().convertToString(field, type, obj);
		}
		
		if (type == Duration.class)
		{
			return getDurationConverter().convertToString(field, type, obj);
		}
		
		if (obj == null)
		{
			return "";
		}
		
		return obj.toString();
	}
	
	protected IConfigConverter getArrayConverter()
	{
		return ArrayConfigConverter.getInstance();
	}
	
	protected IConfigConverter getListConverter()
	{
		return ListConfigConverter.getInstance();
	}
	
	protected IConfigConverter getSetConverter()
	{
		return SetConfigConverter.getInstance();
	}
	
	protected IConfigConverter getPathConverter()
	{
		return PathConfigConverter.getInstance();
	}
	
	protected IConfigConverter getFileConverter()
	{
		return FileConfigConverter.getInstance();
	}
	
	protected IConfigConverter getPatternConverter()
	{
		return PatternConfigConverter.getInstance();
	}
	
	protected IConfigConverter getDurationConverter()
	{
		return DurationConfigConverter.getInstance();
	}
	
	private static final class SingletonHolder
	{
		static final MainConfigConverter INSTANCE = new MainConfigConverter();
	}
	
	public static MainConfigConverter getInstance()
	{
		return SingletonHolder.INSTANCE;
	}
}
