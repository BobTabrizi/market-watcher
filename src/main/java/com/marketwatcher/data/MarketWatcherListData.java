package com.marketwatcher.data;

import lombok.AllArgsConstructor;
import lombok.Getter;

import java.util.List;

@AllArgsConstructor
@Getter
public class MarketWatcherListData {
    private String name;
    private boolean collapsed;
    private List<Integer> items;
}