package org.coin_madness.model;

import org.coin_madness.helpers.ConnectionManager;
import org.coin_madness.helpers.ScopedThreads;
import org.coin_madness.messages.GlobalMessage;
import org.coin_madness.messages.LobbyMessage;
import org.jspace.ActualField;
import org.jspace.FormalField;

public class LobbyCommon {

    ConnectionManager connectionManager;

    public LobbyCommon(ConnectionManager connectionManager) {
        this.connectionManager = connectionManager;
    }

    // send a direct notification to everybody that they need to fetch lobby information
    public void sendLobbyUpdated() {
        try {
            var clients = connectionManager.getLobby().queryAll(new ActualField(GlobalMessage.CLIENTS), new FormalField(String.class));
            for (var client: clients) {
                String otherClientId = client[1].toString();
                connectionManager.getLobby().put(LobbyMessage.LOBBY_UPDATED, otherClientId);
            }
        } catch (InterruptedException e) {
            e.printStackTrace();
        }

    }
}
