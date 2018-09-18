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
package com.github.lordrex34.config;

import com.github.lordrex34.config.util.TimeUtil;
import org.junit.Test;

import java.time.Duration;
import java.time.temporal.ChronoUnit;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;

/**
 * @author Nik
 */
public class TestTimeUtil extends AbstractConfigTest
{
	private static final Map<String, Duration> TEST_VALUES = new HashMap<>();
	private static final Set<String> TEST_EXCEPTIONS = new HashSet<>();
	static
	{
		TEST_VALUES.put("10nanos", Duration.of(10, ChronoUnit.NANOS));
		TEST_VALUES.put("10micros", Duration.of(10, ChronoUnit.MICROS));
		TEST_VALUES.put("10millis", Duration.ofMillis(10));
		TEST_VALUES.put("1sec", Duration.ofSeconds(1));
		TEST_VALUES.put("10secs", Duration.ofSeconds(10));
		TEST_VALUES.put("1second", Duration.ofSeconds(1));
		TEST_VALUES.put("10seconds", Duration.ofSeconds(10));
		TEST_VALUES.put("1min", Duration.ofMinutes(1));
		TEST_VALUES.put("10mins", Duration.ofMinutes(10));
		TEST_VALUES.put("1minute", Duration.ofMinutes(1));
		TEST_VALUES.put("10minutes", Duration.ofMinutes(10));
		TEST_VALUES.put("1hour", Duration.ofHours(1));
		TEST_VALUES.put("10hours", Duration.ofHours(10));
		TEST_VALUES.put("1halfday", Duration.of(1, ChronoUnit.HALF_DAYS));
		TEST_VALUES.put("10halfdays", Duration.of(10, ChronoUnit.HALF_DAYS));
		TEST_VALUES.put("1day", Duration.ofDays(1));
		TEST_VALUES.put("10days", Duration.ofDays(10));
		TEST_VALUES.put("1week", ChronoUnit.WEEKS.getDuration());
		TEST_VALUES.put("2weeks", ChronoUnit.WEEKS.getDuration().plus(ChronoUnit.WEEKS.getDuration()));

		// Now the trickier ones:

		// Create a megapattern from all previous single-type ones.
		TEST_VALUES.put(TEST_VALUES.keySet().stream().collect(Collectors.joining()), TEST_VALUES.values().stream().reduce(Duration.ZERO, Duration::plus));

		// Create forever representation of duration.
		TEST_VALUES.put("9223372036854775807secs999999999nanos", ChronoUnit.FOREVER.getDuration());

		// Create zero representation of duration.
		TEST_VALUES.put("0nanos", Duration.ZERO);
		TEST_VALUES.put("0micros", Duration.ZERO);
		TEST_VALUES.put("0millis", Duration.ZERO);
		TEST_VALUES.put("0secs", Duration.ZERO);
		TEST_VALUES.put("0mins", Duration.ZERO);
		TEST_VALUES.put("0hours", Duration.ZERO);
		TEST_VALUES.put("0halfdays", Duration.ZERO);
		TEST_VALUES.put("0days", Duration.ZERO);
		TEST_VALUES.put("0weeks", Duration.ZERO);

		// Create custom multi-type pattern.
		TEST_VALUES.put("10hours5minutes", Duration.ofHours(10).plusMinutes(5));
		TEST_VALUES.put("10hours5minutes3seconds", Duration.ofHours(10).plusMinutes(5).plusSeconds(3));
		TEST_VALUES.put("10hours5minutes3seconds500millis", Duration.ofHours(10).plusMinutes(5).plusSeconds(3).plusMillis(500));
		TEST_VALUES.put("8days3hours7minutes2seconds333millis", Duration.ofDays(8).plusHours(3).plusMinutes(7).plusSeconds(2).plusMillis(333));
		TEST_VALUES.put("10weeks8days3hours7minutes2seconds333millis", Duration.ofDays((10 * 7) + 8).plusHours(3).plusMinutes(7).plusSeconds(2).plusMillis(333));

		// Create reverse order (should still work)
		TEST_VALUES.put("333millis2seconds7minutes3hours8days10weeks", Duration.ofDays((10 * 7) + 8).plusHours(3).plusMinutes(7).plusSeconds(2).plusMillis(333));

		// Create random order (should still work)
		TEST_VALUES.put("2seconds8days333millis7minutes10weeks3hours", Duration.ofDays((10 * 7) + 8).plusHours(3).plusMinutes(7).plusSeconds(2).plusMillis(333));

		TEST_EXCEPTIONS.add("-10minutes");
		TEST_EXCEPTIONS.add("10minutes-5seconds");
		TEST_EXCEPTIONS.add("gdbvflordrexnubfasdf");
		TEST_EXCEPTIONS.add("10hourss");
		TEST_EXCEPTIONS.add("5milis");
		TEST_EXCEPTIONS.add("10000000000weeks");
		TEST_EXCEPTIONS.add("10000000000days");
		TEST_EXCEPTIONS.add("10000000000000000000000000000000000000000000000000nanos");
		TEST_EXCEPTIONS.add("10");
		TEST_EXCEPTIONS.add("secs");
	}

	@Test
	public void test()
	{
		assertFalse(TEST_VALUES.isEmpty());

		for (Map.Entry<String, Duration> entry : TEST_VALUES.entrySet())
		{
			final String pattern = entry.getKey();
			final Duration result = TimeUtil.parseDuration(pattern);
			final Duration expectedResult = entry.getValue();

			assertEquals("Failed to parse " + pattern, result, expectedResult);
		}

		// Test default parse as well
		for (Duration duration : TEST_VALUES.values())
		{
			assertEquals(TimeUtil.parseDuration(duration.toString()), duration);
		}
	}

	@Test(expected = IllegalStateException.class)
	public void testExceptions()
	{
		for (String pattern : TEST_EXCEPTIONS)
		{
			TimeUtil.parseDuration(pattern);
		}
	}
	
}
