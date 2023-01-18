package org.coin_madness.model;

import org.coin_madness.helpers.ConnectionManager;
import org.coin_madness.messages.GlobalMessage;
import org.jspace.ActualField;
import org.jspace.FormalField;
import org.jspace.Space;

import java.util.List;
import java.util.stream.Collectors;

public class StaticEntityCommon<Entity extends StaticEntity> {

    protected GameState gameState;
    protected ConnectionManager connectionManager;
    protected Space entitySpace;
    protected int clientId;

    public StaticEntityCommon(GameState gameState, Space entitySpace, ConnectionManager connectionManager) {
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

    protected String receiveAnswer(String answerMarker) throws InterruptedException {
        Object[] answer = entitySpace.get(new ActualField(answerMarker),
                                          new FormalField(String.class),
                                          new ActualField(clientId));
        return answer[1].toString();
    }

    protected void sendClientId(String marker, int clientId, int toClientId) throws InterruptedException {
        entitySpace.put(marker, clientId, toClientId);
    }

    protected void sendAnswer(String answerMarker, String answer, int clientId) throws InterruptedException {
        entitySpace.put(answerMarker, answer, clientId);
    }

    protected int receiveClientId(String marker) throws InterruptedException {
        Object[] receivedClientId = entitySpace.get(new ActualField(marker),
                new FormalField(Integer.class),
                new ActualField(clientId));
        return (int) receivedClientId[1];
    }

}
