package org.coin_madness.model;

import org.coin_madness.helpers.ConnectionManager;
import org.coin_madness.messages.GlobalMessage;
import org.coin_madness.messages.StaticEntityMessage;
import org.jspace.ActualField;
import org.jspace.FormalField;
import org.jspace.Space;

import java.util.List;
import java.util.function.Function;
import java.util.stream.Collectors;

public class StaticEntityServer<Entity extends StaticEntity> {

    private final GameState gameState;
    private ConnectionManager connectionManager;
    private Space entitySpace;
    private Function<Object[], Entity> convert;
    private int clientId;

    public StaticEntityServer(GameState gameState, Space entitySpace, Function<Object[], Entity> convert) {
        this.gameState = gameState;
        this.connectionManager = gameState.connectionManager;
        this.entitySpace = entitySpace;
        this.convert = convert;
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
    
    public void add(List<Entity> entities) throws InterruptedException {
        List<Integer> clientIds = getClientIds();
        addNewEntities(StaticEntityMessage.NEW_ENTITY, entities);
        sendNotifications(StaticEntityMessage.ADDED_ENTITIES, clientIds);
        receiveConfirmations(StaticEntityMessage.RECEIVED_ENTITIES, clientIds);
        clearSpaceFromNewEntities(StaticEntityMessage.NEW_ENTITY);
    }

    public void remove(Entity entity, Integer removerClientId) throws InterruptedException {
        List<Integer> clientIds = getClientIds();
        sendEntityNotifications(StaticEntityMessage.REMOVE_ENTITY, entity, removerClientId, clientIds);
    }

    private void addNewEntities(String newEntities, List<Entity> entities) throws InterruptedException {
        for (Entity entity : entities)
            entitySpace.put(newEntities, entity.getX(), entity.getY());
    }

    private void sendNotifications(String notification, List<Integer> clientIds) throws InterruptedException {
        for (int clientId : clientIds)
            entitySpace.put(notification, clientId);
    }

    protected void sendNotification(String notification, int clientId) throws InterruptedException {
        entitySpace.put(notification, clientId);
    }

    private void receiveConfirmations(String confirmation, List<Integer> clientIds) throws InterruptedException {
        for (int clientId : clientIds)
            entitySpace.get(new ActualField(confirmation), new ActualField(clientId));
    }

    private void clearSpaceFromNewEntities(String newEntities) throws InterruptedException {
        entitySpace.getAll(new ActualField(newEntities),
                           new FormalField(Integer.class),
                           new FormalField(Integer.class));
    }

    private void sendEntityNotifications(String notification, Entity entity, Integer removerClientId, List<Integer> clientIds) throws InterruptedException {
        for (int clientId : clientIds)
            entitySpace.put(notification, entity.getX(), entity.getY(), removerClientId, clientId);
    }

    protected void sendAnswer(String answerMarker, String answer, int clientId) throws InterruptedException {
        entitySpace.put(answerMarker, answer, clientId);
    }

    protected Object[] receiveEntityRequest(String request) throws InterruptedException {
        return entitySpace.get(new ActualField(request),
                new FormalField(Integer.class),
                new FormalField(Integer.class),
                new FormalField(Integer.class));
    }

}
