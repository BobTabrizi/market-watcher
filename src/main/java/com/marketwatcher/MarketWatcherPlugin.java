package com.marketwatcher;

import com.fasterxml.jackson.databind.ObjectMapper;

import javax.inject.Inject;
import javax.swing.*;

import com.marketwatcher.data.MarketWatcherItem;
import com.marketwatcher.data.MarketWatcherList;
import com.marketwatcher.data.MarketWatcherListDataManager;

import static com.marketwatcher.ui.Constants.*;

import com.marketwatcher.ui.MarketWatcherListPluginPanel;
import net.runelite.client.game.ItemManager;
import net.runelite.client.ui.ColorScheme;
import net.runelite.client.util.QuantityFormatter;
import lombok.extern.slf4j.Slf4j;

import java.awt.*;
import java.awt.image.BufferedImage;
import java.math.BigInteger;
import java.text.DecimalFormat;
import java.util.*;
import java.util.List;
import java.util.regex.Matcher;
import java.time.Instant;
import java.util.regex.Pattern;

import com.fasterxml.jackson.core.*;

import net.runelite.api.ChatMessageType;
import net.runelite.api.Client;
import net.runelite.api.GameState;
import net.runelite.api.events.GameStateChanged;
import net.runelite.client.callback.ClientThread;
import net.runelite.client.config.ConfigManager;
import net.runelite.client.eventbus.Subscribe;
import net.runelite.client.plugins.Plugin;
import net.runelite.client.plugins.PluginDescriptor;
import net.runelite.client.ui.ClientToolbar;
import net.runelite.client.ui.NavigationButton;
import net.runelite.client.util.ImageUtil;

import lombok.Getter;
import lombok.Setter;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;

@Slf4j
@PluginDescriptor(
        name = PLUGIN_NAME
)
public class MarketWatcherPlugin extends Plugin {

    // Response offset indices are used to obtain a substring of the response string from the market API for processing.
    private static final int RESPONSE_OFFSET_START_INDEX = 8;
    private static final int RESPONSE_OFFSET_END_INDEX = 24;
    private static final String ADD_EDIT_TAB_MESSAGE = "Enter the name of this tab (30 chars max).";
    private static final String ADD_NEW_TAB_TITLE = "Add New Tab";
    private static final String EDIT_TAB_TITLE = "Edit Tab";

    @Inject
    private Client client;

    //	@Inject
    private OkHttpClient okHttpClient;

    @Inject
    private ClientThread clientThread;
    @Inject
    private ItemManager itemManager;

    @Inject
    private MarketWatcherListDataManager dataManager;

    @Inject
    private ClientToolbar clientToolbar;

    private MarketWatcherListPluginPanel panel;

    private NavigationButton navButton;

    @Getter
    @Setter
    private List<MarketWatcherItem> items = new ArrayList<>();

    @Getter
    @Setter
    private List<MarketWatcherList> tabs = new ArrayList<>();

    @Getter
    @Setter
    Map<Integer, Map<String, String>> itemStatsMap = new HashMap<>();

    @Getter
    @Setter
    private long value = 0;


    @Override
    protected void startUp() throws Exception {

        // Retrieve item price histories for one week, month, and three months.
        // Store prices in a map to be accessed during search at any pointer later on.
        retrieveItemPriceHistories(ONE_WEEK);
        retrieveItemPriceHistories(ONE_MONTH);
        retrieveItemPriceHistories(THREE_MONTHS);

        panel = injector.getInstance(MarketWatcherListPluginPanel.class);

        final BufferedImage icon = ImageUtil.loadImageResource(MarketWatcherPlugin.class, PANEL_ICON_PATH);

        navButton = NavigationButton.builder().tooltip(PLUGIN_NAME).icon(icon).priority(11).panel(panel).build();

        clientToolbar.addNavigation(navButton);
    }

    @Override
    protected void shutDown() throws Exception {
        clientToolbar.removeNavigation(navButton);
    }


    public void addItem(MarketWatcherItem item) {
        clientThread.invokeLater(() ->
        {
            if (!containsItem(item)) {
                items.add(item);
                dataManager.saveData();
                SwingUtilities.invokeLater(() ->
                {
                    panel.switchToMarketWatch();
                    panel.updateMarketWatchPanel();
                });
            } else {
                SwingUtilities.invokeLater(() -> panel.containsItemWarning());
            }
        });
    }

    public void removeItem(MarketWatcherItem item) {
        clientThread.invokeLater(() -> {
            items.remove(item);
            dataManager.saveData();
            SwingUtilities.invokeLater(() -> panel.updateMarketWatchPanel());
        });
    }

    public void addItemsToTab(MarketWatcherList tab, List<String> itemNames) {
        clientThread.invokeLater(() -> {
            for (String itemName : itemNames) {
                MarketWatcherItem item = items.stream().filter(o -> o.getName().equals(itemName)).findFirst().orElse(null);
                tab.getItems().add(item);
                items.remove(item);
            }
            dataManager.saveData();
            SwingUtilities.invokeLater(() -> panel.updateMarketWatchPanel());
        });
    }

    public void removeItemFromTab(MarketWatcherList tab, MarketWatcherItem item) {
        clientThread.invokeLater(() -> {
            tab.getItems().remove(item);
            items.add(item);
            dataManager.saveData();
            SwingUtilities.invokeLater(() -> panel.updateMarketWatchPanel());
        });
    }

    public void switchTabCollapse(MarketWatcherList tab) {
        clientThread.invokeLater(() -> {
            tab.setCollapsed(!tab.isCollapsed());
            dataManager.saveData();
            SwingUtilities.invokeLater(() -> panel.updateMarketWatchPanel());
        });
    }

    public void addTab() {
        String name = JOptionPane.showInputDialog(panel, ADD_EDIT_TAB_MESSAGE, ADD_NEW_TAB_TITLE, JOptionPane.PLAIN_MESSAGE);

        if (name == null || name.isEmpty()) {
            return;
        }

        if (name.length() > 30) {
            name = name.substring(0, 30);
        }

        String tabName = name;
        clientThread.invokeLater(() -> {
            MarketWatcherList tab = new MarketWatcherList(tabName, new ArrayList<>());

            if (!tabs.contains(tab)) {
                tabs.add(tab);
                dataManager.saveData();
                SwingUtilities.invokeLater(() -> panel.updateMarketWatchPanel());
            }
        });
    }

    public void shiftItem(int itemIndex, boolean shiftUp) {
        clientThread.invokeLater(() -> {
            MarketWatcherItem shiftedItem = items.get(itemIndex);

            // Out of bounds is checked before call in item panel
            if (shiftUp) {
                items.set(itemIndex, items.get(itemIndex - 1));
                items.set(itemIndex - 1, shiftedItem);
            } else {
                items.set(itemIndex, items.get(itemIndex + 1));
                items.set(itemIndex + 1, shiftedItem);
            }

            dataManager.saveData();
            SwingUtilities.invokeLater(() -> panel.updateMarketWatchPanel());
        });
    }

    public void shiftItemInTab(MarketWatcherList tab, int itemIndex, boolean shiftUp) {
        clientThread.invokeLater(() -> {
            List<MarketWatcherItem> tabItems = tab.getItems();
            MarketWatcherItem shiftedItem = tab.getItems().get(itemIndex);

            // Out of bounds is checked before call in tab item panel
            if (shiftUp) {
                tabItems.set(itemIndex, tabItems.get(itemIndex - 1));
                tabItems.set(itemIndex - 1, shiftedItem);
            } else {
                tabItems.set(itemIndex, tabItems.get(itemIndex + 1));
                tabItems.set(itemIndex + 1, shiftedItem);
            }

            dataManager.saveData();
            SwingUtilities.invokeLater(() -> panel.updateMarketWatchPanel());
        });
    }

    public void removeTab(MarketWatcherList tab) {
        clientThread.invokeLater(() -> {
            // Move items out of tab and delete
            items.addAll(tab.getItems());
            tabs.remove(tab);
            dataManager.saveData();
            SwingUtilities.invokeLater(() -> panel.updateMarketWatchPanel());
        });
    }

    public void updateItemPrices() {
        // Tab item prices
        for (MarketWatcherList tab : tabs) {
            for (MarketWatcherItem item : tab.getItems()) {
                item.setGePrice(itemManager.getItemPrice(item.getItemId()));
            }
        }

        // Individual prices
        for (MarketWatcherItem item : items) {
            item.setGePrice(itemManager.getItemPrice(item.getItemId()));
        }

        SwingUtilities.invokeLater(() -> panel.updateMarketWatchPanel());
    }


    public void editTab(MarketWatcherList tab) {
        String name = JOptionPane.showInputDialog(panel, ADD_EDIT_TAB_MESSAGE, EDIT_TAB_TITLE, JOptionPane.PLAIN_MESSAGE);

        if (name == null || name.isEmpty()) {
            return;
        }

        if (name.length() > 30) {
            name = name.substring(0, 30);
        }

        String tabName = name;
        clientThread.invokeLater(() -> {
            MarketWatcherList nameCheck = tabs.stream().filter(o -> o.getName().equals(tabName)).findFirst().orElse(null);

            if (nameCheck == null) {
                tab.setName(tabName);
                dataManager.saveData();
                SwingUtilities.invokeLater(() -> panel.updateMarketWatchPanel());
            }
        });
    }


    public JPanel createRightPanel(MarketWatcherItem item, String viewType) {
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
        rightPanel.add(itemImage, getGbc(gbc, 0, 0, 1, 2, 2, 0, new Insets(0, 0, 0, 3)));
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
        timeType.setText("W:");
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

    public String[] manageItemPrices(String lowPrice, String medPrice, String highPrice, String viewType) {
        final DecimalFormat df = new DecimalFormat("0.0");
        final DecimalFormat df2 = viewType.equals(COMPACT) ? new DecimalFormat("0.0") : new DecimalFormat("0.00");

        highPrice = truncatePrices(highPrice, df, df2);
        medPrice = truncatePrices(medPrice, df, df2);
        lowPrice = truncatePrices(lowPrice, df, df2);

        return new String[]{lowPrice, medPrice, highPrice};
    }

    private String formatTooltip(String tooltip) {

        if (tooltip.equals(NOT_AVAILABLE) || tooltip.equals(NULL)) {
            return NOT_AVAILABLE;
        } else {
            return QuantityFormatter.formatNumber(Integer.parseInt(tooltip));
        }
    }

    private GridBagConstraints getGbc(GridBagConstraints gbc, int gridx, int gridy, int gridWidth, int gridHeight, int paddingX, int paddingY, Insets insets) {
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


    private String truncatePrices(String price, DecimalFormat df, DecimalFormat df2) {
        if (price.equals(NULL)) {
            price = NOT_AVAILABLE;
        }
        if (price.length() >= 5 && price.length() < 7) {
            price = df2.format((float) Integer.parseInt(price) / 1000) + K_THOUSAND;
        } else if (price.length() >= 7 && price.length() < 10) {
            price = df2.format((float) Integer.parseInt(price) / 1000000) + M_MILLION;
        } else if (price.length() >= 10) {
            price = df.format((float) Integer.parseInt(price) / 100000000) + B_BILLION;
        }
        return price;
    }

    private boolean containsItem(MarketWatcherItem newItem) {
        for (MarketWatcherList tab : tabs) {
            if (tab.getItems().contains(newItem)) {
                return true;
            }
        }
        return items.contains(newItem);
    }

    private void retrieveItemPriceHistories(String timePeriod) throws Exception {
        String resp = EMPTY_STRING;

        long unixTimestamp = Instant.now().getEpochSecond();
        String unixTimeString = EMPTY_STRING;

        if (timePeriod.equals(ONE_WEEK)) {
            long oneWeekAgo = (unixTimestamp - UNIX_WEEK);
            long timeBuffer = (oneWeekAgo % SECONDS_IN_SIX_HOURS);

            unixTimeString = String.valueOf(oneWeekAgo - timeBuffer);
        } else if (timePeriod.equals(ONE_MONTH)) {
            long oneMonthAgo = (unixTimestamp - UNIX_MONTH);
            long timeBuffer = (oneMonthAgo % SECONDS_IN_SIX_HOURS);
            unixTimeString = String.valueOf(oneMonthAgo - timeBuffer);
        } else if (timePeriod.equals(THREE_MONTHS)) {
            long threeMonthsAgo = (unixTimestamp - UNIX_MONTH * 3);
            long timeBuffer = (threeMonthsAgo % SECONDS_IN_SIX_HOURS);
            unixTimeString = String.valueOf(threeMonthsAgo - timeBuffer);
        }

        Request request = new Request.Builder()
                .url(OSRS_WIKI_PRICES_6H_REQUEST_URL + unixTimeString)
                .build();

        okHttpClient = injector.getInstance(OkHttpClient.class);

        try (Response response = okHttpClient.newCall(request).execute()) {
            System.out.println(response.body());
            resp = response.body().string();
        } catch (Exception e) {
            System.out.println(e);
        }

        String modifiedResponse = resp.substring(RESPONSE_OFFSET_START_INDEX, resp.length() - RESPONSE_OFFSET_END_INDEX);
        final Map<String, Object> jsonObject = new ObjectMapper().readValue(modifiedResponse, Map.class);

        for (Map.Entry<String, Object> entry : jsonObject.entrySet()) {
            String text = entry.getValue().toString();
            Pattern pattern = Pattern.compile("([^=\\s]+)=([^=\\s]*(?:\\s+[^=\\s]+)*)(?!\\S)");
            Matcher matcher = pattern.matcher(text);

            String highPrice = NOT_AVAILABLE;
            String lowPrice = NOT_AVAILABLE;
            String medPrice = NOT_AVAILABLE;
            if (matcher.find()) {
                if (matcher.group(2) != null && !matcher.group(2).equals(NULL)) {
                    highPrice = matcher.group(2).replace(COMMA, EMPTY_STRING);
                }
            }

            matcher.find();
            if (matcher.find()) {
                if (matcher.group(2) != null && !matcher.group(2).equals(NULL)) {
                    lowPrice = matcher.group(2).replace(COMMA, EMPTY_STRING);
                }
            }

            if (!highPrice.equals(NULL) && !lowPrice.equals(NULL)) {
                BigInteger highPriceInteger = BigInteger.valueOf(Integer.parseInt(highPrice));
                BigInteger lowPriceInteger = BigInteger.valueOf(Integer.parseInt(lowPrice));
                medPrice = String.valueOf(highPriceInteger.add(lowPriceInteger).divide(new BigInteger("2")));
            }

            int currentItemID = Integer.parseInt(entry.getKey());

            Map<String, String> timeFrameValuesMapping = itemStatsMap.get(currentItemID);

            if (timeFrameValuesMapping == null) {
                Map<String, String> timeFrameValues = new HashMap<>();
                timeFrameValues.put(timePeriod + LOW, lowPrice);
                timeFrameValues.put(timePeriod + MED, medPrice);
                timeFrameValues.put(timePeriod + HIGH, highPrice);
                itemStatsMap.put(Integer.parseInt(entry.getKey()), timeFrameValues);
            } else {
                timeFrameValuesMapping.put(timePeriod + LOW, lowPrice);
                timeFrameValuesMapping.put(timePeriod + MED, medPrice);
                timeFrameValuesMapping.put(timePeriod + HIGH, highPrice);
            }

        }
    }
}
