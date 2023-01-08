package org.coin_madness.screens;

import javafx.geometry.HPos;
import javafx.geometry.Pos;
import javafx.scene.control.Button;
import javafx.scene.control.TextField;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.HBox;
import javafx.scene.paint.Color;
import javafx.scene.text.Text;
import org.coin_madness.helpers.ConnectionManager;
import org.coin_madness.helpers.Action;

import java.io.IOException;

public class MainScreen extends GridPane {

    private ConnectionManager connectionManager;
    private Action onEnterLobby;

    public MainScreen(ConnectionManager connectionManager, String errorMessage) {
        this.connectionManager = connectionManager;
        init(errorMessage);
    }
    public void setOnEnterLobby(Action onEnterLobby) {
        this.onEnterLobby = onEnterLobby;
    }

    public void init(String errorMessage) {
        //Title
        Text title = new Text("Coin Madness");
        title.setFill(Color.BLACK);
        title.setStyle("-fx-font: 24 arial;");


        Text error = new Text(errorMessage);
        error.setFill(Color.RED);
        error.setStyle("-fx-font: 12 arial;");


        //Setting the vertical and horizontal gaps between the columns
        setVgap(5);
        setHgap(5);

        //Setting the Grid alignment
        setAlignment(Pos.CENTER);

        TextField ip = new TextField();
        ip.setText("127.0.0.1");
        ip.setPromptText("ip address");
        ip.setMinWidth(300);
        Button hostButton = new Button("Host");
        hostButton.setOnMouseClicked(mouse -> {
            connectionManager.host();
            onEnterLobby.handle();
        });

        Button joinButton = new Button("Join");
        joinButton.setOnMouseClicked(mouse -> {
            try {
                connectionManager.join(ip.getText());
                onEnterLobby.handle();
            } catch (IOException e) {
                e.printStackTrace();
                error.setText("Unable to connect. Is the ip correct?");
            }
        });

        HBox buttons = new HBox();
        buttons.getChildren().addAll(joinButton, hostButton);
        buttons.setAlignment(Pos.CENTER_RIGHT);
        buttons.setSpacing(4);

        add(title, 0, 0, 5, 1);
        GridPane.setHalignment(title, HPos.CENTER);

        add(ip, 0, 1, 5,1);
        add(buttons, 4,2, 1, 1);
        GridPane.setHalignment(buttons, HPos.RIGHT);

        add(error, 1, 3, 3, 1);
        GridPane.setHalignment(error, HPos.CENTER);
    }




}
