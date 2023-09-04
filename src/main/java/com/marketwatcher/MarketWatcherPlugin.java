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
package com.marketwatcher;

import com.fasterxml.jackson.databind.ObjectMapper;

import javax.inject.Inject;
import javax.swing.*;

import com.marketwatcher.data.MarketWatcherItem;
import com.marketwatcher.data.MarketWatcherTab;
import com.marketwatcher.data.MarketWatcherTabDataManager;

import static com.marketwatcher.utilities.Constants.*;

import com.marketwatcher.ui.MarketWatcherPluginPanel;
import net.runelite.client.game.ItemManager;
import lombok.extern.slf4j.Slf4j;

import java.awt.image.BufferedImage;
import java.math.BigInteger;
import java.util.*;
import java.util.List;
import java.util.regex.Matcher;
import java.time.Instant;
import java.util.regex.Pattern;

import com.google.gson.Gson;
import net.runelite.api.Client;
import net.runelite.client.callback.ClientThread;
import net.runelite.client.config.ConfigManager;
import net.runelite.client.plugins.Plugin;
import net.runelite.client.plugins.PluginDescriptor;
import net.runelite.client.ui.ClientToolbar;
import net.runelite.client.ui.NavigationButton;
import net.runelite.client.util.ImageUtil;

import lombok.Getter;
import lombok.Setter;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;

@Slf4j
@PluginDescriptor(
	name = PLUGIN_NAME
)
public class MarketWatcherPlugin extends Plugin
{
	@Inject
	private Client client;
	@Inject
	private ClientThread clientThread;
	@Inject
	private ItemManager itemManager;
	@Inject
	private ConfigManager configManager;
	@Inject
	private Gson gson;
	@Inject
	private MarketWatcherTabDataManager dataManager;

	@Inject
	private ClientToolbar clientToolbar;

	@Getter
	@Setter
	private List<MarketWatcherItem> items = new ArrayList<>();

	@Getter
	@Setter
	private List<MarketWatcherTab> tabs = new ArrayList<>();

	@Getter
	@Setter
	Map<Integer, Map<String, String>> itemPriceMap = new HashMap<>();

	private OkHttpClient okHttpClient;

	private MarketWatcherPluginPanel panel;

	private NavigationButton navButton;

	// Response offset indices are used to obtain a substring of the response string from the market API for processing.
	private static final int RESPONSE_OFFSET_START_INDEX = 8;
	private static final int RESPONSE_OFFSET_END_INDEX = 24;
	private static final String ADD_EDIT_TAB_MESSAGE = "Enter the name of this tab (30 chars max).";
	private static final String ADD_NEW_TAB_TITLE = "Add New Tab";
	private static final String EDIT_TAB_TITLE = "Edit Tab";


	@Override
	protected void startUp() throws Exception
	{

		// Retrieve item price histories for one week, month, and three months.
		// Store prices in a map to be accessed during search at any pointer later on.
		retrieveItemPriceHistories(ONE_WEEK);
		retrieveItemPriceHistories(ONE_MONTH);
		retrieveItemPriceHistories(THREE_MONTHS);

		panel = injector.getInstance(MarketWatcherPluginPanel.class);

		final BufferedImage icon = ImageUtil.loadImageResource(MarketWatcherPlugin.class, PANEL_ICON_PATH);

		navButton = NavigationButton.builder().tooltip(PLUGIN_NAME).icon(icon).priority(11).panel(panel).build();

		clientToolbar.addNavigation(navButton);

		this.dataManager = new MarketWatcherTabDataManager(this, client, configManager, itemManager, gson);

		clientThread.invokeLater(() -> dataManager.loadData());

	}

	@Override
	protected void shutDown() throws Exception
	{
		clientToolbar.removeNavigation(navButton);
	}


	public void addItem(MarketWatcherItem item)
	{
		clientThread.invokeLater(() ->
		{
			if (!containsItem(item))
			{
				items.add(item);
				dataManager.saveData();
				SwingUtilities.invokeLater(() ->
				{
					panel.switchToMarketWatch();
					panel.updateMarketWatchPanel();
				});
			}
			else
			{
				SwingUtilities.invokeLater(() -> panel.containsItemWarning());
			}
		});
	}

	public void removeItem(MarketWatcherItem item)
	{
		clientThread.invokeLater(() -> {
			items.remove(item);
			dataManager.saveData();
			SwingUtilities.invokeLater(() -> panel.updateMarketWatchPanel());
		});
	}

	public void addItemsToTab(MarketWatcherTab tab, List<String> itemNames)
	{
		clientThread.invokeLater(() -> {
			for (String itemName : itemNames)
			{
				MarketWatcherItem item = items.stream().filter(o -> o.getName().equals(itemName)).findFirst().orElse(null);
				tab.getItems().add(item);
				items.remove(item);
			}
			dataManager.saveData();
			SwingUtilities.invokeLater(() -> panel.updateMarketWatchPanel());
		});
	}

	public void removeItemFromTab(MarketWatcherTab tab, MarketWatcherItem item)
	{
		clientThread.invokeLater(() -> {
			tab.getItems().remove(item);
			items.add(item);
			dataManager.saveData();
			SwingUtilities.invokeLater(() -> panel.updateMarketWatchPanel());
		});
	}

	public void switchTabCollapse(MarketWatcherTab tab)
	{
		clientThread.invokeLater(() -> {
			tab.setCollapsed(!tab.isCollapsed());
			dataManager.saveData();
			SwingUtilities.invokeLater(() -> panel.updateMarketWatchPanel());
		});
	}

	public void addTab()
	{
		String name = JOptionPane.showInputDialog(panel, ADD_EDIT_TAB_MESSAGE, ADD_NEW_TAB_TITLE, JOptionPane.PLAIN_MESSAGE);

		if (name == null || name.isEmpty())
		{
			return;
		}

		if (name.length() > 30)
		{
			name = name.substring(0, 30);
		}

		String tabName = name;
		clientThread.invokeLater(() -> {
			MarketWatcherTab tab = new MarketWatcherTab(tabName, new ArrayList<>());

			if (!tabs.contains(tab))
			{
				tabs.add(tab);
				dataManager.saveData();
				SwingUtilities.invokeLater(() -> panel.updateMarketWatchPanel());
			}
		});
	}

	public void showHelp()
	{
		JOptionPane.showMessageDialog(panel, "Each item displays price history for the past week, month, and 3 months. \nFor each time period, the price lows, averages, and highs are color coded in rows. \nGreen numbers are lows. Yellow numbers are averages. Red numbers are highs.", "Information", JOptionPane.INFORMATION_MESSAGE);
	}

	public void shiftItem(int itemIndex, boolean shiftUp)
	{
		clientThread.invokeLater(() -> {
			MarketWatcherItem shiftedItem = items.get(itemIndex);

			// Out of bounds is checked before call in item panel
			if (shiftUp)
			{
				items.set(itemIndex, items.get(itemIndex - 1));
				items.set(itemIndex - 1, shiftedItem);
			}
			else
			{
				items.set(itemIndex, items.get(itemIndex + 1));
				items.set(itemIndex + 1, shiftedItem);
			}

			dataManager.saveData();
			SwingUtilities.invokeLater(() -> panel.updateMarketWatchPanel());
		});
	}

	public void shiftItemInTab(MarketWatcherTab tab, int itemIndex, boolean shiftUp)
	{
		clientThread.invokeLater(() -> {
			List<MarketWatcherItem> tabItems = tab.getItems();
			MarketWatcherItem shiftedItem = tab.getItems().get(itemIndex);

			// Out of bounds is checked before call in tab item panel
			if (shiftUp)
			{
				tabItems.set(itemIndex, tabItems.get(itemIndex - 1));
				tabItems.set(itemIndex - 1, shiftedItem);
			}
			else
			{
				tabItems.set(itemIndex, tabItems.get(itemIndex + 1));
				tabItems.set(itemIndex + 1, shiftedItem);
			}

			dataManager.saveData();
			SwingUtilities.invokeLater(() -> panel.updateMarketWatchPanel());
		});
	}

	public void removeTab(MarketWatcherTab tab)
	{
		clientThread.invokeLater(() -> {
			// Move items out of tab and delete
			items.addAll(tab.getItems());
			tabs.remove(tab);
			dataManager.saveData();
			SwingUtilities.invokeLater(() -> panel.updateMarketWatchPanel());
		});
	}

	public void updateItemPrices()
	{
		// Tab item prices
		for (MarketWatcherTab tab : tabs)
		{
			for (MarketWatcherItem item : tab.getItems())
			{
				item.setGePrice(itemManager.getItemPrice(item.getItemId()));
			}
		}

		// Individual prices
		for (MarketWatcherItem item : items)
		{
			item.setGePrice(itemManager.getItemPrice(item.getItemId()));
		}

		SwingUtilities.invokeLater(() -> panel.updateMarketWatchPanel());
	}

	public void editTab(MarketWatcherTab tab)
	{
		String name = JOptionPane.showInputDialog(panel, ADD_EDIT_TAB_MESSAGE, EDIT_TAB_TITLE, JOptionPane.PLAIN_MESSAGE);

		if (name == null || name.isEmpty())
		{
			return;
		}

		if (name.length() > 30)
		{
			name = name.substring(0, 30);
		}

		String tabName = name;
		clientThread.invokeLater(() -> {
			MarketWatcherTab nameCheck = tabs.stream().filter(o -> o.getName().equals(tabName)).findFirst().orElse(null);

			if (nameCheck == null)
			{
				tab.setName(tabName);
				dataManager.saveData();
				SwingUtilities.invokeLater(() -> panel.updateMarketWatchPanel());
			}
		});
	}

	private boolean containsItem(MarketWatcherItem newItem)
	{
		for (MarketWatcherTab tab : tabs)
		{
			if (tab.getItems().contains(newItem))
			{
				return true;
			}
		}
		return items.contains(newItem);
	}

	private void retrieveItemPriceHistories(String timePeriod) throws Exception
	{
		String resp = EMPTY_STRING;

		long unixTimestamp = Instant.now().getEpochSecond();
		String unixTimeString = EMPTY_STRING;

		if (timePeriod.equals(ONE_WEEK))
		{
			long oneWeekAgo = (unixTimestamp - UNIX_WEEK);
			long timeBuffer = (oneWeekAgo % SECONDS_IN_SIX_HOURS);

			unixTimeString = String.valueOf(oneWeekAgo - timeBuffer);
		}
		else if (timePeriod.equals(ONE_MONTH))
		{
			long oneMonthAgo = (unixTimestamp - UNIX_MONTH);
			long timeBuffer = (oneMonthAgo % SECONDS_IN_SIX_HOURS);
			unixTimeString = String.valueOf(oneMonthAgo - timeBuffer);
		}
		else if (timePeriod.equals(THREE_MONTHS))
		{
			long threeMonthsAgo = (unixTimestamp - UNIX_MONTH * 3);
			long timeBuffer = (threeMonthsAgo % SECONDS_IN_SIX_HOURS);
			unixTimeString = String.valueOf(threeMonthsAgo - timeBuffer);
		}

		Request request = new Request.Builder()
			.url(OSRS_WIKI_PRICES_6H_REQUEST_URL + unixTimeString)
			.header("User-Agent", "Market Watcher Plugin")
			.build();

		okHttpClient = injector.getInstance(OkHttpClient.class);

		try (Response response = okHttpClient.newCall(request).execute())
		{
			resp = response.body().string();
		}
		catch (Exception e)
		{
			System.out.println(e);
		}

		String modifiedResponse = resp.substring(RESPONSE_OFFSET_START_INDEX, resp.length() - RESPONSE_OFFSET_END_INDEX);
		final Map<String, Object> jsonObject = new ObjectMapper().readValue(modifiedResponse, Map.class);

		for (Map.Entry<String, Object> entry : jsonObject.entrySet())
		{
			String text = entry.getValue().toString();
			Pattern pattern = Pattern.compile("([^=\\s]+)=([^=\\s]*(?:\\s+[^=\\s]+)*)(?!\\S)");
			Matcher matcher = pattern.matcher(text);

			String highPrice = NOT_AVAILABLE;
			String lowPrice = NOT_AVAILABLE;
			String medPrice = NOT_AVAILABLE;
			if (matcher.find())
			{
				if (matcher.group(2) != null && !matcher.group(2).equals(NULL))
				{
					highPrice = matcher.group(2).replace(COMMA, EMPTY_STRING);
				}
			}

			matcher.find();
			if (matcher.find())
			{
				if (matcher.group(2) != null && !matcher.group(2).equals(NULL))
				{
					lowPrice = matcher.group(2).replace(COMMA, EMPTY_STRING);
				}
			}

			if (!highPrice.equals(NULL) && !lowPrice.equals(NULL))
			{
				BigInteger highPriceInteger = BigInteger.valueOf(Integer.parseInt(highPrice));
				BigInteger lowPriceInteger = BigInteger.valueOf(Integer.parseInt(lowPrice));
				medPrice = String.valueOf(highPriceInteger.add(lowPriceInteger).divide(new BigInteger("2")));
			}

			int currentItemID = Integer.parseInt(entry.getKey());

			Map<String, String> timeFrameValuesMapping = itemPriceMap.get(currentItemID);

			if (timeFrameValuesMapping == null)
			{
				Map<String, String> timeFrameValues = new HashMap<>();
				timeFrameValues.put(timePeriod + LOW, lowPrice);
				timeFrameValues.put(timePeriod + MED, medPrice);
				timeFrameValues.put(timePeriod + HIGH, highPrice);
				itemPriceMap.put(Integer.parseInt(entry.getKey()), timeFrameValues);
			}
			else
			{
				timeFrameValuesMapping.put(timePeriod + LOW, lowPrice);
				timeFrameValuesMapping.put(timePeriod + MED, medPrice);
				timeFrameValuesMapping.put(timePeriod + HIGH, highPrice);
			}

		}
	}
}
