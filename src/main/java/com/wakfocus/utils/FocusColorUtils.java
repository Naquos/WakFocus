package com.wakfocus.utils;

import com.sun.jna.Memory;
import com.wakfocus.services.ScreenCaptureService;

import java.awt.Color;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;

public class FocusColorUtils {

    // ⚡ Utilise les buffers capturés dans ScreenCaptureService
    public static boolean containsColorRaw(Color target, int tolerance,
                                           int startX, int startY, int w, int h,
                                           int minPixels) {
        Memory reusableBufferRaw = ScreenCaptureService.getReusableBufferRaw();
        int bufferWidth = ScreenCaptureService.getLastWidth();
        int bufferHeight = ScreenCaptureService.getLastHeight();

        if (reusableBufferRaw == null || bufferWidth <= 0 || bufferHeight <= 0) {
            return false;
        }

        int pixelsFound = 0;

        // Décodage direct depuis le buffer mémoire (BGRA en little-endian)
        ByteBuffer buffer = reusableBufferRaw.getByteBuffer(0, (long) bufferWidth * bufferHeight * 4);
        buffer.order(ByteOrder.LITTLE_ENDIAN);

        for (int yy = startY; yy < startY + h && yy < bufferHeight; yy++) {
            for (int xx = startX; xx < startX + w && xx < bufferWidth; xx++) {
                int idx = (yy * bufferWidth + xx) * 4;

                int b = buffer.get(idx) & 0xFF;
                int g = buffer.get(idx + 1) & 0xFF;
                int r = buffer.get(idx + 2) & 0xFF;

                if (isColorMatch(r, g, b, target, tolerance)) {
                    pixelsFound++;
                    if (pixelsFound >= minPixels) {
                        return true; // early exit
                    }
                }
            }
        }

        return false;
    }

    private static boolean isColorMatch(int r, int g, int b, Color target, int tolerance) {
        return Math.abs(r - target.getRed()) <= tolerance &&
               Math.abs(g - target.getGreen()) <= tolerance &&
               Math.abs(b - target.getBlue()) <= tolerance;
    }
}
