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
    private static final String TEXT_TO_DISPLAY_END_TIME = "C'est la fin du tour de ";
    private static final String TITLE = "WakFocus";
    private static final int DELAY_NOTIFICATION = 5000;
    private static final WAVPlayer wavPlayer = new WAVPlayer();

    private static Position position = Position.BOTTOM_RIGHT;
    private static boolean focusApplication = false;
    private static boolean notifyUser = true;
    private static boolean notifyUserEndTurn = true;
    private static boolean enableSong = true;
    private static ConfigManager configManager = new ConfigManager();

    private static Image bImage;

    // ⚡ Thread pool réutilisable (un seul thread pour toutes les notifs)
    private static final ExecutorService notificationExecutor = Executors.newSingleThreadExecutor();

    static {
        try {
            bImage = Toolkit.getDefaultToolkit().getImage(NotificationService.class.getResource("/images/logo.png"));
            focusApplication = configManager.getFocusApplication();
            notifyUser = configManager.getNotifyUser();
            notifyUserEndTurn = configManager.getNotifyUserEndTurn();
            position = configManager.getNotificationPosition();
            enableSong = configManager.getEnableSong();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public static void notifyFocusUser(HWND hWnd, String keyword, boolean endTurn) {
        if (focusApplication && !endTurn) {
            WindowUtils.focusApplication(hWnd);
        }

        if (notifyUser && !endTurn) {
            String characterName = WindowUtils.getWindowTitle(hWnd).split(keyword)[0];
            showTurnNotification(characterName, hWnd, NotificationService.TEXT_TO_DISPLAY);
        }

        if (notifyUserEndTurn && endTurn) {
            String characterName = WindowUtils.getWindowTitle(hWnd).split(keyword)[0];
            showTurnNotification(characterName, hWnd, NotificationService.TEXT_TO_DISPLAY_END_TIME);
        }

        if (enableSong) {
            wavPlayer.play();
        }
    }

    public static void showTurnNotification(String characterName, HWND hWnd, String text) {
        notificationExecutor.submit(() -> {
            Notify.Companion.create()
                    .title(TITLE)
                    .image(bImage)
                    .text(text + characterName)
                    .theme(Theme.Companion.getDefaultDark())
                    .position(position)
                    .hideAfter(DELAY_NOTIFICATION)
                    .onClickAction((notify) -> {
                        WindowUtils.focusApplication(hWnd);
                        return null;
                    })
                    .show();
        });
    }

    public static Position getPosition() {
        return position;
    }

    public static void setPosition(Position position) {
        NotificationService.position = position;
    }

    public static void toggleFocusApplication() {
        focusApplication = !focusApplication;
        configManager.setFocusApplication(focusApplication);
    }

    public static void toggleNotifyUser() {
        notifyUser = !notifyUser;
        configManager.setNotifyUser(notifyUser);
    }

    public static void toggleNotifyUserEndTurn() {
        notifyUserEndTurn = !notifyUserEndTurn;
        configManager.setNotifyUserEndTurn(notifyUserEndTurn);
    }

    public static void toggleEnableSong() {
        enableSong = !enableSong;
        configManager.setEnableSong(enableSong);
    }

    public static boolean isFocusApplication() {
        return focusApplication;
    }

    public static boolean isNotifyUser() {
        return notifyUser;
    }

    public static boolean isNotifyUserEndTurn() {
        return notifyUserEndTurn;
    }

    public static boolean isEnableSong() {
        return enableSong;
    }

    // Pour bien fermer l'appli proprement
    public static void shutdown() {
        notificationExecutor.shutdownNow();
    }
}
