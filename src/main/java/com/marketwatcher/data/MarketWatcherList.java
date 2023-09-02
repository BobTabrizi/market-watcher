package com.marketwatcher.data;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;

import java.util.List;

@AllArgsConstructor
public class MarketWatcherList {
    @Getter
    @Setter
    private String name;

    @Getter
    @Setter
    private boolean collapsed;

    @Getter
    private final List<MarketWatcherItem> items;

    public MarketWatcherList(String name, List<MarketWatcherItem> items) {
        this.name = name;
        collapsed = false;
        this.items = items;
    }

    @Override
    public boolean equals(Object obj) {
        if (!(obj instanceof MarketWatcherList)) {
            return false;
        }

        final MarketWatcherList group = (MarketWatcherList) obj;
        return group.getName().equals(this.name);
    }
}