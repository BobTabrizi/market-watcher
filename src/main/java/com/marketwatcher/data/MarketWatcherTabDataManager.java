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

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import com.marketwatcher.MarketWatcherPlugin;

import lombok.extern.slf4j.Slf4j;
import net.runelite.api.Client;
import net.runelite.api.GameState;
import net.runelite.client.config.ConfigManager;
import net.runelite.client.game.ItemManager;
import net.runelite.client.util.AsyncBufferedImage;

import javax.inject.Inject;
import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import static com.marketwatcher.utilities.Constants.*;
import static com.marketwatcher.utilities.PriceUtils.createMarketWatchItemWithPriceMap;

@Slf4j
public class MarketWatcherTabDataManager
{
	private static final String CONFIG_KEY_ITEMIDS = "itemIds";
	private static final String CONFIG_KEY_TABS = "tabs";
	private static final String LOAD_ITEM_ERROR = "Exception occurred while loading items";
	private static final String LOAD_TAB_ERROR = "Exception occurred while loading tabs";
	private static final String EMPTY_ARRAY = "[]";
	private final MarketWatcherPlugin plugin;
	private final Client client;
	private final ConfigManager configManager;
	private final ItemManager itemManager;
	private final Gson gson;

	private List<Integer> itemIds = new ArrayList<>();
	private final Type itemsType = new TypeToken<ArrayList<Integer>>()
	{
	}.getType();

	private List<MarketWatcherTabData> tabs = new ArrayList<>();
	private final Type tabsType = new TypeToken<ArrayList<MarketWatcherTabData>>()
	{
	}.getType();

	@Inject
	public MarketWatcherTabDataManager(MarketWatcherPlugin plugin, Client client, ConfigManager configManager, ItemManager itemManager, Gson gson)
	{
		this.plugin = plugin;
		this.client = client;
		this.configManager = configManager;
		this.itemManager = itemManager;
		this.gson = gson;
	}

	public boolean loadData()
	{
		// Load later if not at login screen to prevent data loss
		if (client.getGameState().getState() < GameState.LOGIN_SCREEN.getState())
		{
			return false;
		}

		// Individual Items
		itemIds.clear();

		String itemsJson = configManager.getConfiguration(CONFIG_TAB, CONFIG_KEY_ITEMIDS);
		if (itemsJson == null || itemsJson.equals(EMPTY_ARRAY))
		{
			plugin.setItems(new ArrayList<>());
		}
		else
		{
			try
			{
				itemIds = gson.fromJson(itemsJson, itemsType);
				convertItems();
			}
			catch (Exception e)
			{
				log.error(LOAD_ITEM_ERROR, e);
				plugin.setItems(new ArrayList<>());
			}
		}

		// Tabs and their items
		tabs.clear();

		String tabsJson = configManager.getConfiguration(CONFIG_TAB, CONFIG_KEY_TABS);
		if (tabsJson == null || tabsJson.equals(EMPTY_ARRAY))
		{
			plugin.setTabs(new ArrayList<>());
		}
		else
		{
			try
			{
				tabs = gson.fromJson(tabsJson, tabsType);
				convertTabs();
			}
			catch (Exception e)
			{
				log.error(LOAD_TAB_ERROR, e);
				plugin.setTabs(new ArrayList<>());
			}
		}

		plugin.updateItemPrices();
		return true;
	}

	public void saveData()
	{
		// Individual Items
		itemIds.clear();

		for (MarketWatcherItem item : plugin.getItems())
		{
			itemIds.add(item.getItemId());
		}

		final String itemsJson = gson.toJson(itemIds);
		configManager.setConfiguration(CONFIG_TAB, CONFIG_KEY_ITEMIDS, itemsJson);

		// Tabs and their items
		tabs.clear();

		for (MarketWatcherTab tab : plugin.getTabs())
		{
			List<Integer> tabItems = new ArrayList<>();
			for (MarketWatcherItem item : tab.getItems())
			{
				tabItems.add(item.getItemId());
			}

			tabs.add(new MarketWatcherTabData(tab.getName(), tab.isCollapsed(), tabItems));
		}

		final String tabsJson = gson.toJson(tabs);
		configManager.setConfiguration(CONFIG_TAB, CONFIG_KEY_TABS, tabsJson);
	}

	private void convertItems()
	{
		List<MarketWatcherItem> watchItems = new ArrayList<>();

		for (Integer itemId : itemIds)
		{
			watchItems.add(convertIdToItem(itemId));
		}

		plugin.setItems(watchItems);
	}

	private void convertTabs()
	{
		List<MarketWatcherTab> watchTabs = new ArrayList<>();

		for (MarketWatcherTabData tab : tabs)
		{
			List<MarketWatcherItem> tabItems = new ArrayList<>();
			for (Integer itemId : tab.getItems())
			{
				tabItems.add(convertIdToItem(itemId));
			}

			watchTabs.add(new MarketWatcherTab(tab.getName(), tab.isCollapsed(), tabItems));
		}

		plugin.setTabs(watchTabs);
	}

	private MarketWatcherItem convertIdToItem(int itemId)
	{
		AsyncBufferedImage itemImage = itemManager.getImage(itemId);
		String itemName = itemManager.getItemComposition(itemId).getName();
		Map<String, String> itemPriceMap = plugin.getItemPriceMap().get(itemId);

		return createMarketWatchItemWithPriceMap(itemImage, itemName, itemId, 0, itemPriceMap);
	}
}