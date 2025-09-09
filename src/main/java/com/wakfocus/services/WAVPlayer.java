package com.wakfocus.services;

import java.io.BufferedInputStream;
import java.io.InputStream;

import javax.sound.sampled.AudioInputStream;
import javax.sound.sampled.AudioSystem;
import javax.sound.sampled.Clip;

public class WAVPlayer {

    private Clip clip;

    public WAVPlayer() {
        try {
            InputStream audioSrc = WAVPlayer.class.getResourceAsStream("/oplata.wav");
            InputStream bufferedIn = new BufferedInputStream(audioSrc);
            AudioInputStream audioIn = AudioSystem.getAudioInputStream(bufferedIn);
            this.clip = AudioSystem.getClip();
            clip.open(audioIn);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void play() {
        if (clip == null)
            return;

        // Si le clip est déjà en train de jouer, on le stoppe et on le remet au début
        if (clip.isRunning()) {
            clip.stop();
        }
        clip.setFramePosition(0);

        // Joue le son dans un thread séparé pour ne pas bloquer
        new Thread(() -> clip.start()).start();
    }

    /**
     * Stoppe le son si nécessaire.
     */
    public void stop() {
        if (clip != null && clip.isRunning()) {
            clip.stop();
        }
    }

}
