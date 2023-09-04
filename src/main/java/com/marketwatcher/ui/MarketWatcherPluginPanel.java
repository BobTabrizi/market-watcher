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

import com.google.common.base.Strings;
import com.marketwatcher.MarketWatcherPlugin;
import com.marketwatcher.data.MarketWatcherItem;
import com.marketwatcher.data.MarketWatcherTab;

import static com.marketwatcher.utilities.Constants.*;
import static com.marketwatcher.utilities.PriceUtils.createMarketWatchItemWithPriceMap;

import net.runelite.client.callback.ClientThread;
import net.runelite.client.config.RuneLiteConfig;
import net.runelite.client.ui.ColorScheme;
import net.runelite.client.ui.PluginPanel;
import net.runelite.client.ui.components.IconTextField;
import net.runelite.client.ui.components.PluginErrorPanel;
import net.runelite.client.util.AsyncBufferedImage;
import net.runelite.client.util.ImageUtil;
import net.runelite.http.api.item.ItemPrice;
import net.runelite.client.game.ItemManager;

import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.image.BufferedImage;
import java.io.IOException;
import javax.swing.ImageIcon;
import javax.inject.Inject;
import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.BorderLayout;
import java.awt.CardLayout;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class MarketWatcherPluginPanel extends PluginPanel
{
	private final MarketWatcherPlugin plugin;
	private final ClientThread clientThread;
	private final RuneLiteConfig runeLiteConfig;
	private final ItemManager itemManager;

	private static final int MAX_SEARCH_ITEMS = 100;

	private static final String MARKET_WATCH_PANEL = "MARKET_WATCH_PANEL";
	private static final String SEARCH_PANEL = "SEARCH_PANEL";
	private static final String RESULTS_PANEL = "RESULTS_PANEL";
	private static final String ERROR_PANEL = "ERROR_PANEL";

	private static final String PANEL_TITLE = "Market Watcher";
	private static final String GE_SEARCH_TITLE = "Grand Exchange Search";
	private static final String CONTAINS_ITEM_TITLE = "Info";
	private static final String SEARCH_PROMPT = "Search for an item to select";
	private static final String INFO_TOOLTIP = "Information";
	private static final String ADD_ITEM_TOOLTIP = "Add an item from the Grand Exchange";
	private static final String ADD_TAB_ITEM_TOOLTIP = "Add an item tab";
	private static final String CONTAINS_ITEM_MESSAGE = "This item is already being tracked.";
	private static final String SEARCH_ERROR = "No results found.";
	private static final String SEARCH_ERROR_MESSAGE = "No items were found with that name, please try again.";
	private static final String CANCEL = "Cancel";
	private static final ImageIcon INFO_ICON;
	private static final ImageIcon INFO_HOVER_ICON;
	private static final ImageIcon ADD_ICON;
	private static final ImageIcon ADD_HOVER_ICON;

	private static final ImageIcon ADD_TAB_ICON;

	private static final ImageIcon ADD_TAB_HOVER_ICON;

	private static final ImageIcon CANCEL_ICON;
	private static final ImageIcon CANCEL_HOVER_ICON;
	private final JLabel cancelItem = new JLabel(CANCEL_ICON);

	private final CardLayout centerCard = new CardLayout();
	private final CardLayout searchCard = new CardLayout();

	private final JPanel centerPanel = new JPanel(centerCard);
	private final JPanel marketWatcherPanel = new JPanel(new BorderLayout());
	private final JPanel titlePanel = new JPanel(new BorderLayout());
	private final JPanel searchPanel = new JPanel(new BorderLayout());
	private final JPanel searchCenterPanel = new JPanel(searchCard);
	private final JPanel searchResultsPanel = new JPanel();
	private final JPanel marketWatcherItemsPanel = new JPanel();
	private final IconTextField searchBar = new IconTextField();
	private final PluginErrorPanel searchErrorPanel = new PluginErrorPanel();
	private final GridBagConstraints constraints = new GridBagConstraints();
	private final JLabel title = new JLabel();
	private final JPanel actionPanel = new JPanel(new BorderLayout());
	private final JLabel information = new JLabel(INFO_ICON);
	private final JLabel addTabItem = new JLabel(ADD_TAB_ICON);
	private final JLabel addItem = new JLabel(ADD_ICON);

	private final List<MarketWatcherItem> searchItems = new ArrayList<>();

	static
	{
		final BufferedImage infoImage = ImageUtil.loadImageResource(MarketWatcherPluginPanel.class, INFO_ICON_PATH);
		INFO_ICON = new ImageIcon(infoImage);
		INFO_HOVER_ICON = new ImageIcon(ImageUtil.alphaOffset(infoImage, 0.53f));

		final BufferedImage addImage = ImageUtil.loadImageResource(MarketWatcherPluginPanel.class, ADD_ICON_PATH);
		ADD_ICON = new ImageIcon(addImage);
		ADD_HOVER_ICON = new ImageIcon(ImageUtil.alphaOffset(addImage, 0.53f));

		final BufferedImage addTabImage = ImageUtil.loadImageResource(MarketWatcherPluginPanel.class, ADD_TAB_ICON_PATH);
		ADD_TAB_ICON = new ImageIcon(addTabImage);
		ADD_TAB_HOVER_ICON = new ImageIcon(ImageUtil.alphaOffset(addTabImage, 0.53f));

		final BufferedImage cancelImage = ImageUtil.loadImageResource(MarketWatcherPluginPanel.class, CANCEL_ICON_PATH);
		CANCEL_ICON = new ImageIcon(cancelImage);
		CANCEL_HOVER_ICON = new ImageIcon(ImageUtil.alphaOffset(cancelImage, 0.53f));
	}


	@Inject
	MarketWatcherPluginPanel(MarketWatcherPlugin plugin, ClientThread clientThread, RuneLiteConfig runeLiteConfig, ItemManager itemManager) throws IOException
	{
		super(false);
		this.plugin = plugin;
		this.clientThread = clientThread;
		this.runeLiteConfig = runeLiteConfig;
		this.itemManager = itemManager;

		setLayout(new BorderLayout());
		JPanel container = new JPanel(new BorderLayout());
		container.setBorder(new EmptyBorder(10, 10, 10, 10));

		title.setText(PANEL_TITLE);
		title.setForeground(Color.WHITE);
		title.setBorder(new EmptyBorder(0, 0, 0, 0));
		title.setPreferredSize(new Dimension(100, 10));

		JPanel actions = new JPanel(new FlowLayout(FlowLayout.RIGHT, 3, 3));


		// Info Button
		information.setToolTipText(INFO_TOOLTIP);
		information.setBorder(new EmptyBorder(0, 0, 0, 10));
		information.addMouseListener(new MouseAdapter()
		{
			@Override
			public void mousePressed(MouseEvent e)
			{
				plugin.showHelp();
			}

			@Override
			public void mouseEntered(MouseEvent e)
			{
				information.setIcon(INFO_HOVER_ICON);
			}

			@Override
			public void mouseExited(MouseEvent e)
			{
				information.setIcon(INFO_ICON);
			}
		});

		actionPanel.add(information, BorderLayout.LINE_START);

		// Add Tab Button
		addTabItem.setToolTipText(ADD_TAB_ITEM_TOOLTIP);
		addTabItem.setBorder(new EmptyBorder(0, 0, 0, 10));
		addTabItem.addMouseListener(new MouseAdapter()
		{
			@Override
			public void mousePressed(MouseEvent e)
			{
				plugin.addTab();
			}

			@Override
			public void mouseEntered(MouseEvent e)
			{
				addTabItem.setIcon(ADD_TAB_HOVER_ICON);
			}

			@Override
			public void mouseExited(MouseEvent e)
			{
				addTabItem.setIcon(ADD_TAB_ICON);
			}
		});
		actionPanel.add(addTabItem, BorderLayout.CENTER);

		// Add Item Button
		addItem.setToolTipText(ADD_ITEM_TOOLTIP);
		addItem.addMouseListener(new MouseAdapter()
		{
			@Override
			public void mousePressed(MouseEvent e)
			{
				switchToSearch();
			}

			@Override
			public void mouseEntered(MouseEvent e)
			{
				addItem.setIcon(ADD_HOVER_ICON);
			}

			@Override
			public void mouseExited(MouseEvent e)
			{
				addItem.setIcon(ADD_ICON);
			}
		});

		actionPanel.add(addItem, BorderLayout.LINE_END);

		actions.add(actionPanel);

		// Cancel Button
		cancelItem.setToolTipText(CANCEL);
		cancelItem.setVisible(false);
		cancelItem.addMouseListener(new MouseAdapter()
		{
			@Override
			public void mousePressed(MouseEvent e)
			{
				switchToMarketWatch();
			}

			@Override
			public void mouseEntered(MouseEvent e)
			{
				cancelItem.setIcon(CANCEL_HOVER_ICON);
			}

			@Override
			public void mouseExited(MouseEvent e)
			{
				cancelItem.setIcon(CANCEL_ICON);
			}
		});
		actions.add(cancelItem);

		titlePanel.setBorder(new EmptyBorder(0, 0, 10, 0));
		titlePanel.add(title, BorderLayout.LINE_START);
		titlePanel.add(actions, BorderLayout.LINE_END);

		// Market Watch Items Panel
		marketWatcherItemsPanel.setLayout(new GridBagLayout());

		JPanel pWrapper = new JPanel(new BorderLayout());
		pWrapper.add(marketWatcherItemsPanel, BorderLayout.NORTH);

		JScrollPane marketWrapper = new JScrollPane(pWrapper);
		marketWrapper.setBackground(ColorScheme.DARK_GRAY_COLOR);
		marketWrapper.setBorder(new EmptyBorder(5, 0, 0, 0));
		marketWrapper.getVerticalScrollBar().setPreferredSize(new Dimension(12, 0));
		marketWrapper.getVerticalScrollBar().setBorder(new EmptyBorder(5, 5, 0, 0));

		// Market Watch Panel
		marketWatcherPanel.add(marketWrapper, BorderLayout.CENTER);

		// Search Results Panel
		searchResultsPanel.setLayout(new GridBagLayout());

		JPanel searchResultsWrapper = new JPanel(new BorderLayout());
		searchResultsWrapper.add(searchResultsPanel, BorderLayout.NORTH);

		JScrollPane resultsWrapper = new JScrollPane(searchResultsWrapper);
		resultsWrapper.setBackground(ColorScheme.DARK_GRAY_COLOR);
		resultsWrapper.setBorder(new EmptyBorder(5, 0, 0, 0));
		resultsWrapper.getVerticalScrollBar().setPreferredSize(new Dimension(12, 0));
		resultsWrapper.getVerticalScrollBar().setBorder(new EmptyBorder(5, 5, 0, 0));

		constraints.fill = GridBagConstraints.HORIZONTAL;
		constraints.gridwidth = 1;
		constraints.weightx = 1;
		constraints.gridx = 0;
		constraints.gridy = 0;

		// Search Error Panel
		searchErrorPanel.setContent(GE_SEARCH_TITLE,
			SEARCH_PROMPT);

		JPanel errorWrapper = new JPanel(new BorderLayout());
		errorWrapper.add(searchErrorPanel, BorderLayout.NORTH);

		// Search Center Panel
		searchCenterPanel.add(resultsWrapper, RESULTS_PANEL);
		searchCenterPanel.add(errorWrapper, ERROR_PANEL);
		searchCard.show(searchCenterPanel, ERROR_PANEL);

		searchBar.setIcon(IconTextField.Icon.SEARCH);
		searchBar.setPreferredSize(new Dimension(PluginPanel.PANEL_WIDTH - 15, 30));
		searchBar.setBackground(ColorScheme.DARKER_GRAY_COLOR);
		searchBar.setHoverBackgroundColor(ColorScheme.DARK_GRAY_HOVER_COLOR);
		searchBar.addClearListener(this::searchForItems);
		searchBar.addKeyListener(new KeyListener()
		{
			@Override
			public void keyTyped(KeyEvent e)
			{
			}

			@Override
			public void keyPressed(KeyEvent e)
			{
				if (e.getKeyCode() == KeyEvent.VK_ENTER)
				{
					searchForItems();
				}
			}

			@Override
			public void keyReleased(KeyEvent e)
			{
			}
		});

		// Search Panel
		searchPanel.add(searchBar, BorderLayout.NORTH);
		searchPanel.add(searchCenterPanel, BorderLayout.CENTER);

		// Center Panel
		centerPanel.add(marketWatcherPanel, MARKET_WATCH_PANEL);
		centerPanel.add(searchPanel, SEARCH_PANEL);
		centerCard.show(centerPanel, MARKET_WATCH_PANEL);

		container.add(titlePanel, BorderLayout.NORTH);
		container.add(centerPanel, BorderLayout.CENTER);
		add(container, BorderLayout.CENTER);
	}

	public void switchToMarketWatch()
	{
		cancelItem.setVisible(false);
		actionPanel.setVisible(true);
		centerCard.show(centerPanel, MARKET_WATCH_PANEL);
	}

	private void switchToSearch()
	{
		actionPanel.setVisible(false);
		cancelItem.setVisible(true);
		centerCard.show(centerPanel, SEARCH_PANEL);
	}

	private void searchForItems()
	{
		String lookup = searchBar.getText();
		searchResultsPanel.removeAll();
		if (Strings.isNullOrEmpty(lookup))
		{
			searchResultsPanel.removeAll();
			SwingUtilities.invokeLater(searchResultsPanel::updateUI);
			return;
		}

		List<ItemPrice> results = itemManager.search(searchBar.getText());
		if (results.isEmpty())
		{
			searchErrorPanel.setContent(SEARCH_ERROR, SEARCH_ERROR_MESSAGE);
			searchCard.show(searchCenterPanel, ERROR_PANEL);
			return;
		}

		clientThread.invokeLater(() -> processResults(results));


	}


	private void processResults(List<ItemPrice> results)
	{
		searchItems.clear();
		searchCard.show(searchCenterPanel, RESULTS_PANEL);

		int count = 0;
		boolean useActivelyTradedPrice = runeLiteConfig.useWikiItemPrices();

		// Add each result to items list
		for (ItemPrice item : results)
		{
			if (count++ > MAX_SEARCH_ITEMS)
			{
				break;
			}

			int itemId = item.getId();
			AsyncBufferedImage itemImage = itemManager.getImage(itemId);
			int itemPrice = useActivelyTradedPrice ? itemManager.getWikiPrice(item) : item.getPrice();

			Map<String, String> itemPriceMap = plugin.getItemPriceMap().get(itemId);

			MarketWatcherItem resultItem = createMarketWatchItemWithPriceMap(itemImage, item.getName(), itemId, itemPrice, itemPriceMap);

			searchItems.add(resultItem);
		}

		// Add each item in list to panel
		SwingUtilities.invokeLater(() ->
		{
			int index = 0;
			for (MarketWatcherItem item : searchItems)
			{
				MarketWatcherTabResultPanel panel = new MarketWatcherTabResultPanel(plugin, item);

				if (index++ > 0)
				{
					searchResultsPanel.add(createMarginWrapper(panel), constraints);
				}
				else
				{
					searchResultsPanel.add(panel, constraints);
				}

				constraints.gridy++;
			}

			validate();
		});
	}

	public void updateMarketWatchPanel()
	{
		marketWatcherItemsPanel.removeAll();

		constraints.gridy++;

		int index = 0;

		// Tabs
		for (MarketWatcherTab tab : plugin.getTabs())
		{
			MarketWatcherTabPanel panel = new MarketWatcherTabPanel(plugin, this, tab);

			if (index++ > 0)
			{
				marketWatcherItemsPanel.add(createMarginWrapper(panel), constraints);
			}
			else
			{
				marketWatcherItemsPanel.add(panel, constraints);
			}

			constraints.gridy++;
		}

		// Individual items
		for (MarketWatcherItem item : plugin.getItems())
		{
			MarketWatcherItemPanel panel = new MarketWatcherItemPanel(plugin, item);

			if (index++ > 0)
			{
				marketWatcherItemsPanel.add(createMarginWrapper(panel), constraints);
			}
			else
			{
				marketWatcherItemsPanel.add(panel, constraints);
			}

			constraints.gridy++;
		}

		validate();
	}

	public void containsItemWarning()
	{
		JOptionPane.showConfirmDialog(this,
			CONTAINS_ITEM_MESSAGE, CONTAINS_ITEM_TITLE, JOptionPane.DEFAULT_OPTION);
	}

	private JPanel createMarginWrapper(JPanel panel)
	{
		JPanel marginWrapper = new JPanel(new BorderLayout());
		marginWrapper.setBorder(new EmptyBorder(5, 0, 0, 0));
		marginWrapper.add(panel, BorderLayout.NORTH);
		return marginWrapper;
	}
}
