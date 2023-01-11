package org.coin_madness.model;

import org.coin_madness.helpers.Action;
import org.coin_madness.helpers.ConnectionManager;
import org.coin_madness.helpers.ScopedThreads;
import org.coin_madness.messages.StaticEntityMessage;
import org.jspace.ActualField;
import org.jspace.FormalField;
import org.jspace.Space;

import java.util.List;
import java.util.Objects;
import java.util.function.Function;
import java.util.stream.Collectors;

public class StaticEntityClient<Entity extends StaticEntity> {

    private String clientId;
    private Space entitySpace;
    private Field[][] map;
    private Function<Object[], Entity> convert;
    ScopedThreads staticEntityThreads;

    public StaticEntityClient(ConnectionManager connectionManager, Space entitySpace,
                              ScopedThreads staticEntityThreads, Field[][] map, Function<Object[], Entity> convert) {
        this.clientId = connectionManager.getClientId();
        this.entitySpace = entitySpace;
        this.staticEntityThreads = staticEntityThreads;
        this.map = map;
        this.convert = convert;
    }

    public void listenForChanges(List<Entity> entities) {
        staticEntityThreads.startHandledThread(() -> {
            while (true) {
                add(entities);
            }
        });

        staticEntityThreads.startHandledThread(() -> {
            while (true) {
                remove(entities);
            }
        });
    }

    public void add(List<Entity> entities) {
        try {
            receiveNotification(StaticEntityMessage.ADDED_ENTITIES);
            receiveEntities(StaticEntityMessage.NEW_ENTITY, entities);
            placeEntities(entities);
            sendConfirmation(StaticEntityMessage.RECEIVED_ENTITIES);
        } catch (InterruptedException e) {
            throw new RuntimeException("Unable to receive static entities");
        }
    }

    public void request(Entity entity, Action given, Action denied) {
        try {
            sendEntityRequest(StaticEntityMessage.REQUEST_ENTITY, entity);
            String answer = receiveAnswer(StaticEntityMessage.ANSWER_CLIENT);
            if(Objects.equals(answer, StaticEntityMessage.GIVE_ENTITY)) {
                given.handle();
            } else {
                denied.handle();
            }
        } catch (InterruptedException e) {
            throw new RuntimeException("Could not request static entity");
        }
    }

    public void remove(List<Entity> entities) {
        try {
            Entity entity = receiveEntityNotification(StaticEntityMessage.REMOVE_ENTITY);
            removeEntity(entity, entities);
        } catch (InterruptedException e) {
            throw new RuntimeException("Unable to remove entity");
        }
    }

    private void receiveNotification(String notification) throws InterruptedException {
        entitySpace.get(new ActualField(notification),
                        new ActualField(clientId));
    }

    private void receiveEntities(String newEntities, List<Entity> entities) throws InterruptedException {
        List<Object[]> receivedEntities = entitySpace
                .queryAll(new ActualField(newEntities),
                        new FormalField(Integer.class),
                        new FormalField(Integer.class));

        entities.addAll(receivedEntities.stream().map(convert).collect(Collectors.toList()));
    }

    private void placeEntities(List<Entity> entities) {
        for (Entity entity : entities)
            map[entity.getX()][entity.getY()].addEntity(entity);
    }

    private void sendConfirmation(String confirmation) throws InterruptedException {
        entitySpace.put(confirmation, clientId);
    }

    private void sendEntityRequest(String notification, Entity entity) throws InterruptedException {
        entitySpace.put(notification, entity.getX(), entity.getY(), clientId);
    }

    private String receiveAnswer(String notification) throws InterruptedException {
        Object[] answer = entitySpace.get(new ActualField(notification),
                                          new FormalField(String.class),
                                          new ActualField(clientId));
        return answer[1].toString();
    }

    private Entity receiveEntityNotification(String notification) throws InterruptedException {
        Object[] receivedEntity = entitySpace.get(new ActualField(notification),
                                                  new FormalField(Integer.class),
                                                  new FormalField(Integer.class),
                                                  new ActualField(clientId));
        return convert.apply(receivedEntity);
    }

    private void removeEntity(Entity entity, List<Entity> entities) {
        map[entity.getX()][entity.getY()].removeEntity(entity);
        entities.remove(entity);
    }

}
