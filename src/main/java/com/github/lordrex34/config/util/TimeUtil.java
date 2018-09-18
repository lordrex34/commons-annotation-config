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
					result = parseSingleUnitDuration(matcher, result);
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
	 * Parses a single unit of duration, following the pattern at {@link #parseDuration(String)}
	 * @param matcher the matcher result of the pattern
	 * @param durationToAdd the amount of duration to which the result will be added or {@code null} to return result as is.
	 * @return the resulting duration of the pattern parsing, or the sum of the result with the given duration to add.
	 */
	public static Duration parseSingleUnitDuration(Matcher matcher, Duration durationToAdd)
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

		return durationToAdd == null ? Duration.of(value, unit) : durationToAdd.plus(value, unit);
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
		if (toDaysPart(duration) > 1)
		{
			result += toDaysPart(duration) + "days";
		}
		if (toDaysPart(duration) == 1)
		{
			result += toDaysPart(duration) + "day";
		}
		if (toHoursPart(duration) > 1)
		{
			result += toHoursPart(duration) + "hours";
		}
		if (toHoursPart(duration) == 1)
		{
			result += toHoursPart(duration) + "hour";
		}
		if (toMinutesPart(duration) > 1)
		{
			result += toMinutesPart(duration) + "mins";
		}
		if (toMinutesPart(duration) == 1)
		{
			result += toMinutesPart(duration) + "min";
		}
		if (toSecondsPart(duration) > 1)
		{
			result += toSecondsPart(duration) + "secs";
		}
		if (toSecondsPart(duration) == 1)
		{
			result += toSecondsPart(duration) + "sec";
		}
		if (toMillisPart(duration) >= 1)
		{
			result += toMillisPart(duration) + "millis";
		}
		// I don't know why toNanosPart returns nanoseconds unmodified by milliseconds mod.
		if (duration.getNano() % ChronoUnit.MILLIS.getDuration().getNano() >= 1)
		{
			result += (duration.getNano() % ChronoUnit.MILLIS.getDuration().getNano()) + "nanos";
		}

		return result;
	}

	/** Clone of java 9 {@code duration.toDaysPart()} to allow support until we update to above java 8. */
	private static long toDaysPart(Duration duration)
	{
		return duration.getSeconds() / 86400;
	}

	/** Clone of java 9 {@code duration.toHoursPart()} to allow support until we update to above java 8. */
	private static int toHoursPart(Duration duration)
	{
		return (int) (duration.toHours() % 24);
	}

	/** Clone of java 9 {@code duration.toMinutesPart()} to allow support until we update to above java 8. */
	private static int toMinutesPart(Duration duration)
	{
		return (int) (duration.toMinutes() % 60);
	}

	/** Clone of java 9 {@code duration.toSecondsPart()} to allow support until we update to above java 8. */
	private static int toSecondsPart(Duration duration)
	{
		return (int) (duration.getSeconds() % 60);
	}

	/** Clone of java 9 {@code duration.toMillisPart()} to allow support until we update to above java 8. */
	private static int toMillisPart(Duration duration)
	{
		return duration.getNano() / 1000_000;
	}
}
