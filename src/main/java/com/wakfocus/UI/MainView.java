package com.wakfocus.UI;

import javafx.geometry.Insets;
import javafx.scene.image.ImageView;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;

public class MainView {

    private final StackPane root;

    public MainView(Stage stage) {
        root = new StackPane();
        root.setStyle("-fx-background-radius: 20px; -fx-background-color: transparent;");

        // Fond
        ImageView background = BackgroundView.create(root);

        // Conteneur principal
        VBox container = new VBox();
        container.setStyle("-fx-background-radius: 400px; -fx-z-index: -1;");
        container.setPadding(new Insets(0, 0, 0, 0));

        // Header + Content
        HeaderView header = new HeaderView(stage, root);
        ContentView content = new ContentView(stage);

        container.getChildren().addAll(header.getNode(), content.getNode());
        root.getChildren().addAll(background, container);
    }

    public StackPane getRoot() {
        return root;
    }
}