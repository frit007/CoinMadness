package org.coin_madness.screens;

import javafx.application.Platform;
import javafx.geometry.HPos;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.Button;
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
import org.coin_madness.messages.GlobalMessage;
import org.coin_madness.messages.LobbyMessage;
import org.jspace.ActualField;
import org.jspace.FormalField;

import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.SocketException;
import java.net.UnknownHostException;
import java.util.List;
import java.util.stream.Collectors;

public class LobbyScreen extends GridPane {

    private Action onGameStart;
    private Action1<String> returnToMainScreen;

    private boolean isReady = false;
    int nextClientId = 0;
    private String clientId;
    private ConnectionManager connectionManager;

    // UI
    private Text status;
    private Button readyButton;
    private Button startButton;
    private boolean hasHandledConnectionFailure = false;

    // Threads
    private ScopedThreads screenThreads = new ScopedThreads(this::connectionFailed);

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

        add(leaveButton, 1,2,1,1);
        add(buttons, 4,2, 1, 1);
        GridPane.setHalignment(buttons, HPos.RIGHT);

        if(connectionManager.isHost()) {
            setupHost();
        }
        getClientId();
        readyPlayerListener();
        waitForGameStart();

        connectionManager.setOnClientTimeout((reason) -> {
            this.connectionFailed();
        });
    }

    private void onLeaveButtonClicked(MouseEvent event) {
        // when we leave we will generate connection failures.
        // we want to ignore these connection failures, since we voluntarily left the lobby
        if(connectionManager.isHost()) {
            // TODO - maybe send some kind of graceful message about the server being gone.
        } else {
            try {
                connectionManager.getLobby().put(GlobalMessage.DISCONNECT, clientId);
            } catch (InterruptedException e) {
                e.printStackTrace();
                connectionFailed();
                return;
            }
        }
        hasHandledConnectionFailure = true;
        screenThreads.cleanup();
        connectionManager.stop();
        returnToMainScreen.handle(null);
    }

    private void connectionFailed() {
        if(hasHandledConnectionFailure) {
            return;
        }
        connectionManager.setOnClientDisconnect(null);
        connectionManager.stop();
        Platform.runLater(() -> {
            returnToMainScreen.handle("Sorry, connection failed");
        });
        screenThreads.cleanup();
        hasHandledConnectionFailure = true;
    }

    private void waitForGameStart() {
        screenThreads.startHandledThread(() -> {
            connectionManager.getLobby().query(new ActualField(LobbyMessage.GAME_STARTED));
            Platform.runLater(() -> {
                onGameStart.handle();
            });
        });
    }

    // host needs a list of all rooms

    private String createClientId() {
        nextClientId++;
        return "client" + nextClientId;
    }

    private void readyPlayerListener() {
        screenThreads.startHandledThread(() -> {
            while(true) {
                // wait to update the lobby
                connectionManager.getLobby().get(new ActualField(LobbyMessage.LOBBY_UPDATED), new ActualField(clientId));
                int connectedPlayers = connectionManager.getLobby().queryAll(new ActualField(GlobalMessage.CLIENTS), new FormalField(String.class)).size();
                int readyPlayers = connectionManager.getLobby().queryAll(new ActualField(LobbyMessage.READY), new FormalField(String.class)).size();
                status.setText(readyPlayers + "/" + connectedPlayers);
                if(connectionManager.isHost()) {
                    startButton.setDisable(readyPlayers != connectedPlayers);
                }
            }
        });
    }

    private void setupHost() {
        try {
            // setup lobby lock
            connectionManager.getLobby().put(LobbyMessage.READY_LOCK);
        } catch (InterruptedException e) {
            e.printStackTrace();
            connectionFailed();
            return;
        }

        // Listen for join requests
        screenThreads.startHandledThread(() -> {
            while(true) {
                connectionManager.getLobby().get(new ActualField(LobbyMessage.JOIN));
                String clientId = createClientId();

                connectionManager.getLobby().put(GlobalMessage.CLIENTS, clientId);
                connectionManager.getLobby().put(LobbyMessage.WELCOME, clientId);
            }
        });

        // handle disconnected clients
        connectionManager.setOnClientDisconnect((disconnectedClient, disconnectReason) -> {
            try {
                // when a client has been disconnected they are no longer ready
                connectionManager.getLobby().getp(new ActualField(LobbyMessage.READY), new ActualField(disconnectedClient));
                sendLobbyUpdated();
            } catch (InterruptedException e) {

                e.printStackTrace();
                connectionFailed();
                return;
            }
        });
    }
    private void getClientId() {
        try {
            connectionManager.getLobby().put(LobbyMessage.JOIN);
            Object[] response = connectionManager.getLobby().get(
                    new ActualField(LobbyMessage.WELCOME),
                    new FormalField(String.class)
            );
            clientId = (String) response[1];
            connectionManager.startClientTimeoutThread(clientId);
        } catch (InterruptedException e) {
            e.printStackTrace();
            connectionFailed();
            return;
        }
        sendLobbyUpdated();
    }

    // send a direct notification everybody that they need to fetch lobby information
    private void sendLobbyUpdated() {
        try {
            var clients = connectionManager.getLobby().queryAll(new ActualField(GlobalMessage.CLIENTS), new FormalField(String.class));
            for (var client: clients) {
                String otherClientId = client[1].toString();
                connectionManager.getLobby().put(LobbyMessage.LOBBY_UPDATED, otherClientId);
            }
        } catch (InterruptedException e) {
            e.printStackTrace();
            connectionFailed();
            return;
        }

    }

    public void setOnGameStart(Action onGameStart) {
        this.onGameStart = onGameStart;
    }
    public void setReturnToMainScreen(Action1<String> returnToMainScreen) {
        this.returnToMainScreen = returnToMainScreen;
    }

    private void onReadyButtonClicked(MouseEvent event) {
        if(!isReady) {
            // we are now ready!
            isReady = true;
            readyButton.setText("Unready");

            try {
                connectionManager.getLobby().put(LobbyMessage.READY, clientId);
            } catch (InterruptedException e) {
                e.printStackTrace();
                connectionFailed();
                return;
            }

        } else {
            boolean removedReady = false;

            // we are no longer ready
            try {
                connectionManager.getLobby().get(new ActualField(LobbyMessage.READY_LOCK));

                if(connectionManager.getLobby().queryp(new ActualField(LobbyMessage.READY), new ActualField(clientId)) != null) {
                    connectionManager.getLobby().get(new ActualField(LobbyMessage.READY), new ActualField(clientId));
                    removedReady = true;
                }

                connectionManager.getLobby().put(LobbyMessage.READY_LOCK);
            } catch (InterruptedException e) {
                e.printStackTrace();
                connectionFailed();
                return;
            }

            if(removedReady) {
                isReady = false;
                readyButton.setText("Ready");
            } else {
                // TODO - maybe show the user why there action was ignored. But the game should start at this point.
            }
        }
        sendLobbyUpdated();
    }

    private void onStartButtonClicked(MouseEvent event) {
        // start the game
        boolean everyBodyReady = true;

        try {
            connectionManager.getLobby().get(new ActualField(LobbyMessage.READY_LOCK));
            List<String> clientIds = connectionManager.getLobby().queryAll(new ActualField(GlobalMessage.CLIENTS), new FormalField(String.class))
                    .stream()
                    .map(x -> (String) x[1])
                    .collect(Collectors.toList());

            for (String clientId: clientIds) {
                if(connectionManager.getLobby().queryp(new ActualField(LobbyMessage.READY), new ActualField(clientId)) == null) {
                    everyBodyReady = false;
                }
            }

            if(everyBodyReady) {
                for (String clientId: clientIds) {
                    connectionManager.getLobby().get(new ActualField(LobbyMessage.READY), new ActualField(clientId));
                }
            }
            connectionManager.getLobby().put(LobbyMessage.GAME_STARTED);
            connectionManager.getLobby().put(new ActualField(LobbyMessage.READY_LOCK));

            // start the game somehow?
        } catch (InterruptedException e) {
            e.printStackTrace();
            connectionFailed();
            return;
        }
        sendLobbyUpdated();

    }

}
