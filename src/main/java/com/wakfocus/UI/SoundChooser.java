package com.wakfocus.UI;

import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.Region;
import javafx.stage.FileChooser;
import javafx.stage.Stage;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

import com.wakfocus.services.WAVPlayer;

public class SoundChooser {

    public static File chooseSound(Stage stage) {
        FileChooser fileChooser = new FileChooser();
        fileChooser.setTitle("Choisir un son de notification");

        // Filtres pour limiter aux formats audio courants
        fileChooser.getExtensionFilters().addAll(
                new FileChooser.ExtensionFilter("Fichiers audio (*.wav)", "*.wav"));

        // Ouvre la boîte de dialogue
        return fileChooser.showOpenDialog(stage);
    }

    public static HBox create(Stage stage) {
        HBox container = new HBox(10);
        container.setAlignment(Pos.CENTER_LEFT);
        container.setPadding(new Insets(0, 35, 30, 15));

        Label label = new Label("Changer le son de la notification (*.wav)");
        label.setStyle("-fx-font-size: 14px; -fx-text-fill: #FFFFFF;");

        Button chooseSoundBtn = new Button("Changer");
        chooseSoundBtn.setMinSize(150, 30);
        chooseSoundBtn.setOnAction(e -> {
            File file = SoundChooser.chooseSound(stage);
            if (file != null) {
                Path target = WAVPlayer.getCustomSoundPath();
                try {
                    Files.createDirectories(target.getParent());
                    Files.copy(file.toPath(), target, java.nio.file.StandardCopyOption.REPLACE_EXISTING);
                } catch (IOException e1) {
                    e1.printStackTrace();
                }
            }
        });
        
        Region spacer = new Region();
        HBox.setHgrow(spacer, Priority.ALWAYS);
        container.getChildren().addAll(label, spacer, chooseSoundBtn);

        return container;
    }
}