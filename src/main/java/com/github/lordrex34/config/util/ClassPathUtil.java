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
package com.github.lordrex34.config.util;

import java.io.IOException;
import java.io.UncheckedIOException;
import java.lang.annotation.Annotation;
import java.lang.reflect.Constructor;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.util.Arrays;
import java.util.List;
import java.util.Objects;
import java.util.concurrent.ConcurrentMap;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import com.google.common.collect.Maps;
import com.google.common.reflect.ClassPath;
import com.google.common.reflect.ClassPath.ClassInfo;

/**
 * A simple utility class to handle getting classes/packages.
 * @author UnAfraid
 */
@SuppressWarnings("unchecked")
public final class ClassPathUtil
{
	private static final ConcurrentMap<ClassLoader, ClassPath> _classPathByClassLoaderMap = Maps.newConcurrentMap();
	
	public static ClassPath getClassPath(ClassLoader classLoader) throws IOException
	{
		try
		{
			return _classPathByClassLoaderMap.computeIfAbsent(classLoader, cl ->
			{
				try
				{
					return ClassPath.from(cl);
				}
				catch (IOException e)
				{
					throw new UncheckedIOException(e);
				}
			});
		}
		catch (UncheckedIOException e)
		{
			throw e.getCause();
		}
	}
	
	private ClassPathUtil()
	{
		// utility class
	}
	
	/**
	 * Gets a {@link Class#newInstance()} of the first class extending the given target class.
	 * @param <T>
	 * @param classLoader the class loader that is used for the process
	 * @param targetClass the given target class
	 * @return new instance of the given target class
	 * @throws Exception
	 */
	public static <T> T getInstanceOfExtending(ClassLoader classLoader, Class<T> targetClass) throws Exception
	{
		for (Class<T> targetClassImpl : getAllClassesExtending(classLoader, targetClass))
		{
			for (Constructor<?> constructor : targetClassImpl.getConstructors())
			{
				if (Modifier.isPublic(constructor.getModifiers()) && (constructor.getParameterCount() == 0))
				{
					return (T) constructor.newInstance();
				}
			}
		}
		throw new IllegalStateException("Couldn't find public constructor without prameters");
		
	}
	
	/**
	 * Gets all classes extending the given target class.
	 * @param <T>
	 * @param classLoader the class loader that is used for the process
	 * @param targetClass the given target class
	 * @return a list of classes
	 * @throws IOException
	 */
	public static <T> List<Class<T>> getAllClassesExtending(ClassLoader classLoader, Class<T> targetClass) throws IOException
	{
		final ClassPath classPath = getClassPath(classLoader);
		//@formatter:off
		return classPath.getTopLevelClasses()
			.stream()
			.map(ClassPathUtil::loadClass)
			.filter(Objects::nonNull)
			.filter(clazz -> targetClass.isAssignableFrom(clazz))
			.filter(clazz -> !Modifier.isAbstract(clazz.getModifiers()))
			.filter(clazz -> !Modifier.isInterface(clazz.getModifiers()))
			.map(clazz -> (Class<T>) clazz)
			.collect(Collectors.toList());
		//@formatter:on
	}
	
	/**
	 * Gets all classes extending the given target class.
	 * @param <T>
	 * @param classLoader the class loader that is used for the process
	 * @param packageName the package where you seek
	 * @param targetClass the given target class
	 * @return a list of classes
	 * @throws IOException
	 */
	public static <T> List<Class<T>> getAllClassesExtending(ClassLoader classLoader, String packageName, Class<T> targetClass) throws IOException
	{
		final ClassPath classPath = getClassPath(classLoader);
		//@formatter:off
		return classPath.getTopLevelClassesRecursive(packageName)
			.stream()
			.map(ClassPathUtil::loadClass)
			.filter(Objects::nonNull)
			.filter(clazz -> targetClass.isAssignableFrom(clazz))
			.filter(clazz -> !Modifier.isAbstract(clazz.getModifiers()))
			.filter(clazz -> !Modifier.isInterface(clazz.getModifiers()))
			.map(clazz -> (Class<T>) clazz)
			.collect(Collectors.toList());
		//@formatter:on
	}
	
	/**
	 * Gets all classes extending the given target class.
	 * @param <T>
	 * @param classLoader the class loader that is used for the process
	 * @param packageName the package where you seek
	 * @param annotationClass the given annotation class
	 * @return a list of classes
	 * @throws IOException
	 */
	public static <T> List<Class<T>> getAllClassesAnnotatedWith(ClassLoader classLoader, String packageName, Class<? extends Annotation> annotationClass) throws IOException
	{
		final ClassPath classPath = getClassPath(classLoader);
		//@formatter:off
		return classPath.getTopLevelClassesRecursive(packageName)
			.stream()
			.map(ClassPathUtil::loadClass)
			.filter(Objects::nonNull)
			.filter(clazz -> clazz.isAnnotationPresent(annotationClass))
			.filter(clazz -> !Modifier.isAbstract(clazz.getModifiers()))
			.filter(clazz -> !Modifier.isInterface(clazz.getModifiers()))
			.map(clazz -> (Class<T>) clazz)
			.collect(Collectors.toList());
		//@formatter:on
	}
	
	/**
	 * Gets all methods annotated with the specified annotation.
	 * @param classLoader the class loader that is used for the process
	 * @param annotationClass
	 * @return a list of methods
	 * @throws IOException
	 */
	public static List<Method> getAllMethodsAnnotatedWith(ClassLoader classLoader, Class<? extends Annotation> annotationClass) throws IOException
	{
		final ClassPath classPath = getClassPath(classLoader);
		//@formatter:off
		return classPath.getTopLevelClasses()
			.stream()
			.map(ClassPathUtil::loadClass)
			.filter(Objects::nonNull)
			.flatMap(clazz -> Arrays.stream(clazz.getDeclaredMethods()))
			.filter(method -> method.isAnnotationPresent(annotationClass))
			.collect(Collectors.toList());
		//@formatter:on		
	}
	
	/**
	 * Gets all methods inside the package annotated with the specified annotation.
	 * @param classLoader the class loader that is used for the process
	 * @param packageName the name of the package
	 * @param annotationClass the annotation you seek
	 * @return a list of methods
	 * @throws IOException
	 */
	public static List<Method> getAllMethodsAnnotatedWith(ClassLoader classLoader, String packageName, Class<? extends Annotation> annotationClass) throws IOException
	{
		final ClassPath classPath = getClassPath(classLoader);
		//@formatter:off
		return classPath.getTopLevelClassesRecursive(packageName)
			.stream()
			.map(ClassPathUtil::loadClass)
			.filter(Objects::nonNull)
			.flatMap(clazz -> Arrays.stream(clazz.getDeclaredMethods()))
			.filter(method -> method.isAnnotationPresent(annotationClass))
			.collect(Collectors.toList());
		//@formatter:on
	}
	
	/**
	 * Loads the class inside {@link ClassInfo}
	 * @param info the class info
	 * @return the loaded class
	 */
	private static Class<?> loadClass(ClassInfo info)
	{
		try
		{
			return info.load();
		}
		catch (NoClassDefFoundError e)
		{
			// ignore
		}
		
		return null;
	}
	
	/**
	 * Gets all methods inside the package.
	 * @param classLoader the class loader that is used for the process
	 * @param packageName the name of the package
	 * @return a stream of methods
	 * @throws IOException
	 */
	public static Stream<Method> getAllMethods(ClassLoader classLoader, String packageName) throws IOException
	{
		final ClassPath classPath = getClassPath(classLoader);
		//@formatter:off
		return classPath.getTopLevelClassesRecursive(packageName)
			.stream()
			.map(ClassPathUtil::loadClass)
			.filter(Objects::nonNull)
			.flatMap(clazz -> Arrays.stream(clazz.getDeclaredMethods()));
		//@formatter:on
	}
}
