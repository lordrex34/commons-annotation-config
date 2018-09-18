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
import java.time.format.DateTimeParseException;
import java.time.temporal.ChronoUnit;
import java.util.Objects;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * @author Nik
 */
public final class TimeUtil
{
	private static class Lazy
	{
		/**
		 * Pattern that matches texts similar to "1hour30min20sec" into multiple groups of digit and non-digit parts.
		 */
		static final Pattern PARSE_DURATION_PATTERN = Pattern.compile("(\\d+)([^\\d]+)");
	}

	private TimeUtil()
	{
		// Utility class
	}

	/**
	 * Parses patterns like:
	 * <ul>
	 * <li>1millis or 10millis</li>
	 * <li>1sec or 10secs</li>
	 * <li>1min or 10mins</li>
	 * <li>1day or 10days</li>
	 * <li>1week or 4weeks</li>
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
	 * <li>second</li>
	 * <li>seconds</li>
	 * <li>min</li>
	 * <li>mins</li>
	 * <li>minute</li>
	 * <li>minutes</li>
	 * <li>hour</li>
	 * <li>hours</li>
	 * <li>halfday</li>
	 * <li>halfdays</li>
	 * <li>day</li>
	 * <li>days</li>
	 * <li>week</li>
	 * <li>weeks</li>
	 * </ul>
	 * Values such as months or years and everything above are not supported, because they are estimate values instead of precise ones.<br>
	 * For example, one month can either be 30 or 31 days or one year can either be 365 or 366 days. A decade consists of 10 years with estimated amount of days.<br>
	 * <br>
	 * This method also supports the default duration pattern that is parsed by {@link Duration#parse(CharSequence)}
	 * @param pattern the pattern of duration to be parsed.
	 * @return {@link Duration} object converted by the date pattern specified.
	 * @throws IllegalStateException when malformed pattern specified.
	 */
	public static Duration parseDuration(String pattern)
	{
		try
		{
			return Duration.parse(pattern);
		}
		catch (DateTimeParseException dtpe) // Intentional consequence when we want to parse our own variant of duration pattern.
		{
			try
			{
				Duration result = null;
				final Matcher matcher = Lazy.PARSE_DURATION_PATTERN.matcher(pattern);
				while (matcher.find())
				{
					long value = Long.parseLong(matcher.group(1));
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
						case "second":
						case "seconds":
						{
							unit = ChronoUnit.SECONDS;
							break;
						}
						case "min":
						case "mins":
						case "minutes":
						case "minute":
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
						case "halfday":
						case "halfdays":
						{
							unit = ChronoUnit.HALF_DAYS;
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
							value = ChronoUnit.WEEKS.getDuration().multipliedBy(value).toDays();
							unit = ChronoUnit.DAYS;
							break;
						}
						default:
						{
							throw new IllegalArgumentException("Incorrect or unsupported time unit type: " + type);
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
				throw new IllegalStateException("Incorrect time format given: " + pattern + "!", e);
			}
		}
	}

	/**
	 * Converts the given duration to a format that can be parsed by {@link #parseDuration(String)}.<br>
	 * The util format of duration present in {@link #parseDuration(String)} is preferred over the default {@link Duration#parse(CharSequence)}<br>
	 * Only negative duration values will be converted to the default {@link Duration#parse(CharSequence)} pattern.
	 * @param duration the duration which will be converted to string.
	 * @return a string representation of the given duration, which can then be used to recreate the same duration via {@link #parseDuration(String)}
	 */
	public static String durationToString(Duration duration)
	{
		Objects.requireNonNull(duration);

		if (duration.isNegative())
		{
			return duration.toString();
		}

		if (duration.isZero())
		{
			return "0secs";
		}

		String result = "";
		if (duration.toDaysPart() > 1)
		{
			result += duration.toDaysPart() + "days";
		}
		if (duration.toDaysPart() == 1)
		{
			result += duration.toDaysPart() + "day";
		}
		if (duration.toHoursPart() > 1)
		{
			result += duration.toHoursPart() + "hours";
		}
		if (duration.toHoursPart() == 1)
		{
			result += duration.toHoursPart() + "hour";
		}
		if (duration.toMinutesPart() > 1)
		{
			result += duration.toMinutesPart() + "mins";
		}
		if (duration.toMinutesPart() == 1)
		{
			result += duration.toMinutesPart() + "min";
		}
		if (duration.toSecondsPart() > 1)
		{
			result += duration.toSecondsPart() + "secs";
		}
		if (duration.toSecondsPart() == 1)
		{
			result += duration.toSecondsPart() + "sec";
		}
		if (duration.toMillisPart() >= 1)
		{
			result += duration.toMillisPart() + "millis";
		}
		// I don't know why toNanosPart returns nanoseconds unmodified by milliseconds mod.
		if (duration.toNanosPart() % ChronoUnit.MILLIS.getDuration().getNano() >= 1)
		{
			result += (duration.toNanosPart() % ChronoUnit.MILLIS.getDuration().getNano()) + "nanos";
		}

		return result;
	}
}
