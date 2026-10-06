/*
 * Copyright (c) 2026, Bob Tabrizi
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
import com.marketwatcher.data.PeriodPrices;
import java.util.List;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.Cursor;
import java.awt.image.BufferedImage;
import net.runelite.client.ui.ColorScheme;
import net.runelite.client.util.ImageUtil;
import net.runelite.client.util.QuantityFormatter;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;

import static com.marketwatcher.utilities.Constants.*;
import static com.marketwatcher.utilities.Constants.STANDARD;
import static com.marketwatcher.utilities.PriceUtils.formatPrice;

public final class PanelUtils
{
	private static final int MAX_NAME_LENGTH = 22;
	private static final ImageIcon SHIFT_UP_ICON;
	private static final ImageIcon SHIFT_UP_HOVER_ICON;
	private static final ImageIcon SHIFT_DOWN_ICON;
	private static final ImageIcon SHIFT_DOWN_HOVER_ICON;

	static
	{
		final BufferedImage shiftUpImage = ImageUtil.loadImageResource(PanelUtils.class, SHIFT_UP_ICON_PATH);
		SHIFT_UP_ICON = new ImageIcon(shiftUpImage);
		SHIFT_UP_HOVER_ICON = new ImageIcon(ImageUtil.alphaOffset(shiftUpImage, 0.53f));

		final BufferedImage shiftDownImage = ImageUtil.loadImageResource(PanelUtils.class, SHIFT_DOWN_ICON_PATH);
		SHIFT_DOWN_ICON = new ImageIcon(shiftDownImage);
		SHIFT_DOWN_HOVER_ICON = new ImageIcon(ImageUtil.alphaOffset(shiftDownImage, 0.53f));
	}

	private PanelUtils()
	{
	}

	/**
	 * Builds the "delete + shift up/down" action column shown next to a tracked item row.
	 * Shared by {@link com.marketwatcher.ui.MarketWatcherItemPanel} (top-level items) and
	 * {@link com.marketwatcher.ui.MarketWatcherTabItemPanel} (items inside a tab), which
	 * previously duplicated this wiring with only the delete icon/border, confirm text, and
	 * target actions differing.
	 *
	 * @param dialogParent    component the delete confirmation dialog is anchored to
	 * @param confirmTitle    title of the delete confirmation dialog
	 * @param confirmMessage  message of the delete confirmation dialog
	 * @param deleteIcon      icon shown for the delete action
	 * @param deleteHoverIcon icon shown while hovering the delete action
	 * @param deleteIconInsets border/insets around the delete icon
	 * @param itemIndex       index of this item within its list
	 * @param itemsSize       size of the list this item belongs to
	 * @param onDelete        invoked if the user confirms deletion
	 * @param onShiftUp       invoked when shifting up is requested (itemIndex &gt; 0)
	 * @param onShiftDown     invoked when shifting down is requested (itemIndex &lt; itemsSize - 1)
	 */
	public static JPanel createItemActionPanel(Component dialogParent, String confirmTitle, String confirmMessage,
		ImageIcon deleteIcon, ImageIcon deleteHoverIcon, Insets deleteIconInsets,
		int itemIndex, int itemsSize, Runnable onDelete, Runnable onShiftUp, Runnable onShiftDown)
	{
		JPanel actionPanel = new JPanel(new BorderLayout());
		actionPanel.setBackground(new Color(0, 0, 0, 0));
		actionPanel.setOpaque(false);

		// Delete Item
		JLabel deleteItem = new JLabel(deleteIcon);
		deleteItem.setBorder(new EmptyBorder(deleteIconInsets.top, deleteIconInsets.left, deleteIconInsets.bottom, deleteIconInsets.right));
		deleteItem.addMouseListener(new MouseAdapter()
		{
			@Override
			public void mouseReleased(MouseEvent e)
			{
				int confirm = JOptionPane.showConfirmDialog(dialogParent, confirmMessage, confirmTitle, JOptionPane.YES_NO_OPTION);
				if (confirm == JOptionPane.YES_OPTION)
				{
					onDelete.run();
				}
			}

			@Override
			public void mouseEntered(MouseEvent e)
			{
				deleteItem.setIcon(deleteHoverIcon);
			}

			@Override
			public void mouseExited(MouseEvent e)
			{
				deleteItem.setIcon(deleteIcon);
			}
		});
		actionPanel.add(deleteItem, BorderLayout.NORTH);

		// Shift Item Panel
		JPanel shiftItemPanel = new JPanel(new BorderLayout());
		shiftItemPanel.setOpaque(false);

		// Shift item up
		JLabel shiftUp = new JLabel(SHIFT_UP_ICON);
		shiftUp.setBorder(new EmptyBorder(0, 0, 15, 5));

		if (itemIndex == 0)
		{
			shiftUp.setIcon(SHIFT_UP_HOVER_ICON);
		}

		shiftUp.addMouseListener(new MouseAdapter()
		{
			@Override
			public void mouseReleased(MouseEvent e)
			{
				if (itemIndex != 0)
				{
					onShiftUp.run();
				}
			}

			@Override
			public void mouseEntered(MouseEvent e)
			{
				shiftUp.setIcon(SHIFT_UP_HOVER_ICON);
			}

			@Override
			public void mouseExited(MouseEvent e)
			{
				if (itemIndex != 0)
				{
					shiftUp.setIcon(SHIFT_UP_ICON);
				}
			}
		});
		shiftItemPanel.add(shiftUp, BorderLayout.NORTH);

		// Shift item down
		JLabel shiftDown = new JLabel(SHIFT_DOWN_ICON);
		shiftDown.setBorder(new EmptyBorder(15, 0, 20, 5));

		if (itemIndex == itemsSize - 1)
		{
			shiftDown.setIcon(SHIFT_DOWN_HOVER_ICON);
		}

		shiftDown.addMouseListener(new MouseAdapter()
		{
			@Override
			public void mouseReleased(MouseEvent e)
			{
				if (itemIndex != itemsSize - 1)
				{
					onShiftDown.run();
				}
			}

			@Override
			public void mouseEntered(MouseEvent e)
			{
				shiftDown.setIcon(SHIFT_DOWN_HOVER_ICON);
			}

			@Override
			public void mouseExited(MouseEvent e)
			{
				if (itemIndex != itemsSize - 1)
				{
					shiftDown.setIcon(SHIFT_DOWN_ICON);
				}
			}
		});
		shiftItemPanel.add(shiftDown, BorderLayout.EAST);
		actionPanel.add(shiftItemPanel, BorderLayout.SOUTH);

		return actionPanel;
	}

	public static JPanel createRightPanel(MarketWatcherItem item, MarketWatcherPlugin plugin, String viewType)
	{
		// Image
		JLabel itemImage = new JLabel();
		itemImage.setMinimumSize(new Dimension(32, 32));
		itemImage.setPreferredSize(new Dimension(32, 32));
		itemImage.setMaximumSize(new Dimension(32, 32));

		if (item.getImage() != null)
		{
			item.getImage().addTo(itemImage);
		}

		itemImage.setToolTipText("Open Wiki Price Page");
		itemImage.addMouseListener(new MouseAdapter()
		{
			@Override
			public void mouseEntered(MouseEvent e)
			{
				itemImage.setCursor(new Cursor(Cursor.HAND_CURSOR));
			}

			@Override
			public void mouseExited(MouseEvent e)
			{
				itemImage.setCursor(new Cursor(Cursor.DEFAULT_CURSOR));
			}

			@Override
			public void mouseReleased(MouseEvent e)
			{
				plugin.openWikiPriceLink(item.getItemId());
			}
		});


		GridBagConstraints gbc = new GridBagConstraints();
		// Item Details Panel
		JPanel rightPanel = new JPanel(new GridBagLayout());
		rightPanel.setBackground(new Color(0, 0, 0, 0));

		// Item Name
		JLabel itemName = new JLabel();
		itemName.setForeground(Color.WHITE);

		String dispLabel = item.getName();
		if (dispLabel.length() > MAX_NAME_LENGTH)
		{
			dispLabel = dispLabel.substring(0, MAX_NAME_LENGTH - TRUNCATION_STRING.length()) + TRUNCATION_STRING;
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

		int topBottomInset = viewType.equals(STANDARD) ? 1 : 3;
		Insets medPriceInsets = viewType.equals(STANDARD) ? new Insets(1, 3, 1, 3) : new Insets(3, 3, 3, 3);
		Insets lowHighPriceInsets = viewType.equals(STANDARD) ? new Insets(1, 0, 1, 0) : new Insets(3, 0, 3, 3);

		boolean isColorBlindMode = plugin.getConfig().colorBlindMode();
		Color lowColor = isColorBlindMode ? new Color(136, 204, 238) : Color.GREEN;
		Color medColor = isColorBlindMode ? new Color(221, 204, 119) : Color.YELLOW;
		Color highColor = isColorBlindMode ? new Color(170, 68, 153) : Color.RED;

		List<PeriodPrices> periodPrices = item.getPeriodPrices();
		for (int period = 0; period < periodPrices.size(); period++)
		{
			PeriodPrices prices = periodPrices.get(period);
			int row = period + 2;

			// The first row has extra space above it to separate it from the GE price
			Insets labelInsets = period == 0 ? new Insets(5, 0, topBottomInset, 0) : new Insets(topBottomInset, 0, topBottomInset, 0);
			Insets lowHighInsets = period == 0 ? new Insets(5, 0, topBottomInset, 0) : lowHighPriceInsets;
			Insets medInsets = period == 0 ? new Insets(5, 3, topBottomInset, 3) : medPriceInsets;

			JLabel timeType = new JLabel();
			timeType.setForeground(Color.WHITE);

			int periodQty = plugin.getPeriodQuantity(period);
			String periodType = plugin.getPeriodType(period).name();

			timeType.setText(Integer.toString(periodQty) + periodType.charAt(0) + ":");
			timeType.setToolTipText(Integer.toString(periodQty) + " " + periodType);
			rightPanel.add(timeType, getGbc(gbc, 0, row, 1, 1, 0, 0, labelInsets));

			rightPanel.add(createPriceLabel(LOW, prices.getLow(), lowColor, viewType), getGbc(gbc, 1, row, 1, 1, 0, 0, lowHighInsets));
			rightPanel.add(createPriceLabel(MED, prices.getMed(), medColor, viewType), getGbc(gbc, 2, row, 1, 1, 0, 0, medInsets));
			rightPanel.add(createPriceLabel(HIGH, prices.getHigh(), highColor, viewType), getGbc(gbc, 3, row, 1, 1, 0, 0, lowHighInsets));
		}

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

	private static JLabel createPriceLabel(String priceType, Long price, Color color, String viewType)
	{
		JLabel label = new JLabel();
		label.setForeground(color);
		label.setText(formatPrice(price, viewType));
		label.setToolTipText(priceType + ": " + formatTooltip(price));
		return label;
	}

	public static String formatTooltip(Long price)
	{
		return price == null ? NOT_AVAILABLE : QuantityFormatter.formatNumber(price);
	}
}
