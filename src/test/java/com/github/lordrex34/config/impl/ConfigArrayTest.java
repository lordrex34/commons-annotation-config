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

import com.github.lordrex34.config.annotation.ConfigClass;
import com.github.lordrex34.config.annotation.ConfigField;

/**
 * @author lord_rex
 */
@ConfigClass(fileName = "array_test")
public class ConfigArrayTest
{
	@ConfigField(name = "TestBooleanArray", value = "true,false,true")
	public static boolean[] TEST_BOOLEAN_ARRAY;
	
	@ConfigField(name = "TestByteArray", value = "1,2,3")
	public static byte[] TEST_BYTE_ARRAY;
	
	@ConfigField(name = "TestShortArray", value = "111,221,443")
	public static short[] TEST_SHORT_ARRAY;
	
	@ConfigField(name = "TestIntArray", value = "1222,4442,9993")
	public static int[] TEST_INT_ARRAY;
	
	@ConfigField(name = "TestLongArray", value = "12252353252352,443252353253242,993252362673293")
	public static long[] TEST_LONG_ARRAY;
	
	@ConfigField(name = "TestFloatArray", value = "1.,3.2,5.")
	public static float[] TEST_FLOAT_ARRAY;
	
	@ConfigField(name = "TestDoubleArray", value = "4.1,2.3,9.7")
	public static double[] TEST_DOUBLE_ARRAY;
	
	@ConfigField(name = "TestStringArray", value = "This,is,a,string,array,test.")
	public static String[] TEST_STRING_ARRAY;
	
	@ConfigField(name = "TestEnumArray", value = "TEST_1,TEST_2")
	public static EnumForConfig[] TEST_ENUM_ARRAY;
}
