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

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;

import static com.marketwatcher.utilities.Constants.NOT_AVAILABLE;

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
	private int gePrice;

	@Getter
	@Setter
	private String oneWeekLow;
	@Getter
	@Setter
	private String oneWeekMed;
	@Getter
	@Setter
	private String oneWeekHigh;
	@Getter
	@Setter
	private String oneMonthLow;
	@Getter
	@Setter
	private String oneMonthMed;
	@Getter
	@Setter
	private String oneMonthHigh;
	@Getter
	@Setter
	private String threeMonthLow;
	@Getter
	@Setter
	private String threeMonthMed;
	@Getter
	@Setter
	private String threeMonthHigh;


	public MarketWatcherItem(AsyncBufferedImage itemImage, String itemName, int itemID, int itemPrice)
	{
		image = itemImage;
		name = itemName;
		itemId = itemID;
		gePrice = itemPrice;
		oneWeekLow = NOT_AVAILABLE;
		oneWeekMed = NOT_AVAILABLE;
		oneWeekHigh = NOT_AVAILABLE;
		oneMonthLow = NOT_AVAILABLE;
		oneMonthMed = NOT_AVAILABLE;
		oneMonthHigh = NOT_AVAILABLE;
		threeMonthLow = NOT_AVAILABLE;
		threeMonthMed = NOT_AVAILABLE;
		threeMonthHigh = NOT_AVAILABLE;
	}

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
	public int compareTo(MarketWatcherItem other)
	{
		return Integer.compare(gePrice, other.getGePrice());
	}
}