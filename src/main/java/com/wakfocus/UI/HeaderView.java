package com.wakfocus.UI;

import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.Label;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.HBox;
import javafx.scene.layout.StackPane;
import javafx.stage.Stage;

public class HeaderView {

    private final StackPane header;
    private double xOffset = 0;
    private double yOffset = 0;

    private static final double HEADER_HEIGHT = 48;

    public HeaderView(Stage stage, StackPane root) {
        header = new StackPane();

        // Image de fond
        ImageView headerBg = new ImageView(
                new Image(getClass().getResource("/images/HeaderSmall.png").toExternalForm()));
        headerBg.setFitHeight(HEADER_HEIGHT);
        headerBg.setPreserveRatio(false);
        headerBg.fitWidthProperty().bind(root.widthProperty());

        // Logo
        ImageView logo = new ImageView(
                new Image(getClass().getResource("/images/logo.png").toExternalForm()));
        logo.setPreserveRatio(true);
        logo.setFitHeight(32);

        HBox logoContainer = new HBox(logo);
        logoContainer.setAlignment(Pos.CENTER_LEFT);
        logoContainer.setPadding(new Insets(0, 0, 0, 10));

        // Boutons fenêtre
        HBox buttonsBox = MinimizeCloseButtons.create(stage);

        // Drag window
        header.setOnMousePressed((MouseEvent event) -> {
            xOffset = event.getSceneX();
            yOffset = event.getSceneY();
        });
        header.setOnMouseDragged((MouseEvent event) -> {
            stage.setX(event.getScreenX() - xOffset);
            stage.setY(event.getScreenY() - yOffset);
        });

        // Titre
        Label title = new Label("WakFocus");
        title.setStyle("-fx-font-size: 30px; -fx-text-fill: #E6BD8B; -fx-font-weight: bold;");

        header.getChildren().addAll(headerBg, logoContainer, title, buttonsBox);
        header.setPadding(new Insets(0, 0, 10, 0));
    }

    public StackPane getNode() {
        return header;
    }
}
