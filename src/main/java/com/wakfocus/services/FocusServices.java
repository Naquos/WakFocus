package com.wakfocus.services;

import dorkbox.notify.Notify;
import dorkbox.notify.Position;
import dorkbox.notify.Theme;

import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.concurrent.Executors;

import javax.imageio.ImageIO;
import javax.swing.ImageIcon;
import javax.swing.JFrame;
import javax.swing.JLabel;

import java.awt.AWTException;
import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Graphics;
import java.awt.image.BufferedImage;
import java.io.IOException;

import com.sun.jna.platform.win32.WinDef.RECT;
import com.sun.jna.Native;
import com.sun.jna.Pointer;
import com.sun.jna.platform.win32.User32;
import com.sun.jna.platform.win32.WinDef;
import com.sun.jna.platform.win32.WinDef.HWND;
import com.sun.jna.platform.win32.WinUser;
import com.sun.jna.platform.win32.WinUser.INPUT;

import com.sun.jna.platform.win32.GDI32;
import com.sun.jna.platform.win32.WinDef.HDC;
import com.sun.jna.platform.win32.WinDef.HBITMAP;

import com.sun.jna.Memory;
import com.sun.jna.platform.win32.WinGDI;

import java.awt.image.DataBufferInt;

public class FocusServices {

    private static final String TEXT_TO_DISPLAY = "C'est au tour de ";
    private static final String TITLE = "WakFocus";
    private static final String WAKFU = "- WAKFU";
    private static final int DELAY_NOTIFICATION = 5000;

    private static final int WIDTH_RECT_BOTTOM_RIGHT = 20;
    private static final int HEIGHT_RECT_BOTTOM_RIGHT = 20;
    private static final int WIDTH_RECT_BOTTOM_CENTER = 100;
    private static final int HEIGHT_RECT_BOTTOM_CENTER = 40;

    private static final int PIXEL_GOOD_COLOR = 20;
    private static final int PIXEL_GOOD_COLOR_MAJOR = 20;
    private static final int THREAD_SLEEP_MILLISECONDS = 1000;

    private static final Color COLOR_REFERENCE = new Color(219, 177, 115);
    private static final Color COLOR_REFERENCE_VELOCITE = new Color(59, 60, 48);
    private static final Color COLOR_WHITE = new Color(255, 255, 255);
    private static final Color COLOR_WHITE_VELOCITE = new Color(63, 74, 78);
    private static final Color COLOR_BLUE = new Color(11, 145, 227);
    private static final Color COLOR_BLUE_VELOCITE = new Color(6, 47, 75);
    private static final Color COLOR_GREEN = new Color(117, 179, 36);
    private static final Color COLOR_GREEN_VELOCITE = new Color(31, 59, 32);

    private static final int TOLERANCE_COLOR = 30;
    private static final int TOLERANCE_COLOR_VELOCITE = 7;
    private static final int TOLERANCE_COLOR_WHITE = 20;
    private static final int TOLERANCE_COLOR_WHITE_VELOCITE = 30;
    private static final int TOLERANCE_COLOR_BLUE = 30;
    private static final int TOLERANCE_COLOR_BLUE_VELOCITE = 30;
    private static final int TOLERANCE_COLOR_GREEN = 30;
    private static final int TOLERANCE_COLOR_GREEN_VELOCITE = 30;

    private static boolean RUNNING = true;
    private static boolean focusApplication = false;
    private static boolean notifyUser = true;
    private static BufferedImage bImage = null;
    private static ConfigManager configManager = new ConfigManager();
    private static Map<HWND, Boolean> wakfuYourTurnMap = new HashMap<>();

    // =============== POUR TESTER L APPLI =================
    private static JFrame previewFrame;
    private static JLabel previewLabel;
    private static javax.swing.Timer previewTimer;

    private static BufferedImage maskByColor(BufferedImage img) {
        BufferedImage masked = new BufferedImage(img.getWidth(), img.getHeight(), BufferedImage.TYPE_INT_ARGB);

        for (int x = 0; x < img.getWidth(); x++) {
            for (int y = 0; y < img.getHeight(); y++) {
                Color c = new Color(img.getRGB(x, y));
                if (isClose(c, COLOR_REFERENCE, TOLERANCE_COLOR)
                        || isClose(c, COLOR_REFERENCE_VELOCITE, TOLERANCE_COLOR_VELOCITE)
                        || isClose(c, COLOR_BLUE, TOLERANCE_COLOR_BLUE)
                        || isClose(c, COLOR_WHITE, TOLERANCE_COLOR_WHITE)
                        || isClose(c, COLOR_GREEN, TOLERANCE_COLOR_GREEN)
                        || isClose(c, COLOR_GREEN_VELOCITE, TOLERANCE_COLOR_GREEN_VELOCITE)
                        || isClose(c, COLOR_WHITE_VELOCITE, TOLERANCE_COLOR_WHITE_VELOCITE)
                        || isClose(c, COLOR_BLUE_VELOCITE, TOLERANCE_COLOR_BLUE_VELOCITE)
                        ) {
                    masked.setRGB(x, y, c.getRGB()); // garde la couleur
                } else {
                    masked.setRGB(x, y, Color.BLACK.getRGB()); // sinon noir
                }
            }
        }
        return masked;
    }

    public static void showPreview(HWND hwnd) {
        if (previewFrame != null) {
            previewFrame.dispose();
            previewFrame = null;
            previewLabel = null;
            previewTimer.stop();
            previewTimer = null;
        }

        previewFrame = new JFrame("Preview Capture");
        previewFrame.setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
        previewFrame.setSize(400, 200);

        previewLabel = new JLabel();
        previewLabel.setHorizontalAlignment(JLabel.CENTER);
        previewFrame.add(previewLabel, BorderLayout.CENTER);

        // Timer Swing → rafraîchit l’image toutes les 500 ms
        previewTimer = new javax.swing.Timer(500, e -> {
            try {
                BufferedImage img = captureBottomRight(hwnd);
                // BufferedImage img = captureBottomCenter(hwnd);
                if (img != null) {
                    BufferedImage masked = maskByColor(img);
                    // BufferedImage masked = img;
                    previewLabel.setIcon(new ImageIcon(masked));
                }
            } catch (Exception ex) {
                ex.printStackTrace();
            }
        });

        previewFrame.setVisible(true);
        previewTimer.start();
    }
    // =================================================

    public static void run() throws InterruptedException, IOException {
        init();

        Executors.newSingleThreadExecutor().submit(() -> {
            while (RUNNING) {
                try {

                    findNewWakfuInstance();
                    checkIfItsYourTurn();

                    Thread.sleep(THREAD_SLEEP_MILLISECONDS);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }
        });
    }

    private static boolean isInFight(HWND hwnd) {
        boolean result = false;
        BufferedImage bImage = captureBottomCenter(hwnd);
        result = (containsColor(bImage, COLOR_BLUE, TOLERANCE_COLOR_BLUE, PIXEL_GOOD_COLOR_MAJOR)
                && containsColor(bImage, COLOR_GREEN, TOLERANCE_COLOR_GREEN, PIXEL_GOOD_COLOR_MAJOR)
                && containsColor(bImage, COLOR_WHITE, TOLERANCE_COLOR_WHITE, PIXEL_GOOD_COLOR_MAJOR))
                || (containsColor(bImage, COLOR_BLUE_VELOCITE, TOLERANCE_COLOR_BLUE_VELOCITE, PIXEL_GOOD_COLOR_MAJOR)
                        && containsColor(bImage, COLOR_GREEN_VELOCITE, TOLERANCE_COLOR_GREEN_VELOCITE,
                                PIXEL_GOOD_COLOR_MAJOR)
                        && containsColor(bImage, COLOR_WHITE_VELOCITE, TOLERANCE_COLOR_WHITE_VELOCITE,
                                PIXEL_GOOD_COLOR_MAJOR));

        bImage.flush();
        System.out.println("Est dans un combat : " + result);
        return result;
    }

    private static void checkIfItsYourTurn() {
        User32 user32 = User32.INSTANCE;
        Iterator<Map.Entry<HWND, Boolean>> iterator = wakfuYourTurnMap.entrySet().iterator();

        while (iterator.hasNext()) {
            Map.Entry<HWND, Boolean> entry = iterator.next();
            HWND hWnd = entry.getKey();
            boolean isYourTurn = entry.getValue();

            RECT rect = new RECT();
            boolean exists = user32.GetWindowRect(hWnd, rect);
            if (!exists) {
                // La fenêtre n'existe plus, on la retire de la map
                iterator.remove();
                return;
            }
            if (isInFight(hWnd)) {

                BufferedImage image = captureBottomRight(hWnd);
                boolean result = containsColor(image, COLOR_REFERENCE, TOLERANCE_COLOR, PIXEL_GOOD_COLOR);
                boolean resultVelocite = containsColor(image, COLOR_REFERENCE_VELOCITE, TOLERANCE_COLOR_VELOCITE,
                        PIXEL_GOOD_COLOR);
                boolean resultWhiteColor = true;

                if (resultWhiteColor && (result || resultVelocite) && !isYourTurn) {
                    // C'est à lui de jouer
                    wakfuYourTurnMap.put(hWnd, true);
                    notifyFocusUser(hWnd);
                } else if ((!result && !resultVelocite) && isYourTurn) {
                    // Ce n'est plus à lui de jouer
                    wakfuYourTurnMap.put(hWnd, false);
                }

                image.flush();
            }

        }
    }

    public static String getWindowTitle(HWND hWnd) {
        char[] windowText = new char[512];
        User32.INSTANCE.GetWindowText(hWnd, windowText, 512);
        return Native.toString(windowText);
    }

    private static void notifyFocusUser(HWND hWnd) {
        if (focusApplication) {
            focusApplication(hWnd);
        }

        if (notifyUser) {
            try {
                createNotifyToast(getWindowTitle(hWnd).split(WAKFU)[0], hWnd);
            } catch (AWTException e) {
                e.printStackTrace();
            }
        }
    }

    // Vérifie si une couleur précise est présente dans la capture
    public static boolean containsColor(BufferedImage img, Color target, int tolerance, int minPixels) {
        int colorOk = 0;
        for (int x = 0; x < img.getWidth(); x++) {
            for (int y = 0; y < img.getHeight(); y++) {
                Color c = new Color(img.getRGB(x, y));
                if (isClose(c, target, tolerance)) {
                    colorOk++;
                    if (colorOk >= minPixels) {
                        return true; // on s'arrête dès qu'on a assez de pixels
                    }
                }
            }
        }
        return false;
    }

    private static boolean isClose(Color c1, Color c2, int tolerance) {
        return Math.abs(c1.getRed() - c2.getRed()) <= tolerance &&
                Math.abs(c1.getGreen() - c2.getGreen()) <= tolerance &&
                Math.abs(c1.getBlue() - c2.getBlue()) <= tolerance;
    }

    private static void findNewWakfuInstance() {
        User32 user32 = User32.INSTANCE;
        user32.EnumWindows((hWnd, arg) -> {
            if (user32.IsWindowVisible(hWnd)) { // on ne garde que les fenêtres visibles
                char[] windowText = new char[512];
                user32.GetWindowText(hWnd, windowText, 512);
                String wText = Native.toString(windowText);

                if (!wText.isEmpty() && wText.contains(WAKFU) && wakfuYourTurnMap.get(hWnd) == null) {
                    wakfuYourTurnMap.put(hWnd, false);
                    // showPreview(hWnd);
                }
            }
            return true;
        }, Pointer.NULL);
    }

    public static BufferedImage captureRelative(HWND hwnd,
            double relX, double relY,
            int cropWidth, int cropHeight,
            double yOffsetPercent,
            double xOffsetPercent) {
        RECT rect = new RECT();
        User32.INSTANCE.GetWindowRect(hwnd, rect);
        int width = rect.right - rect.left;
        int height = rect.bottom - rect.top;

        HDC hdcWindow = User32.INSTANCE.GetDC(hwnd);
        HDC hdcMemDC = GDI32.INSTANCE.CreateCompatibleDC(hdcWindow);

        HBITMAP hBitmap = GDI32.INSTANCE.CreateCompatibleBitmap(hdcWindow, width, height);
        GDI32.INSTANCE.SelectObject(hdcMemDC, hBitmap);

        // Capture complète
        User32.INSTANCE.PrintWindow(hwnd, hdcMemDC, 0);

        // Conversion en BufferedImage
        BufferedImage fullImage = convertHBitmapToBufferedImage(hBitmap, width, height, hdcMemDC);

        // Nettoyage
        GDI32.INSTANCE.DeleteObject(hBitmap);
        GDI32.INSTANCE.DeleteDC(hdcMemDC);
        User32.INSTANCE.ReleaseDC(hwnd, hdcWindow);

        // ---- Calcul des coordonnées de départ ----
        int xOffset = (int) (width * xOffsetPercent);
        int x = (int) (width * relX) + xOffset;
        int yOffset = (int) (height * yOffsetPercent);
        int y = (int) (height * relY) - cropHeight - yOffset;

        // ✅ Bornes pour éviter RasterFormatException
        if (x < 0)
            x = 0;
        if (y < 0)
            y = 0;
        if (x + cropWidth > width)
            cropWidth = width - x;
        if (y + cropHeight > height)
            cropHeight = height - y;

        if (cropWidth <= 0 || cropHeight <= 0) {
            throw new IllegalArgumentException("Zone de capture invalide (taille négative ou nulle)");
        }

        BufferedImage cropped = new BufferedImage(cropWidth, cropHeight, BufferedImage.TYPE_INT_ARGB);
        Graphics g = cropped.getGraphics();
        g.drawImage(fullImage, 0, 0, cropWidth, cropHeight, x, y, x + cropWidth, y + cropHeight, null);
        g.dispose();

        fullImage.flush(); // libère la mémoire de l’image complète
        return cropped;
    }

    private static int getWindowWidth(HWND hwnd) {
        RECT rect = new RECT();
        User32.INSTANCE.GetWindowRect(hwnd, rect);
        return rect.right - rect.left;
    }

    // Coin bas-droit
    public static BufferedImage captureBottomRight(HWND hwnd) {
        return captureRelative(hwnd,
                1.0 - ((double) WIDTH_RECT_BOTTOM_RIGHT / getWindowWidth(hwnd)), // X aligné à droite
                1.0, // Y en bas
                WIDTH_RECT_BOTTOM_RIGHT,
                HEIGHT_RECT_BOTTOM_RIGHT,
                0.01f, 0);
    }

    // Milieu bas
    public static BufferedImage captureBottomCenter(HWND hwnd) {
        return captureRelative(hwnd,
                0.5 - ((double) WIDTH_RECT_BOTTOM_CENTER / 2.0 / getWindowWidth(hwnd)), // X centré
                1.0, // Y en bas
                WIDTH_RECT_BOTTOM_CENTER,
                HEIGHT_RECT_BOTTOM_CENTER,
                0.05, 0.02);
    }

    public static BufferedImage convertHBitmapToBufferedImage(HBITMAP hBitmap, int width, int height, HDC hdc) {
        // Préparer la structure pour Bitmaps
        WinGDI.BITMAPINFO bmi = new WinGDI.BITMAPINFO();
        bmi.bmiHeader.biSize = bmi.bmiHeader.size();
        bmi.bmiHeader.biWidth = width;
        bmi.bmiHeader.biHeight = -height; // négatif → évite l’image inversée verticalement
        bmi.bmiHeader.biPlanes = 1;
        bmi.bmiHeader.biBitCount = 32;
        bmi.bmiHeader.biCompression = WinGDI.BI_RGB;

        // Mémoire pour les pixels
        Memory buffer = new Memory(width * height * 4);

        // Copie les pixels du HBITMAP vers notre buffer
        int result = GDI32.INSTANCE.GetDIBits(
                hdc,
                hBitmap,
                0,
                height,
                buffer,
                bmi,
                WinGDI.DIB_RGB_COLORS);

        if (result == 0) {
            throw new RuntimeException("Échec GetDIBits → impossible de convertir le HBITMAP");
        }

        // Construire un BufferedImage ARGB
        BufferedImage image = new BufferedImage(width, height, BufferedImage.TYPE_INT_ARGB);
        int[] pixels = ((DataBufferInt) image.getRaster().getDataBuffer()).getData();

        buffer.read(0, pixels, 0, pixels.length);

        return image;
    }

    private static void init() throws IOException {
        bImage = ImageIO.read(FocusServices.class.getResource("/images/logo.png"));
        focusApplication = configManager.getFocusApplication();
        notifyUser = configManager.getNotifyUser();
    }

    public static boolean isFocusApplication() {
        return focusApplication;
    }

    public static boolean isNotifyUser() {
        return notifyUser;
    }

    public static void handleFocusApplication() {
        focusApplication = !focusApplication;
        configManager.setFocusApplication(focusApplication);
    }

    public static void handleNotifyUser() {
        notifyUser = !notifyUser;
        configManager.setNotifyUser(notifyUser);
    }

    public static void stopRunning() {
        RUNNING = false;
    }

    private static void focusApplication(HWND hWnd) {
        simulateKeyPress();
        User32.INSTANCE.SetForegroundWindow(hWnd);
    }

    private static void simulateKeyPress() {
        User32 user32 = User32.INSTANCE;
        INPUT input = new INPUT();
        input.type = new WinUser.DWORD(INPUT.INPUT_KEYBOARD);
        input.input.setType("ki");
        input.input.ki.wVk = new WinUser.WORD(0); // touche nulle
        input.input.ki.dwFlags = new WinUser.DWORD(0); // key down
        user32.SendInput(new WinDef.DWORD(1), (INPUT[]) new INPUT[] { input }, input.size());
    }

    private static void createNotifyToast(String characterName, HWND hWnd) throws AWTException {

        Notify.Companion.create()
                .title(TITLE)
                .image(bImage)
                .text(TEXT_TO_DISPLAY + characterName)
                .theme(Theme.Companion.getDefaultDark())
                .position(Position.BOTTOM_RIGHT)
                .hideAfter(DELAY_NOTIFICATION)
                .onClickAction((notify) -> {
                    User32.INSTANCE.SetForegroundWindow(hWnd);
                    return null;
                })
                .show();
    }
}
