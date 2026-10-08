package ui.admin;

import dao.MedicineDAO;
import dao.SupplierDAO;
import model.Medicine;
import model.Supplier;
import ui.UITheme;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.math.BigDecimal;
import java.sql.Date;
import java.util.List;

public class ManageMedicinesPanel extends JPanel {

    private final MedicineDAO medicineDAO = new MedicineDAO();
    private final SupplierDAO supplierDAO = new SupplierDAO();

    private final DefaultTableModel tableModel = new DefaultTableModel(
            new Object[]{"ID", "Name", "Company", "Type", "Price", "Stock", "Reorder Lvl", "Expiry Date", "Supplier"}, 0) {
        @Override public boolean isCellEditable(int row, int col) { return false; }
    };
    private final JTable table = new JTable(tableModel);
    private final JTextField searchField = new JTextField(20);

    // Form fields
    private final JTextField nameField = new JTextField(18);
    private final JTextField companyField = new JTextField(18);
    private final JComboBox<String> typeCombo = new JComboBox<>(new String[]{"Tablet", "Capsule", "Syrup", "Injection", "Cream"});
    private final JTextField priceField = new JTextField(8);
    private final JTextField stockField = new JTextField(8);
    private final JTextField reorderField = new JTextField(8);
    private final JTextField expiryField = new JTextField(10); // yyyy-MM-dd
    private final JComboBox<Supplier> supplierCombo = new JComboBox<>();

    private Integer editingMedicineId = null;

    public ManageMedicinesPanel() {
        setLayout(new BorderLayout(10, 10));
        setBorder(new EmptyBorder(16, 16, 16, 16));
        setBackground(UITheme.BG);

        add(buildTopBar(), BorderLayout.NORTH);
        add(buildTable(), BorderLayout.CENTER);
        add(buildFormCard(), BorderLayout.SOUTH);

        loadSuppliers();
        refreshTable(medicineDAO.getAllMedicines());

        table.getSelectionModel().addListSelectionListener(e -> {
            if (!e.getValueIsAdjusting()) loadSelectedIntoForm();
        });
    }

    private JPanel buildTopBar() {
        JPanel bar = new JPanel(new BorderLayout());
        bar.setOpaque(false);
        JLabel heading = UITheme.heading("Manage Medicines");
        bar.add(heading, BorderLayout.WEST);

        JPanel searchPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT, 6, 0));
        searchPanel.setOpaque(false);
        searchPanel.add(new JLabel("Search:"));
        searchPanel.add(searchField);
        JButton searchBtn = UITheme.flatButton("Search");
        searchBtn.addActionListener(e -> {
            String kw = searchField.getText().trim();
            refreshTable(kw.isEmpty() ? medicineDAO.getAllMedicines() : medicineDAO.search(kw));
        });
        JButton clearBtn = UITheme.flatButton("Show All");
        clearBtn.addActionListener(e -> { searchField.setText(""); refreshTable(medicineDAO.getAllMedicines()); });
        searchPanel.add(searchBtn);
        searchPanel.add(clearBtn);
        bar.add(searchPanel, BorderLayout.EAST);
        return bar;
    }

    private JScrollPane buildTable() {
        table.setRowHeight(26);
        table.setFont(UITheme.FONT_LABEL);
        table.getTableHeader().setFont(UITheme.FONT_BOLD);
        table.setSelectionBackground(new Color(0xD9F0E6));
        JScrollPane sp = new JScrollPane(table);
        sp.setBorder(BorderFactory.createLineBorder(new Color(0xDDE3E1)));
        return sp;
    }

    private JPanel buildFormCard() {
        JPanel card = UITheme.card();
        card.setLayout(new GridBagLayout());
        GridBagConstraints gc = new GridBagConstraints();
        gc.insets = new Insets(4, 6, 4, 6);
        gc.anchor = GridBagConstraints.WEST;

        int col = 0, row = 0;
        gc.gridx = col++; gc.gridy = row; card.add(new JLabel("Name:"), gc);
        gc.gridx = col++; card.add(nameField, gc);
        gc.gridx = col++; card.add(new JLabel("Company:"), gc);
        gc.gridx = col++; card.add(companyField, gc);
        gc.gridx = col++; card.add(new JLabel("Type:"), gc);
        gc.gridx = col++; card.add(typeCombo, gc);

        col = 0; row++;
        gc.gridx = col++; gc.gridy = row; card.add(new JLabel("Price (R):"), gc);
        gc.gridx = col++; card.add(priceField, gc);
        gc.gridx = col++; card.add(new JLabel("Stock Qty:"), gc);
        gc.gridx = col++; card.add(stockField, gc);
        gc.gridx = col++; card.add(new JLabel("Reorder Level:"), gc);
        gc.gridx = col++; card.add(reorderField, gc);

        col = 0; row++;
        gc.gridx = col++; gc.gridy = row; card.add(new JLabel("Expiry (yyyy-MM-dd):"), gc);
        gc.gridx = col++; card.add(expiryField, gc);
        gc.gridx = col++; card.add(new JLabel("Supplier:"), gc);
        gc.gridx = col++; gc.gridwidth = 3; card.add(supplierCombo, gc); gc.gridwidth = 1;

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

        buttons.add(addBtn);
        buttons.add(updateBtn);
        buttons.add(deleteBtn);
        buttons.add(clearBtn);

        gc.gridx = 0; gc.gridy = row; gc.gridwidth = 6;
        card.add(buttons, gc);

        return card;
    }

    private void loadSuppliers() {
        supplierCombo.removeAllItems();
        for (Supplier s : supplierDAO.getAllSuppliers()) supplierCombo.addItem(s);
    }

    private void refreshTable(List<Medicine> medicines) {
        tableModel.setRowCount(0);
        for (Medicine m : medicines) {
            tableModel.addRow(new Object[]{
                    m.getMedicineId(), m.getName(), m.getCompany(), m.getMedicineType(),
                    m.getPrice(), m.getQuantityInStock(), m.getReorderLevel(),
                    m.getExpiryDate(), m.getSupplierName() == null ? "-" : m.getSupplierName()
            });
        }
    }

    private void loadSelectedIntoForm() {
        int row = table.getSelectedRow();
        if (row < 0) return;
        editingMedicineId = (Integer) tableModel.getValueAt(row, 0);
        nameField.setText(String.valueOf(tableModel.getValueAt(row, 1)));
        companyField.setText(String.valueOf(tableModel.getValueAt(row, 2)));
        typeCombo.setSelectedItem(String.valueOf(tableModel.getValueAt(row, 3)));
        priceField.setText(String.valueOf(tableModel.getValueAt(row, 4)));
        stockField.setText(String.valueOf(tableModel.getValueAt(row, 5)));
        reorderField.setText(String.valueOf(tableModel.getValueAt(row, 6)));
        expiryField.setText(String.valueOf(tableModel.getValueAt(row, 7)));

        String supplierName = String.valueOf(tableModel.getValueAt(row, 8));
        for (int i = 0; i < supplierCombo.getItemCount(); i++) {
            if (supplierCombo.getItemAt(i).getSupplierName().equals(supplierName)) {
                supplierCombo.setSelectedIndex(i);
                break;
            }
        }
    }

    private Medicine buildMedicineFromForm() {
        Medicine m = new Medicine();
        m.setName(nameField.getText().trim());
        m.setCompany(companyField.getText().trim());
        m.setMedicineType((String) typeCombo.getSelectedItem());
        m.setPrice(new BigDecimal(priceField.getText().trim()));
        m.setQuantityInStock(Integer.parseInt(stockField.getText().trim()));
        m.setReorderLevel(Integer.parseInt(reorderField.getText().trim()));
        m.setExpiryDate(Date.valueOf(expiryField.getText().trim()));
        Supplier selected = (Supplier) supplierCombo.getSelectedItem();
        if (selected != null) m.setSupplierId(selected.getSupplierId());
        return m;
    }

    private void handleAdd() {
        try {
            if (nameField.getText().trim().isEmpty()) {
                JOptionPane.showMessageDialog(this, "Medicine name is required.", "Validation", JOptionPane.WARNING_MESSAGE);
                return;
            }
            Medicine m = buildMedicineFromForm();
            if (medicineDAO.addMedicine(m)) {
                JOptionPane.showMessageDialog(this, "Medicine added successfully.");
                clearForm();
                refreshTable(medicineDAO.getAllMedicines());
            } else {
                JOptionPane.showMessageDialog(this, "Failed to add medicine.", "Error", JOptionPane.ERROR_MESSAGE);
            }
        } catch (Exception ex) {
            JOptionPane.showMessageDialog(this, "Please check your inputs.\n" + ex.getMessage(), "Invalid Input", JOptionPane.WARNING_MESSAGE);
        }
    }

    private void handleUpdate() {
        if (editingMedicineId == null) {
            JOptionPane.showMessageDialog(this, "Select a medicine from the table first.", "No Selection", JOptionPane.WARNING_MESSAGE);
            return;
        }
        try {
            Medicine m = buildMedicineFromForm();
            m.setMedicineId(editingMedicineId);
            if (medicineDAO.updateMedicine(m)) {
                JOptionPane.showMessageDialog(this, "Medicine updated successfully.");
                clearForm();
                refreshTable(medicineDAO.getAllMedicines());
            } else {
                JOptionPane.showMessageDialog(this, "Failed to update medicine.", "Error", JOptionPane.ERROR_MESSAGE);
            }
        } catch (Exception ex) {
            JOptionPane.showMessageDialog(this, "Please check your inputs.\n" + ex.getMessage(), "Invalid Input", JOptionPane.WARNING_MESSAGE);
        }
    }

    private void handleDelete() {
        if (editingMedicineId == null) {
            JOptionPane.showMessageDialog(this, "Select a medicine from the table first.", "No Selection", JOptionPane.WARNING_MESSAGE);
            return;
        }
        int confirm = JOptionPane.showConfirmDialog(this, "Delete this medicine permanently?", "Confirm Delete", JOptionPane.YES_NO_OPTION);
        if (confirm == JOptionPane.YES_OPTION) {
            if (medicineDAO.deleteMedicine(editingMedicineId)) {
                clearForm();
                refreshTable(medicineDAO.getAllMedicines());
            } else {
                JOptionPane.showMessageDialog(this, "Could not delete (it may have existing sales records).", "Error", JOptionPane.ERROR_MESSAGE);
            }
        }
    }

    private void clearForm() {
        editingMedicineId = null;
        nameField.setText("");
        companyField.setText("");
        typeCombo.setSelectedIndex(0);
        priceField.setText("");
        stockField.setText("");
        reorderField.setText("");
        expiryField.setText("");
        table.clearSelection();
    }
}