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
import java.lang.reflect.Field;
import java.lang.reflect.Modifier;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.HashMap;
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
import com.github.lordrex34.config.annotation.ConfigField;
import com.github.lordrex34.config.converter.IConfigConverter;
import com.github.lordrex34.config.generator.AbstractConfigGenerator;
import com.github.lordrex34.config.lang.FieldParser.FieldParserException;
import com.github.lordrex34.config.postloadhooks.ConfigPostLoadHook;
import com.github.lordrex34.config.postloadhooks.EmptyConfigPostLoadHook;
import com.github.lordrex34.config.util.ClassPathUtil;
import com.github.lordrex34.config.util.PropertiesParser;

/**
 * A manager class that handles configuration loading.
 * @author lord_rex
 */
public final class ConfigManager
{
	private static final Logger LOGGER = LoggerFactory.getLogger(ConfigManager.class);
	
	/** A simple {@link AtomicBoolean} that indicates reloading process. */
	private final AtomicBoolean _reloading = new AtomicBoolean(false);
	
	/** Properties registry, that is used for misplaced configuration indication. */
	private final Map<String, Map<Path, Set<String>>> _propertiesRegistry = new TreeMap<>();
	
	/** Path of the override.properties configuration file. */
	private final Path _overridePath;
	
	/** The parsed overridden properties. */
	private PropertiesParser _overridenProperties;
	
	/** Whether override system is being used or not. */
	private boolean _overrideSystemAllowed = true;
	
	/**
	 * Constructs the {@link ConfigManager} class, triggered by the {@link SingletonHolder}.
	 */
	protected ConfigManager()
	{
		_overridePath = Paths.get("config", "override.properties");
		if (Files.notExists(_overridePath) && isOverrideSystemAllowed())
		{
			try
			{
				final Path overridePathParent = _overridePath.getParent();
				if (overridePathParent != null)
				{
					Files.createDirectories(overridePathParent);
				}
				Files.createFile(_overridePath);
			}
			catch (IOException e)
			{
				// Disaster, disaster! Read-only FS alert! NOW!!
				throw new Error("Failed to create override config and/or its directory!", e);
			}
		}
		
		initOverrideProperties();
	}
	
	/**
	 * A method designed to initialize override properties.<br>
	 * In case override system is disabled, it initializes an empty instance of {@link PropertiesParser}.
	 */
	private void initOverrideProperties()
	{
		if (isOverrideSystemAllowed())
		{
			_overridenProperties = new PropertiesParser(_overridePath);
		}
		else
		{
			_overridenProperties = PropertiesParser.EMPTY;
		}
	}
	
	/**
	 * Allows/disallows based on the below {@code boolean} whether override system is being used or not.
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
	 * Registers a configuration property into this manager.
	 * @param packageName the package where configuration related classes are stored
	 * @param configFile path of the configuration file
	 * @param propertyKey the property key to be registered into {@code _propertiesRegistry}
	 */
	private void registerProperty(String packageName, Path configFile, String propertyKey)
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
			
			if (entryProperties.contains(propertyKey))
			{
				LOGGER.warn("Property key '{}' is already defined in config file '{}', so now '{}' overwrites that! Please fix this!", propertyKey, entryConfigFile, configFile);
			}
		});
		
		_propertiesRegistry.get(packageName).get(configFile).add(propertyKey);
	}
	
	/**
	 * Gets the right property regarding all possible user input.
	 * @param properties the original properties file
	 * @param override the override properties that overwrites original settings
	 * @param propertyKey the property key can be found in properties files (user-friendly form)
	 * @param defaultValue a default value in case nothing could be loaded from the properties files
	 * @return the right property
	 */
	private static String getProperty(PropertiesParser properties, PropertiesParser override, String propertyKey, String defaultValue)
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
	 * Loads a configuration class into the manager.
	 * @param clazz the config class itself
	 */
	private void loadConfigClass(Class<?> clazz)
	{
		if (_overridenProperties == null)
		{
			throw new NullPointerException("Override properties is missing!");
		}
		
		final ConfigClass configClass = clazz.getDeclaredAnnotation(ConfigClass.class);
		if (configClass == null)
		{
			LOGGER.warn("Class {} doesn't have @ConfigClass annotation!", clazz);
			return;
		}
		
		final Path configPath = Paths.get("", configClass.pathNames()).resolve(configClass.fileName() + configClass.fileExtension());
		if (Files.notExists(configPath))
		{
			LOGGER.warn("Config File {} doesn't exist! Generating ...", configPath);
			
			try
			{
				AbstractConfigGenerator.printConfigClass(clazz);
			}
			catch (IOException e)
			{
				LOGGER.warn("Failed to generate config!", e);
			}
		}
		
		final PropertiesParser properties = new PropertiesParser(configPath);
		for (Field field : clazz.getDeclaredFields())
		{
			if (field == null)
			{
				continue;
			}
			
			// Skip inappropriate fields.
			if (!Modifier.isStatic(field.getModifiers()) || Modifier.isFinal(field.getModifiers()))
			{
				LOGGER.debug("Skipping non static or final field: {}#{}", clazz.getSimpleName(), field.getName());
				continue;
			}
			
			final ConfigField configField = field.getDeclaredAnnotation(ConfigField.class);
			if (configField != null)
			{
				// If field is just a comment holder, then do not try to load it.
				if (configField.onlyComment())
				{
					continue;
				}
				
				try
				{
					final String propertyKey = configField.name();
					final String propertyValue = configField.value();
					ConfigManager.getInstance().registerProperty(clazz.getPackage().getName(), configPath, propertyKey);
					if (!configField.reloadable() && ConfigManager.getInstance().isReloading())
					{
						LOGGER.debug("Property '{}' retained with its previous value!", propertyKey);
						continue;
					}
					
					final String configProperty = getProperty(properties, _overridenProperties, propertyKey, propertyValue);
					final IConfigConverter converter = configField.converter().newInstance();
					Object value;
					try
					{
						value = converter.convertFromString(field, field.getType(), configProperty);
					}
					catch (FieldParserException e)
					{
						value = converter.convertFromString(field, field.getType(), propertyValue);
						LOGGER.warn("Property '{}' has incorrect syntax! Using default value instead: {}", propertyKey, propertyValue);
					}
					final boolean wasAccessible = field.isAccessible();
					if (!wasAccessible)
					{
						field.setAccessible(true);
					}
					field.set(null, value);
					field.setAccessible(wasAccessible);
					
					// post load hook event for field
					final ConfigPostLoadHook postLoadHook = configField.postLoadHook().newInstance();
					if ((postLoadHook != null) && !(postLoadHook instanceof EmptyConfigPostLoadHook))
					{
						postLoadHook.load(properties, _overridenProperties);
					}
				}
				catch (InstantiationException | IllegalAccessException e)
				{
					LOGGER.warn("Failed to set field!", e);
				}
			}
		}
		
		try
		{
			// post load hook event for class
			final ConfigPostLoadHook postLoadHook = configClass.postLoadHook().newInstance();
			if ((postLoadHook != null) && !(postLoadHook instanceof EmptyConfigPostLoadHook))
			{
				postLoadHook.load(properties, _overridenProperties);
			}
		}
		catch (InstantiationException | IllegalAccessException e)
		{
			LOGGER.warn("Failed to load post load hook!", e);
		}
		
		LOGGER.debug("loaded '{}'", configPath);
	}
	
	/**
	 * Loads all configuration classes from the specified package and overwrites their properties according to override properties, if necessary.
	 * @param classLoader the class loader that is used for the process
	 * @param packageName the package where configuration related classes are stored
	 */
	public void load(ClassLoader classLoader, String packageName)
	{
		if (_overridenProperties == null)
		{
			throw new NullPointerException("Override properties is missing!");
		}
		
		final ConfigCounter configCount = new ConfigCounter();
		try
		{
			// standard annotation based configuration classes
			ClassPathUtil.getAllClassesAnnotatedWith(classLoader, packageName, ConfigClass.class).forEach(clazz ->
			{
				loadConfigClass(clazz);
				configCount.increment();
			});
			
			// non-standard solution
			ClassPathUtil.getAllClassesExtending(classLoader, packageName, IConfigLoader.class).forEach(clazz ->
			{
				if (Stream.of(clazz.getConstructors()).noneMatch((constructor) -> constructor.getParameterCount() == 0))
				{
					// Do not load IConfigLoader if there is no proper constructor match.
					return;
				}
				
				try
				{
					final IConfigLoader configLoader = clazz.newInstance();
					configLoader.load(_overridenProperties);
					configCount.increment();
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
		
		LOGGER.info("Loaded {} config file(s).", configCount.getValue());
	}
	
	/**
	 * Same as {@link #load(ClassLoader, String)}, using {@link ClassLoader#getSystemClassLoader()} as the classLoader parameter.
	 * @param packageName
	 */
	public void load(String packageName)
	{
		load(ClassLoader.getSystemClassLoader(), packageName);
	}
	
	private static final class ConfigCounter
	{
		private int _count;
		
		protected ConfigCounter()
		{
			// visibility
		}
		
		public void increment()
		{
			_count++;
		}
		
		public int getValue()
		{
			return _count;
		}
	}
	
	/**
	 * Reloads configurations by package name.
	 * @param classLoader the class loader that is used for the process
	 * @param packageName the package where configuration related classes are stored
	 */
	public void reload(ClassLoader classLoader, String packageName)
	{
		// overridden properties will always be reloaded
		// as it is path, and not package based, and so not need to be
		// though any package might use override.properties
		initOverrideProperties();
		
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
	 * @param packageName
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
	
	private static final class SingletonHolder
	{
		protected static final ConfigManager INSTANCE = new ConfigManager();
	}
	
	public static ConfigManager getInstance()
	{
		return SingletonHolder.INSTANCE;
	}
}
