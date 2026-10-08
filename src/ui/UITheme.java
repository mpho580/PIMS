package ui;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;

/** Central place for colours/fonts so every screen looks consistent. */
public class UITheme {
    public static final Color PRIMARY      = new Color(0x1E7A5F);   // pharmacy green
    public static final Color PRIMARY_DARK = new Color(0x145C46);
    public static final Color ACCENT       = new Color(0x2E9E6B);
    public static final Color DANGER       = new Color(0xC0392B);
    public static final Color WARNING      = new Color(0xE67E22);
    public static final Color BG           = new Color(0xF4F6F5);
    public static final Color CARD_BG      = Color.WHITE;
    public static final Color TEXT_DARK    = new Color(0x263238);
    public static final Color TEXT_MUTED   = new Color(0x6B7A78);

    public static final Font FONT_TITLE   = new Font("Segoe UI", Font.BOLD, 22);
    public static final Font FONT_HEADING = new Font("Segoe UI", Font.BOLD, 16);
    public static final Font FONT_LABEL   = new Font("Segoe UI", Font.PLAIN, 13);
    public static final Font FONT_BOLD    = new Font("Segoe UI", Font.BOLD, 13);
    public static final Font FONT_MONO    = new Font("Consolas", Font.PLAIN, 13);

    public static JButton primaryButton(String text) {
        JButton b = new JButton(text);
        styleButton(b, PRIMARY, Color.WHITE);
        return b;
    }

    public static JButton dangerButton(String text) {
        JButton b = new JButton(text);
        styleButton(b, DANGER, Color.WHITE);
        return b;
    }

    public static JButton flatButton(String text) {
        JButton b = new JButton(text);
        b.setFocusPainted(false);
        b.setBackground(Color.WHITE);
        b.setForeground(PRIMARY_DARK);
        b.setBorder(BorderFactory.createLineBorder(PRIMARY, 1));
        b.setFont(FONT_BOLD);
        b.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        return b;
    }

    private static void styleButton(JButton b, Color bg, Color fg) {
        b.setBackground(bg);
        b.setForeground(fg);
        b.setFocusPainted(false);
        b.setFont(FONT_BOLD);
        b.setBorder(new EmptyBorder(8, 18, 8, 18));
        b.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
    }

    public static JLabel heading(String text) {
        JLabel l = new JLabel(text);
        l.setFont(FONT_HEADING);
        l.setForeground(TEXT_DARK);
        return l;
    }

    public static JPanel card() {
        JPanel p = new JPanel();
        p.setBackground(CARD_BG);
        p.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(new Color(0xDDE3E1), 1),
                new EmptyBorder(16, 16, 16, 16)));
        return p;
    }
}