package org.coin_madness.model;

import org.coin_madness.helpers.Action1;
import org.coin_madness.helpers.ConnectionManager;
import org.coin_madness.helpers.ScopedThreads;
import org.coin_madness.messages.GlobalMessage;
import org.coin_madness.messages.LobbyMessage;
import org.jspace.ActualField;
import org.jspace.FormalField;

public class LobbyClient {
    private ConnectionManager connectionManager;
    private Integer clientId;
    private LobbyCommon lobbyCommon;
    private ScopedThreads lobbyThreads;
    private boolean connected = false;

    public LobbyClient(ConnectionManager connectionManager, ScopedThreads lobbyThreads, LobbyCommon lobbyCommon) {
        this.connectionManager = connectionManager;
        this.lobbyThreads = lobbyThreads;
        this.lobbyCommon = lobbyCommon;
    }

    public void join(Action1<String> onConnectionFailed) {
        lobbyThreads.startHandledThread("Thread", () -> {
            if(!connected && connectionManager.getLobby().queryp(new ActualField(LobbyMessage.GAME_STARTED)) != null) {
                onConnectionFailed.handle("Sorry, the game has already started");
            }
        });
        try {
            connectionManager.getLobby().put(LobbyMessage.JOIN);
            Object[] response = connectionManager.getLobby().get(
                    new ActualField(LobbyMessage.WELCOME),
                    new FormalField(Integer.class),
                    new FormalField(Integer.class),
                    new FormalField(String.class)
            );
            clientId = (Integer) response[1];
            int serverId = (Integer) response[2];
            String errorMessage = (String) response[3];
            if(errorMessage.length() > 0) {
                onConnectionFailed.handle(errorMessage);
               return;
            }
            connectionManager.startClientTimeoutThread(clientId, serverId);
            connected = true;
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

    public void waitForGameStart(Action1<GameSettings> onGameStart) {
        lobbyThreads.startHandledThread("Wait for game start", () -> {
            Object[] gameStarted = connectionManager.getLobby().query(new ActualField(LobbyMessage.GAME_STARTED), new FormalField(Boolean.class));
            boolean personalGhosts = (boolean) gameStarted[1];

            onGameStart.handle(new GameSettings(personalGhosts));
        });
    }

    // returns is ready
    public boolean toggleReady() {
        try {
            if(isNotReady()) {
                ready();
                lobbyCommon.sendLobbyUpdated();
                return true;
            } else {
                unready();
                lobbyCommon.sendLobbyUpdated();
                return false;
            }
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        return false;
    }

    private boolean isNotReady() throws InterruptedException {
        return connectionManager
                .getLobby()
                .queryp(
                        new ActualField(LobbyMessage.NOT_READY),
                        new ActualField(clientId)
                ) != null;
    }

    private void unready() throws InterruptedException {
        // We need the ready lock, because we need the protection in start game
        connectionManager.getLobby().get(new ActualField(LobbyMessage.READY_LOCK));

        if(connectionManager.getLobby().queryp(new ActualField(LobbyMessage.READY), new ActualField(clientId)) != null) {
            connectionManager.getLobby().get(new ActualField(LobbyMessage.READY), new ActualField(clientId));
            connectionManager.getLobby().put(LobbyMessage.NOT_READY, clientId);
        }

        connectionManager.getLobby().put(LobbyMessage.READY_LOCK);
    }
    private void ready() throws InterruptedException {
        // We don't need a ready lock since
        connectionManager.getLobby().get(new ActualField(LobbyMessage.NOT_READY), new ActualField(clientId));
        connectionManager.getLobby().put(LobbyMessage.READY, clientId);
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
        lobbyThreads.startHandledThread("Wait for lobby update", () -> {
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
