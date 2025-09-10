package com.wakfocus.UI;

import com.wakfocus.models.ThemeComboBox;
import com.wakfocus.models.ThemeEnum;
import com.wakfocus.services.ConfigManager;
import com.wakfocus.services.NotificationService;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Label;
import javafx.scene.control.ListCell;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.Region;

public class ThemeDropdown {

    private static final ConfigManager configManager = new ConfigManager();

    public static HBox create() {
        HBox container = new HBox(10);
        container.setAlignment(Pos.CENTER_LEFT);
        container.setPadding(new Insets(0, 35, 30, 15));

        Label label = new Label("Thême à appliquer aux notifications :");
        label.setStyle("-fx-font-size: 14px; -fx-text-fill: #FFFFFF;");

        ComboBox<ThemeComboBox> comboBox = new ComboBox<>();
        comboBox.setMinSize(150, 30);
        comboBox.getItems().addAll(
                new ThemeComboBox("Dark thême", ThemeEnum.DARK_THEME),
                new ThemeComboBox("Light thême", ThemeEnum.LIGHT_THEME));

        comboBox.setCellFactory(cb -> new ListCell<>() {
            @Override
            protected void updateItem(ThemeComboBox item, boolean empty) {
                super.updateItem(item, empty);
                setText(empty || item == null ? null : item.getLabel());
            }
        });

        comboBox.setButtonCell(new ListCell<>() {
            @Override
            protected void updateItem(ThemeComboBox item, boolean empty) {
                super.updateItem(item, empty);
                setText(empty || item == null ? null : item.getLabel());
            }
        });

        comboBox.setOnAction(e -> {
            ThemeEnum theme = comboBox.getValue().getTheme();
            NotificationService.setTheme(theme);
        });

        comboBox.setValue(comboBox.getItems().stream()
                .filter(x -> x.getTheme() == configManager.getTheme())
                .findFirst().orElse(null));

                
        Region spacer = new Region();
        HBox.setHgrow(spacer, Priority.ALWAYS);
        container.getChildren().addAll(label, spacer, comboBox);
        return container;
    }
}