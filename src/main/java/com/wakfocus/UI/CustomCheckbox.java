package com.wakfocus.UI;

import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.Label;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.HBox;

public class CustomCheckbox {

    public static HBox create(String text, boolean defaultValue, Runnable action) {
        HBox checkboxContainer = new HBox(10);
        checkboxContainer.setAlignment(Pos.CENTER_LEFT);
        checkboxContainer.setPadding(new Insets(0, 0, 30, 15));

        Image uncheckedImage = new Image(
                CustomCheckbox.class.getResource("/images/checkbox_unchecked.png").toExternalForm());
        Image checkedImage = new Image(
                CustomCheckbox.class.getResource("/images/checkbox_checked.png").toExternalForm());

        String uncheckedStyle = "-fx-font-size: 14px; -fx-text-fill: #FFFFFF;";
        String checkedStyle = "-fx-font-size: 14px; -fx-text-fill: #E6BD8B;";

        ImageView checkboxImageView = new ImageView(defaultValue ? checkedImage : uncheckedImage);
        checkboxImageView.setFitWidth(20);
        checkboxImageView.setFitHeight(20);

        Label label = new Label(text);
        label.setStyle(defaultValue ? checkedStyle : uncheckedStyle);

        checkboxContainer.setOnMouseEntered(e -> checkboxContainer.setStyle("-fx-cursor: hand;"));
        checkboxContainer.setOnMouseExited(e -> checkboxContainer.setStyle("-fx-cursor: default;"));

        checkboxContainer.setOnMouseClicked(e -> {
            action.run();
            if (checkboxImageView.getImage().equals(uncheckedImage)) {
                checkboxImageView.setImage(checkedImage);
                label.setStyle(checkedStyle);
            } else {
                checkboxImageView.setImage(uncheckedImage);
                label.setStyle(uncheckedStyle);
            }
        });

        checkboxContainer.getChildren().addAll(checkboxImageView, label);
        return checkboxContainer;
    }
}