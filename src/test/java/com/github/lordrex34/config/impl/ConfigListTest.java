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

import com.github.lordrex34.config.annotation.ConfigClass;
import com.github.lordrex34.config.annotation.ConfigField;

/**
 * @author lord_rex
 */
@ConfigClass(fileName = "list_test")
public class ConfigListTest
{
	@ConfigField(name = "TestBooleanList", value = "true,false,true")
	public static List<Boolean> TEST_BOOLEAN_LIST;
	
	@ConfigField(name = "TestByteList", value = "1,2,3")
	public static List<Byte> TEST_BYTE_LIST;
	
	@ConfigField(name = "TestShortList", value = "111,221,443")
	public static List<Short> TEST_SHORT_LIST;
	
	@ConfigField(name = "TestIntList", value = "1222,4442,9993")
	public static List<Integer> TEST_INT_LIST;
	
	@ConfigField(name = "TestLongList", value = "12252353252352,443252353253242,993252362673293")
	public static List<Long> TEST_LONG_LIST;
	
	@ConfigField(name = "TestFloatList", value = "1.,3.2,5.")
	public static List<Float> TEST_FLOAT_LIST;
	
	@ConfigField(name = "TestDoubleList", value = "4.1,2.3,9.7")
	public static List<Double> TEST_DOUBLE_LIST;
	
	@ConfigField(name = "TestStringList", value = "This,is,a,string,array,test.")
	public static List<String> TEST_STRING_LIST;
	
	@ConfigField(name = "TestEnumList", value = "TEST_1,TEST_2")
	public static List<EnumForConfig> TEST_ENUM_LIST;
}
