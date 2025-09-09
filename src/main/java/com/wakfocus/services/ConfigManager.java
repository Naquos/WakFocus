package com.wakfocus.services;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.Properties;

import dorkbox.notify.Position;

public class ConfigManager {
    private static final String CONFIG_FILE = System.getProperty("user.home")
            + File.separator + ".wakfocus"
            + File.separator + "config.properties";
    private Properties properties;
    private static String focusApplication = "focusApplication";
    private static String notifyUser = "notifyUser";
    private static String notifyUserEndTurn = "notifyUserEndTurn";
    private static String notificationPosition = "notificationPosition";
    private static String enableSong = "enableSong";

    public ConfigManager() {
        properties = new Properties();
        loadOrCreateConfig();
    }

    private void loadOrCreateConfig() {
        File file = new File(CONFIG_FILE);
        File parentDir = file.getParentFile();
        if (!parentDir.exists()) {
            parentDir.mkdirs(); // crée le dossier .wakfocus dans le home
        }
        if (!file.exists()) {
            properties.setProperty(focusApplication, "true");
            properties.setProperty(notifyUser, "false");
            properties.setProperty(notifyUserEndTurn, "false");
            properties.setProperty(enableSong, "false");
            properties.setProperty(notificationPosition, Position.BOTTOM_RIGHT.toString());
            saveConfig();
        } else {
            try (FileInputStream fis = new FileInputStream(file)) {
                properties.load(fis);
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    public boolean getFocusApplication() {
        return Boolean.parseBoolean(properties.getProperty(focusApplication));
    }

    public boolean getNotifyUser() {
        return Boolean.parseBoolean(properties.getProperty(notifyUser));
    }

    public boolean getNotifyUserEndTurn() {
        return Boolean.parseBoolean(properties.getProperty(notifyUserEndTurn));
    }

    public boolean getEnableSong() {
        return Boolean.parseBoolean(properties.getProperty(enableSong));
    }

    public Position getNotificationPosition() {
        String result = properties.getProperty(notificationPosition);
        if (result == null) {
            return Position.BOTTOM_RIGHT;
        }
        return Position.valueOf(properties.getProperty(notificationPosition));
    }

    public void setFocusApplication(boolean value) {
        properties.setProperty(focusApplication, String.valueOf(value));
        saveConfig();
    }

    public void setNotifyUser(boolean value) {
        properties.setProperty(notifyUser, String.valueOf(value));
        saveConfig();
    }

    public void setNotifyUserEndTurn(boolean value) {
        properties.setProperty(notifyUserEndTurn, String.valueOf(value));
        saveConfig();
    }

    public void setEnableSong(boolean value) {
        properties.setProperty(enableSong, String.valueOf(value));
        saveConfig();
    }

    public void setNotificationPosition(Position value) {
        properties.setProperty(notificationPosition, String.valueOf(value));
        saveConfig();
    }

    private void saveConfig() {
        try (FileOutputStream fos = new FileOutputStream(CONFIG_FILE)) {
            properties.store(fos, "Configuration file");
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

}
