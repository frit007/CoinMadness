package org.coin_madness.model;

import org.coin_madness.helpers.ConnectionManager;
import org.coin_madness.helpers.ScopedThreads;
import org.coin_madness.messages.GlobalMessage;
import org.coin_madness.messages.LobbyMessage;
import org.jspace.ActualField;
import org.jspace.FormalField;

import java.util.List;
import java.util.stream.Collectors;

public class LobbyServer {
    ConnectionManager connectionManager;
    ScopedThreads lobbyThreads;
    int nextClientId = 0;
    LobbyCommon lobbyCommon;

    public LobbyServer(ConnectionManager connectionManager, ScopedThreads lobbyThreads, LobbyCommon lobbyCommon) {
        this.connectionManager = connectionManager;
        this.lobbyThreads = lobbyThreads;
        this.lobbyCommon = lobbyCommon;
    }

    private int createClientId() {
        nextClientId++;
        return nextClientId;
    }

    private int findNextAvailableSprite() throws InterruptedException {
        int availableSprites = 4;
        for(int i = 0; i < availableSprites; i++) {
            Object[] existingClientWithModelId = connectionManager
                    .getLobby()
                    .queryp(
                            new ActualField(GlobalMessage.CLIENTS),
                            new FormalField(Integer.class),
                            new ActualField(i)
                    );
            if(existingClientWithModelId == null) {
                return i;
            }
        }

        // give everybody else the last sprite
        return availableSprites - 1;
    }

    public void setup() {
        try {
            // setup lobby lock
            connectionManager.getLobby().put(LobbyMessage.READY_LOCK);
        } catch (InterruptedException e) {
            e.printStackTrace();
            return;
        }

        // Listen for join requests
        lobbyThreads.startHandledThread("Listen for join requests", () -> {
            while(true) {
                connectionManager.getLobby().get(new ActualField(LobbyMessage.JOIN));
                int clientId = createClientId();

                connectionManager.getLobby().put(LobbyMessage.NOT_READY, clientId);
                connectionManager.getLobby().put(GlobalMessage.CLIENTS, clientId, findNextAvailableSprite());
                connectionManager.getLobby().put(LobbyMessage.WELCOME, clientId);
            }
        });

        // handle disconnected clients
        connectionManager.setOnClientDisconnect((disconnectedClient, disconnectReason) -> {
            try {
                // when a client has been disconnected they are no longer ready
                connectionManager.getLobby().getp(new ActualField(LobbyMessage.READY), new ActualField(disconnectedClient));
                lobbyCommon.sendLobbyUpdated();
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        });
    }

    public void startGame(){
        try {
            boolean allPlayersReady = true;
            connectionManager.getLobby().get(new ActualField(LobbyMessage.READY_LOCK));
            List<Integer> clientIds = connectionManager.getLobby().queryAll(new ActualField(GlobalMessage.CLIENTS), new FormalField(Integer.class), new FormalField(Integer.class))
                    .stream()
                    .map(x -> (Integer) x[1])
                    .collect(Collectors.toList());

            // check if everybody is ready
            for (Integer clientId: clientIds) {
                allPlayersReady = allPlayersReady
                        && connectionManager.getLobby().queryp(new ActualField(LobbyMessage.READY), new ActualField(clientId)) != null;
            }

            // if everybody is ready mark them as no longer ready.
            if(allPlayersReady) {
                for (Integer clientId: clientIds) {
                    connectionManager.getLobby().get(new ActualField(LobbyMessage.READY), new ActualField(clientId));
                }
            }

            // if everybody is ready start the game
            if(allPlayersReady) {
                connectionManager.createGameSpaces();
                connectionManager.getLobby().put(LobbyMessage.GAME_STARTED);
            }

            connectionManager.getLobby().put(new ActualField(LobbyMessage.READY_LOCK));


            // start the game somehow?
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }

}
