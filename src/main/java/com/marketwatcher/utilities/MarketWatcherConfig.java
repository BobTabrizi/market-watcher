/*
 * Copyright (c) 2024, Bob Tabrizi
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


import com.marketwatcher.MarketWatcherPlugin;
import com.marketwatcher.config.PricePeriodType;
import net.runelite.client.config.Config;
import net.runelite.client.config.ConfigGroup;
import net.runelite.client.config.ConfigItem;
import net.runelite.client.config.ConfigSection;
import net.runelite.client.config.Range;
import net.runelite.client.config.Units;

@ConfigGroup(MarketWatcherPlugin.CONFIG_GROUP)
public interface MarketWatcherConfig extends Config
{
	@ConfigSection(
		name = "General",
		description = "General settings",
		position = 0
	)
	String generalSettings = "generalSettings";
	String AUTO_REFRESH_INTERVAL = "autoRefreshInterval";
	@ConfigItem(
		keyName = AUTO_REFRESH_INTERVAL,
		name = "Auto Refresh Interval",
		description = "Set a time to automatically refresh item prices.",
		position = 0,
		section = generalSettings
	)
	@Range(min= 6, max = 24)
	@Units(" hours")
	default int refreshInterval()
	{
		return 12;
	}


	String PRICE_PERIOD_ONE_QUANTITY = "pricePeriodOneQuantity";
	@ConfigSection(
		name = "Price Period 1",
		description = "First Price Period",
		position = 1
	)
	String pricePeriodOne = "pricePeriodOne";
	@ConfigItem(
		keyName = PRICE_PERIOD_ONE_QUANTITY,
		name = "Quantity",
		description = "Set the quantity for the first price period.",
		position = 0,
		section = pricePeriodOne
	)
	@Range(min= 1, max = 24)
	default int pricePeriodOneQty()
	{
		return 1;
	}

	String PRICE_PERIOD_ONE_TYPE = "pricePeriodOneType";
	@ConfigItem(
		keyName = PRICE_PERIOD_ONE_TYPE,
		name = "Type",
		description = "Set the first price period type.",
		position = 1,
		section = pricePeriodOne
	)
	default PricePeriodType pricePeriodOneType()
	{
		return PricePeriodType.DAYS;
	}

	String PRICE_PERIOD_TWO_QUANTITY = "pricePeriodTwoQuantity";
	@ConfigSection(
		name = "Price Period 2",
		description = "Second Price Period",
		position = 2
	)
	String pricePeriodTwo = "pricePeriodTwo";
	@ConfigItem(
		keyName = PRICE_PERIOD_TWO_QUANTITY,
		name = "Quantity",
		description = "Set the quantity for the second price period.",
		position = 0,
		section = pricePeriodTwo
	)
	@Range(min= 1, max = 24)
	default int pricePeriodTwoQty()
	{
		return 3;
	}
	String PRICE_PERIOD_TWO_TYPE = "pricePeriodTwoType";
	@ConfigItem(
		keyName = PRICE_PERIOD_TWO_TYPE,
		name = "Type",
		description = "Set the second price period type.",
		position = 1,
		section = pricePeriodTwo
	)
	default PricePeriodType pricePeriodTwoType()
	{
		return PricePeriodType.WEEKS;
	}
	String PRICE_PERIOD_THREE_QUANTITY = "pricePeriodThreeQuantity";
	@ConfigSection(
		name = "Price Period 3",
		description = "Third Price Period",
		position = 2
	)
	String pricePeriodThree = "pricePeriodThree";
	@ConfigItem(
		keyName = PRICE_PERIOD_THREE_QUANTITY,
		name = "Quantity",
		description = "Set the quantity for the third price period.",
		position = 0,
		section = pricePeriodThree
	)
	@Range(min= 1, max = 24)
	default int pricePeriodThreeQty()
	{
		return 3;
	}
	String PRICE_PERIOD_THREE_TYPE = "pricePeriodThreeType";
	@ConfigItem(
		keyName = PRICE_PERIOD_THREE_TYPE,
		name = "Type",
		description = "Set the third price period type.",
		position = 1,
		section = pricePeriodThree
	)
	default PricePeriodType pricePeriodThreeType()
	{
		return PricePeriodType.MONTHS;
	}

}
