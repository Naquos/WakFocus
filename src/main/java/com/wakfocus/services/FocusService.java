package com.wakfocus.services;

import com.sun.jna.platform.win32.WinDef.HWND;
import com.sun.jna.platform.win32.WinDef.RECT;
import com.wakfocus.models.TurnDescriptions;
import com.wakfocus.utils.WindowUtils;

import java.io.IOException;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.concurrent.Executors;

public class FocusService {

    private static final String WAKFU = "- WAKFU";
    private static final int THREAD_SLEEP_MILLISECONDS = 200;

    private static boolean RUNNING = true;
    private static boolean focusApplication = false;
    private static boolean notifyUser = true;

    private static ConfigManager configManager = new ConfigManager();
    private static Map<HWND, TurnDescriptions> wakfuYourTurnMap = new HashMap<>();

    public static void run() throws InterruptedException, IOException {
        init();

        Executors.newSingleThreadExecutor().submit(() -> {
            while (RUNNING) {
                try {
                    WindowUtils.findNewWakfuInstance(WAKFU, wakfuYourTurnMap);
                    checkIfItsYourTurn();
                    Thread.sleep(THREAD_SLEEP_MILLISECONDS);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }
        });
    }

    private static void init() throws IOException {
        focusApplication = configManager.getFocusApplication();
        notifyUser = configManager.getNotifyUser();
    }

    private static void checkIfItsYourTurn() {
        boolean anyWindowHasTurn = wakfuYourTurnMap.values().stream().anyMatch(TurnDescriptions::isPlayerTurn);

        Iterator<Map.Entry<HWND, TurnDescriptions>> iterator = wakfuYourTurnMap.entrySet().iterator();
        while (iterator.hasNext()) {
            Map.Entry<HWND, TurnDescriptions> entry = iterator.next();
            TurnDescriptions turnDesc = entry.getValue();
            HWND hWnd = entry.getKey();
            boolean isPlayerTurn = turnDesc.isPlayerTurn();

            RECT rect = new RECT();
            if (!WindowUtils.isValid(hWnd, rect)) {
                iterator.remove(); // Fenêtre disparue
                continue;
            }

            String characterName = turnDesc.getCharacterName();
            String windowTitle = WindowUtils.getWindowTitle(hWnd);

            if (!windowTitle.equals(characterName)) {
                turnDesc.setCharacterName(windowTitle);
                turnDesc.setPlayerTurn(true);
                NotificationService.notifyFocusUser(hWnd, focusApplication, notifyUser, WAKFU);
                anyWindowHasTurn = true;

            } else if (ScreenCaptureService.isInFight(hWnd, rect)) {
                boolean result = ScreenCaptureService.checkTimelineButtonPass(hWnd, rect);
                if (result && !isPlayerTurn && !anyWindowHasTurn) {
                    turnDesc.setCharacterName(windowTitle);
                    turnDesc.setPlayerTurn(true);
                    NotificationService.notifyFocusUser(hWnd, focusApplication, notifyUser, WAKFU);
                    anyWindowHasTurn = true;

                } else if (!result && isPlayerTurn) {
                    turnDesc.setCharacterName(windowTitle);
                    turnDesc.setPlayerTurn(false);
                }
            }
        }
    }

    public static void stopRunning() {
        RUNNING = false;
    }

    public static void toggleFocusApplication() {
        focusApplication = !focusApplication;
        configManager.setFocusApplication(focusApplication);
    }

    public static void toggleNotifyUser() {
        notifyUser = !notifyUser;
        configManager.setNotifyUser(notifyUser);
    }

    public static boolean isFocusApplication() {
        return focusApplication;
    }

    public static boolean isNotifyUser() {
        return notifyUser;
    }
}
