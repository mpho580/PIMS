package ui.cashier;

import dao.MedicineDAO;
import dao.SalesDAO;
import model.CartItem;
import model.Medicine;
import model.User;
import ui.LoginFrame;
import ui.UITheme;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

public class CashierDashboard extends JFrame {

    private final User currentUser;
    private final MedicineDAO medicineDAO = new MedicineDAO();
    private final SalesDAO salesDAO = new SalesDAO();

    // Left side: medicine catalogue / search
    private final JTextField searchField = new JTextField(18);
    private final DefaultTableModel catalogueModel = new DefaultTableModel(
            new Object[]{"ID", "Name", "Type", "Price (R)", "In Stock"}, 0) {
        @Override public boolean isCellEditable(int r, int c) { return false; }
    };
    private final JTable catalogueTable = new JTable(catalogueModel);
    private List<Medicine> lastSearchResults = new ArrayList<>();

    // Right side: cart
    private final DefaultTableModel cartModel = new DefaultTableModel(
            new Object[]{"Medicine", "Unit Price (R)", "Qty", "Line Total (R)"}, 0) {
        @Override public boolean isCellEditable(int r, int c) { return false; }
    };
    private final JTable cartTable = new JTable(cartModel);
    private final List<CartItem> cart = new ArrayList<>();
    private final JLabel totalLabel = new JLabel("Total: R 0.00");

    public CashierDashboard(User currentUser) {
        this.currentUser = currentUser;

        setTitle("HealthFirst Pharmacy - Point of Sale");
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setSize(1150, 700);
        setLocationRelativeTo(null);
        setLayout(new BorderLayout());
        getContentPane().setBackground(UITheme.BG);

        add(buildTopBar(), BorderLayout.NORTH);

        JSplitPane split = new JSplitPane(JSplitPane.HORIZONTAL_SPLIT, buildCataloguePanel(), buildCartPanel());
        split.setResizeWeight(0.55);
        split.setDividerLocation(600);
        add(split, BorderLayout.CENTER);

        loadCatalogue(medicineDAO.getAllMedicines());
    }

    private JPanel buildTopBar() {
        JPanel bar = new JPanel(new BorderLayout());
        bar.setBackground(UITheme.PRIMARY);
        bar.setBorder(new EmptyBorder(14, 20, 14, 20));

        JLabel title = new JLabel("HealthFirst Pharmacy \u2014 Point of Sale");
        title.setFont(UITheme.FONT_TITLE);
        title.setForeground(Color.WHITE);
        bar.add(title, BorderLayout.WEST);

        JPanel right = new JPanel(new FlowLayout(FlowLayout.RIGHT, 10, 0));
        right.setOpaque(false);
        JLabel who = new JLabel("Cashier: " + currentUser.getFullName());
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

    private JPanel buildCataloguePanel() {
        JPanel panel = new JPanel(new BorderLayout(8, 8));
        panel.setBorder(new EmptyBorder(16, 16, 8, 8));
        panel.setBackground(UITheme.BG);

        JPanel top = new JPanel(new BorderLayout(6, 6));
        top.setOpaque(false);
        top.add(UITheme.heading("Medicine Catalogue"), BorderLayout.NORTH);

        JPanel searchBar = new JPanel(new FlowLayout(FlowLayout.LEFT, 6, 6));
        searchBar.setOpaque(false);
        searchBar.add(new JLabel("Search:"));
        searchBar.add(searchField);
        JButton searchBtn = UITheme.flatButton("Search");
        searchBtn.addActionListener(e -> {
            String kw = searchField.getText().trim();
            loadCatalogue(kw.isEmpty() ? medicineDAO.getAllMedicines() : medicineDAO.search(kw));
        });
        searchField.addActionListener(e -> searchBtn.doClick());
        JButton showAllBtn = UITheme.flatButton("Show All");
        showAllBtn.addActionListener(e -> { searchField.setText(""); loadCatalogue(medicineDAO.getAllMedicines()); });
        searchBar.add(searchBtn);
        searchBar.add(showAllBtn);
        top.add(searchBar, BorderLayout.SOUTH);

        panel.add(top, BorderLayout.NORTH);

        catalogueTable.setRowHeight(26);
        catalogueTable.setFont(UITheme.FONT_LABEL);
        catalogueTable.getTableHeader().setFont(UITheme.FONT_BOLD);
        catalogueTable.setSelectionBackground(new Color(0xD9F0E6));
        panel.add(new JScrollPane(catalogueTable), BorderLayout.CENTER);

        JPanel addBar = new JPanel(new FlowLayout(FlowLayout.LEFT, 6, 6));
        addBar.setOpaque(false);
        addBar.add(new JLabel("Quantity:"));
        JSpinner qtySpinner = new JSpinner(new SpinnerNumberModel(1, 1, 999, 1));
        addBar.add(qtySpinner);
        JButton addToCartBtn = UITheme.primaryButton("Add to Cart");
        addToCartBtn.addActionListener(e -> addSelectedToCart((Integer) qtySpinner.getValue()));
        addBar.add(addToCartBtn);
        panel.add(addBar, BorderLayout.SOUTH);

        return panel;
    }

    private JPanel buildCartPanel() {
        JPanel panel = new JPanel(new BorderLayout(8, 8));
        panel.setBorder(new EmptyBorder(16, 8, 8, 16));
        panel.setBackground(UITheme.BG);

        panel.add(UITheme.heading("Current Sale (Cart)"), BorderLayout.NORTH);

        cartTable.setRowHeight(26);
        cartTable.setFont(UITheme.FONT_LABEL);
        cartTable.getTableHeader().setFont(UITheme.FONT_BOLD);
        cartTable.setSelectionBackground(new Color(0xFCE4D6));
        panel.add(new JScrollPane(cartTable), BorderLayout.CENTER);

        JPanel bottom = new JPanel();
        bottom.setLayout(new BoxLayout(bottom, BoxLayout.Y_AXIS));
        bottom.setOpaque(false);

        totalLabel.setFont(new Font("Segoe UI", Font.BOLD, 20));
        totalLabel.setForeground(UITheme.PRIMARY_DARK);
        totalLabel.setAlignmentX(Component.LEFT_ALIGNMENT);
        bottom.add(totalLabel);
        bottom.add(Box.createVerticalStrut(8));

        JPanel buttons = new JPanel(new FlowLayout(FlowLayout.LEFT, 8, 4));
        buttons.setOpaque(false);
        JButton removeBtn = UITheme.dangerButton("Remove Selected");
        JButton clearBtn = UITheme.flatButton("Clear Cart");
        JButton checkoutBtn = UITheme.primaryButton("Checkout");
        checkoutBtn.setFont(new Font("Segoe UI", Font.BOLD, 15));

        removeBtn.addActionListener(e -> removeSelectedFromCart());
        clearBtn.addActionListener(e -> clearCart());
        checkoutBtn.addActionListener(e -> checkout());

        buttons.add(removeBtn);
        buttons.add(clearBtn);
        buttons.add(checkoutBtn);
        buttons.setAlignmentX(Component.LEFT_ALIGNMENT);
        bottom.add(buttons);

        panel.add(bottom, BorderLayout.SOUTH);
        return panel;
    }

    private void loadCatalogue(List<Medicine> medicines) {
        lastSearchResults = medicines;
        catalogueModel.setRowCount(0);
        for (Medicine m : medicines) {
            catalogueModel.addRow(new Object[]{m.getMedicineId(), m.getName(), m.getMedicineType(),
                    m.getPrice(), m.getQuantityInStock()});
        }
    }

    private void addSelectedToCart(int qty) {
        int row = catalogueTable.getSelectedRow();
        if (row < 0) {
            JOptionPane.showMessageDialog(this, "Select a medicine from the catalogue first.", "No Selection", JOptionPane.WARNING_MESSAGE);
            return;
        }
        Medicine selected = lastSearchResults.get(row);

        if (selected.getQuantityInStock() <= 0) {
            JOptionPane.showMessageDialog(this, selected.getName() + " is out of stock.", "Out of Stock", JOptionPane.WARNING_MESSAGE);
            return;
        }

        // If already in cart, just increase quantity (capped to available stock).
        for (CartItem item : cart) {
            if (item.getMedicine().getMedicineId() == selected.getMedicineId()) {
                int newQty = item.getQuantity() + qty;
                if (newQty > selected.getQuantityInStock()) {
                    JOptionPane.showMessageDialog(this, "Only " + selected.getQuantityInStock() + " unit(s) of "
                            + selected.getName() + " are in stock.", "Insufficient Stock", JOptionPane.WARNING_MESSAGE);
                    return;
                }
                item.setQuantity(newQty);
                refreshCartTable();
                return;
            }
        }

        if (qty > selected.getQuantityInStock()) {
            JOptionPane.showMessageDialog(this, "Only " + selected.getQuantityInStock() + " unit(s) of "
                    + selected.getName() + " are in stock.", "Insufficient Stock", JOptionPane.WARNING_MESSAGE);
            return;
        }

        cart.add(new CartItem(selected, qty));
        refreshCartTable();
    }

    private void removeSelectedFromCart() {
        int row = cartTable.getSelectedRow();
        if (row < 0) {
            JOptionPane.showMessageDialog(this, "Select an item in the cart first.", "No Selection", JOptionPane.WARNING_MESSAGE);
            return;
        }
        cart.remove(row);
        refreshCartTable();
    }

    private void clearCart() {
        cart.clear();
        refreshCartTable();
    }

    private void refreshCartTable() {
        cartModel.setRowCount(0);
        BigDecimal total = BigDecimal.ZERO;
        for (CartItem item : cart) {
            cartModel.addRow(new Object[]{item.getMedicine().getName(), item.getMedicine().getPrice(),
                    item.getQuantity(), item.getLineTotal()});
            total = total.add(item.getLineTotal());
        }
        totalLabel.setText("Total: R " + total.setScale(2, BigDecimal.ROUND_HALF_UP));
    }

    private void checkout() {
        if (cart.isEmpty()) {
            JOptionPane.showMessageDialog(this, "The cart is empty. Add at least one item before checking out.", "Empty Cart", JOptionPane.WARNING_MESSAGE);
            return;
        }

        BigDecimal total = BigDecimal.ZERO;
        for (CartItem item : cart) total = total.add(item.getLineTotal());

        int confirm = JOptionPane.showConfirmDialog(this,
                "Confirm sale of " + cart.size() + " item(s) for a total of R " + total.setScale(2, BigDecimal.ROUND_HALF_UP) + "?",
                "Confirm Checkout", JOptionPane.YES_NO_OPTION);
        if (confirm != JOptionPane.YES_OPTION) return;

        int saleId = salesDAO.checkout(cart, currentUser.getUserId(), total);
        if (saleId == -1) {
            JOptionPane.showMessageDialog(this, "Checkout failed \u2014 one or more items may no longer have enough stock. "
                    + "Please review the cart and try again.", "Checkout Failed", JOptionPane.ERROR_MESSAGE);
            loadCatalogue(medicineDAO.getAllMedicines()); // refresh stock numbers
            return;
        }

        new BillWindow(saleId, new ArrayList<>(cart), total, currentUser.getFullName()).setVisible(true);

        clearCart();
        loadCatalogue(medicineDAO.getAllMedicines()); // reflect reduced stock
    }
}