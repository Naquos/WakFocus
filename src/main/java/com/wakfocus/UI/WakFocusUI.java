package com.wakfocus.UI;

import javafx.application.Application;
import javafx.scene.Scene;
import javafx.scene.image.Image;
import javafx.stage.Stage;
import javafx.stage.StageStyle;

public class WakFocusUI extends Application {

    private static final double WIDTH = 500;
    private static final double HEIGHT = 600;

    @Override
    public void start(Stage stage) {
        // Icône fenêtre
        Image logo = new Image(getClass().getResource("/images/logo.png").toExternalForm());
        stage.getIcons().add(logo);

        // Layout principal
        MainView mainView = new MainView(stage);

        // Scène
        Scene scene = new Scene(mainView.getRoot(), WIDTH, HEIGHT);
        scene.getStylesheets().add(getClass().getResource("/styles/style.css").toExternalForm());
        scene.setFill(null);

        // Stage sans bordures
        stage.initStyle(StageStyle.TRANSPARENT);
        stage.setScene(scene);
        stage.show();
    }

    public static void launchUI(String[] args) {
        launch(args);
    }
}