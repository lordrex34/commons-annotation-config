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
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import java.util.Set;
import java.util.TreeMap;
import java.util.TreeSet;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.stream.Stream;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.github.lordrex34.config.annotation.ConfigClass;
import com.github.lordrex34.config.model.ConfigClassInfo;
import com.github.lordrex34.config.postloadhooks.ConfigPostLoadHook;
import com.github.lordrex34.config.util.ClassPathUtil;
import com.github.lordrex34.config.util.PropertiesParser;

/**
 * A manager class that handles configuration loading.
 * @author lord_rex
 */
public final class ConfigManager
{
	private static final Logger LOGGER = LoggerFactory.getLogger(ConfigManager.class);
	
	/** Contains all the registered {@link ConfigClassInfo}s. */
	private final List<ConfigClassInfo> _configRegistry = new ArrayList<>();
	
	/** A simple {@link AtomicBoolean} that indicates reloading process. */
	private final AtomicBoolean _reloading = new AtomicBoolean(false);
	
	/** Properties registry, that is used for misplaced configuration indication. */
	private final Map<String, Map<Path, Set<String>>> _propertiesRegistry = new TreeMap<>();
	
	/** Whether override system is being used or not. */
	private boolean _overrideSystemAllowed = true;
	
	/** The parsed overridden properties. */
	private PropertiesParser _overridenProperties;
	
	/**
	 * Constructs the {@link ConfigManager} class, triggered by the {@link SingletonHolder}.
	 */
	ConfigManager()
	{
		// visibility
	}
	
	/**
	 * Gets the configuration registry list that contains all the registered {@link ConfigClassInfo}s.
	 * @return the configuration registry
	 */
	public List<ConfigClassInfo> getConfigRegistry()
	{
		return _configRegistry;
	}
	
	/**
	 * Allows/disallows based on the below {@code boolean} parameter whether override system is being used or not.<br>
	 * For this method to take effect, it has to be used before {@link #load(String)} or {@link #load(ClassLoader, String)}.
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
	 * In case override system is disabled, it initializes an empty instance of {@link PropertiesParser}.
	 */
	private void initOverrideProperties()
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
			
			_overridenProperties = new PropertiesParser(overridePath);
			
			LOGGER.info("loaded '{}' with {} overridden properti(es).", overridePath, _overridenProperties.size());
		}
		else
		{
			_overridenProperties = PropertiesParser.EMPTY;
		}
	}
	
	/**
	 * Gets overridden properties stored in this manager class.
	 * @return overridden properties
	 */
	public PropertiesParser getOverriddenProperties()
	{
		return _overridenProperties;
	}
	
	/**
	 * Registers a configuration property into this manager.
	 * @param packageName the package where configuration related classes are stored
	 * @param configFile path of the configuration file
	 * @param propertyKey the property key to be registered into {@code _propertiesRegistry}
	 */
	public void registerProperty(String packageName, Path configFile, String propertyKey)
	{
		if (!_propertiesRegistry.containsKey(packageName))
		{
			_propertiesRegistry.put(packageName, new HashMap<>());
		}
		
		if (!_propertiesRegistry.get(packageName).containsKey(configFile))
		{
			_propertiesRegistry.get(packageName).put(configFile, new TreeSet<>());
		}
		
		_propertiesRegistry.get(packageName).entrySet().forEach(entry ->
		{
			final Path entryConfigFile = entry.getKey();
			final Set<String> entryProperties = entry.getValue();
			
			if (!entryConfigFile.equals(configFile) && entryProperties.contains(propertyKey))
			{
				LOGGER.warn("Property key '{}' is already defined in config file '{}', so now '{}' overwrites that! Please fix this!", propertyKey, entryConfigFile, configFile);
			}
		});
		
		_propertiesRegistry.get(packageName).get(configFile).add(propertyKey);
	}
	
	/**
	 * Loads all configuration classes from the specified package and overwrites their properties according to override properties, if necessary.
	 * @param classLoader the class loader that is used for the process
	 * @param packageName the package where configuration related classes are stored
	 */
	public void load(ClassLoader classLoader, String packageName)
	{
		initOverrideProperties();
		
		// standard annotation based configuration classes
		try
		{
			ClassPathUtil.getAllClassesAnnotatedWith(classLoader, packageName, ConfigClass.class).forEach(clazz -> _configRegistry.add(new ConfigClassInfo(clazz)));
			_configRegistry.forEach(ConfigClassInfo::load);
			LOGGER.info("Loaded {} config file(s).", _configRegistry.size());
		}
		catch (IOException e)
		{
			LOGGER.warn("Failed to load class path.", e);
		}
		
		// non-standard solution
		try
		{
			ClassPathUtil.getAllClassesExtending(classLoader, packageName, IConfigLoader.class).forEach(clazz ->
			{
				if (Stream.of(clazz.getConstructors()).noneMatch((constructor) -> constructor.getParameterCount() == 0))
				{
					// Do not load IConfigLoader if there is no proper constructor match.
					return;
				}
				
				try
				{
					// Whatever black magic the user intend to do, that does not fit into annotation configuration engine, is able to use that pattern.
					final IConfigLoader configLoader = clazz.newInstance();
					configLoader.load(_overridenProperties);
				}
				catch (InstantiationException | IllegalAccessException e)
				{
					LOGGER.warn("Failed to load config.", e);
				}
			});
		}
		catch (IOException e)
		{
			LOGGER.warn("Failed to load class path.", e);
		}
	}
	
	/**
	 * Same as {@link #load(ClassLoader, String)}, using {@link ClassLoader#getSystemClassLoader()} as the classLoader parameter.
	 * @param packageName the package where configuration related classes are stored
	 */
	public void load(String packageName)
	{
		load(ClassLoader.getSystemClassLoader(), packageName);
	}
	
	/**
	 * Full wipe of the configuration manager data.<br>
	 * Designed for testing purposes.<br>
	 * Yes. Curse of singleton holders.<br>
	 * <b>Not to be used for reload process!</b>
	 */
	public void clear()
	{
		if (_overridenProperties != null)
		{
			_overridenProperties.clear();
		}
		
		_configRegistry.clear();
		_propertiesRegistry.clear();
	}
	
	/**
	 * Reloads configurations by package name.
	 * @param classLoader the class loader that is used for the process
	 * @param packageName the package where configuration related classes are stored
	 */
	public void reload(ClassLoader classLoader, String packageName)
	{
		if (_overridenProperties != null)
		{
			_overridenProperties.clear();
		}
		
		_configRegistry.clear();
		
		if (_propertiesRegistry.containsKey(packageName))
		{
			_propertiesRegistry.get(packageName).clear();
		}
		
		_reloading.set(true);
		try
		{
			load(classLoader, packageName);
		}
		finally
		{
			_reloading.set(false);
		}
	}
	
	/**
	 * Same as {@link #reload(ClassLoader, String)}, using {@link ClassLoader#getSystemClassLoader()} as the classLoader parameter.
	 * @param packageName the package where configuration related classes are stored
	 */
	public void reload(String packageName)
	{
		reload(ClassLoader.getSystemClassLoader(), packageName);
	}
	
	/**
	 * Checks whether reload is in progress or not.
	 * @return {@code true} if reload is in progress, otherwise {@code false}
	 */
	public boolean isReloading()
	{
		return _reloading.get();
	}
	
	/**
	 * Gets the result of two properties parser, where second is the override which overwrites the content of the first, if necessary.
	 * @param properties the original properties file
	 * @param override the override properties that overwrites original settings
	 * @return properties of the two properties parser
	 */
	public static Properties propertiesOf(PropertiesParser properties, PropertiesParser override)
	{
		final Properties result = new Properties();
		//@formatter:off
		Stream.concat(properties.entrySet().stream(), override.entrySet().stream())
				.forEach(e -> result.setProperty(e.getKey().toString(), e.getValue().toString()));
		//@formatter:on
		return result;
	}
	
	/**
	 * Gets the right property regarding all possible user input.
	 * @param properties the original properties file
	 * @param override the override properties that overwrites original settings
	 * @param propertyKey the property key can be found in properties files (user-friendly form)
	 * @param defaultValue a default value in case nothing could be loaded from the properties files
	 * @return the right property
	 */
	public static String getProperty(PropertiesParser properties, PropertiesParser override, String propertyKey, String defaultValue)
	{
		String property = override.getValue(propertyKey);
		if (property == null)
		{
			property = properties.getValue(propertyKey);
			if (property == null)
			{
				LOGGER.warn("Property key '{}' is missing, using default value!", propertyKey);
				return defaultValue;
			}
		}
		return property;
	}
	
	/**
	 * Load post-load configuration hooks by the given class.
	 * @param postLoadHooks cache map to avoid countless creation
	 * @param postLoadHookClass the given class
	 * @param properties regular properties
	 * @param overriddenProperties user overridden settings
	 */
	public static void loadPostLoadHook(Map<String, ConfigPostLoadHook> postLoadHooks, Class<? extends ConfigPostLoadHook> postLoadHookClass, PropertiesParser properties, PropertiesParser overriddenProperties)
	{
		try
		{
			final String postLoadHookClassName = postLoadHookClass.getName();
			ConfigPostLoadHook postLoadHook = postLoadHooks.get(postLoadHookClassName);
			if (postLoadHook == null)
			{
				postLoadHook = postLoadHookClass.newInstance();
				postLoadHooks.put(postLoadHookClassName, postLoadHook);
			}
			postLoadHook.load(properties, overriddenProperties);
		}
		catch (InstantiationException | IllegalAccessException e)
		{
			LOGGER.warn("Failed to load post load hook!", e);
		}
	}
	
	public static ConfigManager getInstance()
	{
		return SingletonHolder.INSTANCE;
	}
	
	private static final class SingletonHolder
	{
		static final ConfigManager INSTANCE = new ConfigManager();
	}
}
