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
package com.marketwatcher.data;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;
import org.junit.Test;

public class PeriodPricesTest
{
	@Test
	public void computesMedianOfLowAndHigh()
	{
		PeriodPrices prices = PeriodPrices.fromWikiAverages(100, 201);
		assertEquals(Long.valueOf(100), prices.getLow());
		assertEquals(Long.valueOf(150), prices.getMed());
		assertEquals(Long.valueOf(201), prices.getHigh());
	}

	@Test
	public void roundsDecimalAveragesHalfUp()
	{
		// The v2 API returns averages with up to two decimals; v1 returned them rounded half up
		PeriodPrices prices = PeriodPrices.fromWikiAverages(807703.12, 825682.74);
		assertEquals(Long.valueOf(807703), prices.getLow());
		assertEquals(Long.valueOf(825683), prices.getHigh());
		assertEquals(Long.valueOf(816693), prices.getMed());

		assertEquals(Long.valueOf(11095), PeriodPrices.fromWikiAverages(11094.5, 11094.5).getLow());
	}

	@Test
	public void treatsZeroAsUnavailable()
	{
		PeriodPrices prices = PeriodPrices.fromWikiAverages(0, 500);
		assertNull(prices.getLow());
		assertNull(prices.getMed());
		assertEquals(Long.valueOf(500), prices.getHigh());
	}

	@Test
	public void handlesPricesAboveIntRange()
	{
		// The wiki has returned averages above Integer.MAX_VALUE, which used to fail the whole response
		PeriodPrices prices = PeriodPrices.fromWikiAverages(2200000000L, 2290000000L);
		assertEquals(Long.valueOf(2245000000L), prices.getMed());
	}
}
