package Utils;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.sql.Statement;

/**
 * Utility class for managing database connections.
 */
public class DBConnection {

    // Hardcoded path to the database file.
    // "jdbc:sqlite:storedatabase.db" means the file is located next to the executable (.jar) or project root.
    private static final String DB_URL = "jdbc:sqlite:data/storedatabase.db";

    /**
     * Creates a connection to the SQLite Database and enables foreign key support.
     *
     * @return A Connection object to the database.
     * @throws ClassNotFoundException If the SQLite JDBC driver is not found.
     * @throws SQLException           If a database access error occurs.
     */
    public static Connection getConnection() throws ClassNotFoundException, SQLException {
        // 1. Load SQLite Driver
        Class.forName("org.sqlite.JDBC");

        // 2. Create direct connection
        // If the file does not exist, SQLite will AUTOMATICALLY CREATE a new empty file.
        Connection conn = DriverManager.getConnection(DB_URL);

        // 3. Enable Foreign Key support for each connection
        // This is a crucial step to ensure data integrity in SQLite
        if (conn != null) {
            try (Statement stmt = conn.createStatement()) {
                stmt.execute("PRAGMA foreign_keys = ON;");
            }
        }

        return conn;
    }
}
