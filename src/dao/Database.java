package dao;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

public class Database {
    private static final String URL =
        envOr("DB_URL", "jdbc:postgresql://localhost:5432/medmandb");
    private static final String USER =
        envOr("DB_USER", "postgres");
    private static final String PASSWORD =
        envOr("DB_PASSWORD", "1234");

    public static Connection getConnection() throws SQLException {
        return DriverManager.getConnection(URL, USER, PASSWORD);
    }

    private static String envOr(String key, String fallback) {
        String v = System.getenv(key);
        return (v == null || v.isBlank()) ? fallback : v;
    }
}
