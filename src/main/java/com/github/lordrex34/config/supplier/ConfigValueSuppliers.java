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

import java.util.HashMap;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * A registry to avoid creating the same value supplier thousand times.
 * @author lord_rex
 */
public final class ConfigValueSuppliers
{
	private static final Logger LOGGER = LoggerFactory.getLogger(ConfigValueSuppliers.class);
	
	/** The cache. */
	private static final Map<String, IConfigValueSupplier<?>> VALUE_SUPPLIERS = new HashMap<>();
	
	private ConfigValueSuppliers()
	{
		// utility class
	}
	
	/**
	 * Gets the value supplier from the cache. If it is not present, then it gets registered automatically.
	 * @param valueSupplierClass the class contained by the information holder annotation
	 * @return value supplier
	 */
	public static IConfigValueSupplier<?> get(Class<? extends IConfigValueSupplier<?>> valueSupplierClass)
	{
		return VALUE_SUPPLIERS.computeIfAbsent(valueSupplierClass.getName(), k ->
		{
			try
			{
				return valueSupplierClass.newInstance();
			}
			catch (InstantiationException | IllegalAccessException e)
			{
				LOGGER.warn("Failed to load value supplier!", e);
				return null;
			}
		});
	}
}
