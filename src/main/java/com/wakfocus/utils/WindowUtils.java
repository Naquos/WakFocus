package com.wakfocus.utils;

import com.sun.jna.Memory;
import com.sun.jna.Native;
import com.sun.jna.Pointer;
import com.sun.jna.platform.win32.GDI32;
import com.sun.jna.platform.win32.User32;
import com.sun.jna.platform.win32.WinUser;
import com.sun.jna.platform.win32.WinUser.INPUT;
import com.sun.jna.platform.win32.WinDef.HBITMAP;
import com.sun.jna.platform.win32.WinDef.HDC;
import com.sun.jna.platform.win32.WinDef.HWND;
import com.sun.jna.platform.win32.WinDef.RECT;
import com.sun.jna.platform.win32.WinGDI;
import com.sun.jna.platform.win32.WinDef;
import com.wakfocus.models.TurnDescriptions;

import java.awt.image.BufferedImage;
import java.awt.image.DataBufferInt;
import java.util.Map;

public class WindowUtils {

    public static String getWindowTitle(HWND hWnd) {
        char[] windowText = new char[512];
        User32.INSTANCE.GetWindowText(hWnd, windowText, 512);
        return Native.toString(windowText);
    }

    public static boolean isValid(HWND hWnd, RECT rect) {
        return User32.INSTANCE.GetWindowRect(hWnd, rect);
    }

    public static void findNewWakfuInstance(String keyword, Map<HWND, TurnDescriptions> wakfuMap) {
        User32.INSTANCE.EnumWindows((hWnd, arg) -> {
            if (User32.INSTANCE.IsWindowVisible(hWnd)) {
                String title = getWindowTitle(hWnd);
                if (!title.isEmpty() && title.contains(keyword) && wakfuMap.get(hWnd) == null) {
                    wakfuMap.put(hWnd, new TurnDescriptions(title, false, false));
                }
            }
            return true;
        }, Pointer.NULL);
    }

    public static void focusApplication(HWND hWnd) {
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

    /**
     * Capture l’intégralité de la fenêtre donnée par son hWnd,
     * même si elle est en arrière-plan.
     */
    public static BufferedImage captureFullWindow(HWND hWnd) {
        RECT rect = new RECT();
        if (!User32.INSTANCE.GetWindowRect(hWnd, rect)) {
            return null;
        }

        int width = rect.right - rect.left;
        int height = rect.bottom - rect.top;

        if (width <= 0 || height <= 0) {
            return null;
        }

        // Création des contextes GDI
        HDC hdcWindow = User32.INSTANCE.GetDC(hWnd);
        HDC hdcMemDC = GDI32.INSTANCE.CreateCompatibleDC(hdcWindow);
        HBITMAP hBitmap = GDI32.INSTANCE.CreateCompatibleBitmap(hdcWindow, width, height);
        com.sun.jna.platform.win32.WinNT.HANDLE hOld = GDI32.INSTANCE.SelectObject(hdcMemDC, hBitmap);

        // Copie la fenêtre dans le bitmap
        GDI32.INSTANCE.BitBlt(hdcMemDC, 0, 0, width, height,
                hdcWindow, 0, 0, GDI32.SRCCOPY);

        // Préparer la structure d’info pour extraction
        WinGDI.BITMAPINFO bmi = new WinGDI.BITMAPINFO();
        bmi.bmiHeader.biSize = bmi.bmiHeader.size();
        bmi.bmiHeader.biWidth = width;
        bmi.bmiHeader.biHeight = -height; // négatif => évite inversion verticale
        bmi.bmiHeader.biPlanes = 1;
        bmi.bmiHeader.biBitCount = 32;
        bmi.bmiHeader.biCompression = WinGDI.BI_RGB;

        // Buffer mémoire pour pixels
        Memory buffer = new Memory(width * height * 4);

        int result = GDI32.INSTANCE.GetDIBits(hdcMemDC, hBitmap, 0, height, buffer, bmi, WinGDI.DIB_RGB_COLORS);
        if (result == 0) {
            // nettoyage et sortie
            GDI32.INSTANCE.SelectObject(hdcMemDC, hOld);
            GDI32.INSTANCE.DeleteObject(hBitmap);
            GDI32.INSTANCE.DeleteDC(hdcMemDC);
            User32.INSTANCE.ReleaseDC(hWnd, hdcWindow);
            return null;
        }

        // Construire BufferedImage à partir du buffer
        BufferedImage image = new BufferedImage(width, height, BufferedImage.TYPE_INT_ARGB);
        int[] pixels = ((DataBufferInt) image.getRaster().getDataBuffer()).getData();
        buffer.read(0, pixels, 0, pixels.length);

        // Nettoyage
        GDI32.INSTANCE.SelectObject(hdcMemDC, hOld);
        GDI32.INSTANCE.DeleteObject(hBitmap);
        GDI32.INSTANCE.DeleteDC(hdcMemDC);
        User32.INSTANCE.ReleaseDC(hWnd, hdcWindow);

        return image;
    }
}
