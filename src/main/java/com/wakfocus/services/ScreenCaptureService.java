package com.wakfocus.services;

import java.awt.image.BufferedImage;
import java.awt.image.DataBufferInt;

import com.sun.jna.Memory;
import com.sun.jna.platform.win32.GDI32;
import com.sun.jna.platform.win32.User32;
import com.sun.jna.platform.win32.WinDef.*;
import com.sun.jna.platform.win32.WinGDI;
import com.sun.jna.platform.win32.WinNT.HANDLE;
import com.wakfocus.utils.RuleSets;

public class ScreenCaptureService {

    // Buffers réutilisables
    private static Memory reusableBufferRaw = null;
    private static int lastWidth = -1;
    private static int lastHeight = -1;

    // === LOGIQUE MÉTIER ===
    public static boolean isInFight(HWND hWnd, RECT rect) {
        return !checkBoutonBoutique(hWnd, rect) && checkMajorPaPm(hWnd, rect);
    }

    public static boolean checkTimelineButtonPass(HWND hWnd, RECT rect) {
        int cropWidth = 20, cropHeight = 20;
        int x = rect.right - rect.left - cropWidth - (int) ((rect.right - rect.left) * 0.09);
        int y = rect.bottom - rect.top - cropHeight - (int) ((rect.bottom - rect.top) * 0.01);

        if (!captureRegionRaw(hWnd, x, y, cropWidth, cropHeight))
            return false;

        return RuleSets.TIMELINE.stream().anyMatch(rule -> rule.matches(0, 0, cropWidth, cropHeight));
    }

    public static boolean checkMajorPaPm(HWND hWnd, RECT rect) {
        int cropWidth = 100, cropHeight = 40;
        int winWidth = rect.right - rect.left;
        int winHeight = rect.bottom - rect.top;

        int x = (int) (winWidth * 0.51);
        int y = winHeight - cropHeight - (int) (winHeight * 0.01);

        if (!captureRegionRaw(hWnd, x, y, cropWidth, cropHeight))
            return false;

        return RuleSets.PA_PM.stream().allMatch(rule -> rule.matches(0, 0, cropWidth, cropHeight)) ||
                RuleSets.PA_PM_VELOCITE.stream().allMatch(rule -> rule.matches(0, 0, cropWidth, cropHeight));
    }

    public static boolean checkBoutonBoutique(HWND hWnd, RECT rect) {
        int cropWidth = 100, cropHeight = 40;
        int winWidth = rect.right - rect.left;
        int winHeight = rect.bottom - rect.top;

        int x = (int) (winWidth * 0.01);
        int y = (int) (winHeight * 0.01);

        if (!captureRegionRaw(hWnd, x, y, cropWidth, cropHeight))
            return false;

        return RuleSets.BOUTIQUE.stream().allMatch(rule -> rule.matches(0, 0, cropWidth, cropHeight));
    }

    // === BAS NIVEAU ===
    public static boolean captureRegionRaw(HWND hwnd, int x, int y, int w, int h) {
        RECT rect = new RECT();
        if (!User32.INSTANCE.GetWindowRect(hwnd, rect))
            return false;

        HDC hdcWindow = User32.INSTANCE.GetDC(hwnd);
        HDC hdcMemDC = GDI32.INSTANCE.CreateCompatibleDC(hdcWindow);
        HBITMAP hBitmap = GDI32.INSTANCE.CreateCompatibleBitmap(hdcWindow, w, h);
        HANDLE hOld = GDI32.INSTANCE.SelectObject(hdcMemDC, hBitmap);

        GDI32.INSTANCE.BitBlt(hdcMemDC, 0, 0, w, h, hdcWindow, rect.left + x, rect.top + y, GDI32.SRCCOPY);

        long bufferSize = w * h * 4L;
        if (reusableBufferRaw == null || reusableBufferRaw.size() < bufferSize) {
            reusableBufferRaw = new Memory(bufferSize);
        }

        WinGDI.BITMAPINFO bmi = new WinGDI.BITMAPINFO();
        bmi.bmiHeader.biSize = bmi.bmiHeader.size();
        bmi.bmiHeader.biWidth = w;
        bmi.bmiHeader.biHeight = -h;
        bmi.bmiHeader.biPlanes = 1;
        bmi.bmiHeader.biBitCount = 32;
        bmi.bmiHeader.biCompression = WinGDI.BI_RGB;

        int result = GDI32.INSTANCE.GetDIBits(hdcMemDC, hBitmap, 0, h, reusableBufferRaw, bmi, WinGDI.DIB_RGB_COLORS);

        GDI32.INSTANCE.SelectObject(hdcMemDC, hOld);
        GDI32.INSTANCE.DeleteObject(hBitmap);
        GDI32.INSTANCE.DeleteDC(hdcMemDC);
        User32.INSTANCE.ReleaseDC(hwnd, hdcWindow);

        lastWidth = w;
        lastHeight = h;

        return result != 0;
    }

    /**
     * Convertit le dernier buffer capturé en BufferedImage
     * (uniquement pour debug / affichage)
     */
    public static BufferedImage bufferToImage() {
        if (reusableBufferRaw == null || lastWidth <= 0 || lastHeight <= 0) {
            return null;
        }

        BufferedImage image = new BufferedImage(lastWidth, lastHeight, BufferedImage.TYPE_INT_ARGB);
        int[] pixels = ((DataBufferInt) image.getRaster().getDataBuffer()).getData();

        // Copie brute du buffer → tableau int
        reusableBufferRaw.read(0, pixels, 0, pixels.length);

        return image;
    }

    public static Memory getReusableBufferRaw() {
        return reusableBufferRaw;
    }

    public static int getLastWidth() {
        return lastWidth;
    }

    public static int getLastHeight() {
        return lastHeight;
    }

}
