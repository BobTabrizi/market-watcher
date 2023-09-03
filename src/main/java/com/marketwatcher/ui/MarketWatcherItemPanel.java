package com.marketwatcher.ui;

import com.marketwatcher.MarketWatcherPlugin;
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

public class MarketWatcherItemPanel extends JPanel {
    private static final String DELETE_TITLE = "Warning";
    private static final String DELETE_MESSAGE = "Are you sure you want to delete this item?";
    private static final ImageIcon DELETE_ICON;
    private static final ImageIcon DELETE_HOVER_ICON;
    private static final ImageIcon SHIFT_UP_ICON;
    private static final ImageIcon SHIFT_UP_HOVER_ICON;
    private static final ImageIcon SHIFT_DOWN_ICON;
    private static final ImageIcon SHIFT_DOWN_HOVER_ICON;

    static {
        final BufferedImage deleteImage = ImageUtil.loadImageResource(MarketWatcherItemPanel.class, DELETE_ICON_PATH);
        DELETE_ICON = new ImageIcon(deleteImage);
        DELETE_HOVER_ICON = new ImageIcon(ImageUtil.alphaOffset(deleteImage, 0.53f));

        final BufferedImage shiftUpImage = ImageUtil.loadImageResource(MarketWatcherItemPanel.class, SHIFT_UP_ICON_PATH);
        SHIFT_UP_ICON = new ImageIcon(shiftUpImage);
        SHIFT_UP_HOVER_ICON = new ImageIcon(ImageUtil.alphaOffset(shiftUpImage, 0.53f));

        final BufferedImage shiftDownImage = ImageUtil.loadImageResource(MarketWatcherItemPanel.class, SHIFT_DOWN_ICON_PATH);
        SHIFT_DOWN_ICON = new ImageIcon(shiftDownImage);
        SHIFT_DOWN_HOVER_ICON = new ImageIcon(ImageUtil.alphaOffset(shiftDownImage, 0.53f));
    }

    MarketWatcherItemPanel(MarketWatcherPlugin plugin, MarketWatcherItem item) {
        setLayout(new BorderLayout(5, 0));
        setBorder(new EmptyBorder(5, 5, 5, 0));

        int itemIndex = plugin.getItems().indexOf(item);
        int itemsSize = plugin.getItems().size();

        JPanel rightPanel = createRightPanel(item, STANDARD);

        // Action Panel (Delete, Shift item)
        JPanel actionPanel = new JPanel(new BorderLayout());
        actionPanel.setBackground(new Color(0, 0, 0, 0));
        actionPanel.setOpaque(false);

        // Delete Item
        JLabel deleteItem = new JLabel(DELETE_ICON);
        deleteItem.setBorder(new EmptyBorder(0, 0, 0, 3));
        deleteItem.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseReleased(MouseEvent e) {
                if (deleteConfirm()) {
                    plugin.removeItem(item);
                }
            }

            @Override
            public void mouseEntered(MouseEvent e) {
                deleteItem.setIcon(DELETE_HOVER_ICON);
            }

            @Override
            public void mouseExited(MouseEvent e) {
                deleteItem.setIcon(DELETE_ICON);
            }
        });
        actionPanel.add(deleteItem, BorderLayout.NORTH);

        // Shift Item Panel
        JPanel shiftItemPanel = new JPanel(new BorderLayout());
        shiftItemPanel.setOpaque(false);

        // Shift item up
        JLabel shiftUp = new JLabel(SHIFT_UP_ICON);
        shiftUp.setBorder(new EmptyBorder(0, 0, 15, 5));

        if (itemIndex == 0) {
            shiftUp.setIcon(SHIFT_UP_HOVER_ICON);
        }

        shiftUp.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseReleased(MouseEvent e) {
                if (itemIndex != 0) {
                    plugin.shiftItem(itemIndex, true);
                }
            }

            @Override
            public void mouseEntered(MouseEvent e) {
                shiftUp.setIcon(SHIFT_UP_HOVER_ICON);
            }

            @Override
            public void mouseExited(MouseEvent e) {
                if (itemIndex != 0) {
                    shiftUp.setIcon(SHIFT_UP_ICON);
                }
            }
        });
        shiftItemPanel.add(shiftUp, BorderLayout.NORTH);

        // Shift item down
        JLabel shiftDown = new JLabel(SHIFT_DOWN_ICON);
        shiftDown.setBorder(new EmptyBorder(15, 0, 20, 5));

        if (itemIndex == itemsSize - 1) {
            shiftDown.setIcon(SHIFT_DOWN_HOVER_ICON);
        }

        shiftDown.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseReleased(MouseEvent e) {
                if (itemIndex != itemsSize - 1) {
                    plugin.shiftItem(itemIndex, false);
                }
            }

            @Override
            public void mouseEntered(MouseEvent e) {
                shiftDown.setIcon(SHIFT_DOWN_HOVER_ICON);
            }

            @Override
            public void mouseExited(MouseEvent e) {
                if (itemIndex != itemsSize - 1) {
                    shiftDown.setIcon(SHIFT_DOWN_ICON);
                }
            }
        });
        shiftItemPanel.add(shiftDown, BorderLayout.EAST);
        actionPanel.add(shiftItemPanel, BorderLayout.SOUTH);

        add(rightPanel, BorderLayout.WEST);
        add(actionPanel, BorderLayout.EAST);
    }

    private boolean deleteConfirm() {
        int confirm = JOptionPane.showConfirmDialog(this,
                DELETE_MESSAGE, DELETE_TITLE, JOptionPane.YES_NO_OPTION);

        return confirm == JOptionPane.YES_NO_OPTION;
    }


    @Override
    protected void paintComponent(Graphics g) {
        g.setColor(ColorScheme.DARKER_GRAY_COLOR);
        g.fillRect(0, 0, this.getWidth(), this.getHeight());
    }

}