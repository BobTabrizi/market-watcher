package com.marketwatcher.utilities;

import java.text.DecimalFormat;

import static com.marketwatcher.ui.Constants.*;

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
}