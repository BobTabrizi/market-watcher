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

import com.google.inject.Provides;
import com.marketwatcher.config.PricePeriodType;
import com.marketwatcher.config.MarketWatcherConfig;
import static com.marketwatcher.config.MarketWatcherConfig.AUTO_REFRESH_INTERVAL;
import static com.marketwatcher.config.MarketWatcherConfig.PRICE_PERIOD_ONE_QUANTITY;
import static com.marketwatcher.config.MarketWatcherConfig.PRICE_PERIOD_ONE_TYPE;
import static com.marketwatcher.config.MarketWatcherConfig.PRICE_PERIOD_THREE_QUANTITY;
import static com.marketwatcher.config.MarketWatcherConfig.PRICE_PERIOD_THREE_TYPE;
import static com.marketwatcher.config.MarketWatcherConfig.PRICE_PERIOD_TWO_QUANTITY;
import static com.marketwatcher.config.MarketWatcherConfig.PRICE_PERIOD_TWO_TYPE;
import com.marketwatcher.utilities.WikiItemDetails;
import com.marketwatcher.utilities.WikiRequestResult;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;
import javax.inject.Inject;
import javax.swing.*;

import com.marketwatcher.data.MarketWatcherItem;
import com.marketwatcher.data.MarketWatcherTab;
import com.marketwatcher.data.MarketWatcherTabDataManager;
import com.marketwatcher.data.PeriodPrices;
import java.util.concurrent.CopyOnWriteArrayList;

import static com.marketwatcher.utilities.Constants.*;

import com.marketwatcher.ui.MarketWatcherPluginPanel;
import net.runelite.client.eventbus.Subscribe;
import net.runelite.client.events.ConfigChanged;
import net.runelite.client.game.ItemManager;
import lombok.extern.slf4j.Slf4j;

import java.awt.image.BufferedImage;
import java.util.*;
import java.util.List;
import java.time.Instant;

import com.google.gson.Gson;
import net.runelite.client.callback.ClientThread;
import net.runelite.client.config.ConfigManager;
import net.runelite.client.plugins.Plugin;
import net.runelite.client.plugins.PluginDescriptor;
import net.runelite.client.ui.ClientToolbar;
import net.runelite.client.ui.NavigationButton;
import net.runelite.client.util.ImageUtil;

import lombok.Getter;
import net.runelite.client.util.LinkBrowser;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;

@Slf4j
@PluginDescriptor(
	name = PLUGIN_NAME
)
public class MarketWatcherPlugin extends Plugin
{
	public static final String CONFIG_GROUP = "marketwatcher";
	@Inject
	private ClientThread clientThread;
	@Inject
	private ItemManager itemManager;
	@Inject
	private Gson gson;
	@Inject
	private MarketWatcherTabDataManager dataManager;

	@Inject
	private ClientToolbar clientToolbar;

	// Changed on the client thread but read by the panel on the Swing thread, so copy-on-write
	// lists give the panel a consistent snapshot. Both lists are small and rarely change.
	@Getter
	private volatile List<MarketWatcherItem> items = new CopyOnWriteArrayList<>();

	@Getter
	private volatile List<MarketWatcherTab> tabs = new CopyOnWriteArrayList<>();

	public static final int PERIOD_COUNT = 3;

	// One map of item ID to prices per period. Each map is replaced whole after a fetch,
	// so readers on other threads always see a complete snapshot.
	private final List<Map<Integer, PeriodPrices>> periodPriceMaps = new CopyOnWriteArrayList<>(
		Collections.nCopies(PERIOD_COUNT, Collections.<Integer, PeriodPrices>emptyMap()));

	@Inject
	private OkHttpClient okHttpClient;

	// Own executor so blocking wiki requests don't tie up RuneLite's shared executor.
	// Created in startUp and shut down in shutDown since the plugin instance is reused across restarts.
	private ScheduledExecutorService scheduler;

	private MarketWatcherPluginPanel panel;
	@Inject
	@Getter
	private MarketWatcherConfig config;

	private NavigationButton navButton;

	private boolean isActive;
	private static final String ADD_EDIT_TAB_MESSAGE = "Enter the name of this tab (30 chars max).";
	private static final String ADD_NEW_TAB_TITLE = "Add New Tab";
	private static final String EDIT_TAB_TITLE = "Edit Tab";

	private final int[] periodQuantities = new int[PERIOD_COUNT];
	private final PricePeriodType[] periodTypes = new PricePeriodType[PERIOD_COUNT];
	private final Runnable dataRefresh = this::refreshItemData;
	private ScheduledFuture<?> refreshHandler;
	private ScheduledFuture<?> pendingConfigFuture;
	private final ConcurrentHashMap.KeySetView<String, ?> pendingConfigChanges = ConcurrentHashMap.newKeySet();

	private HashMap<PricePeriodType, Integer> typeMap = new HashMap<PricePeriodType, Integer>();


	// Refresh item data. Every 12h by default.
	public void refreshItemData()
	{
		fetchItemData();
		// loadData rebuilds items from the new price map, updates GE prices and repaints the panel on the client thread
		clientThread.invokeLater(() -> dataManager.loadData());
	}

	// Retrieve item price histories for each configured period.
	// Store prices in a map to be accessed during search at any pointer later on.
	// Each period is fetched independently so one failed request doesn't skip the others.
	protected void fetchItemData()
	{
		for (int period = 0; period < PERIOD_COUNT; period++)
		{
			try
			{
				retrieveItemPriceHistories(period);
			}
			catch (Exception e)
			{
				log.warn("Failed to fetch period {} prices ({} {})", period + 1, periodQuantities[period], periodTypes[period], e);
			}
		}
	}

	public void setItems(List<MarketWatcherItem> items)
	{
		this.items = new CopyOnWriteArrayList<>(items);
	}

	public void setTabs(List<MarketWatcherTab> tabs)
	{
		this.tabs = new CopyOnWriteArrayList<>(tabs);
	}

	public int getPeriodQuantity(int period)
	{
		return periodQuantities[period];
	}

	public PricePeriodType getPeriodType(int period)
	{
		return periodTypes[period];
	}

	/**
	 * @return the prices for each period for this item, using {@link PeriodPrices#EMPTY} where unavailable
	 */
	public List<PeriodPrices> getPeriodPrices(int itemId)
	{
		List<PeriodPrices> prices = new ArrayList<>(PERIOD_COUNT);
		for (Map<Integer, PeriodPrices> periodPriceMap : periodPriceMaps)
		{
			prices.add(periodPriceMap.getOrDefault(itemId, PeriodPrices.EMPTY));
		}
		return prices;
	}

	private void scheduleRefresh()
	{
		if (refreshHandler != null)
		{
			refreshHandler.cancel(false);
		}
		refreshHandler = scheduler.scheduleAtFixedRate(dataRefresh, 0, config.refreshInterval(), TimeUnit.HOURS);
	}

	@Override
	protected void startUp() throws Exception
	{
		updateCachedConfigs();

		typeMap.put(PricePeriodType.Days, UNIX_DAY);
		typeMap.put(PricePeriodType.Weeks, UNIX_WEEK);
		typeMap.put(PricePeriodType.Months, UNIX_MONTH);

		scheduler = Executors.newScheduledThreadPool(2);

		isActive = true;

		panel = injector.getInstance(MarketWatcherPluginPanel.class);

		final BufferedImage icon = ImageUtil.loadImageResource(MarketWatcherPlugin.class, PANEL_ICON_PATH);

		navButton = NavigationButton.builder().tooltip(PLUGIN_NAME).icon(icon).priority(11).panel(panel).build();

		clientToolbar.addNavigation(navButton);

		clientThread.invokeLater(() -> dataManager.loadData());

		// Start refreshing only once the panel and data manager exist
		scheduleRefresh();
	}

	@Override
	protected void shutDown()
	{
		clientToolbar.removeNavigation(navButton);
		refreshHandler.cancel(true);
		if (pendingConfigFuture != null)
		{
			pendingConfigFuture.cancel(false);
		}
		scheduler.shutdownNow();
		isActive = false;
	}

	@Provides
	MarketWatcherConfig provideConfig(ConfigManager configManager)
	{
		return configManager.getConfig(MarketWatcherConfig.class);
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

	public void addItemsToTab(MarketWatcherTab tab, List<MarketWatcherItem> selectedItems)
	{
		clientThread.invokeLater(() -> {
			for (MarketWatcherItem item : selectedItems)
			{
				// Items are matched by ID; skip any that were removed while the dialog was open
				if (items.remove(item))
				{
					tab.getItems().add(item);
				}
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
			MarketWatcherTab tab = new MarketWatcherTab(tabName, new CopyOnWriteArrayList<>());

			if (!tabs.contains(tab))
			{
				tabs.add(tab);
				dataManager.saveData();
				SwingUtilities.invokeLater(() -> panel.updateMarketWatchPanel());
			}
			else
			{
				SwingUtilities.invokeLater(() -> showDuplicateTabWarning(tabName));
			}
		});
	}

	private void showDuplicateTabWarning(String tabName)
	{
		JOptionPane.showMessageDialog(panel, "A tab named \"" + tabName + "\" already exists.", "Duplicate Tab Name", JOptionPane.WARNING_MESSAGE);
	}

	public void showHelp()
	{
		JOptionPane.showMessageDialog(panel, "Each item shows the average wiki prices from a 6-hour window at three points in the past.\nThe periods (e.g. 1 day ago, 1 week ago) can be configured in plugin settings.\nFor each period, the low, medium, and high prices are color coded in rows.\nLows are the left number. Mediums are the center number. Highs are the right number.", "Information", JOptionPane.INFORMATION_MESSAGE);
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

		if (panel != null)
		{
			SwingUtilities.invokeLater(() -> panel.updateMarketWatchPanel());
		}
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
			else if (nameCheck != tab)
			{
				SwingUtilities.invokeLater(() -> showDuplicateTabWarning(tabName));
			}
		});
	}

	public void openWikiPriceLink(int itemId)
	{
		final String url = OSRS_WIKI_ITEM_REQUEST_BASE_URL + itemId;
		LinkBrowser.browse(url);
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

	private void retrieveItemPriceHistories(int period) throws Exception
	{
		long unixTimestamp = Instant.now().getEpochSecond();
		long periodDifference = unixTimestamp - (long) typeMap.get(periodTypes[period]) * periodQuantities[period];
		long periodTimeBuffer = periodDifference % SECONDS_IN_SIX_HOURS;

		Request request = new Request.Builder()
			.url(OSRS_WIKI_PRICES_6H_REQUEST_URL + (periodDifference - periodTimeBuffer))
			.header("User-Agent", "Market Watcher Plugin")
			.build();

		String resp;
		try (Response response = okHttpClient.newCall(request).execute())
		{
			if (!response.isSuccessful() || response.body() == null)
			{
				log.warn("Wiki price request for period {} failed with HTTP {}", period + 1, response.code());
				return;
			}
			resp = response.body().string();
		}

		WikiRequestResult wikiRequestResult = gson.fromJson(resp, WikiRequestResult.class);
		if (wikiRequestResult == null || wikiRequestResult.getData() == null)
		{
			log.warn("Wiki price response for period {} had no data", period + 1);
			return;
		}

		Map<Integer, PeriodPrices> prices = new HashMap<>();
		for (Map.Entry<Integer, WikiItemDetails> entry : wikiRequestResult.getData().entrySet())
		{
			WikiItemDetails details = entry.getValue();
			prices.put(entry.getKey(), PeriodPrices.fromWikiAverages(details.getAvgLowPrice(), details.getAvgHighPrice()));
		}
		periodPriceMaps.set(period, Collections.unmodifiableMap(prices));
		log.debug("Loaded {} item prices for period {} ({} {})", prices.size(), period + 1, periodQuantities[period], periodTypes[period]);
	}

	private void updateCachedConfigs()
	{
		periodQuantities[0] = config.pricePeriodOneQty();
		periodQuantities[1] = config.pricePeriodTwoQty();
		periodQuantities[2] = config.pricePeriodThreeQty();

		periodTypes[0] = config.pricePeriodOneType();
		periodTypes[1] = config.pricePeriodTwoType();
		periodTypes[2] = config.pricePeriodThreeType();

	}

	@Subscribe
	public void onConfigChanged(ConfigChanged event)
	{
		// Exit if the plugin is off or the config is unrelated to the plugin
		if (!isActive || !event.getGroup().equals(CONFIG_GROUP))
		{
			return;
		}

		pendingConfigChanges.add(event.getKey());

		// Debounce so rapid changes (e.g. clicking a spinner) are applied as one batch
		if (pendingConfigFuture != null)
		{
			pendingConfigFuture.cancel(false);
		}
		pendingConfigFuture = scheduler.schedule(this::processPendingConfigChanges, 1, TimeUnit.SECONDS);
	}


	private void processPendingConfigChanges()
	{
		clientThread.invoke(() -> {
			if (pendingConfigChanges.isEmpty())
			{
				return;
			}

			try
			{
				synchronized (this)
				{
					updateCachedConfigs();

					log.debug("Processing {} pending Market Watcher config changes: {}", pendingConfigChanges.size(), pendingConfigChanges);

					boolean refetchData = false;


					for (String key : pendingConfigChanges)
					{
						switch (key)
						{
							case PRICE_PERIOD_ONE_QUANTITY:
							case PRICE_PERIOD_ONE_TYPE:
							case PRICE_PERIOD_TWO_QUANTITY:
							case PRICE_PERIOD_TWO_TYPE:
							case PRICE_PERIOD_THREE_QUANTITY:
							case PRICE_PERIOD_THREE_TYPE:
							case AUTO_REFRESH_INTERVAL:
								refetchData = true;
								break;
						}
					}

					if (pendingConfigChanges.contains(AUTO_REFRESH_INTERVAL))
					{
						// Reschedule with the new interval; this also refreshes immediately
						scheduleRefresh();
					}
					else if (refetchData)
					{
						scheduler.execute(dataRefresh);
					}

					// Period labels and color blind mode are read from config when the panel is rebuilt
					SwingUtilities.invokeLater(() -> panel.updateMarketWatchPanel());
				}
			}
			catch (Throwable ex)
			{
				log.error("Error while changing settings:", ex);
			}
			finally
			{
				pendingConfigChanges.clear();
			}
		});
	}
}
