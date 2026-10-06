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

import java.util.Collections;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;
import org.junit.Test;

public class MarketWatcherItemTest
{
	private static MarketWatcherItem item(String name)
	{
		return new MarketWatcherItem(null, name, 1, 0, Collections.emptyList());
	}

	@Test
	public void emptyFilterMatchesEverything()
	{
		assertTrue(item("Abyssal whip").nameMatches(""));
	}

	@Test
	public void matchesAnyPartOfNameIgnoringCase()
	{
		MarketWatcherItem whip = item("Abyssal whip");
		assertTrue(whip.nameMatches("whip"));
		assertTrue(whip.nameMatches("abyss"));
		assertTrue(whip.nameMatches("sal wh"));
	}

	@Test
	public void rejectsNonMatchingFilter()
	{
		assertFalse(item("Abyssal whip").nameMatches("bow"));
	}
}
