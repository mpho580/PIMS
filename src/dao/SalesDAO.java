package dao;

import db.DBConnection;
import model.CartItem;
import model.Sale;
import java.math.BigDecimal;
import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class SalesDAO {

    private final MedicineDAO medicineDAO = new MedicineDAO();

    /**
     * Finalises a cart as a sale: inserts the sales header, one sale_items row per
     * cart line, and decrements stock — all inside a single DB transaction so a
     * failure part-way through rolls everything back.
     *
     * @return the generated sale_id, or -1 on failure (e.g. insufficient stock).
     */
    public int checkout(List<CartItem> cart, int userId, BigDecimal total) {
        Connection con = null;
        try {
            con = DBConnection.getConnection();
            con.setAutoCommit(false);

            int saleId;
            String saleSql = "INSERT INTO sales (total_amount, user_id) VALUES (?, ?)";
            try (PreparedStatement ps = con.prepareStatement(saleSql, Statement.RETURN_GENERATED_KEYS)) {
                ps.setBigDecimal(1, total);
                ps.setInt(2, userId);
                ps.executeUpdate();
                try (ResultSet keys = ps.getGeneratedKeys()) {
                    if (!keys.next()) throw new SQLException("Could not obtain sale_id");
                    saleId = keys.getInt(1);
                }
            }

            String itemSql = "INSERT INTO sale_items (sale_id, medicine_id, quantity_sold, price_at_sale) VALUES (?,?,?,?)";
            try (PreparedStatement ps = con.prepareStatement(itemSql)) {
                for (CartItem item : cart) {
                    ps.setInt(1, saleId);
                    ps.setInt(2, item.getMedicine().getMedicineId());
                    ps.setInt(3, item.getQuantity());
                    ps.setBigDecimal(4, item.getMedicine().getPrice());
                    ps.addBatch();

                    boolean ok = medicineDAO.decrementStock(con, item.getMedicine().getMedicineId(), item.getQuantity());
                    if (!ok) {
                        throw new SQLException("Insufficient stock for " + item.getMedicine().getName());
                    }
                }
                ps.executeBatch();
            }

            con.commit();
            return saleId;

        } catch (SQLException e) {
            e.printStackTrace();
            if (con != null) {
                try { con.rollback(); } catch (SQLException ex) { ex.printStackTrace(); }
            }
            return -1;
        } finally {
            if (con != null) {
                try { con.setAutoCommit(true); con.close(); } catch (SQLException ex) { ex.printStackTrace(); }
            }
        }
    }

    /** Sales report: one row per transaction between two dates (inclusive). */
    public List<Sale> getSalesBetween(java.sql.Date from, java.sql.Date to) {
        List<Sale> list = new ArrayList<>();
        String sql = "SELECT sa.*, u.full_name AS cashier_name FROM sales sa " +
                "JOIN users u ON sa.user_id = u.user_id " +
                "WHERE DATE(sa.sale_date) BETWEEN ? AND ? ORDER BY sa.sale_date DESC";
        try (Connection con = DBConnection.getConnection();
             PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setDate(1, from);
            ps.setDate(2, to);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    Sale s = new Sale();
                    s.setSaleId(rs.getInt("sale_id"));
                    s.setSaleDate(rs.getTimestamp("sale_date"));
                    s.setTotalAmount(rs.getBigDecimal("total_amount"));
                    s.setUserId(rs.getInt("user_id"));
                    s.setCashierName(rs.getString("cashier_name"));
                    list.add(s);
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return list;
    }

    /** Item-wise sales report: total quantity & revenue per medicine, between two dates. */
    public List<Object[]> getItemWiseSales(java.sql.Date from, java.sql.Date to) {
        List<Object[]> rows = new ArrayList<>();
        String sql = "SELECT m.name, SUM(si.quantity_sold) AS total_qty, " +
                "SUM(si.quantity_sold * si.price_at_sale) AS total_revenue " +
                "FROM sale_items si " +
                "JOIN sales sa ON si.sale_id = sa.sale_id " +
                "JOIN medicines m ON si.medicine_id = m.medicine_id " +
                "WHERE DATE(sa.sale_date) BETWEEN ? AND ? " +
                "GROUP BY m.medicine_id, m.name ORDER BY total_revenue DESC";
        try (Connection con = DBConnection.getConnection();
             PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setDate(1, from);
            ps.setDate(2, to);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    rows.add(new Object[]{
                            rs.getString("name"),
                            rs.getInt("total_qty"),
                            rs.getBigDecimal("total_revenue")
                    });
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return rows;
    }

    /** Line items belonging to one sale, used to reprint/rebuild a bill. */
    public List<Object[]> getSaleItems(int saleId) {
        List<Object[]> rows = new ArrayList<>();
        String sql = "SELECT m.name, si.quantity_sold, si.price_at_sale " +
                "FROM sale_items si JOIN medicines m ON si.medicine_id = m.medicine_id " +
                "WHERE si.sale_id = ?";
        try (Connection con = DBConnection.getConnection();
             PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setInt(1, saleId);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    rows.add(new Object[]{
                            rs.getString("name"),
                            rs.getInt("quantity_sold"),
                            rs.getBigDecimal("price_at_sale")
                    });
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return rows;
    }

    public BigDecimal getTotalRevenue(java.sql.Date from, java.sql.Date to) {
        String sql = "SELECT COALESCE(SUM(total_amount),0) AS total FROM sales WHERE DATE(sale_date) BETWEEN ? AND ?";
        try (Connection con = DBConnection.getConnection();
             PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setDate(1, from);
            ps.setDate(2, to);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) return rs.getBigDecimal("total");
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return BigDecimal.ZERO;
    }
}