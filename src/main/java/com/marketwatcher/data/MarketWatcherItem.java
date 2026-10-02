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
package com.marketwatcher.data;

import java.util.List;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;

import net.runelite.client.util.AsyncBufferedImage;

@AllArgsConstructor
public class MarketWatcherItem implements Comparable<MarketWatcherItem>
{
	@Getter
	private AsyncBufferedImage image;

	@Getter
	private String name;

	@Getter
	private int itemId;

	@Getter
	@Setter
	private long gePrice;

	// One entry per configured price period, in period order
	@Getter
	private List<PeriodPrices> periodPrices;

	@Override
	public boolean equals(Object obj)
	{
		if (!(obj instanceof MarketWatcherItem))
		{
			return false;
		}

		final MarketWatcherItem item = (MarketWatcherItem) obj;
		return item.getItemId() == this.itemId;
	}

	@Override
	public int hashCode()
	{
		return Integer.hashCode(itemId);
	}

	@Override
	public int compareTo(MarketWatcherItem other)
	{
		return Long.compare(gePrice, other.getGePrice());
	}
}
