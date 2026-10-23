package pims;

import javax.swing.*;
import ui.LoginFrame;

public class PIMS {
    public static void main(String[] args) {
        // Use the OS native look and feel for a more polished, less "Java-y" appearance.
        try {
            UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
        } catch (Exception ignored) { }
        SwingUtilities.invokeLater(() -> new LoginFrame().setVisible(true));
    }   
}