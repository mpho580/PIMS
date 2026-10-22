import db.DBConnection;
import java.sql.Connection;

public class TestDBConnection {

    public static void main(String[] args) {
        try (Connection conn = DBConnection.getConnection()) {

            if (conn != null && !conn.isClosed()) {
                System.out.println("CONNECTED TO MYSQL!");
                System.out.println("Database: "
                        + conn.getCatalog());
            }

        } catch (Exception e) {
            System.out.println("CONNECTION FAILED!");
            System.out.println(e.getMessage());
        }
    }
}