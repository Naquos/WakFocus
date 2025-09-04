package com.wakfocus.utils;

import com.sun.jna.Native;
import com.sun.jna.Pointer;
import com.sun.jna.platform.win32.User32;
import com.sun.jna.platform.win32.WinDef.HWND;
import com.sun.jna.platform.win32.WinDef.RECT;
import com.wakfocus.models.TurnDescriptions;

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
                    wakfuMap.put(hWnd, new TurnDescriptions(title, false));
                }
            }
            return true;
        }, Pointer.NULL);
    }

    public static void focusApplication(HWND hWnd) {
        User32.INSTANCE.SetForegroundWindow(hWnd);
    }
}
