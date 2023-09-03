package com.marketwatcher.utilities;

import com.marketwatcher.data.MarketWatcherItem;
import net.runelite.client.ui.ColorScheme;
import net.runelite.client.util.QuantityFormatter;

import javax.swing.*;
import java.awt.*;

import static com.marketwatcher.utilities.Constants.*;
import static com.marketwatcher.utilities.Constants.STANDARD;
import static com.marketwatcher.utilities.PriceUtils.manageItemPrices;

public final class PanelUtils {

    private PanelUtils() {
    }

    public static JPanel createRightPanel(MarketWatcherItem item, String viewType) {
        // Image
        JLabel itemImage = new JLabel();
        itemImage.setMinimumSize(new Dimension(32, 32));
        itemImage.setPreferredSize(new Dimension(32, 32));
        itemImage.setMaximumSize(new Dimension(32, 32));

        String weekLow = item.getOneWeekLow();
        String weekMed = item.getOneWeekMed();
        String weekHigh = item.getOneWeekHigh();
        String monthLow = item.getOneMonthLow();
        String monthMed = item.getOneMonthMed();
        String monthHigh = item.getOneMonthHigh();
        String threeMonthLow = item.getThreeMonthLow();
        String threeMonthMed = item.getThreeMonthMed();
        String threeMonthHigh = item.getThreeMonthHigh();

        String[] weekPrices = manageItemPrices(weekLow, weekMed, weekHigh, viewType);
        String[] monthPrices = manageItemPrices(monthLow, monthMed, monthHigh, viewType);
        String[] threeMonthPrices = manageItemPrices(threeMonthLow, threeMonthMed, threeMonthHigh, viewType);

        if (item.getImage() != null) {
            item.getImage().addTo(itemImage);
        }

        GridBagConstraints gbc = new GridBagConstraints();
        // Item Details Panel
        JPanel rightPanel = new JPanel(new GridBagLayout());
        rightPanel.setBackground(new Color(0, 0, 0, 0));

        // Item Name
        JLabel itemName = new JLabel();
        itemName.setForeground(Color.WHITE);

        String dispLabel = item.getName().substring(0, Math.min(item.getName().length(), 22));
        if (dispLabel.length() == 22) {
            dispLabel = dispLabel.concat(TRUNCATION_STRING);
        }
        itemName.setText(dispLabel);
        itemName.setToolTipText(item.getName());
        itemName.setPreferredSize(new Dimension(125, 15));
        rightPanel.add(itemImage, getGbc(gbc, 0, 0, 1, 2, 0, 0, new Insets(0, 0, 0, 3)));
        rightPanel.add(itemName, getGbc(gbc, 1, 0, 5, 1, 0, 0, new Insets(0, 0, 0, 0)));

        // GE Price
        JLabel gePriceLabel = new JLabel();
        if (item.getGePrice() > 0) {
            gePriceLabel.setText(QuantityFormatter.formatNumber(item.getGePrice()) + GP);
        } else {
            gePriceLabel.setText(NOT_AVAILABLE);
        }
        gePriceLabel.setForeground(ColorScheme.GRAND_EXCHANGE_PRICE);
        rightPanel.add(gePriceLabel, getGbc(gbc, 1, 1, 5, 1, 0, 0, new Insets(0, 0, 0, 0)));

        Integer topBottomInset = viewType.equals(STANDARD) ? 1 : 3;
        Insets medPriceInsets = viewType.equals(STANDARD) ? new Insets(1, 3, 1, 3) : new Insets(3, 3, 3, 3);
        Insets lowHighPriceInsets = viewType.equals(STANDARD) ? new Insets(1, 0, 1, 0) : new Insets(3, 0, 3, 3);

        JLabel timeType = new JLabel();

        timeType.setForeground(Color.WHITE);
        timeType.setText("1W:");
        timeType.setToolTipText("1 Week");
        rightPanel.add(timeType, getGbc(gbc, 0, 2, 1, 1, 0, 0, new Insets(5, 0, topBottomInset, 0)));

        JLabel lowWeekPrice = new JLabel();

        lowWeekPrice.setForeground(Color.GREEN);
        lowWeekPrice.setText(weekPrices[0]);
        lowWeekPrice.setToolTipText(formatTooltip(weekLow));
        rightPanel.add(lowWeekPrice, getGbc(gbc, 1, 2, 1, 1, 0, 0, new Insets(5, 0, topBottomInset, 0)));

        JLabel medWeekPrice = new JLabel();

        medWeekPrice.setForeground(Color.YELLOW);
        medWeekPrice.setText(weekPrices[1]);
        medWeekPrice.setToolTipText(formatTooltip(weekMed));
        rightPanel.add(medWeekPrice, getGbc(gbc, 2, 2, 1, 1, 0, 0, new Insets(5, 3, topBottomInset, 3)));
        JLabel highWeekPrice = new JLabel();

        highWeekPrice.setForeground(Color.RED);
        highWeekPrice.setText(weekPrices[2]);
        highWeekPrice.setToolTipText(formatTooltip(weekHigh));
        rightPanel.add(highWeekPrice, getGbc(gbc, 3, 2, 1, 1, 0, 0, new Insets(5, 0, topBottomInset, 0)));

        JLabel timeType2 = new JLabel();

        timeType2.setForeground(Color.WHITE);
        timeType2.setText("1M:");
        timeType2.setToolTipText("1 Month");
        rightPanel.add(timeType2, getGbc(gbc, 0, 3, 1, 1, 0, 0, new Insets(topBottomInset, 0, topBottomInset, 0)));

        JLabel lowMonthPrice = new JLabel();

        lowMonthPrice.setForeground(Color.GREEN);
        lowMonthPrice.setText(monthPrices[0]);
        lowMonthPrice.setToolTipText(formatTooltip(monthLow));
        rightPanel.add(lowMonthPrice, getGbc(gbc, 1, 3, 1, 1, 0, 0, lowHighPriceInsets));

        JLabel medMonthPrice = new JLabel();

        medMonthPrice.setForeground(Color.YELLOW);
        medMonthPrice.setText(monthPrices[1]);
        medMonthPrice.setToolTipText(formatTooltip(monthMed));
        rightPanel.add(medMonthPrice, getGbc(gbc, 2, 3, 1, 1, 0, 0, medPriceInsets));

        JLabel highMonthPrice = new JLabel();

        highMonthPrice.setForeground(Color.RED);
        highMonthPrice.setText(monthPrices[2]);
        highMonthPrice.setToolTipText(formatTooltip(monthHigh));
        rightPanel.add(highMonthPrice, getGbc(gbc, 3, 3, 1, 1, 0, 0, lowHighPriceInsets));

        JLabel timeType3 = new JLabel();

        timeType3.setForeground(Color.WHITE);
        timeType3.setText("3M:");
        timeType3.setToolTipText("3 Months");
        rightPanel.add(timeType3, getGbc(gbc, 0, 4, 1, 1, 0, 0, new Insets(topBottomInset, 0, topBottomInset, 0)));

        JLabel low3MonthPrice = new JLabel();

        low3MonthPrice.setForeground(Color.GREEN);
        low3MonthPrice.setText(threeMonthPrices[0]);
        low3MonthPrice.setToolTipText(formatTooltip(threeMonthLow));
        rightPanel.add(low3MonthPrice, getGbc(gbc, 1, 4, 1, 1, 0, 0, lowHighPriceInsets));

        JLabel med3MonthPrice = new JLabel();

        med3MonthPrice.setForeground(Color.YELLOW);
        med3MonthPrice.setText(threeMonthPrices[1]);
        med3MonthPrice.setToolTipText(formatTooltip(threeMonthMed));
        rightPanel.add(med3MonthPrice, getGbc(gbc, 2, 4, 1, 1, 0, 0, medPriceInsets));

        JLabel high3MonthPrice = new JLabel();

        high3MonthPrice.setForeground(Color.RED);
        high3MonthPrice.setText(threeMonthPrices[2]);
        high3MonthPrice.setToolTipText(formatTooltip(threeMonthHigh));
        rightPanel.add(high3MonthPrice, getGbc(gbc, 3, 4, 1, 1, 0, 0, lowHighPriceInsets));

        return rightPanel;
    }

    public static GridBagConstraints getGbc(GridBagConstraints gbc, int gridx, int gridy, int gridWidth, int gridHeight, int paddingX, int paddingY, Insets insets) {
        if ((gridx == 1 && gridy == 0) || (gridx == 0 && gridy == 0)) {
            gbc.fill = GridBagConstraints.HORIZONTAL;
        }
        gbc.gridwidth = gridWidth;
        gbc.gridheight = gridHeight;
        gbc.weightx = 0;
        gbc.weighty = 0;
        gbc.gridx = gridx;
        gbc.gridy = gridy;
        gbc.ipadx = paddingX;
        gbc.ipady = paddingY;
        gbc.insets = insets;

        return gbc;
    }

    public static String formatTooltip(String tooltip) {

        if (tooltip.equals(NOT_AVAILABLE) || tooltip.equals(NULL)) {
            return NOT_AVAILABLE;
        } else {
            return QuantityFormatter.formatNumber(Integer.parseInt(tooltip));
        }
    }
}