package com.wakfocus.services;


import com.wakfocus.utils.ColorRule;

import java.awt.Color;
import java.awt.image.BufferedImage;

import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.SwingUtilities;
import javax.swing.ImageIcon;

/**
 * Code pour Debug :
    BufferedImage debugImage = bufferToImage();
    DebugPreviewService.showPreviewWithRules(debugImage, "Timeline Button", new ArrayList<>(RuleSets.TIMELINE));
 */

public class DebugPreviewService {

    /**
     * Affiche une image brute capturée
     */
       private static JFrame frame;
    private static JLabel label;

    /**
     * Initialise la fenêtre si nécessaire et affiche une image
     */
    public static void showPreview(BufferedImage image, String title) {
        if (image == null) return;

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
        if (image == null) return;

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