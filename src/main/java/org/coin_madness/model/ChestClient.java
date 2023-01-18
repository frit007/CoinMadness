package org.coin_madness.model;

import javafx.application.Platform;
import org.coin_madness.messages.StaticEntityMessage;
import org.jspace.ActualField;
import org.jspace.FormalField;
import org.jspace.Space;

import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.function.Function;

public class ChestClient extends StaticEntityClient<Chest> {

    private int serverId;
    private int clientId;
    private GameState gameState;

    public ChestClient(GameState gameState, Space entitySpace, StaticEntityCommon common, Function<Object[], Chest> convert) {
        super(gameState, entitySpace, common, convert);
        this.serverId = gameState.connectionManager.getServerId();
        this.clientId = gameState.connectionManager.getClientId();
        this.gameState = gameState;
    }

    public void listenForChanges() {
        super.listenForChanges();
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

    @Override
    public void remove() throws InterruptedException {
        // chests are automatically removed when their animations are done
        Object[] receivedEntity = receiveEntityNotification(StaticEntityMessage.REMOVE_ENTITY);
        Chest entity = convert.apply(receivedEntity);

        Platform.runLater(() -> {
            for (Entity fieldEntity: gameState.map[entity.getX()][entity.getY()].getEntities()) {
                if(fieldEntity instanceof Chest) {
                    ((Chest) fieldEntity).addOnPendingAnimationDone(() -> {
                        removeEntity(entity);
                    });
                }
            }
        });
    }

    public void updateChest() throws InterruptedException {
        Object[] updatedChest = receiveUpdatedEntity(StaticEntityMessage.UPDATE_ENTITY);
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
            }
        });
    }

    public void updatePlayerScore() throws InterruptedException {
        Object[] updatedScore = receiveUpdatedEntity(StaticEntityMessage.UPDATE_PLAYER_SCORE);
        int playerId = (int) updatedScore[1];
        int newCoins = (int) updatedScore[2];
        int newScore = (int) updatedScore[3];
        if (gameState.networkedPlayers.containsKey(playerId)) {
            Platform.runLater(() -> {
                Player net = gameState.networkedPlayers.get(playerId);
                net.setScore(newScore);
                net.setAmountOfCoins(newCoins);
            });
        }
    }

    public void verifyCoins() throws InterruptedException {
        int checkClientId = common.receiveClientId(StaticEntityMessage.SEND_CLIENTID_WITNESS);
        sendVerification(StaticEntityMessage.VERIFIED, hasACoin(checkClientId), serverId);
    }

    public void depositCoin(Chest chest, Player player) {
        gameState.gameThreads.startHandledThread("deposit coins", () -> {
            List<Integer> clientIds = common.getClientIds();

            while (player.getAmountOfCoins() > 0) {
                common.sendAnswer(StaticEntityMessage.WHILE_STATEMENT, StaticEntityMessage.CONTINUE, serverId);

                common.sendClientId(StaticEntityMessage.SEND_CLIENTID_SERVER, clientId, serverId);
                sendEntityRequest(StaticEntityMessage.SEND_ENTITY, chest, serverId);

                String chestNotFull = common.receiveAnswer(StaticEntityMessage.IF_STATEMENT);
                if (Objects.equals(chestNotFull, StaticEntityMessage.THEN)) {

                    String canVerifyCoin = common.receiveAnswer(StaticEntityMessage.IF_STATEMENT);
                    if (Objects.equals(canVerifyCoin, StaticEntityMessage.THEN)) {

                        receiveNotification(StaticEntityMessage.ACCEPT_ENTITY);
                        player.setAmountOfCoins(player.getAmountOfCoins() - 1);
                        sendCoin(StaticEntityMessage.SEND_ENTITY,1, serverId);
                        player.setScore(player.getScore() + 100);
                        sendUpdatePlayer(StaticEntityMessage.UPDATE_PLAYER_SCORE, player, clientIds);
                    } else {
                        receiveNotification(StaticEntityMessage.DENY_ENTITY);
                        break;
                    }
                } else break;
            }
        });
    }

    private void sendCoin(String marker, int amount, int clientId) throws InterruptedException {
        entitySpace.put(marker, amount, clientId);
    }

    private void sendVerification(String marker, Boolean bool, int clientId) throws InterruptedException {
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

    private Object[] receiveUpdatedEntity(String notification) throws InterruptedException {
        return entitySpace.get(new ActualField(notification),
                               new FormalField(Integer.class),
                               new FormalField(Integer.class),
                               new FormalField(Integer.class),
                               new ActualField(clientId));
    }

}
