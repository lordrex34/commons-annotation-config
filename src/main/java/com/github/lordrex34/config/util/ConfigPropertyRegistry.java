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

import java.nio.file.Path;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;
import java.util.TreeMap;
import java.util.TreeSet;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * A reload supporting registry used to check duplicated properties.
 * @author lord_rex
 */
public final class ConfigPropertyRegistry
{
	private static final Logger LOGGER = LoggerFactory.getLogger(ConfigPropertyRegistry.class);
	
	/** Properties registry, that is used for misplaced configuration indication. */
	private static final Map<String, Map<Path, Set<String>>> PROPERTIES = new TreeMap<>();
	
	private ConfigPropertyRegistry()
	{
		// utility class
	}
	
	/**
	 * Registers a configuration property into this manager.
	 * @param packageName the package where configuration related classes are stored
	 * @param configFile path of the configuration file
	 * @param propertyKey the property key to be registered into {@code PROPERTIES_REGISTRY}
	 */
	public static void add(String packageName, Path configFile, String propertyKey)
	{
		PROPERTIES.putIfAbsent(packageName, new HashMap<>());
		PROPERTIES.get(packageName).putIfAbsent(configFile, new TreeSet<>());
		
		PROPERTIES.get(packageName).entrySet().forEach(entry ->
		{
			final Path entryConfigFile = entry.getKey();
			final Set<String> entryProperties = entry.getValue();
			
			if (!entryConfigFile.equals(configFile) && entryProperties.contains(propertyKey))
			{
				LOGGER.warn("Property key '{}' is already defined in config file '{}', so now '{}' overwrites that! Please fix this!", propertyKey, entryConfigFile, configFile);
			}
		});
		
		PROPERTIES.get(packageName).get(configFile).add(propertyKey);
	}
	
	/**
	 * Clears registered properties that are bound to the specific package.
	 * @param packageName the package where configuration related classes are stored
	 */
	public static void clear(String packageName)
	{
		final Map<Path, Set<String>> registry = PROPERTIES.get(packageName);
		if (registry != null)
		{
			registry.clear();
		}
	}
}
