package com.wakfocus.services;

import java.io.BufferedInputStream;
import java.io.File;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;

import javax.sound.sampled.AudioInputStream;
import javax.sound.sampled.AudioSystem;
import javax.sound.sampled.Clip;

public class WAVPlayer {

    private static Clip clip;
    private static final String APP_FOLDER = ".wakfocus";
    private static final String SOUND_FOLDER = "sounds";
    private static final String CUSTOM_SOUND_NAME = "custom_notification.wav";

    public WAVPlayer() {
        loadClip();
    }

    /**
     * Charge soit le son custom, soit le son par défaut (oplata.wav du JAR).
     */
    private void loadClip() {
        try {
            // Vérifie si un son custom existe
            Path customPath = getCustomSoundPath();
            if (Files.exists(customPath)) {
                clip = loadFromFile(customPath.toFile());
            } else {
                clip = loadFromResource("/sounds/oplata.wav");
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private Clip loadFromResource(String resourcePath) throws Exception {
        try (InputStream audioSrc = WAVPlayer.class.getResourceAsStream(resourcePath);
             InputStream bufferedIn = new BufferedInputStream(audioSrc)) {
            AudioInputStream audioIn = AudioSystem.getAudioInputStream(bufferedIn);
            Clip newClip = AudioSystem.getClip();
            newClip.open(audioIn);
            return newClip;
        }
    }

    private Clip loadFromFile(File file) throws Exception {
        AudioInputStream audioIn = AudioSystem.getAudioInputStream(file);
        Clip newClip = AudioSystem.getClip();
        newClip.open(audioIn);
        return newClip;
    }

    public void play() {
        if (clip == null) return;

        if (clip.isRunning()) {
            clip.stop();
        }
        clip.setFramePosition(0);

        new Thread(() -> clip.start()).start();
    }

    public static void stop() {
        if (clip != null && clip.isRunning()) {
            clip.stop();
        }
    }

    public static Path getCustomSoundPath() {
        String userHome = System.getProperty("user.home");
        return Path.of(userHome, APP_FOLDER, SOUND_FOLDER, CUSTOM_SOUND_NAME);
    }
}