package com.wakfocus.services;

import dorkbox.notify.Notify;
import dorkbox.notify.Position;
import dorkbox.notify.Theme;

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.Executors;

import javax.imageio.ImageIO;

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

import java.awt.*;
import java.awt.image.DataBufferInt;

public class FocusServices {

    private static final String TEXT_TO_DISPLAY = "C'est au tour de ";
    private static final String TITLE = "WakFocus";
    private static final String WAKFU = "- WAKFU";
    private static final int DELAY_NOTIFICATION = 5000;
    private static final int TOLERANCE_COLOR = 10;
    private static final int WIDTH_RECT = 10;
    private static final int HEIGHT_RECT = 3;
    private static final int THREAD_SLEEP_MILLISECONDS = 500;
    private static final Color COLOR_REFERENCE = new Color(219, 160, 88);
    private static final Color COLOR_REFERENCE_VELOCITE = new Color(59, 55, 41);

    private static boolean RUNNING = true;
    private static boolean focusApplication = false;
    private static boolean notifyUser = true;
    private static BufferedImage bImage = null;
    private static ConfigManager configManager = new ConfigManager();
    private static Map<HWND, Boolean> wakfuYourTurnMap = new HashMap<>();

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

    private static void checkIfItsYourTurn() {
        User32 user32 = User32.INSTANCE;
        wakfuYourTurnMap.forEach((hWnd, isYourTurn) -> {
            RECT rect = new RECT();
            boolean exists = user32.GetWindowRect(hWnd, rect);
            if (!exists) {
                // La fenêtre n'existe plus, on la retire de la map
                wakfuYourTurnMap.remove(hWnd);
                return;
            }
            BufferedImage image = captureBottomRight(hWnd, WIDTH_RECT, HEIGHT_RECT);
            boolean result = containsColor(image, COLOR_REFERENCE, TOLERANCE_COLOR);
            boolean resultVelocite = containsColor(image, COLOR_REFERENCE_VELOCITE, TOLERANCE_COLOR);

            if ((result || resultVelocite) && !isYourTurn) {
                // C'est à lui de jouer
                wakfuYourTurnMap.put(hWnd, true);
                notifyFocusUser(hWnd);
            } else if ((!result && !resultVelocite) && isYourTurn) {
                // Ce n'est plus à lui de jouer
                wakfuYourTurnMap.put(hWnd, false);
            }
        });
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
    public static boolean containsColor(BufferedImage img, Color target, int tolerance) {
        int colorOk = 0;
        int colorKo = 0;
        for (int x = 0; x < img.getWidth(); x++) {
            for (int y = 0; y < img.getHeight(); y++) {
                Color c = new Color(img.getRGB(x, y));
                if (isClose(c, target, tolerance)) {
                    colorOk++;
                } else {
                    colorKo++;
                }
            }
        }
        // Logique pour déterminer si la couleur est présente
        return colorOk > colorKo;
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

                if (!wText.isEmpty() && wText.contains(WAKFU) &&  wakfuYourTurnMap.get(hWnd) == null) {
                    wakfuYourTurnMap.put(hWnd, false);
                }
            }
            return true;
        }, Pointer.NULL);
    }

    public static BufferedImage captureBottomRight(HWND hwnd, int cropWidth, int cropHeight) {
        RECT rect = new RECT();
        User32.INSTANCE.GetWindowRect(hwnd, rect);
        int width = rect.right - rect.left;
        int height = rect.bottom - rect.top;

        HDC hdcWindow = User32.INSTANCE.GetDC(hwnd);
        HDC hdcMemDC = GDI32.INSTANCE.CreateCompatibleDC(hdcWindow);

        HBITMAP hBitmap = GDI32.INSTANCE.CreateCompatibleBitmap(hdcWindow, width, height);
        GDI32.INSTANCE.SelectObject(hdcMemDC, hBitmap);

        // Capture complète de la fenêtre
        User32.INSTANCE.PrintWindow(hwnd, hdcMemDC, 0);

        // Conversion → BufferedImage complet
        BufferedImage fullImage = convertHBitmapToBufferedImage(hBitmap, width, height, hdcMemDC);

        // Nettoyage
        GDI32.INSTANCE.DeleteObject(hBitmap);
        GDI32.INSTANCE.DeleteDC(hdcMemDC);
        User32.INSTANCE.ReleaseDC(hwnd, hdcWindow);

        int yOffset = 14;

        // ---- CROP (coin bas-droit) ----
        int x = Math.max(0, width - cropWidth);
        int y = Math.max(0, height - cropHeight - yOffset);

        return fullImage.getSubimage(x, y, cropWidth, cropHeight);
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
