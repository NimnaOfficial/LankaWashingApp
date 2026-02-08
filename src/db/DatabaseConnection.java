package db;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import javax.swing.JOptionPane;

public class DatabaseConnection {

    private static DatabaseConnection instance;
    private Connection connection;

    // ✅ BACK TO LOCALHOST
    // If your port is 3306, change 3307 to 3306.
    private final String URL = "jdbc:mysql://localhost:3307/production_db";
    private final String USERNAME = "root";
    private final String PASSWORD = ""; // Default XAMPP has no password

    private DatabaseConnection() {
        try {
            Class.forName("com.mysql.cj.jdbc.Driver");
            this.connection = DriverManager.getConnection(URL, USERNAME, PASSWORD);
        } catch (ClassNotFoundException e) {
            JOptionPane.showMessageDialog(null, "Driver Not Found: " + e.getMessage());
        } catch (SQLException e) {
            JOptionPane.showMessageDialog(null, "Local DB Failed! Check XAMPP Control Panel.\n" + e.getMessage());
        }
    }

    public Connection getConnection() {
        return connection;
    }

    public static DatabaseConnection getInstance() throws SQLException {
        if (instance == null || instance.getConnection() == null || instance.getConnection().isClosed()) {
            instance = new DatabaseConnection();
        }
        return instance;
    }
}