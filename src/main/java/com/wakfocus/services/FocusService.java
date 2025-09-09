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
    private static boolean notifyUserEndTurn = true;

    private static ConfigManager configManager = new ConfigManager();
    private static Map<HWND, TurnDescriptions> wakfuYourTurnMap = new HashMap<>();
    private static final boolean DEBUG_MODE = false;

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

            if (ScreenCaptureService.isInFight(hWnd, rect)) {
                String characterName = turnDesc.getCharacterName();
                String windowTitle = WindowUtils.getWindowTitle(hWnd);
                if (!windowTitle.equals(characterName)) {
                    turnDesc.setCharacterName(windowTitle);
                    turnDesc.setPlayerTurn(true);
                    turnDesc.setEndTurn(false);
                    NotificationService.notifyFocusUser(hWnd, focusApplication, notifyUser, notifyUserEndTurn, WAKFU, false);
                    anyWindowHasTurn = true;
                    continue;

                }
                boolean result = ScreenCaptureService.checkTimelineButtonPass(hWnd, rect);
                if (result && !isPlayerTurn && !anyWindowHasTurn) {
                    turnDesc.setCharacterName(windowTitle);
                    turnDesc.setPlayerTurn(true);
                    turnDesc.setEndTurn(false);

                    NotificationService.notifyFocusUser(hWnd, focusApplication, notifyUser, notifyUserEndTurn, WAKFU, false);
                    anyWindowHasTurn = true;
                    if (DEBUG_MODE) {
                        ScreenCaptureService.checkTimelineButtonPass(hWnd, rect, true);
                    }
                    continue;

                }
                if (isPlayerTurn) {
                    boolean resultTimelineAlliesOpponent = ScreenCaptureService
                            .checkTimelineButtonPassAlliesAndOpponent(hWnd, rect);
                    if (resultTimelineAlliesOpponent) {
                        turnDesc.setCharacterName(windowTitle);
                        turnDesc.setPlayerTurn(false);
                        turnDesc.setEndTurn(false);
                    } else if (!result && !resultTimelineAlliesOpponent && !turnDesc.isEndTurn()) {
                        turnDesc.setEndTurn(true);
                        NotificationService.notifyFocusUser(hWnd, focusApplication, notifyUser, notifyUserEndTurn, WAKFU, true);
                    }
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

    public static void toggleNotifyUserEndTurn() {
        notifyUserEndTurn = !notifyUserEndTurn;
        configManager.setNotifyUserEndTurn(notifyUserEndTurn);
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
}
