package com.marketwatcher.ui;

import com.marketwatcher.MarketWatcherPlugin;
import com.marketwatcher.data.MarketWatcherTab;
import com.marketwatcher.data.MarketWatcherItem;

import static com.marketwatcher.utilities.Constants.*;

import net.runelite.client.ui.ColorScheme;
import net.runelite.client.util.ImageUtil;

import javax.swing.AbstractAction;
import javax.swing.ImageIcon;
import javax.swing.JLabel;
import javax.swing.JMenuItem;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JPopupMenu;
import javax.swing.SwingConstants;
import javax.swing.SwingUtilities;
import javax.swing.border.EmptyBorder;
import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.Graphics;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.event.ActionEvent;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.image.BufferedImage;
import java.util.Arrays;
import java.util.List;

public class MarketWatcherTabPanel extends JPanel {
    private static final String DELETE_TITLE = "Delete Tab";
    private static final String DELETE_MESSAGE = "Are you sure you want to delete this tab? This will not delete the items.";
    private static final ImageIcon ADD_ICON;
    private static final ImageIcon ADD_HOVER_ICON;
    private static final ImageIcon EDIT_ICON;
    private static final ImageIcon EDIT_HOVER_ICON;
    private static final ImageIcon COLLAPSED_ICON;
    private static final ImageIcon COLLAPSED_HOVER_ICON;
    private static final ImageIcon UNCOLLAPSED_ICON;
    private static final ImageIcon UNCOLLAPSED_HOVER_ICON;
    private final boolean collapsed;

    static {
        final BufferedImage addImage = ImageUtil.loadImageResource(MarketWatcherPlugin.class, ADD_TAB_ITEM_ICON_PATH);
        ADD_ICON = new ImageIcon(ImageUtil.alphaOffset(addImage, 0.53f));
        ADD_HOVER_ICON = new ImageIcon(addImage);

        final BufferedImage editImage = ImageUtil.loadImageResource(MarketWatcherPlugin.class, EDIT_TAB_ICON_PATH);
        EDIT_ICON = new ImageIcon(ImageUtil.alphaOffset(editImage, 0.53f));
        EDIT_HOVER_ICON = new ImageIcon(editImage);

        final BufferedImage collapsedImage = ImageUtil.loadImageResource(MarketWatcherPlugin.class, COLLAPSE_ICON_PATH);
        COLLAPSED_ICON = new ImageIcon(collapsedImage);
        COLLAPSED_HOVER_ICON = new ImageIcon(ImageUtil.alphaOffset(collapsedImage, 0.53f));

        final BufferedImage uncollapsedImage = ImageUtil.loadImageResource(MarketWatcherPlugin.class, SHIFT_DOWN_ICON_PATH);
        UNCOLLAPSED_ICON = new ImageIcon(uncollapsedImage);
        UNCOLLAPSED_HOVER_ICON = new ImageIcon(ImageUtil.alphaOffset(uncollapsedImage, 0.53f));
    }

    MarketWatcherTabPanel(MarketWatcherPlugin plugin, MarketWatcherPluginPanel panel, MarketWatcherTab tab) {
        setLayout(new BorderLayout(5, 0));
        setBorder(new EmptyBorder(5, 5, 5, 0));
        setBackground(ColorScheme.DARKER_GRAY_COLOR);

        this.collapsed = tab.isCollapsed();

        // Top Panel
        JPanel topPanel = new JPanel(new BorderLayout());
        topPanel.setOpaque(false);

        // Right click for tab deletion
        JPopupMenu deletePopup = new JPopupMenu();

        JMenuItem delete = new JMenuItem(new AbstractAction(DELETE_TITLE) {
            @Override
            public void actionPerformed(ActionEvent e) {
                if (deleteConfirm()) {
                    plugin.removeTab(tab);
                }
            }
        });
        deletePopup.add(delete);

        topPanel.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseReleased(MouseEvent e) {
                if (SwingUtilities.isRightMouseButton(e)) {
                    deletePopup.show(e.getComponent(), e.getX(), e.getY());
                }
            }
        });

        // Collapse and Names
        JPanel leftActions = new JPanel(new BorderLayout());
        leftActions.setOpaque(false);

        // Tab Name
        JLabel tabName = new JLabel();
        tabName.setForeground(Color.WHITE);
        tabName.setBorder(new EmptyBorder(0, 5, 0, 0));
        tabName.setPreferredSize(new Dimension(140, 0));
        tabName.setText(tab.getName());

        // Collapse
        JLabel collapseButton = new JLabel();
        collapseButton.setOpaque(false);

        if (collapsed) {
            tabName.setPreferredSize(new Dimension(160, 0));

            collapseButton.setIcon(COLLAPSED_ICON);
            collapseButton.addMouseListener(new MouseAdapter() {
                @Override
                public void mouseReleased(MouseEvent e) {
                    plugin.switchTabCollapse(tab);
                }

                @Override
                public void mouseEntered(MouseEvent e) {
                    collapseButton.setIcon(COLLAPSED_HOVER_ICON);
                }

                @Override
                public void mouseExited(MouseEvent e) {
                    collapseButton.setIcon(COLLAPSED_ICON);
                }
            });

            leftActions.add(tabName, BorderLayout.EAST);
            leftActions.add(collapseButton, BorderLayout.WEST);
            topPanel.add(leftActions, BorderLayout.WEST);

            add(topPanel, BorderLayout.CENTER);
        } else {
            collapseButton.setIcon(UNCOLLAPSED_ICON);
            collapseButton.addMouseListener(new MouseAdapter() {
                @Override
                public void mouseReleased(MouseEvent e) {
                    plugin.switchTabCollapse(tab);
                }

                @Override
                public void mouseEntered(MouseEvent e) {
                    collapseButton.setIcon(UNCOLLAPSED_HOVER_ICON);
                }

                @Override
                public void mouseExited(MouseEvent e) {
                    collapseButton.setIcon(UNCOLLAPSED_ICON);
                }
            });

            leftActions.add(tabName, BorderLayout.EAST);
            leftActions.add(collapseButton, BorderLayout.WEST);

            topPanel.add(leftActions, BorderLayout.WEST);

            // Actions Panel
            JPanel rightActions = new JPanel(new BorderLayout());
            rightActions.setBorder(new EmptyBorder(0, 0, 0, 5));
            rightActions.setOpaque(false);

            // Edit Button
            JLabel edit = new JLabel(EDIT_ICON);
            edit.setVerticalAlignment(SwingConstants.CENTER);
            edit.setBorder(new EmptyBorder(0, 0, 0, 0));
            edit.setOpaque(false);
            edit.addMouseListener(new MouseAdapter() {
                @Override
                public void mouseReleased(MouseEvent e) {
                    plugin.editTab(tab);
                }

                @Override
                public void mouseEntered(MouseEvent e) {
                    edit.setIcon(EDIT_HOVER_ICON);
                }

                @Override
                public void mouseExited(MouseEvent e) {
                    edit.setIcon(EDIT_ICON);
                }
            });

            rightActions.add(edit, BorderLayout.WEST);

            // Empty panel to separate without causing extra hover
            JPanel empty = new JPanel();
            empty.setOpaque(false);
            rightActions.add(empty, BorderLayout.CENTER);

            JLabel addItem = new JLabel(ADD_ICON);
            addItem.setOpaque(false);
            addItem.addMouseListener(new MouseAdapter() {
                @Override
                public void mouseReleased(MouseEvent e) {
                    final String[] itemNames = plugin.getItems().stream().map(MarketWatcherItem::getName).toArray(String[]::new);
                    Arrays.sort(itemNames, String.CASE_INSENSITIVE_ORDER);

                    MarketWatcherSelectionPanel selection = new MarketWatcherSelectionPanel(panel, itemNames);
                    selection.setOnOk(e1 -> {
                        List<String> selectedItems = selection.getSelectedItems();
                        if (!selectedItems.isEmpty()) {
                            plugin.addItemsToTab(tab, selectedItems);
                        }
                    });
                    selection.show();
                }

                @Override
                public void mouseEntered(MouseEvent e) {
                    addItem.setIcon(ADD_HOVER_ICON);
                }

                @Override
                public void mouseExited(MouseEvent e) {
                    addItem.setIcon(ADD_ICON);
                }
            });
            rightActions.add(addItem, BorderLayout.EAST);

            topPanel.add(rightActions, BorderLayout.EAST);

            // Tab Items
            GridBagConstraints constraints = new GridBagConstraints();
            constraints.fill = GridBagConstraints.HORIZONTAL;
            constraints.gridwidth = 1;
            constraints.weightx = 1;
            constraints.gridx = 0;
            constraints.gridy = 1;

            JPanel itemsPanel = new JPanel();
            itemsPanel.setLayout(new GridBagLayout());
            itemsPanel.setBorder(new EmptyBorder(5, 5, 0, 5));
            itemsPanel.setOpaque(false);

            int index = 0;
            for (MarketWatcherItem item : tab.getItems()) {
                MarketWatcherTabItemPanel itemPanel = new MarketWatcherTabItemPanel(plugin, tab, item);

                if (index++ > 0) {
                    itemsPanel.add(createMarginWrapper(itemPanel), constraints);
                } else {
                    itemsPanel.add(itemPanel, constraints);
                }

                constraints.gridy++;
            }

            add(topPanel, BorderLayout.NORTH);
            add(itemsPanel, BorderLayout.CENTER);
        }
    }

    private boolean deleteConfirm() {
        int confirm = JOptionPane.showConfirmDialog(this,
                DELETE_MESSAGE, DELETE_TITLE, JOptionPane.YES_NO_OPTION);

        return confirm == JOptionPane.YES_NO_OPTION;
    }

    private JPanel createMarginWrapper(JPanel panel) {
        JPanel marginWrapper = new JPanel(new BorderLayout());
        marginWrapper.setOpaque(false);
        marginWrapper.setBorder(new EmptyBorder(5, 0, 0, 0));
        marginWrapper.add(panel, BorderLayout.NORTH);
        return marginWrapper;
    }


    @Override
    protected void paintComponent(Graphics g) {
        g.setColor(ColorScheme.DARKER_GRAY_COLOR);
        g.fillRect(0, 0, this.getWidth(), this.getHeight());
    }
}