package com.entropylab.ui;

import com.entropylab.db.RequestLogDAO;
import com.entropylab.model.RequestLogEntry;
import com.entropylab.util.LogEventBus;
import javafx.application.Platform;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.chart.BarChart;
import javafx.scene.chart.CategoryAxis;
import javafx.scene.chart.NumberAxis;
import javafx.scene.chart.XYChart;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.ScrollPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.Region;
import javafx.scene.layout.VBox;

import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CompletableFuture;

public class AnalyticsView extends ScrollPane {

    public static class AnalyticsStats {
        public int totalRequests;
        public int errorCount;
        public double errorRate;
        public double avgDuration;
        public long p50Duration;
        public long p95Duration;
        public long minDuration;
        public long maxDuration;

        public int successCount;
        public int clientErrorCount;
        public int serverErrorCount;
        public int chaosStatusCount;
        public int chaosResetCount;
        public int mockedCount;
    }

    private final RequestLogDAO logDao;

    private final Label totalRequestsLabel = new Label("0");
    private final Label errorRateLabel = new Label("0.0%");
    private final Label avgDurationLabel = new Label("0 ms");
    private final Label p50DurationLabel = new Label("0 ms");
    private final Label p95DurationLabel = new Label("0 ms");
    private final Label statusLabel = new Label("Initializing...");

    private final XYChart.Series<String, Number> series = new XYChart.Series<>();

    public AnalyticsView() {
        this(new RequestLogDAO());
    }

    public AnalyticsView(RequestLogDAO logDao) {
        this.logDao = logDao;

        setFitToWidth(true);
        setHbarPolicy(ScrollBarPolicy.NEVER);
        setVbarPolicy(ScrollBarPolicy.AS_NEEDED);

        VBox root = new VBox(16);
        root.setPadding(new Insets(16, 20, 16, 20));
        root.setMaxWidth(1000);

        // Header Row
        HBox headerRow = new HBox(12);
        headerRow.setAlignment(Pos.CENTER_LEFT);

        VBox titleBox = new VBox(4);
        Label title = new Label("Traffic & Fault Analytics");
        title.setStyle("-fx-font-size: 20px; -fx-font-weight: bold;");

        Label subtitle = new Label("Real-time aggregate performance metrics and chaos fault distribution");
        subtitle.getStyleClass().add("secondary");

        titleBox.getChildren().addAll(title, subtitle);

        Region spacer = new Region();
        HBox.setHgrow(spacer, Priority.ALWAYS);

        Button refreshButton = new Button("Refresh");
        refreshButton.setId("analyticsRefreshButton");
        refreshButton.getStyleClass().addAll("btn", "btn-secondary");
        refreshButton.setOnAction(e -> refreshAsync());

        statusLabel.getStyleClass().add("secondary");
        statusLabel.setStyle("-fx-font-size: 11px;");

        VBox actionBox = new VBox(4, refreshButton, statusLabel);
        actionBox.setAlignment(Pos.CENTER_RIGHT);

        headerRow.getChildren().addAll(titleBox, spacer, actionBox);
        root.getChildren().add(headerRow);

        // KPI Cards Row
        HBox kpiRow = new HBox(12);
        kpiRow.setAlignment(Pos.CENTER);

        VBox cardTotal = createKpiCard("TOTAL REQUESTS", totalRequestsLabel, "All proxy requests");
        VBox cardError = createKpiCard("ERROR RATE", errorRateLabel, "Status ≥ 400 or Chaos");
        VBox cardAvg = createKpiCard("AVG DURATION", avgDurationLabel, "Mean round-trip time");
        VBox cardP50 = createKpiCard("P50 LATENCY", p50DurationLabel, "50th percentile (median)");
        VBox cardP95 = createKpiCard("P95 LATENCY", p95DurationLabel, "95th percentile");

        HBox.setHgrow(cardTotal, Priority.ALWAYS);
        HBox.setHgrow(cardError, Priority.ALWAYS);
        HBox.setHgrow(cardAvg, Priority.ALWAYS);
        HBox.setHgrow(cardP50, Priority.ALWAYS);
        HBox.setHgrow(cardP95, Priority.ALWAYS);

        kpiRow.getChildren().addAll(cardTotal, cardError, cardAvg, cardP50, cardP95);
        root.getChildren().add(kpiRow);

        // Chart Card
        VBox chartCard = new VBox(12);
        chartCard.getStyleClass().add("card-pane");

        Label chartTitle = new Label("Response Status & Traffic Distribution");
        chartTitle.getStyleClass().add("card-title");

        CategoryAxis xAxis = new CategoryAxis();
        xAxis.setLabel("Traffic Category");
        NumberAxis yAxis = new NumberAxis();
        yAxis.setLabel("Request Count");
        yAxis.setMinorTickVisible(false);
        yAxis.setTickUnit(1);

        BarChart<String, Number> barChart = new BarChart<>(xAxis, yAxis);
        barChart.setTitle(null);
        barChart.setLegendVisible(false);
        barChart.setAnimated(false);
        barChart.setPrefHeight(320);

        series.setName("Requests");
        barChart.getData().add(series);

        chartCard.getChildren().addAll(chartTitle, barChart);
        root.getChildren().add(chartCard);

        setContent(root);

        // Auto-refresh when new traffic arrives
        LogEventBus.getInstance().registerListener(entry -> refreshAsync());

        // Initial Load
        refreshAsync();
    }

    private VBox createKpiCard(String titleText, Label valueLabel, String subtext) {
        VBox card = new VBox(6);
        card.getStyleClass().add("card-pane");
        card.setAlignment(Pos.TOP_LEFT);

        Label title = new Label(titleText);
        title.getStyleClass().add("secondary");
        title.setStyle("-fx-font-size: 11px; -fx-font-weight: bold;");

        valueLabel.setStyle("-fx-font-size: 20px; -fx-font-weight: bold;");

        Label sub = new Label(subtext);
        sub.getStyleClass().add("secondary");
        sub.setStyle("-fx-font-size: 10px;");

        card.getChildren().addAll(title, valueLabel, sub);
        return card;
    }

    public void refreshAsync() {
        CompletableFuture.supplyAsync(() -> {
            try {
                List<RequestLogEntry> logs = logDao.getAll();
                return computeStats(logs);
            } catch (Exception e) {
                e.printStackTrace(System.err);
                return new AnalyticsStats();
            }
        }).thenAccept(stats -> Platform.runLater(() -> applyStatsToUI(stats)));
    }

    public void applyStatsToUI(AnalyticsStats stats) {
        totalRequestsLabel.setText(String.format("%,d", stats.totalRequests));

        errorRateLabel.setText(String.format("%.1f%%", stats.errorRate));
        if (stats.errorRate > 0) {
            errorRateLabel.setStyle("-fx-font-size: 20px; -fx-font-weight: bold; -fx-text-fill: #FE0134;");
        } else {
            errorRateLabel.setStyle("-fx-font-size: 20px; -fx-font-weight: bold;");
        }

        avgDurationLabel.setText(String.format("%.1f ms", stats.avgDuration));
        p50DurationLabel.setText(stats.p50Duration + " ms");
        p95DurationLabel.setText(stats.p95Duration + " ms");

        String time = LocalTime.now().format(DateTimeFormatter.ofPattern("HH:mm:ss"));
        statusLabel.setText("Last updated: " + time);

        // Update Chart
        series.getData().clear();
        series.getData().add(new XYChart.Data<>("2xx Success", stats.successCount));
        series.getData().add(new XYChart.Data<>("4xx Client Error", stats.clientErrorCount));
        series.getData().add(new XYChart.Data<>("5xx Server Error", stats.serverErrorCount));
        series.getData().add(new XYChart.Data<>("CHAOS_STATUS", stats.chaosStatusCount));
        series.getData().add(new XYChart.Data<>("CHAOS_RESET", stats.chaosResetCount));
        series.getData().add(new XYChart.Data<>("MOCKED", stats.mockedCount));
    }

    public static AnalyticsStats computeStats(List<RequestLogEntry> logs) {
        AnalyticsStats stats = new AnalyticsStats();
        if (logs == null || logs.isEmpty()) {
            return stats;
        }
        stats.totalRequests = logs.size();
        long totalDuration = 0;
        List<Long> durations = new ArrayList<>(logs.size());
        long min = Long.MAX_VALUE;
        long max = Long.MIN_VALUE;

        for (RequestLogEntry log : logs) {
            long d = log.getDurationMs();
            durations.add(d);
            totalDuration += d;
            if (d < min) min = d;
            if (d > max) max = d;

            String type = log.getType() != null ? log.getType() : "";
            int code = log.getStatusCode();

            boolean isError = code >= 400
                    || "CHAOS_STATUS".equalsIgnoreCase(type)
                    || "CHAOS_RESET".equalsIgnoreCase(type);
            if (isError) {
                stats.errorCount++;
            }

            if ("CHAOS_STATUS".equalsIgnoreCase(type)) {
                stats.chaosStatusCount++;
            } else if ("CHAOS_RESET".equalsIgnoreCase(type)) {
                stats.chaosResetCount++;
            } else if ("MOCKED".equalsIgnoreCase(type)) {
                stats.mockedCount++;
            } else if (code >= 200 && code < 300) {
                stats.successCount++;
            } else if (code >= 400 && code < 500) {
                stats.clientErrorCount++;
            } else if (code >= 500 && code < 600) {
                stats.serverErrorCount++;
            } else {
                stats.successCount++;
            }
        }

        stats.errorRate = (stats.errorCount * 100.0) / stats.totalRequests;
        stats.avgDuration = (double) totalDuration / stats.totalRequests;
        stats.minDuration = (min == Long.MAX_VALUE) ? 0 : min;
        stats.maxDuration = (max == Long.MIN_VALUE) ? 0 : max;

        durations.sort(Long::compare);
        int p50Idx = (int) (durations.size() * 0.50);
        if (p50Idx >= durations.size()) p50Idx = durations.size() - 1;
        stats.p50Duration = durations.get(p50Idx);

        int p95Idx = (int) (durations.size() * 0.95);
        if (p95Idx >= durations.size()) p95Idx = durations.size() - 1;
        stats.p95Duration = durations.get(p95Idx);

        return stats;
    }
}
