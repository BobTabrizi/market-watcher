/*
 * Copyright (c) 2023, Bob Tabrizi
 * All rights reserved.
 *
 * Redistribution and use in source and binary forms, with or without
 * modification, are permitted provided that the following conditions are met:
 *
 * 1. Redistributions of source code must retain the above copyright notice, this
 *    list of conditions and the following disclaimer.
 * 2. Redistributions in binary form must reproduce the above copyright notice,
 *    this list of conditions and the following disclaimer in the documentation
 *    and/or other materials provided with the distribution.
 *
 * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS IS" AND
 * ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE IMPLIED
 * WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE ARE
 * DISCLAIMED. IN NO EVENT SHALL THE COPYRIGHT OWNER OR CONTRIBUTORS BE LIABLE FOR
 * ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES
 * (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES;
 * LOSS OF USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND
 * ON ANY THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT
 * (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE OF THIS
 * SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
 */
package com.marketwatcher.utilities;

import java.util.concurrent.TimeUnit;
import static org.junit.Assert.assertEquals;
import org.junit.Test;

public class TimeFormatTest
{
	private static long minutes(long m)
	{
		return TimeUnit.MINUTES.toMillis(m);
	}

	@Test
	public void formatsTimeAgo()
	{
		assertEquals("just now", TimeFormat.formatAgo(0));
		assertEquals("just now", TimeFormat.formatAgo(TimeUnit.SECONDS.toMillis(59)));
		assertEquals("1m ago", TimeFormat.formatAgo(minutes(1)));
		assertEquals("59m ago", TimeFormat.formatAgo(minutes(59)));
		assertEquals("1h ago", TimeFormat.formatAgo(minutes(119)));
		assertEquals("23h ago", TimeFormat.formatAgo(minutes(24 * 60 - 1)));
		assertEquals("2d ago", TimeFormat.formatAgo(TimeUnit.DAYS.toMillis(2)));
	}

	@Test
	public void formatsTimeUntil()
	{
		assertEquals("1m", TimeFormat.formatUntil(0));
		assertEquals("1m", TimeFormat.formatUntil(1));
		assertEquals("5m", TimeFormat.formatUntil(minutes(4) + 1));
		assertEquals("59m", TimeFormat.formatUntil(minutes(59)));
		assertEquals("1h", TimeFormat.formatUntil(minutes(60)));
		assertEquals("1h", TimeFormat.formatUntil(minutes(89)));
		assertEquals("2h", TimeFormat.formatUntil(minutes(91)));
		assertEquals("12h", TimeFormat.formatUntil(minutes(11 * 60 + 55)));
	}

	@Test
	public void formatsSecondsRoundedUp()
	{
		assertEquals("1s", TimeFormat.formatSeconds(0));
		assertEquals("45s", TimeFormat.formatSeconds(TimeUnit.SECONDS.toMillis(44) + 1));
		assertEquals("60s", TimeFormat.formatSeconds(TimeUnit.SECONDS.toMillis(60)));
	}
}
