package com.marketwatcher.data;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import com.marketwatcher.MarketWatcherPlugin;

import static com.marketwatcher.ui.Constants.CONFIG_TAB;

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

@Slf4j
public class MarketWatcherListDataManager {
    private static final String CONFIG_KEY_VALUE = "value";
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
    private final Type itemsType = new TypeToken<ArrayList<Integer>>() {
    }.getType();

    private List<MarketWatcherListData> tabs = new ArrayList<>();
    private final Type tabsType = new TypeToken<ArrayList<MarketWatcherListData>>() {
    }.getType();

    @Inject
    public MarketWatcherListDataManager(MarketWatcherPlugin plugin, Client client, ConfigManager configManager, ItemManager itemManager, Gson gson) {
        this.plugin = plugin;
        this.client = client;
        this.configManager = configManager;
        this.itemManager = itemManager;
        this.gson = gson;
    }

    public boolean loadData() {
        // Load later if not at login screen to prevent data loss
        if (client.getGameState().getState() < GameState.LOGIN_SCREEN.getState()) {
            return false;
        }

        // Value
        String value = configManager.getConfiguration(CONFIG_TAB, CONFIG_KEY_VALUE);
        plugin.setValue(Long.parseLong(value));

        // Individual Items
        itemIds.clear();

        String itemsJson = configManager.getConfiguration(CONFIG_TAB, CONFIG_KEY_ITEMIDS);
        if (itemsJson == null || itemsJson.equals(EMPTY_ARRAY)) {
            plugin.setItems(new ArrayList<>());
        } else {
            try {
                itemIds = gson.fromJson(itemsJson, itemsType);
                convertItems();
            } catch (Exception e) {
                log.error(LOAD_ITEM_ERROR, e);
                plugin.setItems(new ArrayList<>());
            }
        }

        // Tabs and their items
        tabs.clear();

        String tabsJson = configManager.getConfiguration(CONFIG_TAB, CONFIG_KEY_TABS);
        if (tabsJson == null || tabsJson.equals(EMPTY_ARRAY)) {
            plugin.setTabs(new ArrayList<>());
        } else {
            try {
                tabs = gson.fromJson(tabsJson, tabsType);
                convertTabs();
            } catch (Exception e) {
                log.error(LOAD_TAB_ERROR, e);
                plugin.setTabs(new ArrayList<>());
            }
        }

        plugin.updateItemPrices();
        return true;
    }

    public void saveData() {
        // Value
        configManager.setConfiguration(CONFIG_TAB, CONFIG_KEY_VALUE, String.valueOf(plugin.getValue()));

        // Individual Items
        itemIds.clear();

        for (MarketWatcherItem item : plugin.getItems()) {
            itemIds.add(item.getItemId());
        }

        final String itemsJson = gson.toJson(itemIds);
        configManager.setConfiguration(CONFIG_TAB, CONFIG_KEY_ITEMIDS, itemsJson);

        // Tabs and their items
        tabs.clear();

        for (MarketWatcherList group : plugin.getTabs()) {
            List<Integer> groupItems = new ArrayList<>();
            for (MarketWatcherItem item : group.getItems()) {
                groupItems.add(item.getItemId());
            }

            tabs.add(new MarketWatcherListData(group.getName(), group.isCollapsed(), groupItems));
        }

        final String tabsJson = gson.toJson(tabs);
        configManager.setConfiguration(CONFIG_TAB, CONFIG_KEY_TABS, tabsJson);
    }

    private void convertItems() {
        List<MarketWatcherItem> watchItems = new ArrayList<>();

        for (Integer itemId : itemIds) {
            watchItems.add(convertIdToItem(itemId));
        }

        plugin.setItems(watchItems);
    }

    private void convertTabs() {
        List<MarketWatcherList> watchTabs = new ArrayList<>();

        for (MarketWatcherListData tab : tabs) {
            List<MarketWatcherItem> tabItems = new ArrayList<>();
            for (Integer itemId : tab.getItems()) {
                tabItems.add(convertIdToItem(itemId));
            }

            watchTabs.add(new MarketWatcherList(tab.getName(), tab.isCollapsed(), tabItems));
        }

        plugin.setTabs(watchTabs);
    }

    private MarketWatcherItem convertIdToItem(int itemId) {
        AsyncBufferedImage itemImage = itemManager.getImage(itemId);
        String itemName = itemManager.getItemComposition(itemId).getName();
        return new MarketWatcherItem(itemImage, itemName, itemId, 0, null, null, null, null, null, null, null, null, null); // Item prices updated after load
    }
}