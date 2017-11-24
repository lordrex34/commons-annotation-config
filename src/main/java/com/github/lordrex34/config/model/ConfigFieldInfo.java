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
package com.github.lordrex34.config.model;

import java.lang.reflect.Field;
import java.lang.reflect.Modifier;
import java.nio.file.Path;
import java.util.Arrays;
import java.util.EnumSet;
import java.util.HashMap;
import java.util.Map;
import java.util.stream.Collectors;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.github.lordrex34.config.ConfigManager;
import com.github.lordrex34.config.annotation.ConfigField;
import com.github.lordrex34.config.annotation.ConfigGroupBeginning;
import com.github.lordrex34.config.annotation.ConfigGroupEnding;
import com.github.lordrex34.config.postloadhooks.IConfigPostLoadHook;
import com.github.lordrex34.config.supplier.IConfigValueSupplier;
import com.github.lordrex34.config.util.PropertiesParser;

/**
 * @author NB4L1 (original concept)
 * @author lord_rex
 */
public final class ConfigFieldInfo
{
	private static final Logger LOGGER = LoggerFactory.getLogger(ConfigFieldInfo.class);
	
	/** To avoid creating the same value supplier thousand times. */
	private static final Map<String, IConfigValueSupplier<?>> VALUE_SUPPLIERS = new HashMap<>();
	
	/** To avoid creating the same post load hook thousand times. */
	private static final Map<String, IConfigPostLoadHook> POST_LOAD_HOOKS = new HashMap<>();
	
	/** The class that is being scanned. */
	private final Class<?> _clazz;
	
	/** The field that contains {@link ConfigField} annotation. */
	private final Field _field;
	
	/** The configuration field information annotation. */
	private final ConfigField _configField;
	
	/** Group beginning marker annotation. */
	private final ConfigGroupBeginning _beginningGroup;
	
	/** Group ending marker annotation. */
	private final ConfigGroupEnding _endingGroup;
	
	/**
	 * Constructs a new information container class for the field.
	 * @param clazz the class that is being scanned
	 * @param field the field that contains {@link ConfigField} annotation
	 */
	public ConfigFieldInfo(Class<?> clazz, Field field)
	{
		_clazz = clazz;
		_field = field;
		_configField = _field.getDeclaredAnnotation(ConfigField.class);
		_beginningGroup = _field.getDeclaredAnnotation(ConfigGroupBeginning.class);
		_endingGroup = _field.getDeclaredAnnotation(ConfigGroupEnding.class);
	}
	
	/**
	 * Loads and configures the field with its proper values.
	 * @param configPath path of the properties file
	 * @param properties the regular properties
	 * @param overriddenProperties the content of {@code override.properties}
	 */
	public void load(Path configPath, PropertiesParser properties, PropertiesParser overriddenProperties)
	{
		// Skip inappropriate fields.
		if (!Modifier.isStatic(_field.getModifiers()) || Modifier.isFinal(_field.getModifiers()))
		{
			LOGGER.debug("Skipping non static or final field: {}#{}", _clazz.getSimpleName(), _field.getName());
			return;
		}
		
		// If field is just a comment holder, then do not try to load it.
		if (_configField.onlyComment())
		{
			return;
		}
		
		try
		{
			final String propertyKey = _configField.name();
			ConfigManager.getInstance().registerProperty(_clazz.getPackage().getName(), configPath, propertyKey);
			if (!_configField.reloadable() && ConfigManager.getInstance().isReloading())
			{
				LOGGER.debug("Property '{}' retained with its previous value!", propertyKey);
				return;
			}
			
			// private field support
			final boolean wasAccessible = _field.isAccessible();
			if (!wasAccessible)
			{
				_field.setAccessible(true);
			}
			
			// value suppliers
			final Class<? extends IConfigValueSupplier<?>> valueSupplierClass = _configField.valueSupplier();
			final String valueSupplierClassName = valueSupplierClass.getName();
			IConfigValueSupplier<?> valueSupplier = VALUE_SUPPLIERS.get(valueSupplierClassName);
			if (valueSupplier == null)
			{
				valueSupplier = valueSupplierClass.newInstance();
				VALUE_SUPPLIERS.put(valueSupplierClassName, valueSupplier);
			}
			
			_field.set(null, valueSupplier.supply(_field, _configField, properties, overriddenProperties));
			_field.setAccessible(wasAccessible);
			
			ConfigManager.loadPostLoadHook(POST_LOAD_HOOKS, _configField.postLoadHook(), properties, overriddenProperties);
		}
		catch (InstantiationException | IllegalAccessException e)
		{
			LOGGER.warn("Failed to set field!", e);
		}
	}
	
	/**
	 * Prints the necessary field information into a {@link StringBuilder}.
	 * @param out the {@link StringBuilder} that receives the output
	 */
	public void print(StringBuilder out)
	{
		if (_beginningGroup != null)
		{
			out.append("########################################").append(System.lineSeparator());
			out.append("## Section BEGIN: ").append(_beginningGroup.name()).append(System.lineSeparator());
			
			for (String line : _beginningGroup.comment())
			{
				out.append("# ").append(line).append(System.lineSeparator());
			}
			
			out.append(System.lineSeparator());
		}
		
		for (String line : _configField.comment())
		{
			out.append("# ").append(line).append(System.lineSeparator());
		}
		
		if (!_configField.onlyComment())
		{
			out.append("# Default: ").append(_configField.value()).append(System.lineSeparator());
			if (_field.getType().isEnum())
			{
				out.append("# Available: ").append(Arrays.stream(_field.getType().getEnumConstants()).map(String::valueOf).collect(Collectors.joining("|"))).append(System.lineSeparator());
			}
			else if (_field.getType().isArray())
			{
				final Class<?> fieldComponentType = _field.getType().getComponentType();
				if (fieldComponentType.isEnum())
				{
					@SuppressWarnings("unchecked")
					final EnumSet<?> collection = EnumSet.allOf(fieldComponentType.asSubclass(Enum.class));
					out.append("# Available: ").append(collection.toString().replace("[", "").replace("]", "").replace(" ", "")).append(System.lineSeparator());
				}
			}
			out.append(_configField.name()).append(" = ").append(_configField.value()).append(System.lineSeparator());
			out.append(System.lineSeparator());
		}
		
		if (_endingGroup != null)
		{
			for (String line : _endingGroup.comment())
			{
				out.append("# ").append(line).append(System.lineSeparator());
			}
			
			out.append("## Section END: ").append(_endingGroup.name()).append(System.lineSeparator());
			out.append("########################################").append(System.lineSeparator());
			
			out.append(System.lineSeparator());
		}
	}
}
