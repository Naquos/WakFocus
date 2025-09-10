package com.wakfocus.UI;

import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.StackPane;

public class BackgroundView {

    public static ImageView create(StackPane root) {
        ImageView bgView = new ImageView(
                new Image(BackgroundView.class.getResource("/images/NP-window-1.png").toExternalForm()));
        bgView.setPreserveRatio(false);

        bgView.fitWidthProperty().bind(root.widthProperty());
        bgView.fitHeightProperty().bind(root.heightProperty());
        bgView.setStyle("-fx-background-color: transparent; -fx-background-radius: 20;");
        return bgView;
    }
}