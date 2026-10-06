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

import java.text.DecimalFormat;

import static com.marketwatcher.utilities.Constants.*;

public final class PriceUtils
{

	private PriceUtils()
	{
	}

	public static String standardPricePadder(String price)
	{
		if (price.length() == 1)
		{
			price = price + "\u2800" + "\u2800" + "\u2800" + "\u202F" + "\u202F";
			return price;
		}
		if (price.length() == 2)
		{
			price = price + "\u2800" + "\u2800" + "\u202F" + "\u202F";
			return price;
		}
		if (price.length() == 3)
		{
			price = price + "\u2800" + "\u202F" + "\u202F" + "\u202F";
			return price;
		}
		if (price.length() == 4 && !price.contains(M_MILLION) && !price.contains(K_THOUSAND))
		{
			price = price + "\u2800" + "\u202F";
			return price;
		}
		if (price.length() == 4 && (price.contains(M_MILLION) || price.contains(K_THOUSAND)))
		{
			price = price + "\u2800" + "\u202F" + "\u202F";
			return price;
		}
		if (price.length() == 5 && !price.contains(M_MILLION) && !price.contains(K_THOUSAND) && !price.contains(B_BILLION))
		{
			price = price + "\u2800";
			return price;
		}

		if (price.length() == 5 && price.contains(K_THOUSAND))
		{
			price = price + "\u2800";
			return price;
		}
		if (price.length() == 5 && price.contains(M_MILLION))
		{
			price = price + "\u202F" + "\u202F";
			return price;
		}
		if (price.length() == 5 && price.contains(B_BILLION))
		{
			price = price + "\u2800" + "\u202F";
			return price;
		}

		if (price.length() == 6)
		{
			price = price + "\u202F";
			return price;
		}

		return price;
	}

	public static String compactPricePadder(String price)
	{
		if (price.length() == 1)
		{
			price = price + "\u2800" + "\u2800" + "\u2800";
			return price;
		}
		if (price.length() == 2)
		{
			price = price + "\u2800" + "\u2800" + "\u202F";
			return price;
		}
		if (price.length() == 3)
		{
			price = price + "\u2800" + "\u202F" + "\u202F";
			return price;
		}
		if (price.length() == 4 && !price.contains(M_MILLION) && !price.contains(K_THOUSAND))
		{
			price = price + "\u202F" + "\u202F" + "\u202F";
			return price;
		}
		if (price.length() == 4 && (price.contains(M_MILLION) || price.contains(K_THOUSAND)))
		{
			price = price + "\u2800";
			return price;
		}
		if (price.length() == 5 && !price.contains(M_MILLION) && !price.contains(K_THOUSAND) && !price.contains(B_BILLION))
		{
			price = price + "\u202F" + "\u202F" + "\u202F";
			return price;
		}

		if (price.length() == 5 && price.contains(K_THOUSAND))
		{
			price = price + "\u202F" + "\u202F";
			return price;
		}
		if (price.length() == 5 && price.contains(M_MILLION))
		{
			price = price + "\u202F";
			return price;
		}
		if (price.length() == 5 && price.contains(B_BILLION))
		{
			price = price + "\u202F" + "\u202F";
			return price;
		}

		return price;
	}

	/**
	 * Formats a price for the price grid, abbreviating to K/M/B and padding to align columns.
	 *
	 * @param price    the price, or null if unavailable
	 * @param viewType {@link Constants#STANDARD} or {@link Constants#COMPACT}
	 */
	public static String formatPrice(Long price, String viewType)
	{
		String formatted = price == null ? NOT_AVAILABLE : abbreviatePrice(price);

		if (viewType.equals(STANDARD))
		{
			return standardPricePadder(formatted);
		}
		else if (viewType.equals(COMPACT))
		{
			return compactPricePadder(formatted);
		}
		return formatted;
	}

	static String abbreviatePrice(long price)
	{
		final DecimalFormat df = new DecimalFormat("0.0");
		final DecimalFormat df2 = new DecimalFormat("0.00");

		if (price >= 1000000)
		{
			String millions = df.format((float) price / 1000000);
			// Prices just under 1B round up to 1000.0, so show them in the next unit instead
			if (price < 1000000000 && !millions.equals("1000.0"))
			{
				return millions + M_MILLION;
			}
			return df2.format((float) price / 1000000000) + B_BILLION;
		}
		if (price >= 10000)
		{
			String thousands = df.format((float) price / 1000);
			// Prices just under 1M round up to 1000.0, so show them in the next unit instead
			if (!thousands.equals("1000.0"))
			{
				return thousands + K_THOUSAND;
			}
			return df.format((float) price / 1000000) + M_MILLION;
		}
		return Long.toString(price);
	}
}
