package org.coin_madness;

import javafx.application.Application;
import javafx.scene.Node;
import javafx.scene.Scene;
import javafx.scene.layout.StackPane;
import javafx.stage.Stage;
import org.coin_madness.helpers.ConnectionManager;
import org.coin_madness.screens.LobbyScreen;
import org.coin_madness.screens.MainScreen;


/**
 * JavaFX App
 */
public class App extends Application {
    StackPane root;
    @Override
    public void start(Stage stage) {
        ConnectionManager connectionManager = new ConnectionManager();
        root = new StackPane();

        var scene = new Scene(root, 640, 480);

        MainScreen mainScreen = new MainScreen(connectionManager);
        mainScreen.setOnEnterLobby(() -> {
            LobbyScreen lobbyScreen = new LobbyScreen(connectionManager);

            changeView(lobbyScreen);

            lobbyScreen.setOnGameStart(() -> {
                System.out.println("Go to the game!");
            });
        });


        root.getChildren().add(mainScreen);

        stage.setScene(scene);
        stage.show();
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