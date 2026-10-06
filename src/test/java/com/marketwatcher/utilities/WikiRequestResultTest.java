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

import com.google.gson.Gson;
import com.marketwatcher.data.PeriodPrices;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;
import org.junit.Test;

public class WikiRequestResultTest
{
	// Shaped like a real v2 /6h response: decimal averages, a missing low price, and prices above Integer.MAX_VALUE
	private static final String V2_RESPONSE = "{\"data\":{"
		+ "\"4151\":{\"avgHighPrice\":825682.74,\"highPriceVolume\":685,\"avgLowPrice\":807703.12,\"lowPriceVolume\":627},"
		+ "\"1781\":{\"avgHighPrice\":28.5,\"highPriceVolume\":120,\"avgLowPrice\":null,\"lowPriceVolume\":0},"
		+ "\"20997\":{\"avgHighPrice\":2290000000.25,\"highPriceVolume\":2,\"avgLowPrice\":2200000000,\"lowPriceVolume\":1}"
		+ "},\"timestamp\":1791158400}";

	private static PeriodPrices pricesFor(WikiRequestResult result, int itemId)
	{
		WikiItemDetails details = result.getData().get(itemId);
		return PeriodPrices.fromWikiAverages(details.getAvgLowPrice(), details.getAvgHighPrice());
	}

	@Test
	public void parsesV2Response()
	{
		WikiRequestResult result = new Gson().fromJson(V2_RESPONSE, WikiRequestResult.class);
		assertEquals(3, result.getData().size());

		PeriodPrices whip = pricesFor(result, 4151);
		assertEquals(Long.valueOf(807703), whip.getLow());
		assertEquals(Long.valueOf(816693), whip.getMed());
		assertEquals(Long.valueOf(825683), whip.getHigh());

		PeriodPrices oneSided = pricesFor(result, 1781);
		assertNull(oneSided.getLow());
		assertNull(oneSided.getMed());
		assertEquals(Long.valueOf(29), oneSided.getHigh());

		PeriodPrices expensive = pricesFor(result, 20997);
		assertEquals(Long.valueOf(2200000000L), expensive.getLow());
		assertEquals(Long.valueOf(2290000000L), expensive.getHigh());
	}
}
