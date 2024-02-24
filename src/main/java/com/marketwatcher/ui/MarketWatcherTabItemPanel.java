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
package com.marketwatcher.ui;

import com.marketwatcher.MarketWatcherPlugin;
import com.marketwatcher.data.MarketWatcherTab;
import com.marketwatcher.data.MarketWatcherItem;

import static com.marketwatcher.utilities.Constants.*;
import static com.marketwatcher.utilities.PanelUtils.createRightPanel;

import net.runelite.client.ui.ColorScheme;
import net.runelite.client.util.ImageUtil;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.image.BufferedImage;

public class MarketWatcherTabItemPanel extends JPanel
{
	private static final String REMOVE_TITLE = "Warning";
	private static final String REMOVE_MESSAGE = "Are you sure you want to remove this item from the tab?";
	private static final ImageIcon REMOVE_ICON;
	private static final ImageIcon REMOVE_HOVER_ICON;
	private static final ImageIcon SHIFT_UP_ICON;
	private static final ImageIcon SHIFT_UP_HOVER_ICON;
	private static final ImageIcon SHIFT_DOWN_ICON;
	private static final ImageIcon SHIFT_DOWN_HOVER_ICON;

	static
	{
		final BufferedImage removeImage = ImageUtil.loadImageResource(MarketWatcherPluginPanel.class, DELETE_ICON_PATH);
		REMOVE_ICON = new ImageIcon(removeImage);
		REMOVE_HOVER_ICON = new ImageIcon(ImageUtil.alphaOffset(removeImage, 0.53f));

		final BufferedImage shiftUpImage = ImageUtil.loadImageResource(MarketWatcherPlugin.class, SHIFT_UP_ICON_PATH);
		SHIFT_UP_ICON = new ImageIcon(shiftUpImage);
		SHIFT_UP_HOVER_ICON = new ImageIcon(ImageUtil.alphaOffset(shiftUpImage, 0.53f));

		final BufferedImage shiftDownImage = ImageUtil.loadImageResource(MarketWatcherPlugin.class, SHIFT_DOWN_ICON_PATH);
		SHIFT_DOWN_ICON = new ImageIcon(shiftDownImage);
		SHIFT_DOWN_HOVER_ICON = new ImageIcon(ImageUtil.alphaOffset(shiftDownImage, 0.53f));
	}

	MarketWatcherTabItemPanel(MarketWatcherPlugin plugin, MarketWatcherTab tab, MarketWatcherItem item)
	{
		setLayout(new BorderLayout(5, 0));
		setBorder(new EmptyBorder(5, 0, 5, 0));

		int itemIndex = tab.getItems().indexOf(item);
		int itemsSize = tab.getItems().size();

		JPanel rightPanel = createRightPanel(item, plugin, COMPACT);

		// Action Panel (Delete, Shift item)
		JPanel actionPanel = new JPanel(new BorderLayout());
		actionPanel.setBackground(new Color(0, 0, 0, 0));
		actionPanel.setOpaque(false);

		// Delete Item
		JLabel deleteItem = new JLabel(REMOVE_ICON);
		deleteItem.setBorder(new EmptyBorder(0, 0, 0, 5));
		deleteItem.addMouseListener(new MouseAdapter()
		{
			@Override
			public void mouseReleased(MouseEvent e)
			{
				if (removeConfirm())
				{
					plugin.removeItemFromTab(tab, item);
				}
			}

			@Override
			public void mouseEntered(MouseEvent e)
			{
				deleteItem.setIcon(REMOVE_HOVER_ICON);
			}

			@Override
			public void mouseExited(MouseEvent e)
			{
				deleteItem.setIcon(REMOVE_ICON);
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
					plugin.shiftItemInTab(tab, itemIndex, true);
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
					plugin.shiftItemInTab(tab, itemIndex, false);
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

		add(rightPanel, BorderLayout.WEST);
		add(actionPanel, BorderLayout.EAST);
	}

	private boolean removeConfirm()
	{
		int confirm = JOptionPane.showConfirmDialog(this,
			REMOVE_MESSAGE, REMOVE_TITLE, JOptionPane.YES_NO_OPTION);

		return confirm == JOptionPane.YES_NO_OPTION;
	}

	@Override
	protected void paintComponent(Graphics g)
	{
		g.setColor(ColorScheme.DARK_GRAY_COLOR);
		g.fillRect(0, 0, this.getWidth(), this.getHeight());
	}

}