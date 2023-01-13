package org.coin_madness;

import javafx.application.Application;
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
import org.coin_madness.model.Player;
import org.coin_madness.screens.GameScreen;
import org.coin_madness.screens.LobbyScreen;
import org.coin_madness.screens.MainScreen;

import java.io.IOException;


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
    public void start(Stage stage) throws IOException {
        this.stage = stage;
        ConnectionManager connectionManager = new ConnectionManager();
        MazeLoader loader = new MazeLoader();
        map = loader.load("src/main/resources/map.csv", ",");

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
            connectionManager.stop();
        }
        connectionManager = new ConnectionManager();
        MainScreen mainScreen = new MainScreen(connectionManager, errorMessage);
        mainScreen.setOnEnterLobby(() -> {
            LobbyScreen lobbyScreen = new LobbyScreen(connectionManager);

            changeView(lobbyScreen);

            lobbyScreen.setOnGameStart(() -> {
                // TODO - maybe, move to some kind of GameBuilder
                connectionManager.joinGameSpaces();
                BorderPane gameView = new GameScreen(stage, scene, map, graphics, connectionManager);
                gameView.setFocusTraversable(true);
                changeView(gameView);
            });
            lobbyScreen.setReturnToMainScreen(error -> {
                showStartScreen(error);
            });
        });


        changeView(mainScreen);
    }

    private void changeView(Node view) {
        root.getChildren().clear();
        root.getChildren().add(view);
    }

    public static void main(String[] args) throws InterruptedException {
        launch();
//        ConnectionManager c1 = new ConnectionManager();
//        c1.host();
//
//        String str = (String) ( c1.lobby.get(new FormalField(String.class))[0]);
//        System.out.println(str);
    }

}