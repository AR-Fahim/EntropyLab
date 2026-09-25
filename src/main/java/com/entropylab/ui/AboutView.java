package com.entropylab.ui;

import com.entropylab.config.AppPaths;
import com.entropylab.util.EnvUtil;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.Button;
import javafx.scene.control.Hyperlink;
import javafx.scene.control.Label;
import javafx.scene.control.ScrollPane;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.control.Tooltip;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.Region;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Color;
import javafx.scene.shape.Rectangle;
import javafx.scene.shape.SVGPath;

import java.awt.Desktop;
import java.io.InputStream;
import java.net.URI;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

public class AboutView extends ScrollPane {

    public AboutView() {
        setFitToWidth(true);
        setHbarPolicy(ScrollBarPolicy.NEVER);
        setVbarPolicy(ScrollBarPolicy.AS_NEEDED);

        VBox contentBox = new VBox(20);
        contentBox.setPadding(new Insets(24));
        contentBox.setMaxWidth(Double.MAX_VALUE);
        contentBox.setFillWidth(true);

        // 1. App Introduction Card
        contentBox.getChildren().add(createAppCard());

        // 2. Developer Profile Card
        contentBox.getChildren().add(createDeveloperCard());

        // 3. How to Use Card
        contentBox.getChildren().add(createHowToUseCard());

        // 4. Credits & Libraries Card
        contentBox.getChildren().add(createCreditsCard());

        setContent(contentBox);
    }

    private VBox createAppCard() {
        VBox card = new VBox(12);
        card.getStyleClass().add("card-pane");
        card.setMaxWidth(Double.MAX_VALUE);

        HBox headerRow = new HBox(16);
        headerRow.setAlignment(Pos.CENTER_LEFT);

        ImageView logoView = new ImageView();
        Image logoImage = com.entropylab.util.AppBrandUtil.loadAppLogo(64, 64);
        if (logoImage != null) {
            logoView.setImage(logoImage);
            logoView.setFitWidth(64);
            logoView.setFitHeight(64);
            logoView.setPreserveRatio(true);
            logoView.setSmooth(true);
            headerRow.getChildren().add(logoView);
        }

        VBox titleBox = new VBox(4);
        HBox titleRow = new HBox(10);
        titleRow.setAlignment(Pos.BASELINE_LEFT);

        Label appName = new Label("EntropyLab");
        appName.setStyle("-fx-font-size: 22px; -fx-font-weight: bold;");

        Label versionBadge = new Label("v1.0.0");
        versionBadge.getStyleClass().add("badge");

        titleRow.getChildren().addAll(appName, versionBadge);

        Label tagline = new Label("Local Reverse Proxy & Chaos Engineering Studio");
        tagline.setStyle("-fx-font-size: 13px; -fx-font-weight: bold; -fx-text-fill: #FE0134;");

        titleBox.getChildren().addAll(titleRow, tagline);
        headerRow.getChildren().add(titleBox);

        Label appIntro = new Label(
                "EntropyLab is a lightweight, high-performance developer desktop tool built for modern API workflows. "
                        + "It enables you to intercept HTTP traffic, inject real-world network anomalies (simulated latency, "
                        + "connection resets, and status code overrides), inspect live requests and responses with zero overhead, "
                        + "and capture mock responses directly from your endpoints for offline resilience testing."
        );
        appIntro.setWrapText(true);
        appIntro.setStyle("-fx-line-spacing: 3px;");

        HBox actionRow = new HBox(12);
        actionRow.setAlignment(Pos.CENTER_LEFT);
        actionRow.setPadding(new Insets(6, 0, 0, 0));

        Button docsBtn = new Button("View Documentation");
        docsBtn.setId("aboutDocsButton");
        docsBtn.getStyleClass().addAll("btn", "btn-primary");
        docsBtn.setTooltip(new Tooltip("Open online user manual and guides on GitHub"));

        SVGPath docIcon = new SVGPath();
        docIcon.setContent("M14 2H6c-1.1 0-1.99.9-1.99 2L4 20c0 1.1.89 2 1.99 2H18c1.1 0 2-.9 2-2V8l-6-6zm2 16H8v-2h8v2zm0-4H8v-2h8v2zm-3-5V3.5L18.5 9H13z");
        docIcon.setFill(Color.WHITE);
        docIcon.setScaleX(0.7);
        docIcon.setScaleY(0.7);
        docsBtn.setGraphic(docIcon);
        docsBtn.setOnAction(e -> openDocumentation());

        actionRow.getChildren().add(docsBtn);

        Path localDocs = findLocalDocs();
        if (localDocs != null) {
            Button localDocsBtn = new Button("Open Local Docs");
            localDocsBtn.setId("aboutLocalDocsButton");
            localDocsBtn.getStyleClass().addAll("btn", "btn-secondary");
            localDocsBtn.setTooltip(new Tooltip("Open local documentation files on this computer"));
            localDocsBtn.setOnAction(e -> openLocalPath(localDocs));
            actionRow.getChildren().add(localDocsBtn);
        }

        card.getChildren().addAll(headerRow, appIntro, actionRow);
        return card;
    }

    private VBox createDeveloperCard() {
        VBox card = new VBox(14);
        card.getStyleClass().add("card-pane");
        card.setMaxWidth(Double.MAX_VALUE);

        Label sectionTitle = new Label("About the Developer");
        sectionTitle.getStyleClass().add("card-title");

        HBox profileRow = new HBox(18);
        profileRow.setAlignment(Pos.CENTER_LEFT);

        // Developer Photo with Rounded Corners
        ImageView photoView = new ImageView();
        Image devPhoto = com.entropylab.util.AppBrandUtil.loadDeveloperPhoto(96, 96);
        if (devPhoto != null) {
            photoView.setImage(devPhoto);
            photoView.setFitWidth(96);
            photoView.setFitHeight(96);
            photoView.setPreserveRatio(true);
            photoView.setSmooth(true);

            Rectangle clip = new Rectangle(96, 96);
            clip.setArcWidth(20);
            clip.setArcHeight(20);
            photoView.setClip(clip);
            profileRow.getChildren().add(photoView);
        }

        VBox infoBox = new VBox(6);
        infoBox.setAlignment(Pos.CENTER_LEFT);

        String devName = EnvUtil.get("DEVELOPER_NAME", "Abdur Rahman");
        Label nameLabel = new Label(devName);
        nameLabel.setStyle("-fx-font-size: 18px; -fx-font-weight: bold;");

        Label headlineLabel = new Label("Computer Science & Engineering Student");
        headlineLabel.getStyleClass().add("secondary");
        headlineLabel.setStyle("-fx-font-weight: bold; -fx-font-size: 13px;");

        Label bioLabel = new Label("Building software for fun, learning, experimenting, and as a hobby.");
        bioLabel.setWrapText(true);

        // Social Media Links (Dynamic from .env)
        HBox socialRow = new HBox(12);
        socialRow.setAlignment(Pos.CENTER_LEFT);
        socialRow.setPadding(new Insets(4, 0, 0, 0));

        String githubUrl = EnvUtil.get("DEVELOPER_GITHUB", "https://github.com/");
        if (githubUrl != null && !githubUrl.isBlank()) {
            Button githubBtn = new Button("GitHub Profile");
            githubBtn.setStyle("-fx-font-size: 11px; -fx-padding: 4px 10px;");
            githubBtn.setOnAction(e -> openUrl(githubUrl));
            socialRow.getChildren().add(githubBtn);
        }

        String linkedinUrl = EnvUtil.get("DEVELOPER_LINKEDIN", "https://linkedin.com/in/");
        if (linkedinUrl != null && !linkedinUrl.isBlank()) {
            Button linkedinBtn = new Button("LinkedIn Profile");
            linkedinBtn.setStyle("-fx-font-size: 11px; -fx-padding: 4px 10px;");
            linkedinBtn.setOnAction(e -> openUrl(linkedinUrl));
            socialRow.getChildren().add(linkedinBtn);
        }

        String twitterUrl = EnvUtil.get("DEVELOPER_TWITTER", null);
        if (twitterUrl != null && !twitterUrl.isBlank()) {
            Button twitterBtn = new Button("Twitter / X");
            twitterBtn.setStyle("-fx-font-size: 11px; -fx-padding: 4px 10px;");
            twitterBtn.setOnAction(e -> openUrl(twitterUrl));
            socialRow.getChildren().add(twitterBtn);
        }

        infoBox.getChildren().addAll(nameLabel, headlineLabel, bioLabel, socialRow);
        profileRow.getChildren().add(infoBox);

        card.getChildren().addAll(sectionTitle, profileRow);
        return card;
    }

    private VBox createHowToUseCard() {
        VBox card = new VBox(12);
        card.getStyleClass().add("card-pane");
        card.setMaxWidth(Double.MAX_VALUE);

        Label title = new Label("How to Use");
        title.getStyleClass().add("card-title");

        VBox stepsBox = new VBox(10);
        stepsBox.getChildren().addAll(
                createStepRow("1", "Add a Route Mapping", "Go to the Routes tab, click \"Add Route\", and define a local path prefix (e.g. /api) and its target backend URL."),
                createStepRow("2", "Optionally Configure Chaos Rules", "In the Chaos Rules tab, customize simulated latency, connection resets, or HTTP status overrides with optional sub-path filters."),
                createStepRow("3", "Start the Proxy", "In the Proxy Control tab, choose your port (default 8080) and click \"Start Proxy\" to spin up the reverse proxy."),
                createStepRow("4", "Point Your Application", "Configure your client, frontend, or microservice to send traffic to http://localhost:<port>/<localPath>."),
                createStepRow("5", "Inspect Traffic in Real Time", "Switch to the Inspector tab to monitor live traffic, response codes, duration, headers, and request/response payloads."),
                createStepRow("6", "Save Responses as Mocks", "Select any captured entry in the Inspector table and click \"Save as Mock\" to turn live responses into static offline endpoints."),
                createStepRow("7", "Toggle Light/Dark Mode Anytime", "Use the day-night icon in the top title bar to match your preferred environment.")
        );

        HBox docsFooterRow = new HBox(12);
        docsFooterRow.setAlignment(Pos.CENTER_LEFT);
        docsFooterRow.setPadding(new Insets(6, 0, 0, 0));

        Label docsFooterLabel = new Label("Need step-by-step recipes, test patterns, or troubleshooting?");
        docsFooterLabel.getStyleClass().add("secondary");

        Button exploreDocsBtn = new Button("Explore User Manual");
        exploreDocsBtn.getStyleClass().addAll("btn", "btn-secondary");
        exploreDocsBtn.setOnAction(e -> openDocumentation());

        docsFooterRow.getChildren().addAll(docsFooterLabel, exploreDocsBtn);

        card.getChildren().addAll(title, stepsBox, docsFooterRow);
        return card;
    }

    private HBox createStepRow(String number, String heading, String description) {
        HBox row = new HBox(12);
        row.setAlignment(Pos.TOP_LEFT);

        Label numLabel = new Label(number);
        numLabel.setStyle(
                "-fx-background-color: #FE0134; -fx-text-fill: white; -fx-font-weight: bold; "
                        + "-fx-min-width: 24px; -fx-min-height: 24px; -fx-alignment: center; -fx-background-radius: 12px; -fx-font-size: 11px;"
        );

        VBox textBox = new VBox(2);
        Label head = new Label(heading);
        head.setStyle("-fx-font-weight: bold; -fx-font-size: 13px;");

        Label desc = new Label(description);
        desc.getStyleClass().add("secondary");
        desc.setWrapText(true);

        textBox.getChildren().addAll(head, desc);
        HBox.setHgrow(textBox, Priority.ALWAYS);

        row.getChildren().addAll(numLabel, textBox);
        return row;
    }

    private VBox createCreditsCard() {
        VBox card = new VBox(12);
        card.getStyleClass().add("card-pane");
        card.setMaxWidth(Double.MAX_VALUE);

        Label title = new Label("Credits & Open-Source Libraries");
        title.getStyleClass().add("card-title");

        VBox libBox = new VBox(8);
        libBox.getChildren().addAll(
                createLibRow("OpenJFX (JavaFX 21)", "High-performance cross-platform desktop UI toolkit and graphics engine."),
                createLibRow("FasterXML Jackson", "JSON serialization, deserialization, and schema object-mapping utilities."),
                createLibRow("Xerial SQLite JDBC", "Embedded relational database engine for durable, zero-latency traffic logging (WAL mode)."),
                createLibRow("JDK Sun HTTP Server & HttpClient", "Native high-concurrency embedded reverse proxy server and asynchronous HTTP client."),
                createLibRow("Open Source Community", "Inspired by developer tools and open-source standards (Apache Commons, WireMock, and Postman).")
        );

        card.getChildren().addAll(title, libBox);
        return card;
    }

    private HBox createLibRow(String name, String desc) {
        HBox row = new HBox(8);
        row.setAlignment(Pos.CENTER_LEFT);

        Label nameLabel = new Label("•  " + name + ":");
        nameLabel.setStyle("-fx-font-weight: bold;");

        Label descLabel = new Label(desc);
        descLabel.getStyleClass().add("secondary");
        descLabel.setWrapText(true);

        row.getChildren().addAll(nameLabel, descLabel);
        return row;
    }

    private Image loadDeveloperPhoto() {
        // 1. Check custom path from .env if configured
        String customPath = EnvUtil.get("DEVELOPER_PROFILE_PIC");
        if (customPath != null && !customPath.isBlank()) {
            Image img = tryLoadImageFromPath(Paths.get(customPath));
            if (img != null) {
                return img;
            }
        }

        // 2. Reference the picture available in the Branding folder named "developer_profile_pic.png"
        // Also support other common formats if renamed, prioritizing "developer_profile_pic.png"
        String[] candidateNames = {"developer_profile_pic.png", "developer_profile_pic.jpg", "developer_profile_pic.jpeg"};
        Path[] candidateDirs = {
                Paths.get("Branding"),
                Paths.get(".").toAbsolutePath().resolve("Branding"),
                AppPaths.getAppDataDir().resolve("Branding")
        };

        for (Path dir : candidateDirs) {
            for (String name : candidateNames) {
                Path file = dir.resolve(name);
                Image img = tryLoadImageFromPath(file);
                if (img != null) {
                    return img;
                }
            }
        }

        // 3. Fallback to classpath resource
        for (String name : candidateNames) {
            try (InputStream is = getClass().getResourceAsStream("/com/entropylab/branding/" + name)) {
                if (is != null) {
                    return new Image(is);
                }
            } catch (Exception ignored) {
            }
        }

        return null;
    }

    private Image loadAppLogo() {
        Path[] candidateDirs = {
                Paths.get("Branding"),
                Paths.get(".").toAbsolutePath().resolve("Branding"),
                AppPaths.getAppDataDir().resolve("Branding")
        };
        for (Path dir : candidateDirs) {
            Path file = dir.resolve("EntropyLab_logo.png");
            Image img = tryLoadImageFromPath(file);
            if (img != null) {
                return img;
            }
        }
        try (InputStream is = getClass().getResourceAsStream("/com/entropylab/branding/EntropyLab_logo.png")) {
            if (is != null) {
                return new Image(is);
            }
        } catch (Exception ignored) {
        }
        return null;
    }

    private Image tryLoadImageFromPath(Path path) {
        try {
            if (path != null && Files.isRegularFile(path)) {
                return new Image(path.toUri().toString());
            }
        } catch (Exception ignored) {
        }
        return null;
    }

    private void openDocumentation() {
        String docsUrl = EnvUtil.get("APP_DOCS_URL");
        if (docsUrl == null || docsUrl.isBlank()) {
            String repoUrl = EnvUtil.get("GITHUB_REPO_URL");
            if (repoUrl != null && !repoUrl.isBlank()) {
                docsUrl = repoUrl.replaceAll("/+$", "") + "/blob/main/docs/00-MANUAL-INDEX.md";
            } else {
                String devGithub = EnvUtil.get("DEVELOPER_GITHUB", "https://github.com/AR-Fahim");
                docsUrl = devGithub.replaceAll("/+$", "") + "/EntropyLab/blob/main/docs/00-MANUAL-INDEX.md";
            }
        }
        openUrl(docsUrl);
    }

    private Path findLocalDocs() {
        Path[] candidates = {
                Paths.get("docs", "00-MANUAL-INDEX.md"),
                Paths.get("docs"),
                Paths.get(".").toAbsolutePath().resolve("docs").resolve("00-MANUAL-INDEX.md"),
                AppPaths.getAppDataDir().resolve("docs").resolve("00-MANUAL-INDEX.md")
        };
        for (Path p : candidates) {
            if (Files.exists(p)) {
                return p.toAbsolutePath();
            }
        }
        return null;
    }

    private void openLocalPath(Path path) {
        if (path == null) {
            return;
        }
        new Thread(() -> {
            try {
                if (Desktop.isDesktopSupported() && Desktop.getDesktop().isSupported(Desktop.Action.OPEN)) {
                    Desktop.getDesktop().open(path.toFile());
                } else {
                    new ProcessBuilder("cmd", "/c", "start", "", path.toString()).start();
                }
            } catch (Exception e) {
                System.err.println("Failed to open local path " + path + ": " + e.getMessage());
            }
        }).start();
    }

    private void openUrl(String url) {
        if (url == null || url.isBlank()) {
            return;
        }
        new Thread(() -> {
            try {
                if (Desktop.isDesktopSupported() && Desktop.getDesktop().isSupported(Desktop.Action.BROWSE)) {
                    Desktop.getDesktop().browse(new URI(url));
                } else {
                    new ProcessBuilder("cmd", "/c", "start", "", url).start();
                }
            } catch (Exception e) {
                System.err.println("Failed to open browser URL " + url + ": " + e.getMessage());
            }
        }).start();
    }
}
