package common;

import java.io.IOException;
import java.io.Reader;
import java.nio.file.Files;
import java.nio.file.Path;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.Properties;

/** Doc cau hinh va mo ket noi SQL Server dung chung cho cac module. */
public final class DBConnection implements ConnectionFactory {
    private final Properties config;

    public DBConnection(Path file) throws IOException {
        config = new Properties();
        // File cau hinh dung UTF-8; try-with-resources tu dong dong file.
        try (Reader reader = Files.newBufferedReader(file)) {
            config.load(reader);
        }
    }

    @Override
    public Connection open() throws SQLException {
        // Uu tien bien moi truong, neu khong co thi lay trong db.properties.
        String url = System.getenv().getOrDefault("HOTEL_DB_URL", config.getProperty("db.url"));
        if (url == null || url.isBlank()) {
            throw new SQLException("Thiếu db.url trong config/db.properties");
        }

        String username = System.getenv().getOrDefault("HOTEL_DB_USER", config.getProperty("db.user", ""));
        String password = System.getenv().getOrDefault("HOTEL_DB_PASSWORD", config.getProperty("db.password", ""));
        Properties connectionProperties = new Properties();
        if (!username.isBlank()) {
            connectionProperties.setProperty("user", username);
            connectionProperties.setProperty("password", password);
        }
        connectionProperties.setProperty("loginTimeout", "5");

        // Moi giao dich mo ket noi rieng; DAO chiu trach nhiem dong sau khi dung.
        Connection connection = DriverManager.getConnection(url, connectionProperties);
        try (Statement statement = connection.createStatement()) {
            // SQL Server can cac tuy chon nay khi ghi bang co index loc giay to.
            String sessionSql = "SET ANSI_NULLS ON; SET ANSI_PADDING ON; "
                    + "SET ANSI_WARNINGS ON; SET ARITHABORT ON; "
                    + "SET CONCAT_NULL_YIELDS_NULL ON; SET QUOTED_IDENTIFIER ON; "
                    + "SET NUMERIC_ROUNDABORT OFF;";
            statement.execute(sessionSql);
        } catch (SQLException exception) {
            // Neu cau hinh session bi loi, khong tra ve ket noi chua san sang.
            connection.close();
            throw exception;
        }
        return connection;
    }
}
