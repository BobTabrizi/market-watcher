package com.marketwatcher.data;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;

import java.util.List;

@AllArgsConstructor
public class MarketWatcherTab {
    @Getter
    @Setter
    private String name;

    @Getter
    @Setter
    private boolean collapsed;

    @Getter
    private final List<MarketWatcherItem> items;

    public MarketWatcherTab(String name, List<MarketWatcherItem> items) {
        this.name = name;
        collapsed = false;
        this.items = items;
    }

    @Override
    public boolean equals(Object obj) {
        if (!(obj instanceof MarketWatcherTab)) {
            return false;
        }

        final MarketWatcherTab tab = (MarketWatcherTab) obj;
        return tab.getName().equals(this.name);
    }
}