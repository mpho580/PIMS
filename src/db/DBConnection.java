package db;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

/**
 * Central place that hands out JDBC connections to the pims_db database.
 * Edit the constants below if your MySQL username/password/port differ.
 */
public class DBConnection {

    private static final String HOST = "localhost";
    private static final int    PORT = 3306;
    private static final String DB_NAME = "pims_db";

    private static final String URL =
            "jdbc:mysql://" + HOST + ":" + PORT + "/" + DB_NAME
            + "?useSSL=false&serverTimezone=UTC&allowPublicKeyRetrieval=true";

    private static final String USER = "root";
    private static final String PASSWORD = ""; 

    /** Returns a fresh JDBC connection. Caller is responsible for closing it. */
    public static Connection getConnection() throws SQLException {
        try {
            Class.forName("com.mysql.cj.jdbc.Driver");
        } catch (ClassNotFoundException e) {
            throw new SQLException("MySQL JDBC driver not found on classpath.", e);
        }
        return DriverManager.getConnection(URL, USER, PASSWORD);
    }

    /** Quick connectivity check, used on application start-up. */
    public static boolean testConnection() {
        try (Connection c = getConnection()) {
            return c != null && !c.isClosed();
        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
    }
}