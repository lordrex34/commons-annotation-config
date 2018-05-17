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

import com.github.lordrex34.config.annotation.ConfigField;
import com.github.lordrex34.config.component.IConfigComponent;
import com.github.lordrex34.config.lang.ConfigProperties;

/**
 * Configuration value supplier interface.
 * @author lord_rex
 * @param <T> the type that is being supplied
 */
@FunctionalInterface
public interface IConfigGeneratedValueSupplier<T> extends IConfigComponent
{
	/**
	 * Supplies a value to the field that is being configured.
	 * @param clazz the {@link Class} that is being configured.
	 * @param field the {@link Field} that is being configured.
	 * @param configField the {@link ConfigField} that is being processed
	 * @param properties mixture of normal and overridden properties
	 * @return the supplied value
	 * @throws InstantiationException
	 * @throws IllegalAccessException
	 */
	T supply(Class<?> clazz, Field field, ConfigField configField, ConfigProperties properties) throws InstantiationException, IllegalAccessException;
}
