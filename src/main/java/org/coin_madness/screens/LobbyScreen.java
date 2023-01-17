package org.coin_madness.screens;

import javafx.application.Platform;
import javafx.geometry.HPos;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.Button;
import javafx.scene.control.CheckBox;
import javafx.scene.control.Label;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.HBox;
import javafx.scene.paint.Color;
import javafx.scene.text.Text;
import javafx.scene.text.TextAlignment;
import org.coin_madness.helpers.Action1;
import org.coin_madness.helpers.ConnectionManager;
import org.coin_madness.helpers.Action;
import org.coin_madness.helpers.ScopedThreads;
import org.coin_madness.messages.LobbyMessage;
import org.coin_madness.model.*;
import org.jspace.ActualField;

import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.SocketException;
import java.net.UnknownHostException;
import java.util.concurrent.Callable;
import java.util.function.Consumer;

public class LobbyScreen extends GridPane {

    private Action1<GameSettings> onGameStart;
    private Action1<String> returnToMainScreen;

    private boolean isReady = false;
    private ConnectionManager connectionManager;

    // UI
    private Text status;
    private Button readyButton;
    private Button startButton;
    private boolean hasHandledConnectionFailure = false;
    private LobbyServer lobbyServer;
    private LobbyClient lobbyClient;
    private CheckBox personalGhosts;

    // Threads
    private ScopedThreads lobbyThreads = new ScopedThreads(() -> {
        connectionFailed("Sorry, connection failed");
    });

    private String getIpAddress() throws SocketException, UnknownHostException {
        // https://stackoverflow.com/questions/9481865/getting-the-ip-address-of-the-current-machine-using-java
        // open a connection, so we can get our external ip address.
        // this is done to avoid issues with
        DatagramSocket socket = new DatagramSocket();
        socket.connect(InetAddress.getByName("8.8.8.8"), 10002);
        return socket.getLocalAddress().getHostAddress();
    }


    public LobbyScreen(ConnectionManager connectionManager) {
        this.connectionManager = connectionManager;
        Label title = new Label("Waiting for players...");
        title.setStyle("-fx-font: 24 arial;");
        title.setTextAlignment(TextAlignment.CENTER);

        status = new Text("0/0 ready");
        status.setFill(Color.GREY);
        status.setStyle("-fx-font: 12 arial;");

        //Setting the vertical and horizontal gaps between the columns
        setVgap(5);
        setHgap(5);

        //Setting the Grid alignment
        setAlignment(Pos.CENTER);

        Button leaveButton = new Button("Leave");
        leaveButton.setOnMouseClicked(this::onLeaveButtonClicked);

        readyButton = new Button("Ready");
        readyButton.setOnMouseClicked(this::onReadyButtonClicked);

        startButton = new Button("Start game!");
        startButton.setOnMouseClicked(this::onStartButtonClicked);
        startButton.setDisable(true);

        HBox buttons = new HBox();

        personalGhosts = new CheckBox("Personal Ghosts");
        if(connectionManager.isHost()) {
            // only show the start button for the host
            buttons.getChildren().add(startButton);
            try {
                title.setText("Waiting for players, connect at " + getIpAddress());
            } catch (SocketException | UnknownHostException e) {
                title.setStyle("-fx-font: 20 arial;");
                title.setText("Waiting for players.\nIt was not possible to find the ip address.\nPlease use ipconfig/ifconfig to find your ip address");
            }
        } else {
            personalGhosts.setVisible(false);
        }

        buttons.getChildren().add(readyButton);
        buttons.setAlignment(Pos.CENTER_RIGHT);
        buttons.setPadding(new Insets(2,2,2,2));

        add(title, 0, 0, 5, 1);
        GridPane.setHalignment(title, HPos.CENTER);

        add(status, 1, 1, 3, 1);
        GridPane.setHalignment(status, HPos.CENTER);

        add(leaveButton, 1,2,1,1);
        add(buttons, 4,2, 1, 1);
        GridPane.setHalignment(buttons, HPos.RIGHT);

        add(personalGhosts, 1,3,1,1);

        LobbyCommon lobbyCommon = new LobbyCommon(connectionManager);
        lobbyClient = new LobbyClient(connectionManager, lobbyThreads, lobbyCommon);

        if(connectionManager.isHost()) {
            lobbyServer = new LobbyServer(connectionManager, lobbyThreads, lobbyCommon);
            lobbyServer.setup();
        }
        lobbyClient.join(this::connectionFailed);
        lobbyUpdateListener();
        waitForGameStart();

        connectionManager.setOnClientTimeout((reason) -> {
            this.connectionFailed("Sorry, connection failed");
        });
    }

    public void setOnGameStart(Action1<GameSettings> onGameStart) {
        this.onGameStart = onGameStart;
    }
    public void setReturnToMainScreen(Action1<String> returnToMainScreen) {
        this.returnToMainScreen = returnToMainScreen;
    }

    private void onLeaveButtonClicked(MouseEvent event) {
        // when we leave we will generate connection failures.
        // we want to ignore these connection failures, since we voluntarily left the lobby
        if(connectionManager.isHost()) {
            // TODO - maybe send some kind of graceful message about the server being gone.
        } else {
            lobbyClient.disconnect();
        }
        hasHandledConnectionFailure = true;
        lobbyThreads.cleanup();
        connectionManager.stop();
        returnToMainScreen.handle(null);
    }

    private void connectionFailed(String error) {
        if(hasHandledConnectionFailure) {
            return;
        }
        connectionManager.setOnClientDisconnect(null);
        connectionManager.stop();
        Platform.runLater(() -> {
            returnToMainScreen.handle(error);
        });
        lobbyThreads.cleanup();
        hasHandledConnectionFailure = true;
    }

    private void waitForGameStart() {
        lobbyClient.waitForGameStart((gameSettings) -> {
            Platform.runLater(() -> {
                connectionManager.setOnClientTimeout(null);
                lobbyThreads.cleanup();
                onGameStart.handle(gameSettings);
            });
        });
    }


    private void lobbyUpdateListener() {
        lobbyClient.waitForLobbyUpdate(lobbyUpdate -> {
            Platform.runLater(() -> {
                status.setText(lobbyUpdate.readyPlayers + "/" + lobbyUpdate.connectedPlayers);

                if(connectionManager.isHost()) {
                    startButton.setDisable(lobbyUpdate.readyPlayers != lobbyUpdate.connectedPlayers);
                }
            });
        });
    }

    private void onReadyButtonClicked(MouseEvent event) {
        if(lobbyClient.toggleReady()) {
            readyButton.setText("Unready");
        } else {
            readyButton.setText("Ready");
        }
    }

    private void onStartButtonClicked(MouseEvent event) {
        GameSettings gameSettings = new GameSettings(personalGhosts.isSelected());
        lobbyServer.startGame(gameSettings);
    }

}
