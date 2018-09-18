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
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * @author UnAfraid, Nik
 */
public class TimeUtil
{
	/** Pattern that matches texts similar to "1hour30min20sec" into multiple groups of digit and non-digit parts. */
	public static final Pattern PARSE_DURATION_PATTERN = Pattern.compile("(\\d+)([^\\d]+)");
	
	/**
	 * Parses patterns like:
	 * <ul>
	 * <li>1min or 10mins</li>
	 * <li>1day or 10days</li>
	 * <li>1week or 4weeks</li>
	 * <li>1month or 12months</li>
	 * <li>1year or 5years</li>
	 * </ul>
	 * Also multiple value types in pattern are supported as well:
	 * <ul>
	 * <li>1hour30min20sec</li>
	 * <li>3days22hours20mins</li>
	 * <li>1week5days6hours</li>
	 * <li>10hours20mins30secs500millis200micros100nanos</li>
	 * </ul>
	 * Supported time units:
	 * <ul>
	 * <li>nanos</li>
	 * <li>micros</li>
	 * <li>millis</li>
	 * <li>sec</li>
	 * <li>secs</li>
	 * <li>min</li>
	 * <li>mins</li>
	 * <li>hour</li>
	 * <li>hours</li>
	 * <li>day</li>
	 * <li>days</li>
	 * <li>week</li>
	 * <li>weeks</li>
	 * <li>month</li>
	 * <li>months</li>
	 * <li>year</li>
	 * <li>years</li>
	 * <li>All enum names of {@link ChronoUnit} by auto-converting input time unit to upper case before matching.</li>
	 * </ul>
	 * @param datePattern
	 * @return {@link Duration} object converted by the date pattern specified.
	 * @throws IllegalStateException when malformed pattern specified.
	 */
	public static Duration parseDuration(String datePattern)
	{
		try
		{
			Duration result = null;
			final Matcher matcher = PARSE_DURATION_PATTERN.matcher(datePattern);
			while (matcher.find())
			{
				final long value = Long.parseLong(matcher.group(1));
				final String type = matcher.group(2);
				final ChronoUnit unit;
				switch (type.toLowerCase())
				{
					case "nanos":
					{
						unit = ChronoUnit.NANOS;
						break;
					}
					case "micros":
					{
						unit = ChronoUnit.MICROS;
						break;
					}
					case "millis":
					{
						unit = ChronoUnit.MILLIS;
						break;
					}
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
						unit = ChronoUnit.valueOf(type.toUpperCase());
						if (unit == null)
						{
							throw new IllegalArgumentException("Incorrect unit of time: " + type + "!");
						}
					}
				}

				result = result == null ? Duration.of(value, unit) : result.plus(value, unit);
			}

			if (result == null)
			{
				throw new IllegalStateException("Time format has failed to produce results!");
			}

			return result;
		}
		catch (Exception e)
		{
			throw new IllegalStateException("Incorrect time format given: " + datePattern + "!", e);
		}
	}
}
