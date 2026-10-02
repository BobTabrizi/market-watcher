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
import static com.marketwatcher.utilities.PanelUtils.createItemActionPanel;
import static com.marketwatcher.utilities.PanelUtils.createRightPanel;

import net.runelite.client.ui.ColorScheme;
import net.runelite.client.util.ImageUtil;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;
import java.awt.image.BufferedImage;

public class MarketWatcherTabItemPanel extends JPanel
{
	private static final String REMOVE_TITLE = "Warning";
	private static final String REMOVE_MESSAGE = "Are you sure you want to remove this item from the tab?";
	private static final ImageIcon REMOVE_ICON;
	private static final ImageIcon REMOVE_HOVER_ICON;

	static
	{
		final BufferedImage removeImage = ImageUtil.loadImageResource(MarketWatcherPluginPanel.class, DELETE_ICON_PATH);
		REMOVE_ICON = new ImageIcon(removeImage);
		REMOVE_HOVER_ICON = new ImageIcon(ImageUtil.alphaOffset(removeImage, 0.53f));
	}

	MarketWatcherTabItemPanel(MarketWatcherPlugin plugin, MarketWatcherTab tab, MarketWatcherItem item)
	{
		setLayout(new BorderLayout(5, 0));
		setBorder(new EmptyBorder(5, 0, 5, 0));

		int itemIndex = tab.getItems().indexOf(item);
		int itemsSize = tab.getItems().size();

		JPanel rightPanel = createRightPanel(item, plugin, COMPACT);

		// Action Panel (Delete, Shift item)
		JPanel actionPanel = createItemActionPanel(this, REMOVE_TITLE, REMOVE_MESSAGE,
			REMOVE_ICON, REMOVE_HOVER_ICON, new Insets(0, 0, 0, 5),
			itemIndex, itemsSize,
			() -> plugin.removeItemFromTab(tab, item),
			() -> plugin.shiftItemInTab(tab, itemIndex, true),
			() -> plugin.shiftItemInTab(tab, itemIndex, false));

		add(rightPanel, BorderLayout.WEST);
		add(actionPanel, BorderLayout.EAST);
	}

	@Override
	protected void paintComponent(Graphics g)
	{
		g.setColor(ColorScheme.DARK_GRAY_COLOR);
		g.fillRect(0, 0, this.getWidth(), this.getHeight());
	}

}