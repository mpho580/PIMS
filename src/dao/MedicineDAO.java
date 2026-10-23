package dao;

import db.DBConnection;
import model.Medicine;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class MedicineDAO {

    private static final String SELECT_BASE =
            "SELECT m.*, s.supplier_name FROM medicines m " +
            "LEFT JOIN suppliers s ON m.supplier_id = s.supplier_id ";

    public List<Medicine> getAllMedicines() {
        List<Medicine> list = new ArrayList<>();
        String sql = SELECT_BASE + "ORDER BY m.name";
        try (Connection con = DBConnection.getConnection();
             Statement st = con.createStatement();
             ResultSet rs = st.executeQuery(sql)) {
            while (rs.next()) list.add(mapRow(rs));
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return list;
    }

    /** Search by name/company, used by both Manage Medicines and the POS screen. */
    public List<Medicine> search(String keyword) {
        List<Medicine> list = new ArrayList<>();
        String sql = SELECT_BASE + "WHERE m.name LIKE ? OR m.company LIKE ? ORDER BY m.name";
        try (Connection con = DBConnection.getConnection();
             PreparedStatement ps = con.prepareStatement(sql)) {
            String like = "%" + keyword + "%";
            ps.setString(1, like);
            ps.setString(2, like);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) list.add(mapRow(rs));
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return list;
    }

    public List<Medicine> getLowStock() {
        List<Medicine> list = new ArrayList<>();
        String sql = SELECT_BASE + "WHERE m.quantity_in_stock <= m.reorder_level ORDER BY m.quantity_in_stock";
        try (Connection con = DBConnection.getConnection();
             Statement st = con.createStatement();
             ResultSet rs = st.executeQuery(sql)) {
            while (rs.next()) list.add(mapRow(rs));
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return list;
    }

    /** Medicines expiring within the given number of days (inclusive), soonest first. */
    public List<Medicine> getExpiringWithinDays(int days) {
        List<Medicine> list = new ArrayList<>();
        String sql = SELECT_BASE +
                "WHERE m.expiry_date IS NOT NULL AND m.expiry_date <= DATE_ADD(CURDATE(), INTERVAL ? DAY) " +
                "ORDER BY m.expiry_date";
        try (Connection con = DBConnection.getConnection();
             PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setInt(1, days);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) list.add(mapRow(rs));
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return list;
    }

    public boolean addMedicine(Medicine m) {
        String sql = "INSERT INTO medicines (name, company, medicine_type, price, quantity_in_stock, " +
                "reorder_level, expiry_date, supplier_id) VALUES (?,?,?,?,?,?,?,?)";
        try (Connection con = DBConnection.getConnection();
             PreparedStatement ps = con.prepareStatement(sql)) {
            bindMedicine(ps, m);
            return ps.executeUpdate() > 0;
        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
    }

    public boolean updateMedicine(Medicine m) {
        String sql = "UPDATE medicines SET name=?, company=?, medicine_type=?, price=?, quantity_in_stock=?, " +
                "reorder_level=?, expiry_date=?, supplier_id=? WHERE medicine_id=?";
        try (Connection con = DBConnection.getConnection();
             PreparedStatement ps = con.prepareStatement(sql)) {
            bindMedicine(ps, m);
            ps.setInt(9, m.getMedicineId());
            return ps.executeUpdate() > 0;
        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
    }

    public boolean deleteMedicine(int medicineId) {
        String sql = "DELETE FROM medicines WHERE medicine_id=?";
        try (Connection con = DBConnection.getConnection();
             PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setInt(1, medicineId);
            return ps.executeUpdate() > 0;
        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
    }

    /** Reduces stock after a sale. Uses the given connection so it can share a transaction. */
    public boolean decrementStock(Connection con, int medicineId, int qty) throws SQLException {
        String sql = "UPDATE medicines SET quantity_in_stock = quantity_in_stock - ? " +
                "WHERE medicine_id = ? AND quantity_in_stock >= ?";
        try (PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setInt(1, qty);
            ps.setInt(2, medicineId);
            ps.setInt(3, qty);
            return ps.executeUpdate() > 0;
        }
    }

    private void bindMedicine(PreparedStatement ps, Medicine m) throws SQLException {
        ps.setString(1, m.getName());
        ps.setString(2, m.getCompany());
        ps.setString(3, m.getMedicineType());
        ps.setBigDecimal(4, m.getPrice());
        ps.setInt(5, m.getQuantityInStock());
        ps.setInt(6, m.getReorderLevel());
        ps.setDate(7, (Date) m.getExpiryDate());
        if (m.getSupplierId() > 0) {
            ps.setInt(8, m.getSupplierId());
        } else {
            ps.setNull(8, Types.INTEGER);
        }
    }

    private Medicine mapRow(ResultSet rs) throws SQLException {
        Medicine m = new Medicine();
        m.setMedicineId(rs.getInt("medicine_id"));
        m.setName(rs.getString("name"));
        m.setCompany(rs.getString("company"));
        m.setMedicineType(rs.getString("medicine_type"));
        m.setPrice(rs.getBigDecimal("price"));
        m.setQuantityInStock(rs.getInt("quantity_in_stock"));
        m.setReorderLevel(rs.getInt("reorder_level"));
        m.setExpiryDate(rs.getDate("expiry_date"));
        m.setSupplierId(rs.getInt("supplier_id"));
        m.setSupplierName(rs.getString("supplier_name"));
        return m;
    }
}