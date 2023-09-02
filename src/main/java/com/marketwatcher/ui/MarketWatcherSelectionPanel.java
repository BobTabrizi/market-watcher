package com.marketwatcher.ui;

import net.runelite.client.ui.ColorScheme;

import javax.swing.DefaultListCellRenderer;
import javax.swing.JButton;
import javax.swing.JDialog;
import javax.swing.JLabel;
import javax.swing.JList;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.SwingConstants;
import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.List;

public class MarketWatcherSelectionPanel {
    private final JList<String> list;
    private ActionListener okEvent;
    private final JDialog dialog;
    private static final String OK = "Ok";
    private static final String CANCEL = "Cancel";
    private static final String TITLE = "Select Items";
    private static final String MESSAGE = "Select items to add to this tab";
    private static final String SUBMESSAGE = "Ctrl+Click to select multiple items";

    public MarketWatcherSelectionPanel(JPanel parent, String[] options) {
        this.list = new JList<>(options);
        this.list.setBackground(ColorScheme.DARKER_GRAY_COLOR);

        // Top Panel
        JPanel topPanel = new JPanel(new BorderLayout());

        JLabel message = new JLabel(MESSAGE);
        message.setHorizontalAlignment(SwingConstants.CENTER);
        JLabel subMessage = new JLabel(SUBMESSAGE);
        subMessage.setHorizontalAlignment(SwingConstants.CENTER);

        topPanel.add(message, BorderLayout.NORTH);
        topPanel.add(subMessage, BorderLayout.CENTER);

        // Center Panel with Items
        JPanel centerPanel = new JPanel(new BorderLayout(5, 5));
        centerPanel.setPreferredSize(new Dimension(250, 300));

        DefaultListCellRenderer renderer = (DefaultListCellRenderer) list.getCellRenderer();
        renderer.setHorizontalAlignment(SwingConstants.CENTER);

        JScrollPane scrollPane = new JScrollPane();
        scrollPane.setViewportView(list);

        centerPanel.add(topPanel, BorderLayout.NORTH);
        centerPanel.add(scrollPane, BorderLayout.CENTER);

        // Options
        JOptionPane optionPane = new JOptionPane(centerPanel);

        JButton okButton = new JButton(OK);
        okButton.addActionListener(this::onOkButtonClick);

        JButton cancelButton = new JButton(CANCEL);
        cancelButton.addActionListener(this::onCancelButtonClick);

        optionPane.setOptions(new Object[]{okButton, cancelButton});

        dialog = optionPane.createDialog(parent, TITLE);
        dialog.setTitle(TITLE);
    }

    public List<String> getSelectedItems() {
        return list.getSelectedValuesList();
    }

    public void setOnOk(ActionListener event) {
        okEvent = event;
    }

    private void onOkButtonClick(ActionEvent e) {
        if (okEvent != null) {
            okEvent.actionPerformed(e);
        }
        dialog.setVisible(false);
    }

    private void onCancelButtonClick(ActionEvent e) {
        dialog.setVisible(false);
    }

    public void show() {
        dialog.setVisible(true);
    }
}