package com.wakfocus.services;

import dorkbox.notify.Notify;
import dorkbox.notify.Position;
import dorkbox.notify.Theme;

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import javax.imageio.ImageIO;

import java.awt.AWTException;
import java.awt.image.BufferedImage;
import java.io.IOException;

import com.sun.jna.Native;
import com.sun.jna.Pointer;
import com.sun.jna.platform.win32.User32;
import com.sun.jna.platform.win32.WinDef;
import com.sun.jna.platform.win32.WinDef.HWND;
import com.sun.jna.platform.win32.WinUser;
import com.sun.jna.platform.win32.WinUser.INPUT;

public class FocusServices {

    private static final String TEXT_TO_DISPLAY = "C'est au tour de ";
    private static final String TITLE = "WakFocus";
    private static final String WAKFU = "- WAKFU";
    private static final int DELAY_NOTIFICATION = 5000;

    private static boolean RUNNING = true;
    private static boolean focusApplication = false;
    private static boolean notifyUser = true;
    private static BufferedImage bImage = null;
    private static ConfigManager configManager = new ConfigManager();

    public static void run() throws InterruptedException, IOException {
        User32 user32 = User32.INSTANCE;
        Map<HWND, String> processMap = new HashMap<>();
        ExecutorService executor = Executors.newSingleThreadExecutor();
        bImage = ImageIO.read(FocusServices.class.getResource("/images/logo.png"));
        focusApplication = configManager.getFocusApplication();
        notifyUser = configManager.getNotifyUser();

        executor.submit(() -> {
            while (RUNNING) {
                try {
                    user32.EnumWindows((hWnd, arg) -> {
                        if (user32.IsWindowVisible(hWnd)) { // on ne garde que les fenêtres visibles
                            char[] windowText = new char[512];
                            user32.GetWindowText(hWnd, windowText, 512);
                            String wText = Native.toString(windowText);

                            if (!wText.isEmpty() && wText.contains(WAKFU)) {
                                String processTitle = processMap.get(hWnd);
                                if (processTitle == null || !processTitle.equals(wText)) {
                                    processMap.put(hWnd, wText);

                                    if (focusApplication) {
                                        focusApplication(hWnd);
                                    }

                                    if (notifyUser) {
                                        try {
                                            notifyUser(wText.split(WAKFU)[0], hWnd);
                                        } catch (AWTException e) {
                                            e.printStackTrace();
                                        }
                                    }
                                }
                            }
                        }
                        return true; // continue l’énumération
                    }, Pointer.NULL);

                    Thread.sleep(500); // Pause de 0.5 secondes avant la prochaine énumération
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }

        });
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

    private static void notifyUser(String characterName, HWND hWnd) throws AWTException {

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
