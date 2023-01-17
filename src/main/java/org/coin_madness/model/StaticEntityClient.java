package org.coin_madness.model;

import javafx.application.Platform;
import org.coin_madness.messages.StaticEntityMessage;
import org.jspace.ActualField;
import org.jspace.FormalField;
import org.jspace.Space;

import java.util.List;
import java.util.function.Function;
import java.util.stream.Collectors;

public class StaticEntityClient<Entity extends StaticEntity> {

    protected final GameState gameState;
    protected int clientId;
    protected Space entitySpace;
    protected Function<Object[], Entity> convert;

    public StaticEntityClient(Space entitySpace, GameState gameState, Function<Object[], Entity> convert) {
        this.clientId = gameState.connectionManager.getClientId();
        this.entitySpace = entitySpace;
        this.convert = convert;
        this.gameState = gameState;
    }

    public void listenForChanges() {
        gameState.gameThreads.startHandledThread(() -> {
            while (true) {
                add();
            }
        });

        gameState.gameThreads.startHandledThread(() -> {
            while (true) {
                remove();
            }
        });
    }

    public void add() {
        try {
            receiveNotification(StaticEntityMessage.ADDED_ENTITIES);
            List<Entity> entities = receiveEntities(StaticEntityMessage.NEW_ENTITY);
            placeEntities(entities);
            sendConfirmation(StaticEntityMessage.RECEIVED_ENTITIES);
        } catch (InterruptedException e) {
            throw new RuntimeException("Unable to receive static entities");
        }
    }

    public void remove() {
        try {
            Object[] receivedEntity = receiveEntityNotification(StaticEntityMessage.REMOVE_ENTITY);
            Entity entity = convert.apply(receivedEntity);
            removeEntity(entity);
        } catch (InterruptedException e) {
            throw new RuntimeException("Unable to remove entity");
        }
    }

    protected void receiveNotification(String notification) throws InterruptedException {
        entitySpace.get(new ActualField(notification),
                        new ActualField(clientId));
    }

    private List<Entity> receiveEntities(String newEntities) throws InterruptedException {
        List<Object[]> receivedEntities = entitySpace
                .queryAll(new ActualField(newEntities),
                        new FormalField(Integer.class),
                        new FormalField(Integer.class));

        return receivedEntities.stream().map(convert).collect(Collectors.toList());
    }

    private void placeEntities(List<Entity> entities) {
        for (Entity entity : entities)
            Platform.runLater(() -> gameState.map[entity.getX()][entity.getY()].addEntity(entity));
    }

    private void sendConfirmation(String confirmation) throws InterruptedException {
        entitySpace.put(confirmation, clientId);
    }

    protected void sendEntityRequest(String notification, Entity entity) throws InterruptedException {
        entitySpace.put(notification, entity.getX(), entity.getY(), clientId);
    }

    protected String receiveAnswer(String answerMarker) throws InterruptedException {
        Object[] answer = entitySpace.get(new ActualField(answerMarker),
                                          new FormalField(String.class),
                                          new ActualField(clientId));
        return answer[1].toString();
    }

    protected Object[] receiveEntityNotification(String notification) throws InterruptedException {
        return entitySpace.get(new ActualField(notification),
                               new FormalField(Integer.class),
                               new FormalField(Integer.class),
                               new FormalField(Integer.class),
                               new ActualField(clientId));
    }

    protected void removeEntity(Entity entity) {
        Platform.runLater(() -> gameState.map[entity.getX()][entity.getY()].removeEntity(entity));
    }

}
