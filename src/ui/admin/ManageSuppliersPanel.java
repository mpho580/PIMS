package ui.admin;

import dao.SupplierDAO;
import model.Supplier;
import ui.UITheme;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.util.List;

public class ManageSuppliersPanel extends JPanel {

    private final SupplierDAO supplierDAO = new SupplierDAO();

    private final DefaultTableModel tableModel = new DefaultTableModel(
            new Object[]{"ID", "Supplier Name", "Contact Person", "Phone", "Email", "Address"}, 0) {
        @Override public boolean isCellEditable(int row, int col) { return false; }
    };
    private final JTable table = new JTable(tableModel);

    private final JTextField nameField = new JTextField(18);
    private final JTextField contactField = new JTextField(18);
    private final JTextField phoneField = new JTextField(14);
    private final JTextField emailField = new JTextField(18);
    private final JTextField addressField = new JTextField(24);

    private Integer editingSupplierId = null;

    public ManageSuppliersPanel() {
        setLayout(new BorderLayout(10, 10));
        setBorder(new EmptyBorder(16, 16, 16, 16));
        setBackground(UITheme.BG);

        add(UITheme.heading("Manage Suppliers"), BorderLayout.NORTH);
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
        gc.gridx = 0; gc.gridy = row; card.add(new JLabel("Supplier Name:"), gc);
        gc.gridx = 1; card.add(nameField, gc);
        gc.gridx = 2; card.add(new JLabel("Contact Person:"), gc);
        gc.gridx = 3; card.add(contactField, gc);

        row++;
        gc.gridx = 0; gc.gridy = row; card.add(new JLabel("Phone:"), gc);
        gc.gridx = 1; card.add(phoneField, gc);
        gc.gridx = 2; card.add(new JLabel("Email:"), gc);
        gc.gridx = 3; card.add(emailField, gc);

        row++;
        gc.gridx = 0; gc.gridy = row; card.add(new JLabel("Address:"), gc);
        gc.gridx = 1; gc.gridwidth = 3; card.add(addressField, gc); gc.gridwidth = 1;

        row++;
        JPanel buttons = new JPanel(new FlowLayout(FlowLayout.LEFT, 8, 4));
        buttons.setOpaque(false);
        JButton addBtn = UITheme.primaryButton("Add New");
        JButton updateBtn = UITheme.flatButton("Update Selected");
        JButton deleteBtn = UITheme.dangerButton("Delete Selected");
        JButton clearBtn = UITheme.flatButton("Clear Form");

        addBtn.addActionListener(e -> handleAdd());
        updateBtn.addActionListener(e -> handleUpdate());
        deleteBtn.addActionListener(e -> handleDelete());
        clearBtn.addActionListener(e -> clearForm());

        buttons.add(addBtn); buttons.add(updateBtn); buttons.add(deleteBtn); buttons.add(clearBtn);

        gc.gridx = 0; gc.gridy = row; gc.gridwidth = 4;
        card.add(buttons, gc);
        return card;
    }

    private void refresh() {
        tableModel.setRowCount(0);
        List<Supplier> suppliers = supplierDAO.getAllSuppliers();
        for (Supplier s : suppliers) {
            tableModel.addRow(new Object[]{s.getSupplierId(), s.getSupplierName(), s.getContactPerson(),
                    s.getPhone(), s.getEmail(), s.getAddress()});
        }
    }

    private void loadSelectedIntoForm() {
        int row = table.getSelectedRow();
        if (row < 0) return;
        editingSupplierId = (Integer) tableModel.getValueAt(row, 0);
        nameField.setText(String.valueOf(tableModel.getValueAt(row, 1)));
        contactField.setText(String.valueOf(tableModel.getValueAt(row, 2)));
        phoneField.setText(String.valueOf(tableModel.getValueAt(row, 3)));
        emailField.setText(String.valueOf(tableModel.getValueAt(row, 4)));
        addressField.setText(String.valueOf(tableModel.getValueAt(row, 5)));
    }

    private Supplier buildFromForm() {
        Supplier s = new Supplier();
        s.setSupplierName(nameField.getText().trim());
        s.setContactPerson(contactField.getText().trim());
        s.setPhone(phoneField.getText().trim());
        s.setEmail(emailField.getText().trim());
        s.setAddress(addressField.getText().trim());
        return s;
    }

    private void handleAdd() {
        if (nameField.getText().trim().isEmpty()) {
            JOptionPane.showMessageDialog(this, "Supplier name is required.", "Validation", JOptionPane.WARNING_MESSAGE);
            return;
        }
        if (supplierDAO.addSupplier(buildFromForm())) {
            clearForm();
            refresh();
        } else {
            JOptionPane.showMessageDialog(this, "Failed to add supplier.", "Error", JOptionPane.ERROR_MESSAGE);
        }
    }

    private void handleUpdate() {
        if (editingSupplierId == null) {
            JOptionPane.showMessageDialog(this, "Select a supplier from the table first.", "No Selection", JOptionPane.WARNING_MESSAGE);
            return;
        }
        Supplier s = buildFromForm();
        s.setSupplierId(editingSupplierId);
        if (supplierDAO.updateSupplier(s)) {
            clearForm();
            refresh();
        } else {
            JOptionPane.showMessageDialog(this, "Failed to update supplier.", "Error", JOptionPane.ERROR_MESSAGE);
        }
    }

    private void handleDelete() {
        if (editingSupplierId == null) {
            JOptionPane.showMessageDialog(this, "Select a supplier from the table first.", "No Selection", JOptionPane.WARNING_MESSAGE);
            return;
        }
        int confirm = JOptionPane.showConfirmDialog(this, "Delete this supplier?", "Confirm Delete", JOptionPane.YES_NO_OPTION);
        if (confirm == JOptionPane.YES_OPTION) {
            if (supplierDAO.deleteSupplier(editingSupplierId)) {
                clearForm();
                refresh();
            } else {
                JOptionPane.showMessageDialog(this, "Could not delete (medicines may still reference this supplier).", "Error", JOptionPane.ERROR_MESSAGE);
            }
        }
    }

    private void clearForm() {
        editingSupplierId = null;
        nameField.setText("");
        contactField.setText("");
        phoneField.setText("");
        emailField.setText("");
        addressField.setText("");
        table.clearSelection();
    }
}