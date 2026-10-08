package ui.admin;

import model.User;
import ui.LoginFrame;
import ui.UITheme;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;

public class AdminDashboard extends JFrame {

    private final User currentUser;

    public AdminDashboard(User currentUser) {
        this.currentUser = currentUser;

        setTitle("HealthFirst Pharmacy - Admin Dashboard");
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setSize(1100, 700);
        setLocationRelativeTo(null);
        setLayout(new BorderLayout());
        getContentPane().setBackground(UITheme.BG);

        add(buildTopBar(), BorderLayout.NORTH);
        add(buildTabs(), BorderLayout.CENTER);
    }

    private JPanel buildTopBar() {
        JPanel bar = new JPanel(new BorderLayout());
        bar.setBackground(UITheme.PRIMARY);
        bar.setBorder(new EmptyBorder(14, 20, 14, 20));

        JLabel title = new JLabel("HealthFirst Pharmacy \u2014 Admin Dashboard");
        title.setFont(UITheme.FONT_TITLE);
        title.setForeground(Color.WHITE);
        bar.add(title, BorderLayout.WEST);

        JPanel right = new JPanel(new FlowLayout(FlowLayout.RIGHT, 10, 0));
        right.setOpaque(false);
        JLabel who = new JLabel("Logged in as " + currentUser.getFullName() + " (Administrator)");
        who.setForeground(Color.WHITE);
        who.setFont(UITheme.FONT_LABEL);
        JButton logout = UITheme.flatButton("Logout");
        logout.setBackground(UITheme.PRIMARY);
        logout.setForeground(Color.WHITE);
        logout.setBorder(BorderFactory.createLineBorder(Color.WHITE, 1));
        logout.addActionListener(e -> {
            dispose();
            new LoginFrame().setVisible(true);
        });
        right.add(who);
        right.add(logout);
        bar.add(right, BorderLayout.EAST);
        return bar;
    }

    private JTabbedPane buildTabs() {
        JTabbedPane tabs = new JTabbedPane();
        tabs.setFont(UITheme.FONT_BOLD);
        tabs.addTab("  Manage Medicines  ", new ManageMedicinesPanel());
        tabs.addTab("  Manage Suppliers  ", new ManageSuppliersPanel());
        tabs.addTab("  Manage Users  ", new ManageUsersPanel());
        tabs.addTab("  Reports  ", new ReportsPanel());
        return tabs;
    }
}
