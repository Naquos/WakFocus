package com.wakfocus.services;

import com.sun.jna.platform.win32.WinDef.HWND;
import com.sun.jna.platform.win32.WinDef.RECT;
import com.wakfocus.models.ZoneDefinition;
import com.wakfocus.utils.ColorRule;
import com.wakfocus.utils.WindowUtils;

import javafx.scene.shape.Rectangle;

import java.awt.Color;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;

import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.SwingUtilities;
import javax.imageio.ImageIO;
import javax.swing.ImageIcon;

/**
 * Code pour Debug :
 * BufferedImage debugImage = bufferToImage();
 * DebugPreviewService.showPreviewWithRules(debugImage, "Timeline Button", new
 * ArrayList<>(RuleSets.TIMELINE));
 */

public class DebugPreviewService {

    public static class DebugZone {
        public Rectangle area;
        public List<ColorRule> rules;

        public DebugZone(Rectangle area, List<ColorRule> rules) {
            this.area = area;
            this.rules = rules;
        }
    }

    /**
     * Génère une image debug contenant uniquement les zones traitées.
     * Le reste est grisé.
     */
    public static void generateGlobalDebugImage(HWND hWnd, List<ZoneDefinition> zones) {
        RECT rect = new RECT();
        if (!WindowUtils.isValid(hWnd, rect))
            return;

        BufferedImage fullImage = WindowUtils.captureFullWindow(hWnd);
        if (fullImage == null)
            return;

        BufferedImage debugImage = new BufferedImage(
                fullImage.getWidth(),
                fullImage.getHeight(),
                BufferedImage.TYPE_INT_ARGB);

        for (int y = 0; y < fullImage.getHeight(); y++) {
            for (int x = 0; x < fullImage.getWidth(); x++) {
                boolean inZone = false;
                boolean pixelValid = false;

                for (ZoneDefinition zone : zones) {
                    if (zone.contains(x, y)) {
                        inZone = true;
                        Color pixelColor = new Color(fullImage.getRGB(x, y));
                        if (zone.matches(pixelColor)) {
                            pixelValid = true;
                        }
                    }
                }

                if (inZone && pixelValid) {
                    // Garder la couleur d'origine
                    // debugImage.setRGB(x, y, fullImage.getRGB(x, y));
                    debugImage.setRGB(x, y, new Color(255, 0, 0).getRGB() );
                } else {
                    // Convertir en gris
                    int argb = fullImage.getRGB(x, y);
                    int r = (argb >> 16) & 0xFF;
                    int g = (argb >> 8) & 0xFF;
                    int b = argb & 0xFF;

                    int gray = (int) (0.299 * r + 0.587 * g + 0.114 * b);
                    int grayPixel = (0xFF << 24) | (gray << 16) | (gray << 8) | gray;

                    debugImage.setRGB(x, y, grayPixel);
                }
            }
        }
        // Sauvegarde avec timestamp
        // Création du dossier debug si besoin
        File debugDir = new File("debug");
        if (!debugDir.exists()) {
            debugDir.mkdirs();
        }

        // Sauvegarde avec timestamp dans le dossier debug
        String filename = "debug/debug_" + System.currentTimeMillis() + ".png";
        try {
            ImageIO.write(debugImage, "png", new File(filename));
            System.out.println("Image de debug générée: " + filename);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    /**
     * Sauvegarde l’image debug avec un timestamp comme nom
     */
    public static void saveDebugImage(BufferedImage image, String folder) {
        try {
            File dir = new File(folder);
            if (!dir.exists()) {
                dir.mkdirs();
            }

            String timestamp = new SimpleDateFormat("yyyyMMdd_HHmmss_SSS").format(new Date());
            File output = new File(dir, "debug_" + timestamp + ".png");
            ImageIO.write(image, "png", output);

            System.out.println("✅ Image debug sauvegardée : " + output.getAbsolutePath());
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    /**
     * Affiche une image brute capturée
     */
    private static JFrame frame;
    private static JLabel label;

    /**
     * Initialise la fenêtre si nécessaire et affiche une image
     */
    public static void showPreview(BufferedImage image, String title) {
        if (image == null)
            return;

        SwingUtilities.invokeLater(() -> {
            if (frame == null) {
                frame = new JFrame(title);
                frame.setDefaultCloseOperation(JFrame.HIDE_ON_CLOSE);

                label = new JLabel(new ImageIcon(image));
                frame.add(label);

                frame.pack();
                frame.setLocationRelativeTo(null);
                frame.setVisible(true);
            } else {
                frame.setTitle(title);
                label.setIcon(new ImageIcon(image));
                frame.pack();
            }
        });
    }

    /**
     * Affiche une image en ne gardant que les pixels qui matchent une règle
     */
    public static void showPreviewWithRules(BufferedImage image, String title, java.util.List<ColorRule> rules) {
        if (image == null)
            return;

        BufferedImage debugImage = new BufferedImage(image.getWidth(), image.getHeight(), BufferedImage.TYPE_INT_RGB);

        for (int x = 0; x < image.getWidth(); x++) {
            for (int y = 0; y < image.getHeight(); y++) {
                Color c = new Color(image.getRGB(x, y));

                // Noir par défaut
                Color pixel = Color.BLACK;

                for (ColorRule rule : rules) {
                    if (isClose(c, rule.getTargetColor(), rule.getTolerance())) {
                        pixel = c; // garde la couleur d'origine
                        break;
                    }
                }

                debugImage.setRGB(x, y, pixel.getRGB());
            }
        }

        showPreview(debugImage, title + " (debug)");
    }

    private static boolean isClose(Color c1, Color c2, int tolerance) {
        return Math.abs(c1.getRed() - c2.getRed()) <= tolerance &&
                Math.abs(c1.getGreen() - c2.getGreen()) <= tolerance &&
                Math.abs(c1.getBlue() - c2.getBlue()) <= tolerance;
    }
}