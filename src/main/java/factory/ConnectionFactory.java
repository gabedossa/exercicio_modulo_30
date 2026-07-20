package factory;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

public class ConnectionFactory {

    private static final String URL = getConfig("DB_URL", "jdbc:postgresql://localhost:15432/postgres");
    private static final String USER = getConfig("DB_USER", "postgres");
    private static final String PASSWORD = getConfig("DB_PASSWORD", "Adm1234");

    private ConnectionFactory() {
    }

    public static Connection getConnection() {
        try {
            return DriverManager.getConnection(URL, USER, PASSWORD);
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }

    private static String getConfig(String key, String defaultValue) {
        String value = System.getenv(key);
        return (value != null && !value.isBlank()) ? value : defaultValue;
    }
}
