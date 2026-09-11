package com.entropylab.db;

import com.entropylab.model.RequestLogEntry;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.function.Consumer;

public class RequestLogDAO {

    private final ExecutorService writeExecutor = Executors.newSingleThreadExecutor();

    public void insertAsync(RequestLogEntry entry, Consumer<RequestLogEntry> onComplete) {
        writeExecutor.submit(() -> {
            try {
                synchronized (DatabaseManager.DB_LOCK) {
                    Connection conn = DatabaseManager.getInstance().getConnection();
                    String sql = "INSERT INTO request_logs (" +
                            "timestamp, method, path, targetUrl, statusCode, " +
                            "requestHeaders, requestBody, responseHeaders, responseBody, durationMs, type) " +
                            "VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)";
                    try (PreparedStatement stmt = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
                        stmt.setLong(1, entry.getTimestamp());
                        stmt.setString(2, entry.getMethod());
                        stmt.setString(3, entry.getPath());
                        stmt.setString(4, entry.getTargetUrl());
                        stmt.setInt(5, entry.getStatusCode());
                        stmt.setString(6, entry.getRequestHeaders());
                        stmt.setString(7, entry.getRequestBody());
                        stmt.setString(8, entry.getResponseHeaders());
                        stmt.setString(9, entry.getResponseBody());
                        stmt.setLong(10, entry.getDurationMs());
                        stmt.setString(11, entry.getType());

                        stmt.executeUpdate();

                        try (ResultSet generatedKeys = stmt.getGeneratedKeys()) {
                            if (generatedKeys.next()) {
                                long generatedId = generatedKeys.getLong(1);
                                entry.setId(generatedId);
                            }
                        }
                    }
                }
                if (onComplete != null) {
                    onComplete.accept(entry);
                }
            } catch (Exception e) {
                e.printStackTrace(System.err);
            }
        });
    }

    public synchronized List<RequestLogEntry> getAll() {
        List<RequestLogEntry> entries = new ArrayList<>();
        synchronized (DatabaseManager.DB_LOCK) {
            try {
                Connection conn = DatabaseManager.getInstance().getConnection();
                String sql = "SELECT id, timestamp, method, path, targetUrl, statusCode, " +
                        "requestHeaders, requestBody, responseHeaders, responseBody, durationMs, type " +
                        "FROM request_logs ORDER BY timestamp ASC";
                try (PreparedStatement stmt = conn.prepareStatement(sql);
                     ResultSet rs = stmt.executeQuery()) {
                    while (rs.next()) {
                        entries.add(mapRow(rs));
                    }
                }
            } catch (SQLException e) {
                e.printStackTrace(System.err);
            }
        }
        return entries;
    }

    public synchronized List<RequestLogEntry> getRecent(int limit) {
        List<RequestLogEntry> entries = new ArrayList<>();
        synchronized (DatabaseManager.DB_LOCK) {
            try {
                Connection conn = DatabaseManager.getInstance().getConnection();
                String sql = "SELECT id, timestamp, method, path, targetUrl, statusCode, " +
                        "requestHeaders, requestBody, responseHeaders, responseBody, durationMs, type " +
                        "FROM request_logs ORDER BY timestamp DESC LIMIT ?";
                try (PreparedStatement stmt = conn.prepareStatement(sql)) {
                    stmt.setInt(1, limit);
                    try (ResultSet rs = stmt.executeQuery()) {
                        while (rs.next()) {
                            entries.add(mapRow(rs));
                        }
                    }
                }
            } catch (SQLException e) {
                e.printStackTrace(System.err);
            }
        }
        return entries;
    }

    public synchronized RequestLogEntry getById(long id) {
        synchronized (DatabaseManager.DB_LOCK) {
            try {
                Connection conn = DatabaseManager.getInstance().getConnection();
                String sql = "SELECT id, timestamp, method, path, targetUrl, statusCode, " +
                        "requestHeaders, requestBody, responseHeaders, responseBody, durationMs, type " +
                        "FROM request_logs WHERE id = ?";
                try (PreparedStatement stmt = conn.prepareStatement(sql)) {
                    stmt.setLong(1, id);
                    try (ResultSet rs = stmt.executeQuery()) {
                        if (rs.next()) {
                            return mapRow(rs);
                        }
                    }
                }
            } catch (SQLException e) {
                e.printStackTrace(System.err);
            }
        }
        return null;
    }

    private RequestLogEntry mapRow(ResultSet rs) throws SQLException {
        RequestLogEntry entry = new RequestLogEntry();
        entry.setId(rs.getLong("id"));
        entry.setTimestamp(rs.getLong("timestamp"));
        entry.setMethod(rs.getString("method"));
        entry.setPath(rs.getString("path"));
        entry.setTargetUrl(rs.getString("targetUrl"));
        entry.setStatusCode(rs.getInt("statusCode"));
        entry.setRequestHeaders(rs.getString("requestHeaders"));
        entry.setRequestBody(rs.getString("requestBody"));
        entry.setResponseHeaders(rs.getString("responseHeaders"));
        entry.setResponseBody(rs.getString("responseBody"));
        entry.setDurationMs(rs.getLong("durationMs"));
        entry.setType(rs.getString("type"));
        return entry;
    }
}
