package com.wakfocus.UI;

import com.wakfocus.services.FocusService;
import com.wakfocus.services.NotificationService;
import com.wakfocus.services.WAVPlayer;

import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.layout.HBox;
import javafx.scene.layout.StackPane;
import javafx.stage.Stage;

public class MinimizeCloseButtons {

    public static HBox create(Stage stage) {
        StackPane minimizeBtn = CustomButton.create("_", () -> stage.setIconified(true));

        StackPane closeBtn = CustomButton.create("X", () -> {
            FocusService.stopRunning();
            NotificationService.shutdown();
            WAVPlayer.stop();
            stage.close();
        });

        HBox buttonsBox = new HBox(5, minimizeBtn, closeBtn);
        buttonsBox.setAlignment(Pos.CENTER_RIGHT);
        buttonsBox.setPadding(new Insets(0, 10, 0, 0));
        return buttonsBox;
    }
}