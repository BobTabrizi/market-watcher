package com.marketwatcher.utilities;

import com.marketwatcher.data.MarketWatcherItem;
import net.runelite.client.util.AsyncBufferedImage;

import java.text.DecimalFormat;
import java.util.Map;

import static com.marketwatcher.utilities.Constants.*;

public final class PriceUtils {

    private PriceUtils() {
    }

    public static String standardPricePadder(String price) {
        if (price.length() == 1) {
            price = price + "\u2800" + "\u2800" + "\u2800" + "\u202F" + "\u202F";
            return price;
        }
        if (price.length() == 2) {
            price = price + "\u2800" + "\u2800" + "\u202F" + "\u202F";
            return price;
        }
        if (price.length() == 3) {
            price = price + "\u2800" + "\u202F" + "\u202F" + "\u202F";
            return price;
        }
        if (price.length() == 4 && !price.contains(M_MILLION) && !price.contains(K_THOUSAND)) {
            price = price + "\u2800" + "\u202F";
            return price;
        }
        if (price.length() == 4 && (price.contains(M_MILLION) || price.contains(K_THOUSAND))) {
            price = price + "\u2800" + "\u202F" + "\u202F";
            return price;
        }
        if (price.length() == 5 && !price.contains(M_MILLION) && !price.contains(K_THOUSAND) && !price.contains(B_BILLION)) {
            price = price + "\u2800";
            return price;
        }

        if (price.length() == 5 && price.contains(K_THOUSAND)) {
            price = price + "\u2800";
            return price;
        }
        if (price.length() == 5 && price.contains(M_MILLION)) {
            price = price + "\u202F" + "\u202F";
            return price;
        }
        if (price.length() == 5 && price.contains(B_BILLION)) {
            price = price + "\u2800" + "\u202F";
            return price;
        }

        if (price.length() == 6) {
            price = price + "\u202F";
            return price;
        }

        return price;
    }

    public static String compactPricePadder(String price) {
        if (price.length() == 1) {
            price = price + "\u2800" + "\u2800" + "\u2800";
            return price;
        }
        if (price.length() == 2) {
            price = price + "\u2800" + "\u2800" + "\u202F";
            return price;
        }
        if (price.length() == 3) {
            price = price + "\u2800" + "\u202F" + "\u202F";
            return price;
        }
        if (price.length() == 4 && !price.contains(M_MILLION) && !price.contains(K_THOUSAND)) {
            price = price + "\u202F" + "\u202F" + "\u202F";
            return price;
        }
        if (price.length() == 4 && (price.contains(M_MILLION) || price.contains(K_THOUSAND))) {
            price = price + "\u2800" + "\u202F" + "\u202F";
            return price;
        }
        if (price.length() == 5 && !price.contains(M_MILLION) && !price.contains(K_THOUSAND) && !price.contains(B_BILLION)) {
            price = price + "\u202F" + "\u202F" + "\u202F";
            return price;
        }

        if (price.length() == 5 && price.contains(K_THOUSAND)) {
            price = price + "\u202F" + "\u202F";
            return price;
        }
        if (price.length() == 5 && price.contains(M_MILLION)) {
            price = price + "\u202F";
            return price;
        }
        if (price.length() == 5 && price.contains(B_BILLION)) {
            price = price + "\u2800";
            return price;
        }

        return price;
    }

    public static String[] manageItemPrices(String lowPrice, String medPrice, String highPrice, String viewType) {
        final DecimalFormat df = new DecimalFormat("0.0");
        final DecimalFormat df2 = new DecimalFormat("0.00");
        highPrice = truncatePrices(highPrice, df, df2, viewType);
        medPrice = truncatePrices(medPrice, df, df2, viewType);
        lowPrice = truncatePrices(lowPrice, df, df2, viewType);

        return new String[]{lowPrice, medPrice, highPrice};
    }

    public static String truncatePrices(String price, DecimalFormat df, DecimalFormat df2, String viewType) {
        if (price.equals(NULL)) {
            price = NOT_AVAILABLE;
            return price;
        }

        if (price.length() >= 5 && price.length() < 7) {
            price = df.format((float) Integer.parseInt(price) / 1000) + K_THOUSAND;
        } else if (price.length() >= 7 && price.length() < 10) {
            price = df.format((float) Integer.parseInt(price) / 1000000) + M_MILLION;
        } else if (price.length() >= 10) {
            price = df2.format((float) Integer.parseInt(price) / 1000000000) + B_BILLION;
        }

        if (viewType.equals(STANDARD)) {
            price = standardPricePadder(price);
        } else if (viewType.equals(COMPACT)) {
            price = compactPricePadder(price);
        }
        return price;
    }

    public static MarketWatcherItem createMarketWatchItemWithPriceMap(AsyncBufferedImage itemImage, String itemName, int itemId, int itemPrice, Map<String, String> itemPriceMap) {
        String oneWeekLow = NOT_AVAILABLE;
        String oneWeekMed = NOT_AVAILABLE;
        String oneWeekHigh = NOT_AVAILABLE;

        String oneMonthLow = NOT_AVAILABLE;
        String oneMonthMed = NOT_AVAILABLE;
        String oneMonthHigh = NOT_AVAILABLE;

        String threeMonthLow = NOT_AVAILABLE;
        String threeMonthMed = NOT_AVAILABLE;
        String threeMonthHigh = NOT_AVAILABLE;

        if (itemPriceMap != null) {
            oneWeekLow = itemPriceMap.get(ONE_WEEK_LOW) != null ? itemPriceMap.get(ONE_WEEK_LOW) : NOT_AVAILABLE;
            oneWeekMed = itemPriceMap.get(ONE_WEEK_MED) != null ? itemPriceMap.get(ONE_WEEK_MED) : NOT_AVAILABLE;
            oneWeekHigh = itemPriceMap.get(ONE_WEEK_HIGH) != null ? itemPriceMap.get(ONE_WEEK_HIGH) : NOT_AVAILABLE;

            oneMonthLow = itemPriceMap.get(ONE_MONTH_LOW) != null ? itemPriceMap.get(ONE_MONTH_LOW) : NOT_AVAILABLE;
            oneMonthMed = itemPriceMap.get(ONE_MONTH_MED) != null ? itemPriceMap.get(ONE_MONTH_MED) : NOT_AVAILABLE;
            oneMonthHigh = itemPriceMap.get(ONE_MONTH_HIGH) != null ? itemPriceMap.get(ONE_MONTH_HIGH) : NOT_AVAILABLE;

            threeMonthLow = itemPriceMap.get(THREE_MONTHS_LOW) != null ? itemPriceMap.get(THREE_MONTHS_LOW) : NOT_AVAILABLE;
            threeMonthMed = itemPriceMap.get(THREE_MONTHS_MED) != null ? itemPriceMap.get(THREE_MONTHS_MED) : NOT_AVAILABLE;
            threeMonthHigh = itemPriceMap.get(THREE_MONTHS_HIGH) != null ? itemPriceMap.get(THREE_MONTHS_HIGH) : NOT_AVAILABLE;
        }

        return new MarketWatcherItem(itemImage, itemName, itemId, itemPrice, oneWeekLow, oneWeekMed, oneWeekHigh, oneMonthLow, oneMonthMed, oneMonthHigh, threeMonthLow, threeMonthMed, threeMonthHigh);
    }


}