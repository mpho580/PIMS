package ui.admin;

import dao.UserDAO;
import model.User;
import ui.UITheme;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.util.List;

public class ManageUsersPanel extends JPanel {

    private final UserDAO userDAO = new UserDAO();

    private final DefaultTableModel tableModel = new DefaultTableModel(
            new Object[]{"ID", "Username", "Full Name", "Role", "Active"}, 0) {
        @Override public boolean isCellEditable(int row, int col) { return false; }
    };
    private final JTable table = new JTable(tableModel);

    private final JTextField usernameField = new JTextField(16);
    private final JTextField fullNameField = new JTextField(18);
    private final JPasswordField passwordField = new JPasswordField(16);
    private final JComboBox<String> roleCombo = new JComboBox<>(new String[]{"ADMIN", "CASHIER"});
    private final JCheckBox activeCheck = new JCheckBox("Active", true);

    private Integer editingUserId = null;

    public ManageUsersPanel() {
        setLayout(new BorderLayout(10, 10));
        setBorder(new EmptyBorder(16, 16, 16, 16));
        setBackground(UITheme.BG);

        add(UITheme.heading("Manage Users"), BorderLayout.NORTH);
        add(buildTable(), BorderLayout.CENTER);
        add(buildFormCard(), BorderLayout.SOUTH);

        refresh();
        table.getSelectionModel().addListSelectionListener(e -> {
            if (!e.getValueIsAdjusting()) loadSelectedIntoForm();
        });
    }

    private JScrollPane buildTable() {
        table.setRowHeight(26);
        table.setFont(UITheme.FONT_LABEL);
        table.getTableHeader().setFont(UITheme.FONT_BOLD);
        table.setSelectionBackground(new Color(0xD9F0E6));
        return new JScrollPane(table);
    }

    private JPanel buildFormCard() {
        JPanel card = UITheme.card();
        card.setLayout(new GridBagLayout());
        GridBagConstraints gc = new GridBagConstraints();
        gc.insets = new Insets(4, 6, 4, 6);
        gc.anchor = GridBagConstraints.WEST;

        int row = 0;
        gc.gridx = 0; gc.gridy = row; card.add(new JLabel("Username:"), gc);
        gc.gridx = 1; card.add(usernameField, gc);
        gc.gridx = 2; card.add(new JLabel("Full Name:"), gc);
        gc.gridx = 3; card.add(fullNameField, gc);

        row++;
        gc.gridx = 0; gc.gridy = row; card.add(new JLabel("Password:"), gc);
        gc.gridx = 1; card.add(passwordField, gc);
        gc.gridx = 2; card.add(new JLabel("Role:"), gc);
        gc.gridx = 3; card.add(roleCombo, gc);

        row++;
        gc.gridx = 0; gc.gridy = row; card.add(activeCheck, gc);
        JLabel note = new JLabel("(Password field only needed when adding a new user or resetting one.)");
        note.setFont(new Font("Segoe UI", Font.ITALIC, 11));
        note.setForeground(UITheme.TEXT_MUTED);
        gc.gridx = 1; gc.gridwidth = 3; card.add(note, gc); gc.gridwidth = 1;

        row++;
        JPanel buttons = new JPanel(new FlowLayout(FlowLayout.LEFT, 8, 4));
        buttons.setOpaque(false);
        JButton addBtn = UITheme.primaryButton("Add New User");
        JButton updateBtn = UITheme.flatButton("Update Selected");
        JButton resetPwBtn = UITheme.flatButton("Reset Password");
        JButton deleteBtn = UITheme.dangerButton("Delete / Deactivate");
        JButton clearBtn = UITheme.flatButton("Clear Form");

        addBtn.addActionListener(e -> handleAdd());
        updateBtn.addActionListener(e -> handleUpdate());
        resetPwBtn.addActionListener(e -> handleResetPassword());
        deleteBtn.addActionListener(e -> handleDelete());
        clearBtn.addActionListener(e -> clearForm());

        buttons.add(addBtn); buttons.add(updateBtn); buttons.add(resetPwBtn);
        buttons.add(deleteBtn); buttons.add(clearBtn);

        gc.gridx = 0; gc.gridy = row; gc.gridwidth = 4;
        card.add(buttons, gc);
        return card;
    }

    private void refresh() {
        tableModel.setRowCount(0);
        List<User> users = userDAO.getAllUsers();
        for (User u : users) {
            tableModel.addRow(new Object[]{u.getUserId(), u.getUsername(), u.getFullName(),
                    u.getRole(), u.isActive() ? "Yes" : "No"});
        }
    }

    private void loadSelectedIntoForm() {
        int row = table.getSelectedRow();
        if (row < 0) return;
        editingUserId = (Integer) tableModel.getValueAt(row, 0);
        usernameField.setText(String.valueOf(tableModel.getValueAt(row, 1)));
        usernameField.setEnabled(false); // username is fixed once created
        fullNameField.setText(String.valueOf(tableModel.getValueAt(row, 2)));
        roleCombo.setSelectedItem(String.valueOf(tableModel.getValueAt(row, 3)));
        activeCheck.setSelected("Yes".equals(String.valueOf(tableModel.getValueAt(row, 4))));
        passwordField.setText("");
    }

    private void handleAdd() {
        String username = usernameField.getText().trim();
        String fullName = fullNameField.getText().trim();
        String password = new String(passwordField.getPassword());
        String role = (String) roleCombo.getSelectedItem();

        if (username.isEmpty() || fullName.isEmpty() || password.isEmpty()) {
            JOptionPane.showMessageDialog(this, "Username, full name and password are all required for a new user.",
                    "Validation", JOptionPane.WARNING_MESSAGE);
            return;
        }
        if (userDAO.addUser(username, password, fullName, role)) {
            JOptionPane.showMessageDialog(this, "User created successfully.");
            clearForm();
            refresh();
        } else {
            JOptionPane.showMessageDialog(this, "Failed to create user (username may already exist).", "Error", JOptionPane.ERROR_MESSAGE);
        }
    }

    private void handleUpdate() {
        if (editingUserId == null) {
            JOptionPane.showMessageDialog(this, "Select a user from the table first.", "No Selection", JOptionPane.WARNING_MESSAGE);
            return;
        }
        String fullName = fullNameField.getText().trim();
        String role = (String) roleCombo.getSelectedItem();
        boolean active = activeCheck.isSelected();

        if (userDAO.updateUser(editingUserId, fullName, role, active)) {
            JOptionPane.showMessageDialog(this, "User updated successfully.");
            clearForm();
            refresh();
        } else {
            JOptionPane.showMessageDialog(this, "Failed to update user.", "Error", JOptionPane.ERROR_MESSAGE);
        }
    }

    private void handleResetPassword() {
        if (editingUserId == null) {
            JOptionPane.showMessageDialog(this, "Select a user from the table first.", "No Selection", JOptionPane.WARNING_MESSAGE);
            return;
        }
        String newPassword = new String(passwordField.getPassword());
        if (newPassword.isEmpty()) {
            JOptionPane.showMessageDialog(this, "Type the new password into the Password field first.", "Validation", JOptionPane.WARNING_MESSAGE);
            return;
        }
        if (userDAO.resetPassword(editingUserId, newPassword)) {
            JOptionPane.showMessageDialog(this, "Password reset successfully.");
            passwordField.setText("");
        } else {
            JOptionPane.showMessageDialog(this, "Failed to reset password.", "Error", JOptionPane.ERROR_MESSAGE);
        }
    }

    private void handleDelete() {
        if (editingUserId == null) {
            JOptionPane.showMessageDialog(this, "Select a user from the table first.", "No Selection", JOptionPane.WARNING_MESSAGE);
            return;
        }
        int confirm = JOptionPane.showConfirmDialog(this,
                "Delete this user permanently?\n(If they have processed sales, they'll be deactivated instead so sales history stays intact.)",
                "Confirm Delete", JOptionPane.YES_NO_OPTION);
        if (confirm != JOptionPane.YES_OPTION) return;

        if (userDAO.deleteUser(editingUserId)) {
            clearForm();
            refresh();
        } else {
            // Hard delete failed (FK constraint from sales) - fall back to deactivating.
            String fullName = fullNameField.getText().trim();
            String role = (String) roleCombo.getSelectedItem();
            userDAO.updateUser(editingUserId, fullName, role, false);
            JOptionPane.showMessageDialog(this, "This user has sales on record, so they were deactivated instead of deleted.");
            clearForm();
            refresh();
        }
    }

    private void clearForm() {
        editingUserId = null;
        usernameField.setText("");
        usernameField.setEnabled(true);
        fullNameField.setText("");
        passwordField.setText("");
        roleCombo.setSelectedIndex(0);
        activeCheck.setSelected(true);
        table.clearSelection();
    }
}