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
package com.github.lordrex34.config.impl;

import java.util.List;
import java.util.Set;

import com.github.lordrex34.config.annotation.ConfigClass;
import com.github.lordrex34.config.annotation.ConfigField;

/**
 * @author lord_rex
 */
@ConfigClass(fileName = "test")
public class ConfigTest
{
	@ConfigField(name = "TestBoolean", value = "true")
	public static boolean TEST_BOOLEAN;
	
	@ConfigField(name = "TestInt", value = "1234")
	public static int TEST_INT;
	
	@ConfigField(name = "TestFloat", value = "1234.")
	public static float TEST_FLOAT;
	
	@ConfigField(name = "TestDouble", value = "1234.14")
	public static double TEST_DOUBLE;
	
	@ConfigField(name = "TestString", value = "Any string is good here.")
	public static String TEST_STRING;
	
	@ConfigField(name = "TestIntArray", value = "1,2,3,4,5")
	public static int[] TEST_INT_ARRAY;
	
	@ConfigField(name = "TestStringList", value = "apple,orange,banana")
	public static List<String> TEST_STRING_LIST;
	
	@ConfigField(name = "TestDoubleSet", value = "1.1,4.2,1.3,3.4,2.5")
	public static Set<Double> TEST_DOUBLE_SET;
}
