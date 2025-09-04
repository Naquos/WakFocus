package com.wakfocus.services;

import dorkbox.notify.Notify;
import dorkbox.notify.Position;
import dorkbox.notify.Theme;

import com.sun.jna.platform.win32.WinDef.HWND;
import com.wakfocus.utils.WindowUtils;

import java.awt.Image;
import java.awt.Toolkit;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class NotificationService {
    private static final String TEXT_TO_DISPLAY = "C'est au tour de ";
    private static final String TITLE = "WakFocus";
    private static final int DELAY_NOTIFICATION = 5000;
    private static Image bImage;

    // ⚡ Thread pool réutilisable (un seul thread pour toutes les notifs)
    private static final ExecutorService notificationExecutor = Executors.newSingleThreadExecutor();

    static {
        try {
            bImage = Toolkit.getDefaultToolkit().getImage(NotificationService.class.getResource("/images/logo.png"));
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public static void notifyFocusUser(HWND hWnd, boolean focusApplication, boolean notifyUser, String keyword) {
        if (focusApplication) {
            WindowUtils.focusApplication(hWnd);
        }

        if (notifyUser) {
            String characterName = WindowUtils.getWindowTitle(hWnd).split(keyword)[0];
            showTurnNotification(characterName, hWnd);
        }
    }

    public static void showTurnNotification(String characterName, HWND hWnd) {
        notificationExecutor.submit(() -> {
            Notify.Companion.create()
                    .title(TITLE)
                    .image(bImage)
                    .text(TEXT_TO_DISPLAY + characterName)
                    .theme(Theme.Companion.getDefaultDark())
                    .position(Position.BOTTOM_RIGHT)
                    .hideAfter(DELAY_NOTIFICATION)
                    .onClickAction((notify) -> {
                        WindowUtils.focusApplication(hWnd);
                        return null;
                    })
                    .show();
        });
    }

    // Pour bien fermer l'appli proprement
    public static void shutdown() {
        notificationExecutor.shutdownNow();
    }
}
