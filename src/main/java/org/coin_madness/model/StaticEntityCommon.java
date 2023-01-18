package org.coin_madness.model;

import org.coin_madness.helpers.ConnectionManager;
import org.coin_madness.messages.GlobalMessage;
import org.jspace.ActualField;
import org.jspace.FormalField;
import org.jspace.Space;

import java.util.List;
import java.util.stream.Collectors;

public class StaticEntityCommon<Entity extends StaticEntity> {

    private GameState gameState;
    private ConnectionManager connectionManager;
    private Space entitySpace;
    private int clientId;

    public StaticEntityCommon(GameState gameState, ConnectionManager connectionManager, Space entitySpace) {
        this.gameState = gameState;
        this.connectionManager = connectionManager;
        this.entitySpace = entitySpace;
        this.clientId = connectionManager.getClientId();
    }

    protected List<Integer> getClientIds() throws InterruptedException {
        List<Object[]> clients = connectionManager
                .getLobby()
                .queryAll(new ActualField(GlobalMessage.CLIENTS),
                        new FormalField(Integer.class),
                        new FormalField(Integer.class)
                );
        return clients.stream().map(c -> (int) c[1]).collect(Collectors.toList());
    }

}
