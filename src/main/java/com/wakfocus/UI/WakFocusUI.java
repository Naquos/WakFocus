package com.wakfocus.UI;

import java.time.LocalDate;

import com.wakfocus.services.FocusService;
import com.wakfocus.services.NotificationService;

import javafx.application.Application;
import javafx.stage.Stage;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.Hyperlink;
import javafx.scene.control.Label;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.Background;
import javafx.scene.layout.BackgroundImage;
import javafx.scene.layout.BackgroundPosition;
import javafx.scene.layout.BackgroundRepeat;
import javafx.scene.layout.BackgroundSize;
import javafx.scene.layout.HBox;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;
import javafx.stage.StageStyle;

public class WakFocusUI extends Application {

    private double xOffset = 0;
    private double yOffset = 0;

    private static final double WIDTH = 500;
    private static final double HEIGHT = 410;
    private static final double HEADER_HEIGHT = 48;

    private StackPane createStackPaneButton(String text) {
        StackPane button = new StackPane();
        button.setPrefSize(30, 30); // taille du bouton

        Image defaultImage = new Image(getClass().getResource("/images/NP-minus2.png").toExternalForm());
        Image pressedImage = new Image(getClass().getResource("/images/NP-minus1.png").toExternalForm());

        String defaultStyleLabel = "-fx-text-fill: #E6BD8B; -fx-font-size: 16; -fx-font-weight: bold;";
        String pressedStyleLabel = "-fx-text-fill: white; -fx-font-size: 16; -fx-font-weight: bold;";

        // Image de fond
        ImageView bgImage = new ImageView(defaultImage);
        bgImage.setFitWidth(30);
        bgImage.setFitHeight(30);
        bgImage.setPreserveRatio(false);

        // Label "_"
        Label label = new Label(text);
        label.setStyle(defaultStyleLabel);

        // ===== Curseur main (hand) =====
        button.setOnMouseEntered(e -> button.setStyle("-fx-cursor: hand;"));
        button.setOnMouseExited(e -> button.setStyle("-fx-cursor: default;"));

        // ===== Effet hover =====
        button.setOnMouseEntered(e -> {
            bgImage.setOpacity(0.7); // par ex. foncé légèrement
        });
        button.setOnMouseExited(e -> {
            bgImage.setOpacity(1.0); // retour à normal
        });

        // ===== Effet noircissement =====
        button.setOnMousePressed(e -> {
            bgImage.setImage(pressedImage);
            label.setStyle(pressedStyleLabel);
        });
        bgImage.setOnMouseReleased(e -> {
            bgImage.setImage(defaultImage);
            label.setStyle(defaultStyleLabel);

        });

        // Ajouter image + label dans le StackPane
        button.getChildren().addAll(bgImage, label);
        return button;
    }

    @Override
    public void start(Stage stage) {
        Image logo = new Image(getClass().getResource("/images/logo.png").toExternalForm());
        stage.getIcons().add(logo);

        StackPane root = new StackPane();
        root.setStyle("-fx-background-radius: 20px; -fx-background-color: transparent;");

        // ===== BACKGROUND IMAGE =====

        // Image de fond
        ImageView bgView = createBackground(root);

        // ===== Conteneur principal avec coins arrondis =====
        VBox container = new VBox();
        container.setStyle(
                "-fx-background-radius: 200px; -fx-z-index: -1;" // coins arrondis
        );

        // ===== HEADER PERSONNALISÉ =====
        StackPane header = createHeader(stage, root);
        header.setPadding(new Insets(0, 0, HEADER_HEIGHT + 10, 0));

        // ===== CONTENU =====
        VBox content = createContent();

        container.getChildren().addAll(header, content);

        root.getChildren().addAll(bgView, container);

        // ===== SCENE =====
        Scene scene = new Scene(root, WIDTH, HEIGHT);
        scene.setFill(null);

        // Fenêtre sans bordures natives
        stage.initStyle(StageStyle.TRANSPARENT);
        stage.setScene(scene);
        stage.show();
    }

    private VBox createContent() {
        VBox container = new VBox();

        container.setPadding(new Insets(0, 0, 0, WIDTH * 0.05));

        // ===== BACKGROUND IMAGE =====
        Image bgImage = new Image(getClass().getResource("/images/NP-window-1.png").toExternalForm());
        BackgroundSize bgSize = new BackgroundSize(WIDTH * 0.9, HEIGHT * 0.9, false, false, false, false);
        BackgroundImage bg = new BackgroundImage(bgImage,
                BackgroundRepeat.NO_REPEAT,
                BackgroundRepeat.NO_REPEAT,
                BackgroundPosition.CENTER,
                bgSize);
        container.setBackground(new Background(bg));

        // ===== CHECKBOX =====
        Label title = new Label("Paramètres");
        title.setStyle("-fx-font-size: 24px; -fx-text-fill: #FFFFFF; -fx-font-weight: bold;");
        title.setPadding(new Insets(5, 0, 20, 10));

        HBox focus = createCheckbox("Activer le focus automatique sur Wakfu", FocusService.isFocusApplication(),
                () -> FocusService.toggleFocusApplication());
        HBox notifications = createCheckbox("Activer les notifications de début de tour",
                FocusService.isNotifyUser(), () -> FocusService.toggleNotifyUser());
        HBox notificationsEndTurn = createCheckbox("Activer les notifications de fin de tour",
                FocusService.isNotifyUserEndTurn(), () -> FocusService.toggleNotifyUserEndTurn());

        // ===== GEARFU =====
        ImageView logo = new ImageView(
                new Image(getClass().getResource("/images/logo_gearfu.png").toExternalForm()));
        logo.setPreserveRatio(true);
        logo.setFitHeight(32);
        logo.setFitWidth(32);
        Hyperlink gearfuLink = new Hyperlink("Gearfu - Trieur d'items");
        gearfuLink.setOnAction(e -> {
            // Ouvrir le lien Gearfu
            getHostServices().showDocument("https://naquos.github.io/Gearfu");
        });

        HBox gearfuContainer = new HBox(10, logo, gearfuLink);
        gearfuContainer.setPadding(new Insets(5, 0, 20, 10));

        // ===== FOOTER =====
        Label footer = new Label(
                "WAKFU MMORPG : © 2012-" + LocalDate.now().getYear() + " Ankama Studio. Tous droits réservés");
        footer.setStyle("-fx-font-size: 12px; -fx-text-fill: #FFFFFF;");
        footer.setAlignment(Pos.BOTTOM_CENTER);
        footer.setPadding(new Insets(0, 0, 0, 30));

        container.getChildren().addAll(title, focus, notifications,notificationsEndTurn, gearfuContainer, footer);
        return container;
    }

    private HBox createCheckbox(String text, boolean defaultValue, Runnable action) {
        HBox checkboxContainer = new HBox();
        checkboxContainer.setSpacing(10);
        checkboxContainer.setAlignment(Pos.CENTER_LEFT);
        checkboxContainer.setPadding(new Insets(0, 0, 30, 15));

        Image uncheckedImage = new Image(getClass().getResource("/images/checkbox_unchecked.png").toExternalForm());
        Image checkedImage = new Image(getClass().getResource("/images/checkbox_checked.png").toExternalForm());
        String uncheckedStyle = "-fx-font-size: 14px; -fx-text-fill: #FFFFFF;";
        String checkedStyle = "-fx-font-size: 14px; -fx-text-fill: #E6BD8B;";

        ImageView checkboxImageView = new ImageView(defaultValue ? checkedImage : uncheckedImage);
        checkboxImageView.setFitWidth(20);
        checkboxImageView.setFitHeight(20);
        checkboxImageView.setPreserveRatio(true);

        Label label = new Label(text);
        label.setStyle(defaultValue ? checkedStyle : uncheckedStyle);

        // ===== CURSEUR MAIN (HAND) =====
        checkboxContainer.setOnMouseEntered(e -> checkboxContainer.setStyle("-fx-cursor: hand;"));
        checkboxContainer.setOnMouseExited(e -> checkboxContainer.setStyle("-fx-cursor: default;"));

        // ===== GESTION CLIC =====
        checkboxContainer.setOnMouseClicked(e -> {
            action.run();
            if (checkboxImageView.getImage() == uncheckedImage) {
                checkboxImageView.setImage(checkedImage);
                label.setStyle(checkedStyle);
                // FocusService.handleFocusApplication();
            } else {
                checkboxImageView.setImage(uncheckedImage);
                label.setStyle(uncheckedStyle);
                // FocusService.handleFocusApplication();
            }
        });

        checkboxContainer.getChildren().addAll(checkboxImageView, label);
        return checkboxContainer;
    }

    private StackPane createHeader(Stage stage, StackPane root) {
        StackPane header = new StackPane();

        // Chargement image header (logo ou bandeau)
        ImageView headerBg = new ImageView(
                new Image(getClass().getResource("/images/HeaderSmall.png").toExternalForm()));
        headerBg.setFitHeight(HEADER_HEIGHT); // hauteur fixe du header
        headerBg.setPreserveRatio(false);
        headerBg.fitWidthProperty().bind(root.widthProperty());

        ImageView logo = new ImageView(
                new Image(getClass().getResource("/images/logo.png").toExternalForm()));
        logo.setPreserveRatio(true);
        logo.setFitHeight(32);
        logo.setFitWidth(32);

        HBox logoContainer = new HBox(logo);
        logoContainer.setAlignment(Pos.CENTER_LEFT);
        logoContainer.setPadding(new Insets(0, 0, 0, 10));

        // ===== BUTTON =====
        HBox buttonsBox = createMinimizeCloseButtons(stage);

        // Drag & drop pour déplacer la fenêtre
        header.setOnMousePressed((MouseEvent event) -> {
            xOffset = event.getSceneX();
            yOffset = event.getSceneY();
        });
        header.setOnMouseDragged((MouseEvent event) -> {
            stage.setX(event.getScreenX() - xOffset);
            stage.setY(event.getScreenY() - yOffset);
        });

        Label title = new Label("WakFocus");
        title.setStyle("-fx-font-size: 30px; -fx-text-fill: #E6BD8B; -fx-font-weight: bold;");

        header.getChildren().addAll(headerBg, logoContainer, title, buttonsBox);
        return header;
    }

    private HBox createMinimizeCloseButtons(Stage stage) {
        StackPane minimizeBtn = createStackPaneButton("_");
        minimizeBtn.setOnMouseClicked(e -> stage.setIconified(true));

        StackPane closeBtn = createStackPaneButton("X");
        closeBtn.setOnMouseClicked(e -> {
            FocusService.stopRunning();
            NotificationService.shutdown();
            stage.close();
        });

        HBox buttonsBox = new HBox(5, minimizeBtn, closeBtn);
        buttonsBox.setAlignment(Pos.CENTER_RIGHT);
        buttonsBox.setPadding(new Insets(0, 10, 0, 0));
        return buttonsBox;
    }

    private ImageView createBackground(StackPane root) {
        ImageView bgView = new ImageView(new Image(getClass().getResource("/images/NP-window-1.png").toExternalForm()));
        bgView.setPreserveRatio(false); // ne pas garder le ratio pour remplir complètement

        // Binder la taille de l'image à la taille de la scène
        bgView.fitWidthProperty().bind(root.widthProperty());
        bgView.fitHeightProperty().bind(root.heightProperty());
        bgView.setStyle("-fx-background-color: transparent;" + "-fx-background-radius: 20;");
        return bgView;
    }

    public static void launchUI(String[] args) {
        launch(args);
    }
}
