package ui;

import dao.UserDAO;
import model.User;
import ui.admin.AdminDashboard;
import ui.cashier.CashierDashboard;
import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;
import java.awt.event.ActionEvent;

public class LoginFrame extends JFrame {

    private final JTextField usernameField = new JTextField(18);
    private final JPasswordField passwordField = new JPasswordField(18);
    private final JLabel messageLabel = new JLabel(" ");
    private final UserDAO userDAO = new UserDAO();

    public LoginFrame() {
        setTitle("HealthFirst Pharmacy - Login");
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setSize(420, 480);
        setLocationRelativeTo(null);
        setResizable(false);
        getContentPane().setBackground(UITheme.PRIMARY);
        setLayout(new BorderLayout());

        add(buildHeader(), BorderLayout.NORTH);
        add(buildFormCard(), BorderLayout.CENTER);
    }

    private JPanel buildHeader() {
        JPanel header = new JPanel();
        header.setOpaque(false);
        header.setBorder(new EmptyBorder(30, 20, 10, 20));
        header.setLayout(new BoxLayout(header, BoxLayout.Y_AXIS));

        JLabel logo = new JLabel("HealthFirst Pharmacy");
        logo.setFont(new Font("Segoe UI", Font.BOLD, 24));
        logo.setForeground(Color.WHITE);
        logo.setAlignmentX(Component.CENTER_ALIGNMENT);

        JLabel subtitle = new JLabel("Pharmacy Inventory Management System");
        subtitle.setFont(UITheme.FONT_LABEL);
        subtitle.setForeground(new Color(230, 240, 238));
        subtitle.setAlignmentX(Component.CENTER_ALIGNMENT);

        header.add(logo);
        header.add(Box.createVerticalStrut(6));
        header.add(subtitle);
        return header;
    }

    private JPanel buildFormCard() {
        JPanel card = UITheme.card();
        card.setLayout(new GridBagLayout());
        card.setPreferredSize(new Dimension(320, 300));

        GridBagConstraints gc = new GridBagConstraints();
        gc.insets = new Insets(8, 8, 8, 8);
        gc.gridx = 0; gc.gridy = 0; gc.gridwidth = 2; gc.anchor = GridBagConstraints.CENTER;
        card.add(UITheme.heading("Sign in to continue"), gc);

        gc.gridwidth = 1; gc.gridy++; gc.anchor = GridBagConstraints.WEST;
        card.add(new JLabel("Username:"), gc);
        gc.gridx = 1;
        card.add(usernameField, gc);

        gc.gridx = 0; gc.gridy++;
        card.add(new JLabel("Password:"), gc);
        gc.gridx = 1;
        card.add(passwordField, gc);

        gc.gridx = 0; gc.gridy++; gc.gridwidth = 2; gc.anchor = GridBagConstraints.CENTER;
        messageLabel.setForeground(UITheme.DANGER);
        messageLabel.setFont(UITheme.FONT_LABEL);
        card.add(messageLabel, gc);

        gc.gridy++;
        JButton loginBtn = UITheme.primaryButton("Login");
        loginBtn.addActionListener(this::attemptLogin);
        card.add(loginBtn, gc);
        getRootPane().setDefaultButton(loginBtn);

        gc.gridy++;
        JLabel hint = new JLabel("<html><center>Demo logins &mdash; Admin: admin/admin123<br>Cashier: cashier/cash123</center></html>");
        hint.setFont(new Font("Segoe UI", Font.PLAIN, 11));
        hint.setForeground(UITheme.TEXT_MUTED);
        card.add(hint, gc);

        JPanel outer = new JPanel(new GridBagLayout());
        outer.setBackground(UITheme.PRIMARY);
        outer.add(card);
        return outer;
    }

    private void attemptLogin(ActionEvent e) {
        String username = usernameField.getText().trim();
        String password = new String(passwordField.getPassword());

        if (username.isEmpty() || password.isEmpty()) {
            messageLabel.setText("Please enter both username and password.");
            return;
        }

        User user = userDAO.authenticate(username, password);
        if (user == null) {
            messageLabel.setText("Invalid username or password.");
            passwordField.setText("");
            return;
        }

        dispose();
        if (user.isAdmin()) {
            new AdminDashboard(user).setVisible(true);
        } else {
            new CashierDashboard(user).setVisible(true);
        }
    }
}