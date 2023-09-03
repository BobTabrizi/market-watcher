package com.marketwatcher;

import net.runelite.client.RuneLite;
import net.runelite.client.externalplugins.ExternalPluginManager;

public class MarketWatcherPluginTest {
    public static void main(String[] args) throws Exception {
        ExternalPluginManager.loadBuiltin(MarketWatcherPlugin.class);
        RuneLite.main(args);
    }
}