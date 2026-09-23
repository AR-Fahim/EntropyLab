package com.entropylab.util;

import com.entropylab.config.AppPaths;
import javafx.scene.image.Image;

import javax.imageio.ImageIO;
import java.awt.Graphics2D;
import java.awt.RenderingHints;
import java.awt.Transparency;
import java.awt.image.BufferedImage;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.HashMap;
import java.util.Map;

public class AppBrandUtil {

    private static Image cachedFullLogo = null;
    private static final Map<String, Image> scaledLogoCache = new HashMap<>();
    private static final Map<String, Image> scaledDevPhotoCache = new HashMap<>();

    public static Image loadAppLogo() {
        if (cachedFullLogo != null) {
            return cachedFullLogo;
        }
        cachedFullLogo = loadAppLogo(128, 128);
        return cachedFullLogo;
    }

    public static Image loadAppLogo(int targetWidth, int targetHeight) {
        String key = targetWidth + "x" + targetHeight;
        if (scaledLogoCache.containsKey(key)) {
            return scaledLogoCache.get(key);
        }

        BufferedImage bimg = loadLogoBufferedImage();
        if (bimg != null) {
            // Scale to 2x for Retina/HiDPI sharpness
            BufferedImage scaled = getScaledInstance(bimg, targetWidth * 2, targetHeight * 2);
            Image fxImg = toFXImage(scaled);
            if (fxImg != null) {
                scaledLogoCache.put(key, fxImg);
                return fxImg;
            }
        }

        // Fallback to standard loading
        Image fallback = loadFallbackLogo();
        if (fallback != null) {
            scaledLogoCache.put(key, fallback);
        }
        return fallback;
    }

    public static Image loadDeveloperPhoto(int targetWidth, int targetHeight) {
        String key = targetWidth + "x" + targetHeight;
        if (scaledDevPhotoCache.containsKey(key)) {
            return scaledDevPhotoCache.get(key);
        }

        BufferedImage bimg = loadDevPhotoBufferedImage();
        if (bimg != null) {
            // Scale to 2x-3x for crystal clear HiDPI display
            int reqW = Math.max(targetWidth * 2, 288);
            int reqH = Math.max(targetHeight * 2, 288);
            BufferedImage scaled = getScaledInstance(bimg, reqW, reqH);
            Image fxImg = toFXImage(scaled);
            if (fxImg != null) {
                scaledDevPhotoCache.put(key, fxImg);
                return fxImg;
            }
        }

        Image fallback = loadFallbackDevPhoto();
        if (fallback != null) {
            scaledDevPhotoCache.put(key, fallback);
        }
        return fallback;
    }

    private static BufferedImage loadLogoBufferedImage() {
        Path[] candidateDirs = {
                Paths.get("Branding"),
                Paths.get(".").toAbsolutePath().resolve("Branding"),
                AppPaths.getAppDataDir().resolve("Branding")
        };
        for (Path dir : candidateDirs) {
            Path file = dir.resolve("EntropyLab_logo.png");
            if (Files.isRegularFile(file)) {
                try {
                    return ImageIO.read(file.toFile());
                } catch (Exception ignored) {
                }
            }
        }
        try (InputStream is = AppBrandUtil.class.getResourceAsStream("/com/entropylab/branding/EntropyLab_logo.png")) {
            if (is != null) {
                return ImageIO.read(is);
            }
        } catch (Exception ignored) {
        }
        return null;
    }

    private static BufferedImage loadDevPhotoBufferedImage() {
        String[] candidateNames = {"developer_profile_pic.png", "developer_profile_pic.jpg", "developer_profile_pic.jpeg"};
        Path[] candidateDirs = {
                Paths.get("Branding"),
                Paths.get(".").toAbsolutePath().resolve("Branding"),
                AppPaths.getAppDataDir().resolve("Branding")
        };
        for (Path dir : candidateDirs) {
            for (String name : candidateNames) {
                Path file = dir.resolve(name);
                if (Files.isRegularFile(file)) {
                    try {
                        return ImageIO.read(file.toFile());
                    } catch (Exception ignored) {
                    }
                }
            }
        }
        for (String name : candidateNames) {
            try (InputStream is = AppBrandUtil.class.getResourceAsStream("/com/entropylab/branding/" + name)) {
                if (is != null) {
                    return ImageIO.read(is);
                }
            } catch (Exception ignored) {
            }
        }
        return null;
    }

    /**
     * Progressive multi-step bicubic downscaling algorithm.
     * Prevents aliasing, jagged edges, and texture pixel skipping when scaling down high-resolution images.
     */
    public static BufferedImage getScaledInstance(BufferedImage img, int targetWidth, int targetHeight) {
        int type = (img.getTransparency() == Transparency.OPAQUE) ?
                BufferedImage.TYPE_INT_RGB : BufferedImage.TYPE_INT_ARGB;
        BufferedImage ret = img;
        int w = img.getWidth();
        int h = img.getHeight();

        do {
            if (w > targetWidth) {
                w = Math.max(w / 2, targetWidth);
            }
            if (h > targetHeight) {
                h = Math.max(h / 2, targetHeight);
            }

            BufferedImage tmp = new BufferedImage(w, h, type);
            Graphics2D g2 = tmp.createGraphics();
            g2.setRenderingHint(RenderingHints.KEY_INTERPOLATION, RenderingHints.VALUE_INTERPOLATION_BICUBIC);
            g2.setRenderingHint(RenderingHints.KEY_RENDERING, RenderingHints.VALUE_RENDER_QUALITY);
            g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
            g2.setRenderingHint(RenderingHints.KEY_ALPHA_INTERPOLATION, RenderingHints.VALUE_ALPHA_INTERPOLATION_QUALITY);
            g2.drawImage(ret, 0, 0, w, h, null);
            g2.dispose();

            ret = tmp;
        } while (w != targetWidth || h != targetHeight);

        return ret;
    }

    private static Image toFXImage(BufferedImage bimg) {
        try {
            ByteArrayOutputStream baos = new ByteArrayOutputStream();
            ImageIO.write(bimg, "PNG", baos);
            return new Image(new ByteArrayInputStream(baos.toByteArray()));
        } catch (Exception ignored) {
            return null;
        }
    }

    private static Image loadFallbackLogo() {
        Path[] candidateDirs = {
                Paths.get("Branding"),
                Paths.get(".").toAbsolutePath().resolve("Branding"),
                AppPaths.getAppDataDir().resolve("Branding")
        };
        for (Path dir : candidateDirs) {
            Path file = dir.resolve("EntropyLab_logo.png");
            if (Files.isRegularFile(file)) {
                return new Image(file.toUri().toString());
            }
        }
        try (InputStream is = AppBrandUtil.class.getResourceAsStream("/com/entropylab/branding/EntropyLab_logo.png")) {
            if (is != null) {
                return new Image(is);
            }
        } catch (Exception ignored) {
        }
        return null;
    }

    private static Image loadFallbackDevPhoto() {
        String[] candidateNames = {"developer_profile_pic.png", "developer_profile_pic.jpg", "developer_profile_pic.jpeg"};
        Path[] candidateDirs = {
                Paths.get("Branding"),
                Paths.get(".").toAbsolutePath().resolve("Branding"),
                AppPaths.getAppDataDir().resolve("Branding")
        };
        for (Path dir : candidateDirs) {
            for (String name : candidateNames) {
                Path file = dir.resolve(name);
                if (Files.isRegularFile(file)) {
                    return new Image(file.toUri().toString());
                }
            }
        }
        return null;
    }
}
