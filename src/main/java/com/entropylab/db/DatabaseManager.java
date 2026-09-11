package com.entropylab.db;

import com.entropylab.config.AppPaths;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.sql.Statement;

public class DatabaseManager {

    public static final Object DB_LOCK = new Object();

    private static final DatabaseManager INSTANCE = new DatabaseManager();

    private Connection connection;

    private DatabaseManager() {
    }

    public static DatabaseManager getInstance() {
        return INSTANCE;
    }

    // Callers must synchronize on DatabaseManager.DB_LOCK before calling this
    public Connection getConnection() throws SQLException {
        if (connection == null || connection.isClosed()) {
            try {
                Class.forName("org.sqlite.JDBC");
            } catch (ClassNotFoundException e) {
                throw new SQLException("SQLite JDBC driver class not found", e);
            }
            connection = DriverManager.getConnection("jdbc:sqlite:" + AppPaths.getDatabaseFilePath());
            try (Statement stmt = connection.createStatement()) {
                stmt.execute("PRAGMA journal_mode=WAL;");
            }
            initSchema();
        }
        return connection;
    }

    private void initSchema() throws SQLException {
        String sql = "CREATE TABLE IF NOT EXISTS request_logs (\n" +
                "  id INTEGER PRIMARY KEY AUTOINCREMENT,\n" +
                "  timestamp INTEGER NOT NULL,\n" +
                "  method TEXT, path TEXT, targetUrl TEXT, statusCode INTEGER,\n" +
                "  requestHeaders TEXT, requestBody TEXT, responseHeaders TEXT,\n" +
                "  responseBody TEXT, durationMs INTEGER, type TEXT\n" +
                ");";
        try (Statement stmt = connection.createStatement()) {
            stmt.execute(sql);
        }
    }
}
