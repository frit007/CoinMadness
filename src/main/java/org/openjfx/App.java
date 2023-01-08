package org.openjfx;

import javafx.application.Application;
import javafx.geometry.Insets;
import javafx.scene.Group;
import javafx.scene.Scene;
import javafx.scene.layout.*;
import javafx.scene.paint.Color;
import javafx.stage.Stage;
import java.io.IOException;

/**
 * JavaFX App
 */
public class App extends Application {

    public static final Color BACKGROUND = Color.BLACK;
    private static final int SCENE_WIDTH = 640;
    private static final int SCENE_HEIGHT = 640;

    @Override
    public void start(Stage stage) throws IOException {
        ImageLibrary graphics = new ImageLibrary();
        MazeLoader loader = new MazeLoader();
        Field[][] map = loader.load("src/main/resources/map.csv", ",");
        Player player = new Player(0, 0, 0);

        StackPane root = new StackPane();
        root.setBackground(new Background(new BackgroundFill(BACKGROUND, CornerRadii.EMPTY, Insets.EMPTY)));
        Scene scene = new Scene(root, SCENE_WIDTH, SCENE_HEIGHT);
        Group gameView = new GameView(stage, scene, player, map, graphics);
        gameView.setFocusTraversable(true);
        root.getChildren().add(gameView);

        stage.setScene(scene);
        stage.setTitle("CoinMadness");
        stage.show();
    }

    public static void main(String[] args) {
        launch();
    }

}