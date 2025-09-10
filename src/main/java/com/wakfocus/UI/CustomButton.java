package com.wakfocus.UI;

import javafx.scene.control.Label;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.StackPane;

public class CustomButton {

    public static StackPane create(String text, Runnable onClick) {
        StackPane button = new StackPane();
        button.setPrefSize(30, 30);

        Image defaultImage = new Image(CustomButton.class.getResource("/images/NP-minus2.png").toExternalForm());
        Image pressedImage = new Image(CustomButton.class.getResource("/images/NP-minus1.png").toExternalForm());

        String defaultStyleLabel = "-fx-text-fill: #E6BD8B; -fx-font-size: 16; -fx-font-weight: bold;";
        String pressedStyleLabel = "-fx-text-fill: white; -fx-font-size: 16; -fx-font-weight: bold;";

        ImageView bgImage = new ImageView(defaultImage);
        bgImage.setFitWidth(30);
        bgImage.setFitHeight(30);

        Label label = new Label(text);
        label.setStyle(defaultStyleLabel);

        button.setOnMouseEntered(e -> {
            button.setStyle("-fx-cursor: hand;");
            bgImage.setOpacity(0.7);
        });
        button.setOnMouseExited(e -> {
            button.setStyle("-fx-cursor: default;");
            bgImage.setOpacity(1.0);
        });

        button.setOnMousePressed(e -> {
            bgImage.setImage(pressedImage);
            label.setStyle(pressedStyleLabel);
        });
        button.setOnMouseReleased(e -> {
            bgImage.setImage(defaultImage);
            label.setStyle(defaultStyleLabel);
        });

        button.setOnMouseClicked(e -> onClick.run());

        button.getChildren().addAll(bgImage, label);
        return button;
    }
}