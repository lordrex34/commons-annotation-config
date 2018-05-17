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
package com.github.lordrex34.config.annotation;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

import com.github.lordrex34.config.converter.IConfigConverter;
import com.github.lordrex34.config.converter.MainConfigConverter;
import com.github.lordrex34.config.postloadhooks.EmptyConfigPostLoadFieldHook;
import com.github.lordrex34.config.postloadhooks.IConfigPostLoadFieldHook;
import com.github.lordrex34.config.supplier.DefaultConfigSupplier;
import com.github.lordrex34.config.supplier.DefaultGeneratedConfigSupplier;
import com.github.lordrex34.config.supplier.IConfigGeneratedValueSupplier;
import com.github.lordrex34.config.supplier.IConfigValueSupplier;

/**
 * @author NB4L1 (original idea)
 * @author lord_rex
 */
@Target(ElementType.FIELD)
@Retention(RetentionPolicy.RUNTIME)
public @interface ConfigField
{
	/**
	 * The property name itself. Also known as property key/or the user-friendly name.
	 * @return name
	 */
	String name();
	
	/**
	 * Here you may specify your own configuration value supplier for your field.
	 * @return the configuration value supplier
	 */
	Class<? extends IConfigValueSupplier<?>> valueSupplier() default DefaultConfigSupplier.class;

	/**
	 * Here you may specify your own configuration generated value supplier for your field.
	 * @return the configuration value supplier
	 */
	Class<? extends IConfigGeneratedValueSupplier<?>> generatedValueSupplier() default DefaultGeneratedConfigSupplier.class;
	
	/**
	 * A default value used both for generation, and in case the property key is missing, it is going to be loaded.
	 * @return value
	 */
	String value();
	
	/**
	 * The comment itself that is provided through annotations.
	 * @return comment
	 */
	String[] comment() default {};
	
	/**
	 * That should return {@code true}, whenever your field is only a comment, and nothing should be loaded, otherwise {@code false}
	 * @return real property or just a comment?
	 */
	boolean onlyComment() default false;
	
	/**
	 * Some of the configurations must not be reloaded, if this is the case set it to {@code false}.
	 * @return is reloadable or not?
	 */
	boolean reloadable() default true;
	
	/**
	 * The converter grants you the possibility to convert your configuration into a list, set, array or anything else.<br>
	 * Please see {@code com.github.lordrex34.config.converter} package for further details.
	 * @return the converter
	 */
	Class<? extends IConfigConverter> converter() default MainConfigConverter.class;
	
	/**
	 * Can be used to assign post-load events into a specific field.
	 * @return the post load hook
	 */
	Class<? extends IConfigPostLoadFieldHook> postLoadHook() default EmptyConfigPostLoadFieldHook.class;
}
