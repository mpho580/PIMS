package ui.cashier;

import model.CartItem;
import ui.UITheme;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;
import java.io.FileWriter;
import java.io.IOException;
import java.math.BigDecimal;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;

/** Shows a printable/saveable bill immediately after a successful checkout. */
public class BillWindow extends JFrame {

    private final int saleId;
    private final List<CartItem> items;
    private final BigDecimal total;
    private final String cashierName;
    private final JTextArea billArea = new JTextArea();

    public BillWindow(int saleId, List<CartItem> items, BigDecimal total, String cashierName) {
        this.saleId = saleId;
        this.items = items;
        this.total = total;
        this.cashierName = cashierName;

        setTitle("Bill / Receipt - Sale #" + saleId);
        setSize(450, 600);
        setLocationRelativeTo(null);
        setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
        setLayout(new BorderLayout());
        getContentPane().setBackground(UITheme.BG);

        billArea.setFont(UITheme.FONT_MONO);
        billArea.setEditable(false);
        billArea.setText(buildBillText());
        billArea.setMargin(new Insets(12, 12, 12, 12));

        JScrollPane scroll = new JScrollPane(billArea);
        scroll.setBorder(new EmptyBorder(12, 12, 0, 12));
        add(scroll, BorderLayout.CENTER);

        JPanel buttonBar = new JPanel(new FlowLayout(FlowLayout.CENTER, 10, 12));
        buttonBar.setOpaque(false);
        JButton printBtn = UITheme.primaryButton("Print");
        JButton saveBtn = UITheme.flatButton("Save As Text File");
        JButton closeBtn = UITheme.flatButton("Close");

        printBtn.addActionListener(e -> printBill());
        saveBtn.addActionListener(e -> saveBillToFile());
        closeBtn.addActionListener(e -> dispose());

        buttonBar.add(printBtn);
        buttonBar.add(saveBtn);
        buttonBar.add(closeBtn);
        add(buttonBar, BorderLayout.SOUTH);
    }

    private String buildBillText() {
        StringBuilder sb = new StringBuilder();
        String sep = "----------------------------------------\n";

        sb.append("        HealthFirst Pharmacy\n");
        sb.append("      Official Sales Receipt\n");
        sb.append(sep);
        sb.append("Sale ID   : ").append(saleId).append("\n");
        sb.append("Date/Time : ").append(new SimpleDateFormat("yyyy-MM-dd HH:mm:ss").format(new Date())).append("\n");
        sb.append("Cashier   : ").append(cashierName).append("\n");
        sb.append(sep);
        sb.append(String.format("%-18s %4s %8s%n", "Item", "Qty", "Total"));
        sb.append(sep);

        for (CartItem item : items) {
            String name = item.getMedicine().getName();
            if (name.length() > 18) name = name.substring(0, 18);
            sb.append(String.format("%-18s %4d %8.2f%n", name, item.getQuantity(), item.getLineTotal()));
        }

        sb.append(sep);
        sb.append(String.format("%-23s R%7.2f%n", "TOTAL:", total));
        sb.append(sep);
        sb.append("     Thank you for shopping with us!\n");
        sb.append("      Get well soon. Come again.\n");
        return sb.toString();
    }

    private void printBill() {
        try {
            boolean printed = billArea.print();
            if (!printed) {
                JOptionPane.showMessageDialog(this, "Print was cancelled.", "Print", JOptionPane.INFORMATION_MESSAGE);
            }
        } catch (Exception ex) {
            JOptionPane.showMessageDialog(this, "No printer available in this environment.\n"
                    + "Use 'Save As Text File' instead to keep a copy of the bill.\n\n" + ex.getMessage(),
                    "Printing Unavailable", JOptionPane.WARNING_MESSAGE);
        }
    }

    private void saveBillToFile() {
        JFileChooser chooser = new JFileChooser();
        chooser.setSelectedFile(new java.io.File("bill_" + saleId + ".txt"));
        int result = chooser.showSaveDialog(this);
        if (result != JFileChooser.APPROVE_OPTION) return;

        try (FileWriter writer = new FileWriter(chooser.getSelectedFile())) {
            writer.write(billArea.getText());
            JOptionPane.showMessageDialog(this, "Bill saved successfully.");
        } catch (IOException ex) {
            JOptionPane.showMessageDialog(this, "Could not save file: " + ex.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
        }
    }
}