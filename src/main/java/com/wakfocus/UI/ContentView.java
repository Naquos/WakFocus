package com.wakfocus.UI;

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

import java.time.LocalDate;


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
                ImageView logo = new ImageView(
                                new Image(getClass().getResource("/images/logo_gearfu.png").toExternalForm()));
                logo.setFitHeight(32);
                logo.setPreserveRatio(true);

                Hyperlink gearfuLink = new Hyperlink("Gearfu - Trieur d'items");
                gearfuLink.setOnAction(e -> getClass().getResource("https://naquos.github.io/Gearfu"));

                HBox gearfuContainer = new HBox(10, logo, gearfuLink);
                gearfuContainer.setPadding(new Insets(5, 0, 20, 10));

                // Footer
                Label footer = new Label("WAKFU MMORPG : © 2012-" + LocalDate.now().getYear()
                                + " Ankama Studio. Tous droits réservés");
                footer.setStyle("-fx-font-size: 12px; -fx-text-fill: #FFFFFF;");
                footer.setAlignment(Pos.BOTTOM_CENTER);
                footer.setPadding(new Insets(0, 0, 0, 30));

                container.getChildren().addAll(title, focus, notifications, notificationsEndTurn, enableSong,
                                dropDownNotificationPosition, dropDownTheme, chooseSoundBtn, gearfuContainer, footer);
        }

        public VBox getNode() {
                return container;
        }
}