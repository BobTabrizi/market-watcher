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

public class MarketWatcherItemPanel extends JPanel
{
	private static final String DELETE_TITLE = "Warning";
	private static final String DELETE_MESSAGE = "Are you sure you want to delete this item?";
	private static final ImageIcon DELETE_ICON;
	private static final ImageIcon DELETE_HOVER_ICON;

	static
	{
		final BufferedImage deleteImage = ImageUtil.loadImageResource(MarketWatcherItemPanel.class, DELETE_ICON_PATH);
		DELETE_ICON = new ImageIcon(deleteImage);
		DELETE_HOVER_ICON = new ImageIcon(ImageUtil.alphaOffset(deleteImage, 0.53f));
	}

	MarketWatcherItemPanel(MarketWatcherPlugin plugin, MarketWatcherItem item)
	{
		setLayout(new BorderLayout(5, 0));
		setBorder(new EmptyBorder(5, 5, 5, 0));

		int itemIndex = plugin.getItems().indexOf(item);
		int itemsSize = plugin.getItems().size();

		JPanel rightPanel = createRightPanel(item, plugin, STANDARD);

		// Action Panel (Delete, Shift item)
		JPanel actionPanel = createItemActionPanel(this, DELETE_TITLE, DELETE_MESSAGE,
			DELETE_ICON, DELETE_HOVER_ICON, new Insets(0, 0, 0, 3),
			itemIndex, itemsSize,
			() -> plugin.removeItem(item),
			() -> plugin.shiftItem(itemIndex, true),
			() -> plugin.shiftItem(itemIndex, false));

		add(rightPanel, BorderLayout.WEST);
		add(actionPanel, BorderLayout.EAST);
	}

	@Override
	protected void paintComponent(Graphics g)
	{
		g.setColor(ColorScheme.DARKER_GRAY_COLOR);
		g.fillRect(0, 0, this.getWidth(), this.getHeight());
	}

}