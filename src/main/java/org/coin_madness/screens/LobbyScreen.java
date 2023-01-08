package org.coin_madness.screens;

import javafx.geometry.HPos;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.TextArea;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.HBox;
import javafx.scene.paint.Color;
import javafx.scene.text.Text;
import javafx.scene.text.TextAlignment;
import org.coin_madness.exceptions.MadnessException;
import org.coin_madness.helpers.ConnectionManager;
import org.coin_madness.helpers.Action;
import org.jspace.FormalField;

import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.SocketException;
import java.net.UnknownHostException;

public class LobbyScreen extends GridPane {

    private Action onGameStart;
    private boolean isReady = false;
    private Text status;
    private Button readyButton;
    private Button startButton;

    public LobbyScreen(Action onGameStart) {
        this.onGameStart = onGameStart;
    }

    private String getIpAddress() throws SocketException, UnknownHostException {
        // https://stackoverflow.com/questions/9481865/getting-the-ip-address-of-the-current-machine-using-java
        // open a connection so we can get our external ip address.
        // this is done to avoid issues with
        DatagramSocket socket = new DatagramSocket();
        socket.connect(InetAddress.getByName("8.8.8.8"), 10002);
        return socket.getLocalAddress().getHostAddress();
    }

    private void evaluateCanStartGame() {
        // TODO -
        startButton.setDisable(true);
    }

    private ConnectionManager connectionManager;
    public LobbyScreen(ConnectionManager connectionManager) {
        this.connectionManager = connectionManager;
        Label title = new Label("Waiting for players...");
        title.setStyle("-fx-font: 24 arial;");
        title.setTextAlignment(TextAlignment.CENTER);

        Text status = new Text("0/0 ready");
        status.setFill(Color.GREY);
        status.setStyle("-fx-font: 12 arial;");

        //Setting the vertical and horizontal gaps between the columns
        setVgap(5);
        setHgap(5);

        //Setting the Grid alignment
        setAlignment(Pos.CENTER);

        readyButton = new Button("Ready");
        readyButton.setOnMouseClicked(this::onReadyButtonClicked);

        startButton = new Button("Start game!");
        startButton.setOnMouseClicked(this::onStartButtonClicked);
        HBox buttons = new HBox();

        evaluateCanStartGame();

        if(connectionManager.isHost()) {
            // only show the start button for the host
            buttons.getChildren().add(startButton);
            try {
                title.setText("Waiting for players, connect at " + getIpAddress());
            } catch (SocketException | UnknownHostException e) {
                title.setStyle("-fx-font: 20 arial;");
                title.setText("Waiting for players.\nIt was not possible to find the ip address.\nPlease use ipconfig/ifconfig to find your ip address");
            }
        }

        buttons.getChildren().add(readyButton);
        buttons.setAlignment(Pos.CENTER_RIGHT);
        buttons.setPadding(new Insets(2,2,2,2));

        add(title, 0, 0, 5, 1);
        GridPane.setHalignment(title, HPos.CENTER);

        add(status, 1, 1, 3, 1);
        GridPane.setHalignment(status, HPos.CENTER);

        add(buttons, 4,2, 1, 1);
        GridPane.setHalignment(buttons, HPos.RIGHT);
    }

    public void setOnGameStart(Action onGameStart) {
        this.onGameStart = onGameStart;
    }

    private void onReadyButtonClicked(MouseEvent event) {
        if(isReady) {
            isReady = false;
            readyButton.setText("Ready");
        } else {
            isReady = true;
            readyButton.setText("Unready");

        }
    }

    private void onStartButtonClicked(MouseEvent event) {
        // start the game
    }





}
