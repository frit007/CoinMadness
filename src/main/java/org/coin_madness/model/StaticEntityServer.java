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

    private List<Integer> getClientIds() throws InterruptedException {
        List<Object[]> clients = connectionManager
                .getLobby()
                .queryAll(new ActualField(GlobalMessage.CLIENTS),
                        new FormalField(Integer.class),
                        new FormalField(Integer.class)
                );
        return clients.stream().map(c -> (int) c[1]).collect(Collectors.toList());
    }

    public void listenForEntityRequests(List<Entity> entities) {
        gameState.gameThreads.startHandledThread(() -> {
            while (true) {
                checkRequest(entities);
            }
        });
    }

    public void add(List<Entity> entities) {
        try {
            List<Integer> clientIds = getClientIds();
            addNewEntities(StaticEntityMessage.NEW_ENTITY, entities);
            sendNotifications(StaticEntityMessage.ADDED_ENTITIES, clientIds);
            receiveConfirmations(StaticEntityMessage.RECEIVED_ENTITIES, clientIds);
            clearSpaceFromNewEntities(StaticEntityMessage.NEW_ENTITY);
        } catch (InterruptedException e) {
            throw new RuntimeException("Unable to send static entities");
        }
    }

    public void checkRequest(List<Entity> entities) {
        try {
            Object[] receivedEntity =  receiveEntityRequest(StaticEntityMessage.REQUEST_ENTITY);
            Entity entity = convert.apply(receivedEntity);
            int clientId = (int) receivedEntity[3];
            if (entities.contains(entity)) {
                entities.remove(entity);
               sendAnswer(StaticEntityMessage.ANSWER_CLIENT, StaticEntityMessage.GIVE_ENTITY, clientId);
               remove(entity, clientId);
            } else {
                sendAnswer(StaticEntityMessage.ANSWER_CLIENT, StaticEntityMessage.DENY_ENTITY, clientId);
            }
        } catch (InterruptedException e) {
            throw new RuntimeException("Could not check static entity request");
        }
    }

    public void remove(Entity entity, Integer removerClientId) {
        try {
            List<Integer> clientIds = getClientIds();
            sendEntityNotifications(StaticEntityMessage.REMOVE_ENTITY, entity, removerClientId, clientIds);
        } catch (InterruptedException e) {
            throw new RuntimeException("Unable to notify clients to remove entity");
        }
    }

    private void addNewEntities(String newEntities, List<Entity> entities) throws InterruptedException {
        for (Entity entity : entities)
            entitySpace.put(newEntities, entity.getX(), entity.getY());
    }

    private void sendNotifications(String notification, List<Integer> clientIds) throws InterruptedException {
        for (int clientId : clientIds)
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

    private void sendAnswer(String answerMarker, String answer, int clientId) throws InterruptedException {
        entitySpace.put(answerMarker, answer, clientId);
    }

    private Object[] receiveEntityRequest(String request) throws InterruptedException {
        return entitySpace.get(new ActualField(request),
                new FormalField(Integer.class),
                new FormalField(Integer.class),
                new FormalField(Integer.class));
    }

}
