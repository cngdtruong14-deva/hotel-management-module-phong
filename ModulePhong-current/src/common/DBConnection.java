package common;
import java.nio.file.*;
import java.io.*;
import java.sql.*;
import java.util.Properties;

public final class DBConnection implements ConnectionFactory {
    private final Properties config;
    public DBConnection(Path file) throws IOException {
        config = new Properties(); try (Reader r = Files.newBufferedReader(file)) { config.load(r); }
    }
    public Connection open() throws SQLException {
        String url = System.getenv().getOrDefault("HOTEL_DB_URL", config.getProperty("db.url"));
        if (url == null || url.isBlank()) throw new SQLException("Thiếu db.url trong config/db.properties");
        Properties p = new Properties();
        String user = System.getenv().getOrDefault("HOTEL_DB_USER", config.getProperty("db.user", ""));
        String pass = System.getenv().getOrDefault("HOTEL_DB_PASSWORD", config.getProperty("db.password", ""));
        if (!user.isBlank()) { p.setProperty("user", user); p.setProperty("password", pass); }
        p.setProperty("loginTimeout", "5");
        Connection connection = DriverManager.getConnection(url, p);
        try (Statement st = connection.createStatement()) {
            st.execute("SET ANSI_NULLS ON; SET ANSI_PADDING ON; SET ANSI_WARNINGS ON; SET ARITHABORT ON; SET CONCAT_NULL_YIELDS_NULL ON; SET QUOTED_IDENTIFIER ON; SET NUMERIC_ROUNDABORT OFF;");
        } catch (SQLException e) { connection.close(); throw e; }
        return connection;
    }
}
