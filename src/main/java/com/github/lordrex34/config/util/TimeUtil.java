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

import java.time.Duration;
import java.time.temporal.ChronoUnit;

/**
 * @author UnAfraid
 */
public class TimeUtil
{
	public static int findIndexOfNonDigit(CharSequence text)
	{
		for (int i = 0; i < text.length(); i++)
		{
			if (Character.isDigit(text.charAt(i)))
			{
				continue;
			}
			return i;
		}
		return -1;
	}
	
	/**
	 * Parses patterns like:
	 * <ul>
	 * <li>1min or 10mins</li>
	 * <li>1day or 10days</li>
	 * <li>1week or 4weeks</li>
	 * <li>1month or 12months</li>
	 * <li>1year or 5years</li>
	 * </ul>
	 * @param datePattern
	 * @return {@link Duration} object converted by the date pattern specified.
	 * @throws IllegalStateException when malformed pattern specified.
	 */
	public static Duration parseDuration(String datePattern)
	{
		final int index = findIndexOfNonDigit(datePattern);
		if (index == -1)
		{
			throw new IllegalStateException("Incorrect time format given: " + datePattern);
		}
		try
		{
			int val = Integer.parseInt(datePattern.substring(0, index));
			final String type = datePattern.substring(index);
			final ChronoUnit unit;
			switch (type.toLowerCase())
			{
				case "sec":
				case "secs":
				{
					unit = ChronoUnit.SECONDS;
					break;
				}
				case "min":
				case "mins":
				{
					unit = ChronoUnit.MINUTES;
					break;
				}
				case "hour":
				case "hours":
				{
					unit = ChronoUnit.HOURS;
					break;
				}
				case "day":
				case "days":
				{
					unit = ChronoUnit.DAYS;
					break;
				}
				case "week":
				case "weeks":
				{
					unit = ChronoUnit.WEEKS;
					break;
				}
				case "month":
				case "months":
				{
					unit = ChronoUnit.MONTHS;
					break;
				}
				case "year":
				case "years":
				{
					unit = ChronoUnit.YEARS;
					break;
				}
				default:
				{
					unit = ChronoUnit.valueOf(type);
					if (unit == null)
					{
						throw new IllegalStateException("Incorrect format: " + type + " !!");
					}
				}
			}
			return Duration.of(val, unit);
		}
		catch (Exception e)
		{
			throw new IllegalStateException("Incorrect time format given: " + datePattern + " val: " + datePattern.substring(0, index));
		}
	}
}
