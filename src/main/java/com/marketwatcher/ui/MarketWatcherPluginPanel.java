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
package com.marketwatcher.ui;

import com.google.common.base.Strings;
import com.marketwatcher.MarketWatcherPlugin;
import com.marketwatcher.data.MarketWatcherItem;
import com.marketwatcher.data.MarketWatcherTab;

import static com.marketwatcher.utilities.Constants.*;

import net.runelite.client.callback.ClientThread;
import net.runelite.client.config.RuneLiteConfig;
import com.marketwatcher.utilities.TimeFormat;
import net.runelite.client.ui.ColorScheme;
import net.runelite.client.ui.FontManager;
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
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import java.awt.BorderLayout;
import java.awt.CardLayout;
import java.awt.Color;
import java.awt.Cursor;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.time.Instant;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.time.format.FormatStyle;

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
	private static final String FILTER_TOOLTIP = "Search your tracked items";
	private static final String NO_FILTER_MATCHES = "No tracked items match your search.";
	private static final String REFRESH_TOOLTIP = "Refresh prices now";
	private static final String UPDATING_PRICES = "Updating prices...";
	private static final DateTimeFormatter CLOCK_FORMAT = DateTimeFormatter.ofLocalizedTime(FormatStyle.SHORT);
	private static final String CANCEL = "Cancel";
	private static final ImageIcon INFO_ICON;
	private static final ImageIcon INFO_HOVER_ICON;
	private static final ImageIcon ADD_ICON;
	private static final ImageIcon ADD_HOVER_ICON;

	private static final ImageIcon ADD_TAB_ICON;

	private static final ImageIcon ADD_TAB_HOVER_ICON;

	private static final ImageIcon CANCEL_ICON;
	private static final ImageIcon CANCEL_HOVER_ICON;
	private static final ImageIcon REFRESH_ICON;
	private static final ImageIcon REFRESH_HOVER_ICON;
	private static final ImageIcon REFRESH_DISABLED_ICON;
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
	private final IconTextField filterBar = new IconTextField();
	private final PluginErrorPanel searchErrorPanel = new PluginErrorPanel();
	private final GridBagConstraints constraints = new GridBagConstraints();
	private final JLabel title = new JLabel();
	private final JPanel actionPanel = new JPanel(new BorderLayout());
	private final JLabel information = new JLabel(INFO_ICON);
	private final JLabel addTabItem = new JLabel(ADD_TAB_ICON);
	private final JLabel addItem = new JLabel(ADD_ICON);

	private final List<MarketWatcherItem> searchItems = new ArrayList<>();

	private final JLabel refreshStatus = new JLabel();
	private final JLabel refreshButton = new JLabel(REFRESH_ICON);
	private boolean refreshButtonHovered;
	// Keeps the refresh status and cooldown current while the panel is visible
	private final Timer refreshStatusTimer = new Timer(1000, e -> updateRefreshStatus());

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

		final BufferedImage refreshImage = ImageUtil.loadImageResource(MarketWatcherPluginPanel.class, REFRESH_ICON_PATH);
		REFRESH_ICON = new ImageIcon(refreshImage);
		REFRESH_HOVER_ICON = new ImageIcon(ImageUtil.alphaOffset(refreshImage, 0.53f));
		REFRESH_DISABLED_ICON = new ImageIcon(ImageUtil.alphaOffset(refreshImage, 0.25f));
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

		JPanel pWrapper = new NoHorizontalScrollPanel(new BorderLayout());
		pWrapper.add(marketWatcherItemsPanel, BorderLayout.NORTH);

		JScrollPane marketWrapper = new JScrollPane(pWrapper);
		marketWrapper.setBackground(ColorScheme.DARK_GRAY_COLOR);
		marketWrapper.setBorder(new EmptyBorder(5, 0, 0, 0));
		marketWrapper.setHorizontalScrollBarPolicy(JScrollPane.HORIZONTAL_SCROLLBAR_NEVER);
		marketWrapper.getVerticalScrollBar().setPreferredSize(new Dimension(12, 0));
		marketWrapper.getVerticalScrollBar().setBorder(new EmptyBorder(5, 5, 0, 0));

		// Watchlist search, which filters tracked items as you type
		filterBar.setIcon(IconTextField.Icon.SEARCH);
		filterBar.setPreferredSize(new Dimension(PluginPanel.PANEL_WIDTH - 15, 30));
		filterBar.setBackground(ColorScheme.DARKER_GRAY_COLOR);
		filterBar.setHoverBackgroundColor(ColorScheme.DARK_GRAY_HOVER_COLOR);
		filterBar.setToolTipText(FILTER_TOOLTIP);
		filterBar.getDocument().addDocumentListener(new DocumentListener()
		{
			@Override
			public void insertUpdate(DocumentEvent e)
			{
				updateMarketWatchPanel();
			}

			@Override
			public void removeUpdate(DocumentEvent e)
			{
				updateMarketWatchPanel();
			}

			@Override
			public void changedUpdate(DocumentEvent e)
			{
				updateMarketWatchPanel();
			}
		});

		// Price refresh status and button, under the watchlist search
		refreshStatus.setFont(FontManager.getRunescapeSmallFont());
		refreshStatus.setForeground(ColorScheme.LIGHT_GRAY_COLOR);

		refreshButton.addMouseListener(new MouseAdapter()
		{
			@Override
			public void mousePressed(MouseEvent e)
			{
				plugin.refreshNow();
				updateRefreshStatus();
			}

			@Override
			public void mouseEntered(MouseEvent e)
			{
				refreshButtonHovered = true;
				updateRefreshButton();
			}

			@Override
			public void mouseExited(MouseEvent e)
			{
				refreshButtonHovered = false;
				updateRefreshButton();
			}
		});

		// The status text sits right next to the button, aligned to the right of the panel
		JPanel refreshControls = new JPanel(new BorderLayout(5, 0));
		refreshControls.add(refreshStatus, BorderLayout.CENTER);
		refreshControls.add(refreshButton, BorderLayout.EAST);

		JPanel refreshRow = new JPanel(new BorderLayout());
		refreshRow.setBorder(new EmptyBorder(6, 2, 0, 0));
		refreshRow.add(refreshControls, BorderLayout.EAST);

		JPanel watchlistHeader = new JPanel(new BorderLayout());
		watchlistHeader.add(filterBar, BorderLayout.NORTH);
		watchlistHeader.add(refreshRow, BorderLayout.SOUTH);

		// Market Watch Panel
		marketWatcherPanel.add(watchlistHeader, BorderLayout.NORTH);
		marketWatcherPanel.add(marketWrapper, BorderLayout.CENTER);

		// Search Results Panel
		searchResultsPanel.setLayout(new GridBagLayout());

		JPanel searchResultsWrapper = new NoHorizontalScrollPanel(new BorderLayout());
		searchResultsWrapper.add(searchResultsPanel, BorderLayout.NORTH);

		JScrollPane resultsWrapper = new JScrollPane(searchResultsWrapper);
		resultsWrapper.setBackground(ColorScheme.DARK_GRAY_COLOR);
		resultsWrapper.setBorder(new EmptyBorder(5, 0, 0, 0));
		resultsWrapper.setHorizontalScrollBarPolicy(JScrollPane.HORIZONTAL_SCROLLBAR_NEVER);
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
			if (count++ >= MAX_SEARCH_ITEMS)
			{
				break;
			}

			int itemId = item.getId();
			AsyncBufferedImage itemImage = itemManager.getImage(itemId);
			long itemPrice = useActivelyTradedPrice ? itemManager.getWikiPrice(item) : item.getPrice();

			MarketWatcherItem resultItem = new MarketWatcherItem(itemImage, item.getName(), itemId, itemPrice, plugin.getPeriodPrices(itemId));

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
		final String filter = filterBar.getText().trim().toLowerCase(Locale.ROOT);

		// Tabs, hidden while searching unless the tab's name or one of its items matches
		for (MarketWatcherTab tab : plugin.getTabs())
		{
			if (!tab.nameMatches(filter) && tab.getItems().stream().noneMatch(item -> item.nameMatches(filter)))
			{
				continue;
			}

			MarketWatcherTabPanel panel = new MarketWatcherTabPanel(plugin, this, tab, filter);

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
			if (!item.nameMatches(filter))
			{
				continue;
			}

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

		if (index == 0 && !filter.isEmpty())
		{
			JLabel noMatches = new JLabel(NO_FILTER_MATCHES);
			noMatches.setForeground(Color.GRAY);
			noMatches.setHorizontalAlignment(SwingConstants.CENTER);
			noMatches.setBorder(new EmptyBorder(10, 0, 0, 0));
			marketWatcherItemsPanel.add(noMatches, constraints);
			constraints.gridy++;
		}

		validate();
		repaint();
	}

	/**
	 * Shows when prices were last updated, with the next automatic refresh in the tooltip,
	 * and enables the refresh button once its cooldown has passed. Called on the Swing thread.
	 */
	public void updateRefreshStatus()
	{
		final long now = System.currentTimeMillis();
		final boolean refreshing = plugin.isRefreshing();
		final boolean failed = plugin.isLastRefreshFailed();
		final long lastRefresh = plugin.getLastRefreshMillis();
		final long untilNextAuto = plugin.getMillisUntilNextAutoRefresh();

		String text;
		if (refreshing)
		{
			text = UPDATING_PRICES;
		}
		else if (lastRefresh == 0)
		{
			text = failed ? "Couldn't load prices" : "Loading prices...";
		}
		else
		{
			text = "Updated " + TimeFormat.formatAgo(now - lastRefresh);
		}
		refreshStatus.setText(text);
		refreshStatus.setForeground(failed && !refreshing ? ColorScheme.PROGRESS_ERROR_COLOR : ColorScheme.LIGHT_GRAY_COLOR);

		final List<String> tooltipLines = new ArrayList<>();
		if (lastRefresh > 0)
		{
			tooltipLines.add("Prices last updated at " + formatClockTime(lastRefresh) + ".");
		}
		if (failed && !refreshing)
		{
			tooltipLines.add(lastRefresh > 0
				? "The last update failed, so some prices may be out of date."
				: "Prices couldn't be loaded from the OSRS Wiki.");
		}
		if (untilNextAuto >= 0)
		{
			tooltipLines.add("Next refresh in " + TimeFormat.formatUntil(untilNextAuto) + ", at " + formatClockTime(now + untilNextAuto) + ".");
		}
		refreshStatus.setToolTipText(tooltipLines.isEmpty() ? null : "<html>" + String.join("<br>", tooltipLines) + "</html>");

		updateRefreshButton();
	}

	private void updateRefreshButton()
	{
		final boolean refreshing = plugin.isRefreshing();
		final long cooldown = plugin.getRefreshCooldownRemainingMillis();
		final boolean available = !refreshing && cooldown == 0;

		refreshButton.setIcon(!available ? REFRESH_DISABLED_ICON : refreshButtonHovered ? REFRESH_HOVER_ICON : REFRESH_ICON);
		refreshButton.setCursor(Cursor.getPredefinedCursor(available ? Cursor.HAND_CURSOR : Cursor.DEFAULT_CURSOR));
		if (refreshing)
		{
			refreshButton.setToolTipText(UPDATING_PRICES);
		}
		else if (!available)
		{
			refreshButton.setToolTipText("Prices were just updated. You can refresh again in " + TimeFormat.formatSeconds(cooldown) + ".");
		}
		else
		{
			refreshButton.setToolTipText(REFRESH_TOOLTIP);
		}
	}

	private static String formatClockTime(long epochMillis)
	{
		return CLOCK_FORMAT.format(Instant.ofEpochMilli(epochMillis).atZone(ZoneId.systemDefault()));
	}

	@Override
	public void onActivate()
	{
		updateRefreshStatus();
		refreshStatusTimer.start();
	}

	@Override
	public void onDeactivate()
	{
		refreshStatusTimer.stop();
	}

	/**
	 * Stops the status timer when the plugin shuts down.
	 */
	public void shutdown()
	{
		refreshStatusTimer.stop();
	}

	public void containsItemWarning()
	{
		JOptionPane.showMessageDialog(this,
			CONTAINS_ITEM_MESSAGE, CONTAINS_ITEM_TITLE, JOptionPane.WARNING_MESSAGE);
	}

	private JPanel createMarginWrapper(JPanel panel)
	{
		JPanel marginWrapper = new JPanel(new BorderLayout());
		marginWrapper.setBorder(new EmptyBorder(5, 0, 0, 0));
		marginWrapper.add(panel, BorderLayout.NORTH);
		return marginWrapper;
	}

	/**
	 * A JPanel that always tracks the width of the JScrollPane viewport it's placed in,
	 * so it never grows wider than the panel and triggers a horizontal scrollbar.
	 * Vertical size is left as the panel's own preferred height so vertical scrolling
	 * still works normally.
	 */
	private static class NoHorizontalScrollPanel extends JPanel implements Scrollable
	{
		NoHorizontalScrollPanel(java.awt.LayoutManager layout)
		{
			super(layout);
		}

		@Override
		public Dimension getPreferredScrollableViewportSize()
		{
			return getPreferredSize();
		}

		@Override
		public int getScrollableUnitIncrement(java.awt.Rectangle visibleRect, int orientation, int direction)
		{
			return 16;
		}

		@Override
		public int getScrollableBlockIncrement(java.awt.Rectangle visibleRect, int orientation, int direction)
		{
			return visibleRect.height;
		}

		@Override
		public boolean getScrollableTracksViewportWidth()
		{
			return true;
		}

		@Override
		public boolean getScrollableTracksViewportHeight()
		{
			return false;
		}
	}
}
