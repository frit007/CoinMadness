package org.coin_madness.model;

import javafx.application.Platform;
import org.coin_madness.helpers.Action1;
import org.coin_madness.helpers.ConnectionManager;
import org.coin_madness.helpers.ScopedThreads;
import org.coin_madness.messages.GlobalMessage;
import org.coin_madness.messages.LobbyMessage;
import org.jspace.ActualField;
import org.jspace.FormalField;

import java.util.concurrent.Callable;
import java.util.function.Consumer;

public class LobbyClient {
    private ConnectionManager connectionManager;
    private Integer clientId;
    private LobbyCommon lobbyCommon;
    private ScopedThreads lobbyThreads;

    public LobbyClient(ConnectionManager connectionManager, ScopedThreads lobbyThreads, LobbyCommon lobbyCommon) {
        this.connectionManager = connectionManager;
        this.lobbyThreads = lobbyThreads;
        this.lobbyCommon = lobbyCommon;
    }

    public void join() {
        try {
            connectionManager.getLobby().put(LobbyMessage.JOIN);
            Object[] response = connectionManager.getLobby().get(
                    new ActualField(LobbyMessage.WELCOME),
                    new FormalField(Integer.class),
                    new FormalField(Integer.class)
            );
            clientId = (Integer) response[1];
            int serverId = (Integer) response[2];
            connectionManager.startClientTimeoutThread(clientId, serverId);
        } catch (InterruptedException e) {
            e.printStackTrace();
            return;
        }
        lobbyCommon.sendLobbyUpdated();
    }

    public void disconnect() {

        try {
            connectionManager.getLobby().put(GlobalMessage.DISCONNECT, clientId);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }

    public void waitForGameStart(Runnable onGameStart) {
        lobbyThreads.startHandledThread(() -> {
            connectionManager.getLobby().query(new ActualField(LobbyMessage.GAME_STARTED));
            Platform.runLater(() -> {
                onGameStart.run();
            });
        });
    }

    // returns is ready
    public boolean toggleReady() {
        boolean isReady = false;
        try {
            connectionManager.getLobby().get(new ActualField(LobbyMessage.READY_LOCK));

            if(connectionManager.getLobby().queryp(new ActualField(LobbyMessage.READY), new ActualField(clientId)) != null) {
                connectionManager.getLobby().get(new ActualField(LobbyMessage.READY), new ActualField(clientId));
                isReady = false;
            } else {
                connectionManager.getLobby().put(LobbyMessage.READY, clientId);
                isReady = true;
            }

            connectionManager.getLobby().put(LobbyMessage.READY_LOCK);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        lobbyCommon.sendLobbyUpdated();
        return isReady;
    }

    public static class LobbyUpdate {
        public int connectedPlayers;
        public int readyPlayers;

        public LobbyUpdate(int connectedPlayers, int readyPlayers) {
            this.connectedPlayers = connectedPlayers;
            this.readyPlayers = readyPlayers;
        }
    }
    public void waitForLobbyUpdate(Action1<LobbyUpdate> onLobbyUpdate) {
        lobbyThreads.startHandledThread(() -> {
            while(true) {
                // wait to update the lobby
                connectionManager.getLobby().get(new ActualField(LobbyMessage.LOBBY_UPDATED), new ActualField(clientId));
                int connectedPlayers = connectionManager
                        .getLobby()
                        .queryAll(
                                new ActualField(GlobalMessage.CLIENTS),
                                new FormalField(Integer.class),
                                new FormalField(Integer.class)
                        ).size();
                int readyPlayers = connectionManager.getLobby().queryAll(new ActualField(LobbyMessage.READY), new FormalField(Integer.class)).size();

                LobbyUpdate lobbyUpdate = new LobbyUpdate(connectedPlayers, readyPlayers);
                onLobbyUpdate.handle(lobbyUpdate);
            }
        });
    }
}
