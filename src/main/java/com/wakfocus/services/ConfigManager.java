package com.wakfocus.services;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.Properties;

public class ConfigManager {
    private static final String CONFIG_FILE = "config.properties";
    private Properties properties;

    public ConfigManager() {
        properties = new Properties();
        loadOrCreateConfig();
    }

    private void loadOrCreateConfig() {
        File file = new File(CONFIG_FILE);
        if (!file.exists()) {
            // Si le fichier n'existe pas, on le crée avec des valeurs par défaut
            properties.setProperty("focusApplication", "true");
            properties.setProperty("notifyUser", "false");
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
        return Boolean.parseBoolean(properties.getProperty("focusApplication"));
    }

    public boolean getNotifyUser() {
        return Boolean.parseBoolean(properties.getProperty("notifyUser"));
    }

    public void setFocusApplication(boolean value) {
        properties.setProperty("focusApplication", String.valueOf(value));
        saveConfig();
    }

    public void setNotifyUser(boolean value) {
        properties.setProperty("notifyUser", String.valueOf(value));
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
