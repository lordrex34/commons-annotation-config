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
 * An interface used by the converters inside this package.
 * @author NB4L1 (original idea)
 * @author _dev_ (original idea)
 * @author lord_rex
 */
public interface IConfigConverter
{
	/**
	 * Converts from {@link String} into {@link Object} according to the implementation.
	 * @param field the field that has the required information
	 * @param type the field type
	 * @param value the value that is used for the process
	 * @return converted result
	 */
	public Object convertFromString(Field field, Class<?> type, String value);
	
	/**
	 * Converts {@link Object} into {@link String} according to the implementation.
	 * @param field the field that has the required information
	 * @param type the field type
	 * @param obj the object that is subject of the process
	 * @return converted result
	 */
	public String convertToString(Field field, Class<?> type, Object obj);
}
