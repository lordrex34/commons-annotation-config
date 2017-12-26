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
package com.github.lordrex34.config;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.HashSet;
import java.util.Set;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.github.lordrex34.config.annotation.ConfigClass;
import com.github.lordrex34.config.context.ConfigClassLoadingContext;
import com.github.lordrex34.config.lang.ConfigProperties;
import com.github.lordrex34.config.model.ConfigClassInfo;
import com.github.lordrex34.config.util.ClassPathUtil;
import com.github.lordrex34.config.util.ConfigPropertyRegistry;
import com.google.common.annotations.VisibleForTesting;

/**
 * A manager class that handles configuration loading.
 * @author lord_rex
 */
public final class ConfigManager
{
	private static final Logger LOGGER = LoggerFactory.getLogger(ConfigManager.class);
	
	/** Contains all the registered {@link ConfigClassInfo}s. */
	private final Set<ConfigClassInfo> _configRegistry = new HashSet<>();
	
	/** Whether override system is being used or not. */
	private boolean _overrideSystemAllowed = true;
	
	/** The parsed overridden properties. */
	private ConfigProperties _overridenProperties;
	
	/**
	 * Gets how many {@link ConfigClassInfo}s are registered in the configuration registry.
	 * @return registry size
	 */
	public int getConfigRegistrySize()
	{
		return _configRegistry.size();
	}
	
	/**
	 * Allows/disallows based on the below {@code boolean} parameter whether override system is being used or not.<br>
	 * For this method to take effect, it has to be used before {@link #load(String)} or {@link #load(ClassLoader, String, boolean)}.
	 * @param overrideSystemAllowed user choice to allow/disallow the override system
	 */
	public void setOverrideSystemAllowed(boolean overrideSystemAllowed)
	{
		_overrideSystemAllowed = overrideSystemAllowed;
	}
	
	/**
	 * Checks whether the override system is allowed or not.<br>
	 * When it's disabled {@code config/override.properties} won't be used.
	 * @return {@code true} when override system is allowed, otherwise {@code false}
	 */
	public boolean isOverrideSystemAllowed()
	{
		return _overrideSystemAllowed;
	}
	
	/**
	 * A method designed to initialize override properties.<br>
	 * In case override system is disabled, it initializes an empty instance of {@link ConfigProperties}.
	 * @throws IOException
	 */
	private void initOverrideProperties() throws IOException
	{
		if (isOverrideSystemAllowed())
		{
			final Path overridePath = Paths.get("config", "override.properties");
			if (Files.notExists(overridePath))
			{
				try
				{
					final Path overridePathParent = overridePath.getParent();
					if (overridePathParent != null)
					{
						Files.createDirectories(overridePathParent);
					}
					Files.createFile(overridePath);
					LOGGER.info("Generated empty file: '{}'", overridePath);
				}
				catch (IOException e)
				{
					// Disaster, disaster! Read-only FS alert! NOW!!
					throw new Error("Failed to create override config and/or its directory!", e);
				}
			}
			
			_overridenProperties = new ConfigProperties(overridePath);
			
			LOGGER.info("loaded '{}' with {} overridden properti(es).", overridePath, _overridenProperties.size());
		}
		else
		{
			_overridenProperties = ConfigProperties.EMPTY;
		}
	}
	
	/**
	 * Gets overridden properties stored in this manager class.
	 * @return overridden properties
	 */
	@VisibleForTesting
	ConfigProperties getOverriddenProperties()
	{
		return _overridenProperties;
	}
	
	/**
	 * Loads all configuration classes from the specified package and overwrites their properties according to override properties, if necessary.
	 * @param classLoader the class loader that is used for the process
	 * @param packageName the package where configuration related classes are stored
	 * @param reloading whether actual loading is a reload or not
	 * @throws IOException
	 * @throws InstantiationException
	 * @throws IllegalAccessException
	 * @throws IllegalArgumentException
	 */
	public void load(ClassLoader classLoader, String packageName, boolean reloading) throws IOException, IllegalArgumentException, IllegalAccessException, InstantiationException
	{
		initOverrideProperties();
		
		ClassPathUtil.getAllClassesAnnotatedWith(classLoader, packageName, ConfigClass.class).forEach(clazz -> _configRegistry.add(new ConfigClassInfo(clazz)));
		final ConfigClassLoadingContext classLoadingContext = new ConfigClassLoadingContext();
		classLoadingContext.setOverriddenProperties(_overridenProperties);
		classLoadingContext.setReloading(reloading);
		for (ConfigClassInfo configClassInfo : _configRegistry)
		{
			configClassInfo.load(classLoadingContext);
		}
		
		LOGGER.info("Loaded {} config file(s).", _configRegistry.size());
	}
	
	/**
	 * Same as {@link #load(ClassLoader, String, boolean)}, using {@link ClassLoader#getSystemClassLoader()} as the classLoader parameter.
	 * @param packageName the package where configuration related classes are stored
	 * @throws IOException
	 * @throws InstantiationException
	 * @throws IllegalAccessException
	 * @throws IllegalArgumentException
	 */
	public void load(String packageName) throws IOException, IllegalArgumentException, IllegalAccessException, InstantiationException
	{
		load(ClassLoader.getSystemClassLoader(), packageName, false);
	}
	
	/**
	 * Reloads configurations by package name.
	 * @param classLoader the class loader that is used for the process
	 * @param packageName the package where configuration related classes are stored
	 * @throws IOException
	 * @throws InstantiationException
	 * @throws IllegalAccessException
	 * @throws IllegalArgumentException
	 */
	public void reload(ClassLoader classLoader, String packageName) throws IOException, IllegalArgumentException, IllegalAccessException, InstantiationException
	{
		if (_overridenProperties != null)
		{
			_overridenProperties.clear();
		}
		
		_configRegistry.clear();
		
		ConfigPropertyRegistry.clear(packageName);
		load(classLoader, packageName, true);
	}
	
	/**
	 * Same as {@link #reload(ClassLoader, String)}, using {@link ClassLoader#getSystemClassLoader()} as the classLoader parameter.
	 * @param packageName the package where configuration related classes are stored
	 * @throws IOException
	 * @throws InstantiationException
	 * @throws IllegalAccessException
	 * @throws IllegalArgumentException
	 */
	public void reload(String packageName) throws IOException, IllegalArgumentException, IllegalAccessException, InstantiationException
	{
		reload(ClassLoader.getSystemClassLoader(), packageName);
	}
}
