package com.wakfocus.UI;

import java.awt.Desktop;
import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;

import com.wakfocus.services.NotificationService;

import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.Hyperlink;
import javafx.scene.control.Label;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;

public class ContentView {

    private final VBox container;

    private static final double WIDTH = 500;

    public ContentView(Stage stage) {
        container = new VBox();
        container.setPadding(new Insets(0, 0, 0, WIDTH * 0.05));

        // Titre
        Label title = new Label("Paramètres");
        title.setStyle("-fx-font-size: 24px; -fx-text-fill: #FFFFFF; -fx-font-weight: bold;");
        title.setPadding(new Insets(5, 0, 20, 10));

        // Checkboxes
        HBox focus = CustomCheckbox.create("Activer le focus automatique sur Wakfu",
                NotificationService.isFocusApplication(),
                NotificationService::toggleFocusApplication);

        HBox notifications = CustomCheckbox.create("Activer les notifications de début de tour",
                NotificationService.isNotifyUser(),
                NotificationService::toggleNotifyUser);

        HBox notificationsEndTurn = CustomCheckbox.create("Activer les notifications de fin de tour",
                NotificationService.isNotifyUserEndTurn(),
                NotificationService::toggleNotifyUserEndTurn);

        HBox enableSong = CustomCheckbox.create("Activer le son pour les notifications",
                NotificationService.isEnableSong(),
                NotificationService::toggleEnableSong);

        // Dropdown
        HBox dropDownNotificationPosition = NotificationPositionDropdown.create();

        HBox dropDownTheme = ThemeDropdown.create();

        // Choix du son
        HBox chooseSoundBtn = SoundChooser.create(stage);

        // Gearfu
        ImageView gearfuLogo = new ImageView(
                new Image(getClass().getResource("/images/logo_gearfu.png").toExternalForm()));

        gearfuLogo.setFitHeight(32);
        gearfuLogo.setPreserveRatio(true);

        Hyperlink gearfuLink = new Hyperlink("Gearfu - Trieur d'items");
        gearfuLink.setOnAction(e -> {
            new Thread(() -> {
                try {
                    Desktop.getDesktop().browse(new URI("https://naquos.github.io/Gearfu"));
                } catch (IOException | URISyntaxException ex) {
                    ex.printStackTrace();
                }
            }).start();
        });

        HBox gearfuContainer = new HBox(10, gearfuLogo, gearfuLink);
        gearfuContainer.setPadding(new Insets(5, 0, 20, 10));

        // Footer
        Label footer = new Label("WAKFU est un MMORPG édité par Ankama.");
        footer.setStyle("-fx-font-size: 12px; -fx-text-fill: #FFFFFF;");
        footer.setAlignment(Pos.BOTTOM_CENTER);

        Label footer2 = new Label("WakFocus est une application non officielle sans aucun lien avec Ankama.");
        footer2.setStyle("-fx-font-size: 12px; -fx-text-fill: #FFFFFF;");
        footer2.setAlignment(Pos.BOTTOM_CENTER);

        VBox footerBox = new VBox(0);
        footerBox.setAlignment(Pos.CENTER);
        footerBox.getChildren().addAll(footer, footer2);

        container.getChildren().addAll(title, focus, notifications, notificationsEndTurn, enableSong,
                dropDownNotificationPosition, dropDownTheme, chooseSoundBtn, gearfuContainer, footerBox);
    }

    public VBox getNode() {
        return container;
    }
}
