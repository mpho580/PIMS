package ui.admin;

import dao.MedicineDAO;
import dao.SalesDAO;
import model.Medicine;
import model.Sale;
import ui.UITheme;
import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.math.BigDecimal;
import java.sql.Date;
import java.time.LocalDate;
import java.util.List;


public class ReportsPanel extends JPanel {

    private final SalesDAO salesDAO = new SalesDAO();
    private final MedicineDAO medicineDAO = new MedicineDAO();
    private Date expiryDate;
    
    public Date getExpiryDate() { 
        return expiryDate; 
    }

    public ReportsPanel() {
        setLayout(new BorderLayout());
        setBackground(UITheme.BG);
        setBorder(new EmptyBorder(16, 16, 16, 16));

        JTabbedPane subTabs = new JTabbedPane();
        subTabs.setFont(UITheme.FONT_BOLD);
        subTabs.addTab("Sales Report", buildSalesReportTab());
        subTabs.addTab("Item-Wise Sales", buildItemWiseTab());
        subTabs.addTab("Low Stock Report", buildLowStockTab());
        subTabs.addTab("Expiry Report", buildExpiryTab());

        add(UITheme.heading("Business Reports"), BorderLayout.NORTH);
        add(subTabs, BorderLayout.CENTER);
    }

    // ---------------------------------------------------------------
    // 1. SALES REPORT - transactions between two dates
    // ---------------------------------------------------------------
    private JPanel buildSalesReportTab() {
        JPanel panel = new JPanel(new BorderLayout(8, 8));
        panel.setBackground(UITheme.BG);
        panel.setBorder(new EmptyBorder(10, 0, 0, 0));

        DefaultTableModel model = new DefaultTableModel(
                new Object[]{"Sale ID", "Date/Time", "Cashier", "Total Amount (R)"}, 0) {
            @Override public boolean isCellEditable(int r, int c) { return false; }
        };
        JTable table = new JTable(model);
        table.setRowHeight(24);

        JTextField fromField = new JTextField(LocalDate.now().minusDays(30).toString(), 10);
        JTextField toField = new JTextField(LocalDate.now().toString(), 10);
        JLabel totalLabel = new JLabel("Total revenue in range: R 0.00");
        totalLabel.setFont(UITheme.FONT_BOLD);

        JPanel controls = new JPanel(new FlowLayout(FlowLayout.LEFT, 6, 6));
        controls.setOpaque(false);
        controls.add(new JLabel("From (yyyy-MM-dd):"));
        controls.add(fromField);
        controls.add(new JLabel("To:"));
        controls.add(toField);
        JButton runBtn = UITheme.primaryButton("Run Report");
        controls.add(runBtn);

        runBtn.addActionListener(e -> {
            try {
                Date from = Date.valueOf(fromField.getText().trim());
                Date to = Date.valueOf(toField.getText().trim());
                model.setRowCount(0);
                List<Sale> sales = salesDAO.getSalesBetween(from, to);
                for (Sale s : sales) {
                    model.addRow(new Object[]{s.getSaleId(), s.getSaleDate(), s.getCashierName(), s.getTotalAmount()});
                }
                BigDecimal total = salesDAO.getTotalRevenue(from, to);
                totalLabel.setText("Total revenue in range: R " + total.setScale(2, BigDecimal.ROUND_HALF_UP));
            } catch (IllegalArgumentException ex) {
                JOptionPane.showMessageDialog(this, "Please use the date format yyyy-MM-dd.", "Invalid Date", JOptionPane.WARNING_MESSAGE);
            }
        });

        panel.add(controls, BorderLayout.NORTH);
        panel.add(new JScrollPane(table), BorderLayout.CENTER);
        panel.add(totalLabel, BorderLayout.SOUTH);

        runBtn.doClick(); // populate on first load
        return panel;
    }

    // ---------------------------------------------------------------
    // 2. ITEM-WISE SALES REPORT
    // ---------------------------------------------------------------
    private JPanel buildItemWiseTab() {
        JPanel panel = new JPanel(new BorderLayout(8, 8));
        panel.setBackground(UITheme.BG);
        panel.setBorder(new EmptyBorder(10, 0, 0, 0));

        DefaultTableModel model = new DefaultTableModel(
                new Object[]{"Medicine", "Total Qty Sold", "Total Revenue (R)"}, 0) {
            @Override public boolean isCellEditable(int r, int c) { return false; }
        };
        JTable table = new JTable(model);
        table.setRowHeight(24);

        JTextField fromField = new JTextField(LocalDate.now().minusDays(30).toString(), 10);
        JTextField toField = new JTextField(LocalDate.now().toString(), 10);

        JPanel controls = new JPanel(new FlowLayout(FlowLayout.LEFT, 6, 6));
        controls.setOpaque(false);
        controls.add(new JLabel("From (yyyy-MM-dd):"));
        controls.add(fromField);
        controls.add(new JLabel("To:"));
        controls.add(toField);
        JButton runBtn = UITheme.primaryButton("Run Report");
        controls.add(runBtn);

        runBtn.addActionListener(e -> {
            try {
                Date from = Date.valueOf(fromField.getText().trim());
                Date to = Date.valueOf(toField.getText().trim());
                model.setRowCount(0);
                for (Object[] row : salesDAO.getItemWiseSales(from, to)) {
                    model.addRow(row);
                }
            } catch (IllegalArgumentException ex) {
                JOptionPane.showMessageDialog(this, "Please use the date format yyyy-MM-dd.", "Invalid Date", JOptionPane.WARNING_MESSAGE);
            }
        });

        panel.add(controls, BorderLayout.NORTH);
        panel.add(new JScrollPane(table), BorderLayout.CENTER);

        runBtn.doClick();
        return panel;
    }

    // ---------------------------------------------------------------
    // 3. LOW STOCK REPORT - quantity <= reorder level
    // ---------------------------------------------------------------
    private JPanel buildLowStockTab() {
        JPanel panel = new JPanel(new BorderLayout(8, 8));
        panel.setBackground(UITheme.BG);
        panel.setBorder(new EmptyBorder(10, 0, 0, 0));

        DefaultTableModel model = new DefaultTableModel(
                new Object[]{"Medicine", "Company", "Current Stock", "Reorder Level", "Supplier"}, 0) {
            @Override public boolean isCellEditable(int r, int c) { return false; }
        };
        JTable table = new JTable(model);
        table.setRowHeight(24);

        JPanel controls = new JPanel(new FlowLayout(FlowLayout.LEFT, 6, 6));
        controls.setOpaque(false);
        JButton refreshBtn = UITheme.primaryButton("Refresh");
        controls.add(refreshBtn);
        JLabel countLabel = new JLabel();
        countLabel.setFont(UITheme.FONT_BOLD);
        controls.add(countLabel);

        Runnable load = () -> {
            model.setRowCount(0);
            List<Medicine> list = medicineDAO.getLowStock();
            for (Medicine m : list) {
                model.addRow(new Object[]{m.getName(), m.getCompany(), m.getQuantityInStock(),
                        m.getReorderLevel(), m.getSupplierName() == null ? "-" : m.getSupplierName()});
            }
            countLabel.setText(list.size() + " item(s) at or below reorder level");
        };
        refreshBtn.addActionListener(e -> load.run());

        panel.add(controls, BorderLayout.NORTH);
        panel.add(new JScrollPane(table), BorderLayout.CENTER);

        load.run();
        return panel;
    }

    // ---------------------------------------------------------------
    // 4. EXPIRY REPORT - medicines expiring within the next N days
    // ---------------------------------------------------------------
    private JPanel buildExpiryTab() {
        JPanel panel = new JPanel(new BorderLayout(8, 8));
        panel.setBackground(UITheme.BG);
        panel.setBorder(new EmptyBorder(10, 0, 0, 0));

        DefaultTableModel model = new DefaultTableModel(
                new Object[]{"Medicine", "Company", "Expiry Date", "Days Remaining", "Stock Qty", "Supplier"}, 0) {
            @Override public boolean isCellEditable(int r, int c) { return false; }
        };
        JTable table = new JTable(model);
        table.setRowHeight(24);

        JSpinner daysSpinner = new JSpinner(new SpinnerNumberModel(30, 1, 365, 1));

        JPanel controls = new JPanel(new FlowLayout(FlowLayout.LEFT, 6, 6));
        controls.setOpaque(false);
        controls.add(new JLabel("Show medicines expiring within (days):"));
        controls.add(daysSpinner);
        JButton runBtn = UITheme.primaryButton("Run Report");
        controls.add(runBtn);
        JLabel countLabel = new JLabel();
        countLabel.setFont(UITheme.FONT_BOLD);
        countLabel.setForeground(UITheme.WARNING);

        runBtn.addActionListener(e -> {
            int days = (Integer) daysSpinner.getValue();
            model.setRowCount(0);
            List<Medicine> list = medicineDAO.getExpiringWithinDays(days);
            LocalDate today = LocalDate.now();
            for (Medicine m : list) {
                LocalDate expiry = m.getExpiryDate().toLocalDate();
                long chronoDays = java.time.temporal.ChronoUnit.DAYS.between(today, expiry);
                model.addRow(new Object[]{m.getName(), m.getCompany(), m.getExpiryDate(),
                        chronoDays < 0 ? "EXPIRED" : chronoDays + " day(s)",
                        m.getQuantityInStock(), m.getSupplierName() == null ? "-" : m.getSupplierName()});
            }
            countLabel.setText(list.size() + " item(s) expiring within " + days + " day(s) (default view: next 1 month)");
        });

        panel.add(controls, BorderLayout.NORTH);
        panel.add(new JScrollPane(table), BorderLayout.CENTER);
        panel.add(countLabel, BorderLayout.SOUTH);

        runBtn.doClick(); // default: next 30 days (~1 month) as required
        return panel;
    }
}