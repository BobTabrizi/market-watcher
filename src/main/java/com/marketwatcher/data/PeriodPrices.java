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

import lombok.Value;

/**
 * Average wiki prices for one price period. A null price means no trades were recorded.
 */
@Value
public class PeriodPrices
{
	public static final PeriodPrices EMPTY = new PeriodPrices(null, null, null);

	Long low;
	Long med;
	Long high;

	/**
	 * Builds period prices from the wiki's 6h averages, where 0 means no trades in that window.
	 * Averages are rounded to the nearest gp with halves rounded up.
	 */
	public static PeriodPrices fromWikiAverages(double avgLowPrice, double avgHighPrice)
	{
		// Prices are longs because wiki averages can exceed Integer.MAX_VALUE
		Long low = toWholeGp(avgLowPrice);
		Long high = toWholeGp(avgHighPrice);
		Long med = low != null && high != null ? (low + high) / 2 : null;
		return new PeriodPrices(low, med, high);
	}

	private static Long toWholeGp(double averagePrice)
	{
		long rounded = Math.round(averagePrice);
		return rounded > 0 ? rounded : null;
	}
}
