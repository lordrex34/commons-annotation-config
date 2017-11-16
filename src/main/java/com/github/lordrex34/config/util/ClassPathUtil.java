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
import java.lang.reflect.Method;
import java.util.Arrays;
import java.util.Objects;
import java.util.concurrent.ConcurrentMap;

import com.google.common.collect.FluentIterable;
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
	private static final ConcurrentMap<ClassLoader, ClassPath> CLASS_PATH_BY_CLASS_LOADER_MAP = Maps.newConcurrentMap();
	
	private static ClassPath getClassPath(ClassLoader classLoader) throws IOException
	{
		try
		{
			return CLASS_PATH_BY_CLASS_LOADER_MAP.computeIfAbsent(classLoader, cl ->
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
	 * Gets all classes.
	 * @param classLoader the class loader that is used for the process
	 * @param packagePrefix the package where you seek
	 * @return a list of classes
	 * @throws IOException
	 */
	public static FluentIterable<Class<?>> getAllClasses(ClassLoader classLoader, String packagePrefix) throws IOException
	{
		final ClassPath classPath = getClassPath(classLoader);
		//@formatter:off
		return FluentIterable.from(classPath.getResources())
			.filter(ClassInfo.class)
			.filter(classInfo -> classInfo.getName().startsWith(packagePrefix))
			.transform(ClassPathUtil::loadClass)
			.filter(Objects::nonNull)
			.transform(clazz -> (Class<?>) clazz);
		//@formatter:on
	}
	
	/**
	 * Same as {@link #getAllClasses(ClassLoader, String)}, using {@link ClassLoader#getSystemClassLoader()} as the classLoader parameter.
	 * @param packagePrefix
	 * @return
	 * @throws IOException
	 */
	public static FluentIterable<Class<?>> getAllClasses(String packagePrefix) throws IOException
	{
		return getAllClasses(ClassLoader.getSystemClassLoader(), packagePrefix);
	}
	
	/**
	 * Gets all classes extending the given target class.
	 * @param <T>
	 * @param classLoader the class loader that is used for the process
	 * @param packagePrefix the package where you seek
	 * @param targetClass the given target class
	 * @return a list of classes
	 * @throws IOException
	 */
	public static <T> FluentIterable<Class<? extends T>> getAllClassesExtending(ClassLoader classLoader, String packagePrefix, Class<T> targetClass) throws IOException
	{
		//@formatter:off
		return getAllClasses(classLoader, packagePrefix)
			.filter(targetClass::isAssignableFrom)
			.transform(clazz -> (Class<? extends T>) clazz);
		//@formatter:on
	}
	
	/**
	 * Same as {@link #getAllClassesExtending(ClassLoader, String, Class)}, using {@link ClassLoader#getSystemClassLoader()} as the classLoader parameter.
	 * @param packagePrefix
	 * @param targetClass
	 * @param <T>
	 * @return
	 * @throws IOException
	 */
	public static <T> FluentIterable<Class<? extends T>> getAllClassesExtending(String packagePrefix, Class<T> targetClass) throws IOException
	{
		return getAllClassesExtending(ClassLoader.getSystemClassLoader(), packagePrefix, targetClass);
	}
	
	/**
	 * Gets all classes extending the given target class.
	 * @param classLoader the class loader that is used for the process
	 * @param packagePrefix the package where you seek
	 * @param annotationClass the given annotation class
	 * @return a list of classes
	 * @throws IOException
	 */
	public static FluentIterable<Class<?>> getAllClassesAnnotatedWith(ClassLoader classLoader, String packagePrefix, Class<? extends Annotation> annotationClass) throws IOException
	{
		//@formatter:off
		return getAllClasses(classLoader, packagePrefix)
			.filter(clazz -> clazz.isAnnotationPresent(annotationClass));
		//@formatter:on
	}
	
	/**
	 * Same as {@link #getAllClassesAnnotatedWith(ClassLoader, String, Class)}, using {@link ClassLoader#getSystemClassLoader()} as the classLoader parameter.
	 * @param packagePrefix
	 * @param annotationClass
	 * @return
	 * @throws IOException
	 */
	public static FluentIterable<Class<?>> getAllClassesAnnotatedWith(String packagePrefix, Class<? extends Annotation> annotationClass) throws IOException
	{
		return getAllClassesAnnotatedWith(ClassLoader.getSystemClassLoader(), packagePrefix, annotationClass);
	}
	
	/**
	 * Gets all methods inside the package.
	 * @param classLoader the class loader that is used for the process
	 * @param packagePrefix the name of the package
	 * @return a stream of methods
	 * @throws IOException
	 */
	public static FluentIterable<Method> getAllMethods(ClassLoader classLoader, String packagePrefix) throws IOException
	{
		//@formatter:off
		return getAllClasses(classLoader, packagePrefix)
			.transformAndConcat(clazz -> Arrays.asList(clazz.getDeclaredMethods()));
		//@formatter:on
	}
	
	/**
	 * Same as {@link #getAllMethods(ClassLoader, String)}, using {@link ClassLoader#getSystemClassLoader()} as the classLoader parameter.
	 * @param packagePrefix
	 * @return
	 * @throws IOException
	 */
	public static FluentIterable<Method> getAllMethods(String packagePrefix) throws IOException
	{
		return getAllMethods(ClassLoader.getSystemClassLoader(), packagePrefix);
	}
	
	/**
	 * Gets all methods inside the package annotated with the specified annotation.
	 * @param classLoader the class loader that is used for the process
	 * @param packagePrefix the name of the package
	 * @param annotationClass the annotation you seek
	 * @return a list of methods
	 * @throws IOException
	 */
	public static FluentIterable<Method> getAllMethodsAnnotatedWith(ClassLoader classLoader, String packagePrefix, Class<? extends Annotation> annotationClass) throws IOException
	{
		//@formatter:off
		return getAllMethods(classLoader, packagePrefix)
			.filter(method -> method.isAnnotationPresent(annotationClass));
		//@formatter:on
	}
	
	/**
	 * Same as {@link #getAllMethodsAnnotatedWith(ClassLoader, String, Class)}, using {@link ClassLoader#getSystemClassLoader()} as the classLoader parameter.
	 * @param packagePrefix
	 * @param annotationClass
	 * @return
	 * @throws IOException
	 */
	public static FluentIterable<Method> getAllMethodsAnnotatedWith(String packagePrefix, Class<? extends Annotation> annotationClass) throws IOException
	{
		return getAllMethodsAnnotatedWith(ClassLoader.getSystemClassLoader(), packagePrefix, annotationClass);
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
	
}
