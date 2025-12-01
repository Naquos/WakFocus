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

            boolean isInFight = ScreenCaptureService.isInFight(hWnd, rect);

            if (isInFight) {
                String characterName = turnDesc.getCharacterName();
                String windowTitle = WindowUtils.getWindowTitle(hWnd);
                if (!windowTitle.equals(characterName)) {
                    setTurnDesc(turnDesc, windowTitle, true);
                    NotificationService.notifyFocusUser(hWnd, WAKFU, false);
                    anyWindowHasTurn = true;
                    continue;

                }
                boolean result = ScreenCaptureService.checkTimelineButtonPass(hWnd, rect);
                if (result && !isPlayerTurn && !anyWindowHasTurn) {
                    setTurnDesc(turnDesc, windowTitle, true);

                    NotificationService.notifyFocusUser(hWnd, WAKFU, false);
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
                        setTurnDesc(turnDesc, windowTitle, false);
                    } else if (!result && !resultTimelineAlliesOpponent && !turnDesc.isEndTurn()) {
                        turnDesc.setEndTurn(true);
                        NotificationService.notifyFocusUser(hWnd, WAKFU, true);
                    }
                }
            } else {
                setTurnDesc(turnDesc, WAKFU, false);
            }
        }
    }

    private static void setTurnDesc(TurnDescriptions turnDesc, String windowTitle, boolean isPlayerTurn) {
        turnDesc.setCharacterName(windowTitle);
        turnDesc.setPlayerTurn(isPlayerTurn);
        turnDesc.setEndTurn(false);
    }

    public static void stopRunning() {
        RUNNING = false;
    }
}
