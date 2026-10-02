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

import static com.marketwatcher.utilities.Constants.COMPACT;
import static com.marketwatcher.utilities.Constants.STANDARD;
import static org.junit.Assert.assertEquals;
import org.junit.Test;

public class PriceUtilsTest
{
	// Padding characters used to align the price columns
	private static final String BLANK = "⠀";
	private static final String NARROW = " ";

	@Test
	public void formatsUnavailablePrice()
	{
		assertEquals("N/A" + BLANK + NARROW + NARROW + NARROW, PriceUtils.formatPrice(null, STANDARD));
		assertEquals("N/A" + BLANK + NARROW + NARROW, PriceUtils.formatPrice(null, COMPACT));
	}

	@Test
	public void formatsSmallPricesUnabbreviated()
	{
		assertEquals("5" + BLANK + BLANK + BLANK + NARROW + NARROW, PriceUtils.formatPrice(5, STANDARD));
		assertEquals("5" + BLANK + BLANK + BLANK, PriceUtils.formatPrice(5, COMPACT));
		assertEquals("42" + BLANK + BLANK + NARROW, PriceUtils.formatPrice(42, COMPACT));
		assertEquals("999" + BLANK + NARROW + NARROW + NARROW, PriceUtils.formatPrice(999, STANDARD));
		assertEquals("1000" + BLANK + NARROW, PriceUtils.formatPrice(1000, STANDARD));
		assertEquals("9999" + NARROW + NARROW + NARROW, PriceUtils.formatPrice(9999, COMPACT));
	}

	@Test
	public void abbreviatesThousands()
	{
		assertEquals("12.3K" + BLANK, PriceUtils.formatPrice(12345, STANDARD));
		assertEquals("12.3K" + NARROW + NARROW, PriceUtils.formatPrice(12345, COMPACT));
		assertEquals("100.0K" + NARROW, PriceUtils.formatPrice(100000, STANDARD));
		assertEquals("100.0K", PriceUtils.formatPrice(100000, COMPACT));
	}

	@Test
	public void abbreviatesMillionsAndBillions()
	{
		assertEquals("1.0M" + BLANK + NARROW + NARROW, PriceUtils.formatPrice(1000000, STANDARD));
		assertEquals("1.0M" + BLANK, PriceUtils.formatPrice(1000000, COMPACT));
		assertEquals("123.5M" + NARROW, PriceUtils.formatPrice(123456789, STANDARD));
		assertEquals("1.50B" + BLANK + NARROW, PriceUtils.formatPrice(1500000000, STANDARD));
		assertEquals("2.15B" + NARROW + NARROW, PriceUtils.formatPrice(Integer.MAX_VALUE, COMPACT));
	}

	@Test
	public void roundsUpIntoNextUnit()
	{
		// Previously shown as 1000.0K and 1000.0M
		assertEquals("1.0M", PriceUtils.abbreviatePrice(999999));
		assertEquals("1.0M", PriceUtils.abbreviatePrice(999950));
		assertEquals("999.9K", PriceUtils.abbreviatePrice(999949));
		assertEquals("1.00B", PriceUtils.abbreviatePrice(999999999));
	}
}
