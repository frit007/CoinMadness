package org.coin_madness;

import javafx.application.Application;
import javafx.application.Platform;
import javafx.geometry.Insets;
import javafx.scene.Node;
import javafx.scene.Scene;
import javafx.scene.layout.*;
import javafx.scene.paint.Color;
import javafx.stage.Stage;
import org.coin_madness.helpers.ConnectionManager;
import org.coin_madness.helpers.ImageLibrary;
import org.coin_madness.model.Field;
import org.coin_madness.helpers.MazeLoader;
import org.coin_madness.model.GameSettings;
import org.coin_madness.model.GameState;
import org.coin_madness.model.Player;
import org.coin_madness.screens.EndScreen;
import org.coin_madness.screens.GameScreen;
import org.coin_madness.screens.LobbyScreen;
import org.coin_madness.screens.MainScreen;

import java.io.IOException;
import java.util.function.Consumer;


/**
 * JavaFX App
 */
public class App extends Application {

    private StackPane root;
    public static final Color BACKGROUND = Color.WHITE;
    private static final int SCENE_WIDTH = 640;
    private static final int SCENE_HEIGHT = 670;
    ImageLibrary graphics = new ImageLibrary();
    Stage stage;
    Scene scene;
    Field[][] map;

    @Override
    public void start(Stage stage) {
        this.stage = stage;

        root = new StackPane();
        root.setBackground(new Background(new BackgroundFill(BACKGROUND, CornerRadii.EMPTY, Insets.EMPTY)));
        scene = new Scene(root, SCENE_WIDTH, SCENE_HEIGHT);

        showStartScreen(null);

        stage.setScene(scene);
        stage.setTitle("CoinMadness");
        stage.show();
    }
    ConnectionManager connectionManager;
    //Dispay of the "main screen"
    //Changing scenes
    private void showStartScreen(String errorMessage) {
        if(connectionManager != null) {
            ConnectionManager previousConnectionManager = connectionManager;
            new Thread(() -> {
                previousConnectionManager.stop();
            }).start();
        }
        connectionManager = new ConnectionManager();
        MainScreen mainScreen = new MainScreen(connectionManager, errorMessage);
        mainScreen.setOnEnterLobby(this::showLobby);

        changeView(mainScreen);
    }
    private void showLobby() {
        MazeLoader loader = new MazeLoader();
        try {
            map = loader.load("src/main/resources/map.csv", ",");
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
        LobbyScreen lobbyScreen = new LobbyScreen(connectionManager);

        changeView(lobbyScreen);

        lobbyScreen.setOnGameStart(this::showGame);
        lobbyScreen.setReturnToMainScreen(this::showStartScreen);
    }

    private void showGame(GameSettings gameSettings) {
        connectionManager.joinGameSpaces();
        BorderPane gameView = new GameScreen(stage, scene, gameSettings, map, graphics, connectionManager, this::showEndScreen, this::showStartScreen);
        gameView.setFocusTraversable(true);
        Platform.runLater(() -> {
            changeView(gameView);
        });
    }

    private void showEndScreen(GameState gameState) {
        EndScreen endScreen = new EndScreen(gameState, graphics, this::showStartScreen);

        new Thread(() -> {
            try {
                Thread.sleep(2000);
                Platform.runLater(() -> {
                    changeView(endScreen);
                });
                gameState.gameThreads.cleanup();
            } catch (InterruptedException e) {
                throw new RuntimeException(e);
            }
        }).start();
    }

    private void changeView(Node view) {
        root.getChildren().clear();
        root.getChildren().add(view);
    }

    @Override
    public void stop() {
        System.exit(0);
    }

    public static void main(String[] args) {
        launch();
    }

}