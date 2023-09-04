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

public final class Constants
{
	private Constants()
	{
		// restrict instantiation
	}

	public static final String PLUGIN_NAME = "Market Watcher";
	public static final String CONFIG_TAB = "marketwatch";
	public static final String K_THOUSAND = "K";
	public static final String M_MILLION = "M";
	public static final String B_BILLION = "B";
	public static final String NOT_AVAILABLE = "N/A";
	public static final String NULL = "null";

	public static final String TRUNCATION_STRING = "...";
	public static final String STANDARD = "Standard";
	public static final String COMPACT = "Compact";

	public static final int UNIX_WEEK = 604800;

	public static final int UNIX_MONTH = 2592000;

	public static final int SECONDS_IN_SIX_HOURS = 21600;

	public static final String ONE_WEEK = "oneWeek";
	public static final String ONE_MONTH = "oneMonth";
	public static final String THREE_MONTHS = "threeMonths";

	public static final String LOW = "Low";
	public static final String MED = "Med";
	public static final String HIGH = "High";

	public static final String ONE_WEEK_LOW = "oneWeekLow";
	public static final String ONE_WEEK_MED = "oneWeekMed";
	public static final String ONE_WEEK_HIGH = "oneWeekHigh";
	public static final String ONE_MONTH_LOW = "oneMonthLow";
	public static final String ONE_MONTH_MED = "oneMonthMed";
	public static final String ONE_MONTH_HIGH = "oneMonthHigh";
	public static final String THREE_MONTHS_LOW = "threeMonthsLow";
	public static final String THREE_MONTHS_MED = "threeMonthsMed";
	public static final String THREE_MONTHS_HIGH = "threeMonthsHigh";
	public static final String GP = " gp";

	public static final String OSRS_WIKI_PRICES_6H_REQUEST_URL = "https://prices.runescape.wiki/api/v1/osrs/6h?timestamp=";
	public static final String COMMA = ",";
	public static final String EMPTY_STRING = "";

	public static final String PANEL_ICON_PATH = "/panelicon.png";
	public static final String DELETE_ICON_PATH = "/deleteicon.png";
	public static final String SHIFT_UP_ICON_PATH = "/shiftupicon.png";
	public static final String SHIFT_DOWN_ICON_PATH = "/shiftdownicon.png";
	public static final String ADD_TAB_ITEM_ICON_PATH = "/addtabitemicon.png";
	public static final String EDIT_TAB_ICON_PATH = "/edittabicon.png";
	public static final String DELETE_TAB_ICON_PATH = "/trashicon.png";
	public static final String COLLAPSE_ICON_PATH = "/collapseicon.png";
	public static final String INFO_ICON_PATH = "/infoicon.png";
	public static final String ADD_ICON_PATH = "/addicon.png";
	public static final String ADD_TAB_ICON_PATH = "/addtabicon.png";
	public static final String CANCEL_ICON_PATH = "/cancelicon.png";

}
