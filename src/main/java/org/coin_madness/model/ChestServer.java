package org.coin_madness.model;

import org.coin_madness.helpers.ConnectionManager;
import org.coin_madness.helpers.ScopedThreads;
import org.coin_madness.messages.StaticEntityMessage;
import org.jspace.ActualField;
import org.jspace.FormalField;
import org.jspace.Space;

import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.function.Function;

public class ChestServer extends StaticEntityServer<Chest> {

    private ConnectionManager connectionManager;
    private Space entitySpace;
    private int clientId;
    private Function<Object[], Chest> convert;

    public ChestServer(ConnectionManager connectionManager, Space entitySpace, ScopedThreads staticEntityThreads, Function<Object[], Chest> convert) {
        super(connectionManager, entitySpace, staticEntityThreads, convert);
        this.connectionManager = connectionManager;
        this.entitySpace = entitySpace;
        this.clientId = connectionManager.getClientId();
    }

    public void listenForChestRequests(List<Chest> entities) {
        staticEntityThreads.startHandledThread(() -> {
            while (true) {
                acceptCoins(entities);
            }
        });
    }

    public void acceptCoins(List<Chest> chests) {
        try {
            List<Integer> clientIds = getClientIds();
            while (true) {
                String postulate = receiveAnswer(StaticEntityMessage.ANSWER_MARKER);
                if (postulate == StaticEntityMessage.CONTINUE) {
                    int otherClientId = receiveClientId(StaticEntityMessage.SEND_CLIENTID);
                    Object[] receivedChest = receiveEntityRequest(StaticEntityMessage.SEND_CHEST);
                    Chest chestId = convert.apply(receivedChest);
                    Optional<Chest> chest = chests.stream().filter(c -> Objects.equals(c, chestId)).findFirst();
                    int fromClientId = (int) receivedChest[3];
                    if (chest.isPresent() && chest.get().getAmountOfCoins() + 1 <= chest.get().getMaxCoins()) {
                        sendAnswer(StaticEntityMessage.ANSWER_MARKER, StaticEntityMessage.THEN, fromClientId);
                        sendAnswer(StaticEntityMessage.ANSWER_MARKER, StaticEntityMessage.THEN, otherClientId);
                        Boolean isVerified = receiveBool(StaticEntityMessage.ANSWER_MARKER);
                        if (isVerified) {
                            sendAnswer(StaticEntityMessage.ANSWER_MARKER, StaticEntityMessage.THEN, fromClientId);
                            sendAnswer(StaticEntityMessage.ANSWER_MARKER, StaticEntityMessage.ACCEPT_ENTITY, fromClientId);
                            int coin = receiveCoin(StaticEntityMessage.SEND_ENTITY);
                            chest.get().setAmountOfCoins(chest.get().getAmountOfCoins() + coin);
                            //Send to everyone it was updated
                            sendUpdateChest(StaticEntityMessage.UPDATE_PLAYER_SCORE, chest.get(), clientIds);
                        } else {
                            sendAnswer(StaticEntityMessage.ANSWER_MARKER, StaticEntityMessage.ELSE, fromClientId);
                            sendAnswer(StaticEntityMessage.ANSWER_MARKER, StaticEntityMessage.DENY_ENTITY, fromClientId);
                            break;
                        }
                    } else {
                        sendAnswer(StaticEntityMessage.ANSWER_MARKER, StaticEntityMessage.ELSE, fromClientId);
                        sendAnswer(StaticEntityMessage.ANSWER_MARKER, StaticEntityMessage.ELSE, otherClientId);
                        break;
                    }
                }
            }
        } catch (InterruptedException e) {
            throw new RuntimeException("Unable to accept coins");
        }
    }

    // From Static Entity Client
    private String receiveAnswer(String answerMarker) throws InterruptedException {
        Object[] answer = entitySpace.get(new ActualField(answerMarker),
                new FormalField(String.class),
                new ActualField(clientId));
        return answer[1].toString();
    }

    private int receiveClientId(String marker) throws InterruptedException {
        Object[] recievedClientId = entitySpace.get(new ActualField(marker), new FormalField(Integer.class), new ActualField(clientId));
        return (int) recievedClientId[1];
    }

    private Boolean receiveBool(String answerMarker) throws InterruptedException {
        Object[] answer = entitySpace.get(new ActualField(answerMarker),
                new FormalField(Boolean.class),
                new ActualField(clientId));
        return (boolean) answer[1];
    }

    private int receiveCoin(String marker) throws InterruptedException {
        Object[] receivedCoin = entitySpace.get(new ActualField(marker), new FormalField(Integer.class), new ActualField(clientId));
        return (int) receivedCoin[1];
    }

    private void sendUpdateChest(String notification, Chest chest, List<Integer> clientIds) throws InterruptedException {
        for (int clientId : clientIds) {
            entitySpace.put(notification, chest.getX(), chest.getY(), chest.getAmountOfCoins(), clientId);
        }
    }

}
