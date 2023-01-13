package org.coin_madness.model;

import org.coin_madness.helpers.ConnectionManager;
import org.coin_madness.helpers.ScopedThreads;
import org.coin_madness.messages.GlobalMessage;
import org.coin_madness.messages.StaticEntityMessage;
import org.jspace.ActualField;
import org.jspace.FormalField;
import org.jspace.Space;

import java.util.List;
import java.util.Random;
import java.util.function.Function;
import java.util.stream.Collectors;

public class ChestClient extends StaticEntityClient<Chest> {

    private ConnectionManager connectionManager;
    private Space entitySpace;
    private Random rand = new Random();
    private int serverId;
    private int clientId;

    public ChestClient(ConnectionManager connectionManager, Space entitySpace,
                       ScopedThreads staticEntityThreads, Field[][] map, Function<Object[], Chest> convert) {
        super(connectionManager, entitySpace, staticEntityThreads, map, convert);
        this.connectionManager = connectionManager;
        this.entitySpace = entitySpace;
        this.serverId = connectionManager.getServerId();
        this.clientId = connectionManager.getClientId();
    }

    // from StaticEntityServer!
    private List<Integer> getClientIds() throws InterruptedException {
        List<Object[]> clients = connectionManager.getLobby().queryAll(new ActualField(GlobalMessage.CLIENTS),
                new FormalField(Integer.class));
        return clients.stream().map(c -> (int) c[1]).collect(Collectors.toList());
    }

    public void listenForChestChanges() {
        staticEntityThreads.startHandledThread(() -> {
            while (true) {
                verifyCoins();
            }
        });

        staticEntityThreads.startHandledThread(() -> {
            while (true) {
                updatePlayerScore();
            }
        });
    }

    public void updatePlayerScore() {
        try {
            Object[] updatedScore = receiveUpdatedPlayerScore(StaticEntityMessage.UPDATE_PLAYER_SCORE);
            int playerId = (int) updatedScore[1];
            int newCoins = (int) updatedScore[2];
            int newScore = (int) updatedScore[3];
            //Get networkPlayer map from gameState and update the score and coins.
        } catch (InterruptedException e) {
            throw new RuntimeException("Unable to update player score");
        }
    }

    public void verifyCoins() throws InterruptedException {
        while (true) {
            String postulate = receiveAnswer(StaticEntityMessage.ANSWER_MARKER);
            if (postulate == StaticEntityMessage.CONTINUE) {
                String chestNotFull = receiveAnswer(StaticEntityMessage.ANSWER_MARKER);
                if (chestNotFull == StaticEntityMessage.THEN) {
                    int clientId = receiveClientId(StaticEntityMessage.SEND_CLIENTID);
                    sendBool(StaticEntityMessage.ANSWER_MARKER, hasACoin(clientId), serverId);
                } else break;
            } else break;
        }
    }

    // server id might simplify one other communication
    public void placeCoins(Chest chest, Player player) {
        try {
            List<Integer> clientIds = getClientIds().stream()
                                                    .filter(c -> c != serverId)
                                                    .collect(Collectors.toList());
            int otherClient = clientIds.get(rand.nextInt(clientIds.size()));

            while (player.getAmountOfCoins() > 0) {
                sendAnswer(StaticEntityMessage.ANSWER_MARKER, StaticEntityMessage.CONTINUE, serverId);
                sendAnswer(StaticEntityMessage.ANSWER_MARKER, StaticEntityMessage.CONTINUE, otherClient);
                sendClientId(StaticEntityMessage.SEND_CLIENTID, otherClient, serverId);
                sendEntityRequest(StaticEntityMessage.SEND_CHEST, chest); //Could send to serverId in sendEntityRequest?
//                for (int clientId : clientIds) {
//                    sendAnswer(StaticEntityMessage.ANSWER_MARKER, StaticEntityMessage.CONTINUE, otherClient);
//                }
                String chestNotFull = receiveAnswer(StaticEntityMessage.ANSWER_MARKER);
                if (chestNotFull == StaticEntityMessage.THEN) {
                    String canVerifyCoin = receiveAnswer(StaticEntityMessage.ANSWER_MARKER);
                    if (canVerifyCoin == StaticEntityMessage.THEN) {
                        receiveNotification(StaticEntityMessage.SEND_ENTITY);
                        player.setAmountOfCoins(player.getAmountOfCoins() - 1);
                        sendCoin(StaticEntityMessage.SEND_ENTITY,1, serverId);
                        player.setScore(player.getScore() + 100);
                        sendUpdatePlayer(StaticEntityMessage.UPDATE_PLAYER_SCORE, player, clientIds);
                        //TODO: Play animation?
                    } else {
                        receiveNotification(StaticEntityMessage.DENY_ENTITY);
                    }
                }
            }
        } catch (InterruptedException e) {
            throw new RuntimeException("Unable to place coins in chest");
        }
    }

    // From StaticEntityServer
    protected void sendAnswer(String answerMarker, String answer, int clientId) throws InterruptedException {
        entitySpace.put(answerMarker, answer, clientId);
    }

    private void sendCoin(String marker, int amount, int clientId) throws InterruptedException {
        entitySpace.put(marker, amount, clientId);
    }

    private void sendClientId(String marker, int clientId, int toClientId) throws InterruptedException {
        entitySpace.put(marker, clientId, toClientId);
    }

    private int receiveClientId(String marker) throws InterruptedException {
        Object[] clientIdRes = entitySpace.get(new ActualField(marker), new FormalField(Integer.class));
        return (int) clientIdRes[1];
    }

    private void sendBool(String marker, Boolean bool, int clientId) throws InterruptedException {
        entitySpace.put(marker, bool, clientId);
    }

    private boolean hasACoin(int clientId) {
        //TODO: Get networkPlayer and check their amount of coins
        return true;
    }
    private void sendUpdatePlayer(String notification, Player player, List<Integer> clientIds) throws InterruptedException {
        for (int clientId : clientIds) {
            entitySpace.put(notification, player.getId(), player.getAmountOfCoins(), player.getScore(), clientId);
        }
    }

    private Object[] receiveUpdatedPlayerScore(String notification) throws InterruptedException {
        return entitySpace.get(new ActualField(notification), new FormalField(Integer.class), new FormalField(Integer.class), new FormalField(Integer.class), new ActualField(clientId));
    }


}
