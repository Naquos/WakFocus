package com.wakfocus.UI;

import com.wakfocus.models.PositionComboBox;
import com.wakfocus.services.ConfigManager;
import com.wakfocus.services.NotificationService;
import dorkbox.notify.Position;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Label;
import javafx.scene.control.ListCell;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.Region;

public class NotificationPositionDropdown {

    private static final ConfigManager configManager = new ConfigManager();

    public static HBox create() {
        HBox container = new HBox(10);
        container.setAlignment(Pos.CENTER_LEFT);
        container.setPadding(new Insets(0, 35, 30, 15));

        Label label = new Label("Position de la pop up de notification :");
        label.setStyle("-fx-font-size: 14px; -fx-text-fill: #FFFFFF;");

        ComboBox<PositionComboBox> comboBox = new ComboBox<>();
        comboBox.setMinSize(150, 30);
        comboBox.getItems().addAll(
                new PositionComboBox("En haut à gauche", Position.TOP_LEFT),
                new PositionComboBox("En haut", Position.TOP),
                new PositionComboBox("En haut à droite", Position.TOP_RIGHT),
                new PositionComboBox("Au centre", Position.CENTER),
                new PositionComboBox("En bas à gauche", Position.BOTTOM_LEFT),
                new PositionComboBox("En bas", Position.BOTTOM),
                new PositionComboBox("En bas à droite", Position.BOTTOM_RIGHT));

        comboBox.setCellFactory(cb -> new ListCell<>() {
            @Override
            protected void updateItem(PositionComboBox item, boolean empty) {
                super.updateItem(item, empty);
                setText(empty || item == null ? null : item.getLabel());
            }
        });

        comboBox.setButtonCell(new ListCell<>() {
            @Override
            protected void updateItem(PositionComboBox item, boolean empty) {
                super.updateItem(item, empty);
                setText(empty || item == null ? null : item.getLabel());
            }
        });

        comboBox.setOnAction(e -> {
            Position position = comboBox.getValue().getPosition();
            NotificationService.setPosition(position);
        });

        comboBox.setValue(comboBox.getItems().stream()
                .filter(x -> x.getPosition() == configManager.getNotificationPosition())
                .findFirst().orElse(null));

                
        Region spacer = new Region();
        HBox.setHgrow(spacer, Priority.ALWAYS);
        container.getChildren().addAll(label, spacer, comboBox);
        return container;
    }
}