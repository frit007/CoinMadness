package org.coin_madness.model;

import javafx.application.Platform;
import org.coin_madness.components.ChestDrawer;
import org.coin_madness.messages.GlobalMessage;
import org.coin_madness.messages.StaticEntityMessage;
import org.jspace.ActualField;
import org.jspace.FormalField;
import org.jspace.Space;

import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.Random;
import java.util.function.Function;
import java.util.stream.Collectors;

public class ChestClient extends StaticEntityClient<Chest> {

    private Random rand = new Random();
    private int serverId;
    private int clientId;
    private GameState gameState;

    public ChestClient(Space entitySpace, GameState gameState, Function<Object[], Chest> convert) {
        super(entitySpace, gameState, convert);
        this.serverId = gameState.connectionManager.getServerId();
        this.clientId = gameState.connectionManager.getClientId();
        this.gameState = gameState;
    }

    // from StaticEntityServer!
    private List<Integer> getClientIds() throws InterruptedException {
        List<Object[]> clients = gameState.connectionManager.getLobby()
                                                            .queryAll(new ActualField(GlobalMessage.CLIENTS),
                                                                      new FormalField(Integer.class),
                                                                      new FormalField(Integer.class));
        return clients.stream().map(c -> (int) c[1]).collect(Collectors.toList());
    }

    public void listenForChestChanges() {
        gameState.gameThreads.startHandledThread("verify coin", () -> {
            while (true) {
                verifyCoins();
            }
        });

        gameState.gameThreads.startHandledThread("update player", () -> {
            while (true) {
                updatePlayerScore();
            }
        });

        gameState.gameThreads.startHandledThread("update chest", () -> {
            while (true) {
                updateChest();
            }
        });
    }

    public void updateChest() {
        try {
            Object[] updatedChest = sendReceiveUpdatedChest(StaticEntityMessage.UPDATE_ENTITY);
            int x = (int) updatedChest[1];
            int y = (int) updatedChest[2];
            int newAmount = (int) updatedChest[3];

            Platform.runLater(() -> {
                Optional<Entity> entity = gameState.map[x][y].getEntities().stream()
                                                                           .filter(e -> e instanceof Chest)
                                                                           .findFirst();
                if (entity.isPresent()) {
                    Chest chest = (Chest) entity.get();
                    chest.setAmountOfCoins(newAmount);
                } else {
                    throw new RuntimeException("Unable to update coins in chest");
                }
            });
        } catch (InterruptedException e) {
            throw new RuntimeException("Unable to update coins in chest");
        }
    }

    public void updatePlayerScore() {
        try {
            Object[] updatedScore = receiveUpdatedPlayerScore(StaticEntityMessage.UPDATE_PLAYER_SCORE);
            int playerId = (int) updatedScore[1];
            int newCoins = (int) updatedScore[2];
            int newScore = (int) updatedScore[3];
            System.out.println("Client " + gameState.connectionManager.getClientId());
            System.out.println("Update a score : " + playerId + " : " + newCoins + " : " + newScore);
            if (gameState.networkedPlayers.containsKey(playerId)) {
                Platform.runLater(() -> {
                    System.out.println("actually do it?");
                    Player net = gameState.networkedPlayers.get(playerId);
                    net.setScore(newScore);
                    net.setAmountOfCoins(newCoins);
                });
            }
        } catch (InterruptedException e) {
            throw new RuntimeException("Unable to update player score");
        }
    }

    public void verifyCoins() throws InterruptedException {
        while (true) {
            String postulate = receiveAnswer(StaticEntityMessage.WHILE_STATEMENT_OTHER_CLIENT);
            if (Objects.equals(postulate, StaticEntityMessage.CONTINUE)) {
                String chestNotFull = receiveAnswer(StaticEntityMessage.IF_STATEMENT_OTHER_CLIENT);
                if (Objects.equals(chestNotFull, StaticEntityMessage.THEN)) {
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
            int otherClient = clientIds.size() > 0 ? clientIds.get(rand.nextInt(clientIds.size())) : serverId;

            while (player.getAmountOfCoins() > 0) {
                sendAnswer(StaticEntityMessage.WHILE_STATEMENT_SERVER, StaticEntityMessage.CONTINUE, serverId);
                sendAnswer(StaticEntityMessage.WHILE_STATEMENT_OTHER_CLIENT, StaticEntityMessage.CONTINUE, otherClient);
                sendClientId(StaticEntityMessage.SEND_CLIENTID, otherClient, serverId);
                sendEntityRequest(StaticEntityMessage.SEND_CHEST, chest); //Could send to serverId in sendEntityRequest?
                String chestNotFull = receiveAnswer(StaticEntityMessage.IF_STATEMENT_CLIENT);
                if (Objects.equals(chestNotFull, StaticEntityMessage.THEN)) {
                    sendClientId(StaticEntityMessage.SEND_CLIENTID, clientId, otherClient);
                    String canVerifyCoin = receiveAnswer(StaticEntityMessage.IF_STATEMENT_2);
                    if (Objects.equals(canVerifyCoin, StaticEntityMessage.THEN)) {
                        receiveNotification(StaticEntityMessage.ACCEPT_ENTITY);
                        player.setAmountOfCoins(player.getAmountOfCoins() - 1);
                        sendCoin(StaticEntityMessage.SEND_ENTITY,1, serverId);
                        player.setScore(player.getScore() + 100);
                        sendUpdatePlayer(StaticEntityMessage.UPDATE_PLAYER_SCORE, player, getClientIds());
                        //TODO: chest animation
                    } else {
                        receiveNotification(StaticEntityMessage.DENY_ENTITY);
                    }
                } else break;
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
        Object[] recievedClientId = entitySpace.get(new ActualField(marker),
                                                    new FormalField(Integer.class),
                                                    new ActualField(clientId));
        return (int) recievedClientId[1];
    }

    private void sendBool(String marker, Boolean bool, int clientId) throws InterruptedException {
        entitySpace.put(marker, bool, clientId);
    }

    private boolean hasACoin(int clientId) {
        if (gameState.networkedPlayers.containsKey(clientId)) {
            Player net = gameState.networkedPlayers.get(clientId);
            return net.getAmountOfCoins() > 0;
        } else return gameState.localPlayer.getAmountOfCoins() > 0;
    }

    private void sendUpdatePlayer(String notification, Player player, List<Integer> clientIds) throws InterruptedException {
        for (int clientId : clientIds) {
            entitySpace.put(notification, player.getId(), player.getAmountOfCoins(), player.getScore(), clientId);
        }
    }

    private Object[] receiveUpdatedPlayerScore(String notification) throws InterruptedException {
        return entitySpace.get(new ActualField(notification),
                               new FormalField(Integer.class),
                               new FormalField(Integer.class),
                               new FormalField(Integer.class),
                               new ActualField(clientId));
    }

    private Object[] sendReceiveUpdatedChest(String notification) throws InterruptedException {
        return entitySpace.get(new ActualField(notification),
                               new FormalField(Integer.class),
                               new FormalField(Integer.class),
                               new FormalField(Integer.class),
                               new ActualField(clientId));
    }


}
