package com.marketwatcher.data;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;

import static com.marketwatcher.ui.Constants.NOT_AVAILABLE;

import net.runelite.client.util.AsyncBufferedImage;

@AllArgsConstructor
public class MarketWatcherItem implements Comparable<MarketWatcherItem> {
    @Getter
    private AsyncBufferedImage image;

    @Getter
    private String name;

    @Getter
    private int itemId;

    @Getter
    @Setter
    private int gePrice;

    @Getter
    @Setter
    private String oneWeekLow;
    @Getter
    @Setter
    private String oneWeekMed;
    @Getter
    @Setter
    private String oneWeekHigh;
    @Getter
    @Setter
    private String oneMonthLow;
    @Getter
    @Setter
    private String oneMonthMed;
    @Getter
    @Setter
    private String oneMonthHigh;
    @Getter
    @Setter
    private String threeMonthLow;
    @Getter
    @Setter
    private String threeMonthMed;
    @Getter
    @Setter
    private String threeMonthHigh;


    public MarketWatcherItem(AsyncBufferedImage itemImage, String itemName, int itemID, int itemPrice) {
        image = itemImage;
        name = itemName;
        itemId = itemID;
        gePrice = itemPrice;
        oneWeekLow = NOT_AVAILABLE;
        oneWeekMed = NOT_AVAILABLE;
        oneWeekHigh = NOT_AVAILABLE;
        oneMonthLow = NOT_AVAILABLE;
        oneMonthMed = NOT_AVAILABLE;
        oneMonthHigh = NOT_AVAILABLE;
        threeMonthLow = NOT_AVAILABLE;
        threeMonthMed = NOT_AVAILABLE;
        threeMonthHigh = NOT_AVAILABLE;
    }

    @Override
    public boolean equals(Object obj) {
        if (!(obj instanceof MarketWatcherItem)) {
            return false;
        }

        final MarketWatcherItem item = (MarketWatcherItem) obj;
        return item.getItemId() == this.itemId;
    }

    @Override
    public int compareTo(MarketWatcherItem other) {
        return Integer.compare(gePrice, other.getGePrice());
    }
}