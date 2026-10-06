/*
 * Copyright (c) 2026, Bob Tabrizi
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

/**
 * Compact relative times for the price refresh status, e.g. "5m ago" or "next in 12h".
 */
public final class TimeFormat
{
	private static final long MINUTE = TimeUnit.MINUTES.toMillis(1);
	private static final long SECOND = TimeUnit.SECONDS.toMillis(1);

	private TimeFormat()
	{
	}

	/**
	 * @param elapsedMillis how long ago something happened
	 * @return "just now", "5m ago", "3h ago" or "2d ago", rounded down
	 */
	public static String formatAgo(long elapsedMillis)
	{
		long minutes = elapsedMillis / MINUTE;
		if (minutes < 1)
		{
			return "just now";
		}
		if (minutes < 60)
		{
			return minutes + "m ago";
		}
		long hours = minutes / 60;
		if (hours < 24)
		{
			return hours + "h ago";
		}
		return hours / 24 + "d ago";
	}

	/**
	 * @param remainingMillis time left until something happens
	 * @return minutes rounded up below an hour ("1m" to "59m"), otherwise the nearest hour ("12h")
	 */
	public static String formatUntil(long remainingMillis)
	{
		long minutes = Math.max(1, (remainingMillis + MINUTE - 1) / MINUTE);
		if (minutes < 60)
		{
			return minutes + "m";
		}
		return Math.round(minutes / 60.0) + "h";
	}

	/**
	 * @param remainingMillis a short wait, such as a cooldown
	 * @return whole seconds rounded up, e.g. "45s"
	 */
	public static String formatSeconds(long remainingMillis)
	{
		return Math.max(1, (remainingMillis + SECOND - 1) / SECOND) + "s";
	}
}
