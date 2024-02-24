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
package com.marketwatcher.utilities;

import com.marketwatcher.MarketWatcherPlugin;
import com.marketwatcher.data.MarketWatcherItem;
import net.runelite.client.ui.ColorScheme;
import net.runelite.client.util.QuantityFormatter;

import javax.swing.*;
import java.awt.*;

import static com.marketwatcher.utilities.Constants.*;
import static com.marketwatcher.utilities.Constants.STANDARD;
import static com.marketwatcher.utilities.PriceUtils.manageItemPrices;

public final class PanelUtils
{

	private PanelUtils()
	{
	}

	public static JPanel createRightPanel(MarketWatcherItem item, MarketWatcherPlugin plugin,  String viewType)
	{
		// Image
		JLabel itemImage = new JLabel();
		itemImage.setMinimumSize(new Dimension(32, 32));
		itemImage.setPreferredSize(new Dimension(32, 32));
		itemImage.setMaximumSize(new Dimension(32, 32));

		String periodOneLow = item.getPeriodOneLow();
		String periodOneMed = item.getPeriodOneMed();
		String periodOneHigh = item.getPeriodOneHigh();
		String periodTwoLow = item.getPeriodTwoLow();
		String periodTwoMed = item.getPeriodTwoMed();
		String periodTwoHigh = item.getPeriodTwoHigh();
		String periodThreeLow = item.getPeriodThreeLow();
		String periodThreeMed = item.getPeriodThreeMed();
		String periodThreeHigh = item.getPeriodThreeHigh();

		String[] periodOnePrices = manageItemPrices(periodOneLow, periodOneMed, periodOneHigh, viewType);
		String[] periodTwoPrices = manageItemPrices(periodTwoLow, periodTwoMed, periodTwoHigh, viewType);
		String[] periodThreePrices = manageItemPrices(periodThreeLow, periodThreeMed, periodThreeHigh, viewType);

		if (item.getImage() != null)
		{
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
		if (dispLabel.length() == 22)
		{
			dispLabel = dispLabel.concat(TRUNCATION_STRING);
		}
		itemName.setText(dispLabel);
		itemName.setToolTipText(item.getName());
		itemName.setMaximumSize(new Dimension(30, 15));

		rightPanel.add(itemImage, getGbc(gbc, 0, 0, 1, 2, 0, 0, new Insets(0, 0, 0, 3)));
		rightPanel.add(itemName, getGbc(gbc, 1, 0, 5, 1, 0, 0, new Insets(0, 0, 0, 0)));

		// GE Price
		JLabel gePriceLabel = new JLabel();
		if (item.getGePrice() > 0)
		{
			gePriceLabel.setText(QuantityFormatter.formatNumber(item.getGePrice()) + GP);
		}
		else
		{
			gePriceLabel.setText(NOT_AVAILABLE);
		}
		gePriceLabel.setForeground(ColorScheme.GRAND_EXCHANGE_PRICE);
		rightPanel.add(gePriceLabel, getGbc(gbc, 1, 1, 5, 1, 0, 0, new Insets(0, 0, 0, 0)));

		Integer topBottomInset = viewType.equals(STANDARD) ? 1 : 3;
		Insets medPriceInsets = viewType.equals(STANDARD) ? new Insets(1, 3, 1, 3) : new Insets(3, 3, 3, 3);
		Insets lowHighPriceInsets = viewType.equals(STANDARD) ? new Insets(1, 0, 1, 0) : new Insets(3, 0, 3, 3);

		JLabel timeType = new JLabel();

		timeType.setForeground(Color.WHITE);

		int periodOneQty = plugin.configPricePeriodOneQty;
		String periodOneType = plugin.configPeriodOneType.name();

		timeType.setText(Integer.toString(periodOneQty) + periodOneType.charAt(0) + ":");
		timeType.setToolTipText(Integer.toString(periodOneQty) + " " + periodOneType);
		rightPanel.add(timeType, getGbc(gbc, 0, 2, 1, 1, 0, 0, new Insets(5, 0, topBottomInset, 0)));

		JLabel lowPeriodOnePrice = new JLabel();

		lowPeriodOnePrice.setForeground(Color.GREEN);
		lowPeriodOnePrice.setText(periodOnePrices[0]);
		lowPeriodOnePrice.setToolTipText(formatTooltip(periodOneLow));
		rightPanel.add(lowPeriodOnePrice, getGbc(gbc, 1, 2, 1, 1, 0, 0, new Insets(5, 0, topBottomInset, 0)));

		JLabel medPeriodOnePrice = new JLabel();

		medPeriodOnePrice.setForeground(Color.YELLOW);
		medPeriodOnePrice.setText(periodOnePrices[1]);
		medPeriodOnePrice.setToolTipText(formatTooltip(periodOneMed));
		rightPanel.add(medPeriodOnePrice, getGbc(gbc, 2, 2, 1, 1, 0, 0, new Insets(5, 3, topBottomInset, 3)));
		JLabel highPeriodOnePrice = new JLabel();

		highPeriodOnePrice.setForeground(Color.RED);
		highPeriodOnePrice.setText(periodOnePrices[2]);
		highPeriodOnePrice.setToolTipText(formatTooltip(periodOneHigh));
		rightPanel.add(highPeriodOnePrice, getGbc(gbc, 3, 2, 1, 1, 0, 0, new Insets(5, 0, topBottomInset, 0)));

		JLabel timeType2 = new JLabel();

		timeType2.setForeground(Color.WHITE);


		int periodTwoQty = plugin.configPricePeriodTwoQty;
		String periodTwoType = plugin.configPeriodTwoType.name();

		timeType2.setText(Integer.toString(periodTwoQty) + periodTwoType.charAt(0) + ":");
		timeType2.setToolTipText(Integer.toString(periodTwoQty) + " " + periodTwoType);

		rightPanel.add(timeType2, getGbc(gbc, 0, 3, 1, 1, 0, 0, new Insets(topBottomInset, 0, topBottomInset, 0)));

		JLabel lowPeriodTwoPrice = new JLabel();

		lowPeriodTwoPrice.setForeground(Color.GREEN);
		lowPeriodTwoPrice.setText(periodTwoPrices[0]);
		lowPeriodTwoPrice.setToolTipText(formatTooltip(periodTwoLow));
		rightPanel.add(lowPeriodTwoPrice, getGbc(gbc, 1, 3, 1, 1, 0, 0, lowHighPriceInsets));

		JLabel medPeriodTwoPrice = new JLabel();

		medPeriodTwoPrice.setForeground(Color.YELLOW);
		medPeriodTwoPrice.setText(periodTwoPrices[1]);
		medPeriodTwoPrice.setToolTipText(formatTooltip(periodTwoMed));
		rightPanel.add(medPeriodTwoPrice, getGbc(gbc, 2, 3, 1, 1, 0, 0, medPriceInsets));

		JLabel highPeriodTwoPrice = new JLabel();

		highPeriodTwoPrice.setForeground(Color.RED);
		highPeriodTwoPrice.setText(periodTwoPrices[2]);
		highPeriodTwoPrice.setToolTipText(formatTooltip(periodTwoHigh));
		rightPanel.add(highPeriodTwoPrice, getGbc(gbc, 3, 3, 1, 1, 0, 0, lowHighPriceInsets));

		JLabel timeType3 = new JLabel();

		timeType3.setForeground(Color.WHITE);

		int periodThreeQty = plugin.configPricePeriodThreeQty;
		String periodThreeType = plugin.configPeriodThreeType.name();

		timeType3.setText(Integer.toString(periodThreeQty) + periodThreeType.charAt(0) + ":");
		timeType3.setToolTipText(Integer.toString(periodThreeQty) + " " + periodThreeType);

		rightPanel.add(timeType3, getGbc(gbc, 0, 4, 1, 1, 0, 0, new Insets(topBottomInset, 0, topBottomInset, 0)));

		JLabel lowPeriodThreePrice = new JLabel();

		lowPeriodThreePrice.setForeground(Color.GREEN);
		lowPeriodThreePrice.setText(periodThreePrices[0]);
		lowPeriodThreePrice.setToolTipText(formatTooltip(periodThreeLow));
		rightPanel.add(lowPeriodThreePrice, getGbc(gbc, 1, 4, 1, 1, 0, 0, lowHighPriceInsets));

		JLabel medPeriodThreePrice = new JLabel();

		medPeriodThreePrice.setForeground(Color.YELLOW);
		medPeriodThreePrice.setText(periodThreePrices[1]);
		medPeriodThreePrice.setToolTipText(formatTooltip(periodThreeMed));
		rightPanel.add(medPeriodThreePrice, getGbc(gbc, 2, 4, 1, 1, 0, 0, medPriceInsets));

		JLabel highPeriodThreePrice = new JLabel();

		highPeriodThreePrice.setForeground(Color.RED);
		highPeriodThreePrice.setText(periodThreePrices[2]);
		highPeriodThreePrice.setToolTipText(formatTooltip(periodThreeHigh));
		rightPanel.add(highPeriodThreePrice, getGbc(gbc, 3, 4, 1, 1, 0, 0, lowHighPriceInsets));

		return rightPanel;
	}

	public static GridBagConstraints getGbc(GridBagConstraints gbc, int gridx, int gridy, int gridWidth, int gridHeight, int paddingX, int paddingY, Insets insets)
	{
		if ((gridx == 1 && gridy == 0) || (gridx == 0 && gridy == 0))
		{
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

	public static String formatTooltip(String tooltip)
	{

		if (tooltip.equals(NOT_AVAILABLE) || tooltip.equals(NULL))
		{
			return NOT_AVAILABLE;
		}
		else
		{
			return QuantityFormatter.formatNumber(Integer.parseInt(tooltip));
		}
	}
}