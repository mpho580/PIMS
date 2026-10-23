package dao;

import java.sql.Connection;
import java.sql.Statement;
import java.sql.ResultSet;
import java.sql.SQLException;
import db.DBConnection;
import java.util.ArrayList;
import java.util.List;
import model.Supplier;
import java.sql.*;

public class SupplierDAO {
 
    public List<Supplier> getAllSuppliers() {
        List<Supplier> list = new ArrayList<>();
        String sql = "SELECT * FROM suppliers ORDER BY supplier_name";
        try (Connection con = DBConnection.getConnection();
             Statement st = con.createStatement();
             ResultSet rs = st.executeQuery(sql)) {
            while (rs.next()) list.add(mapRow(rs));
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return list;
    }
 
    public boolean addSupplier(Supplier s) {
        String sql = "INSERT INTO suppliers (supplier_name, contact_person, phone, email, address) VALUES (?,?,?,?,?)";
        try (Connection con = DBConnection.getConnection();
             PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setString(1, s.getSupplierName());
            ps.setString(2, s.getContactPerson());
            ps.setString(3, s.getPhone());
            ps.setString(4, s.getEmail());
            ps.setString(5, s.getAddress());
            return ps.executeUpdate() > 0;
        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
    }
 
    public boolean updateSupplier(Supplier s) {
        String sql = "UPDATE suppliers SET supplier_name=?, contact_person=?, phone=?, email=?, address=? WHERE supplier_id=?";
        try (Connection con = DBConnection.getConnection();
             PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setString(1, s.getSupplierName());
            ps.setString(2, s.getContactPerson());
            ps.setString(3, s.getPhone());
            ps.setString(4, s.getEmail());
            ps.setString(5, s.getAddress());
            ps.setInt(6, s.getSupplierId());
            return ps.executeUpdate() > 0;
        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
    }
 
    public boolean deleteSupplier(int supplierId) {
        String sql = "DELETE FROM suppliers WHERE supplier_id=?";
        try (Connection con = DBConnection.getConnection();
             PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setInt(1, supplierId);
            return ps.executeUpdate() > 0;
        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
    }
 
    private Supplier mapRow(ResultSet rs) throws SQLException {
        return new Supplier(
                rs.getInt("supplier_id"),
                rs.getString("supplier_name"),
                rs.getString("contact_person"),
                rs.getString("phone"),
                rs.getString("email"),
                rs.getString("address")
        );
    }
}
