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
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.HashSet;
import java.util.Set;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.github.lordrex34.config.annotation.ConfigClass;
import com.github.lordrex34.config.context.ConfigClassLoadingContext;
import com.github.lordrex34.config.exception.ConfigOverrideLoadingException;
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
	
	/** Input stream of the override system. */
	private final InputStream _overrideInputStream;
	
	/** The parsed overridden properties. */
	private ConfigProperties _overridenProperties;
	
	/**
	 * Constructs the {@link ConfigManager} class, used by user-end implementation.
	 * @param overrideInputStream By setting this to {@code null} you can disable the override system.<br>
	 *            You can as well set this to whatever you want. See tests for example.
	 */
	public ConfigManager(InputStream overrideInputStream)
	{
		_overrideInputStream = overrideInputStream;
	}
	
	/**
	 * Constructs the {@link ConfigManager} class, used by user-end implementation.
	 */
	public ConfigManager()
	{
		this(defaultOverrideInputStream());
	}
	
	/**
	 * Gets how many {@link ConfigClassInfo}s are registered in the configuration registry.
	 * @return registry size
	 */
	public int getConfigRegistrySize()
	{
		return _configRegistry.size();
	}
	
	/**
	 * Creates the default {@link InputStream} for the override system.
	 * @return default override input stream
	 */
	private static InputStream defaultOverrideInputStream()
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
				throw new ConfigOverrideLoadingException("Failed to create override config and/or its directory!", e);
			}
		}
		
		try
		{
			return Files.newInputStream(overridePath);
		}
		catch (IOException e)
		{
			throw new ConfigOverrideLoadingException("Failed to load override input stream!", e);
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
		if (_overrideInputStream != null)
		{
			_overridenProperties = new ConfigProperties(_overrideInputStream);
			LOGGER.info("Loaded {} overridden properti(es).", _overridenProperties.size());
		}
		else
		{
			_overridenProperties = ConfigProperties.EMPTY;
		}
		
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
