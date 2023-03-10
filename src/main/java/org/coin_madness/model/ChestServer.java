package org.coin_madness.model;

import org.coin_madness.helpers.ConnectionManager;
import org.coin_madness.messages.StaticEntityMessage;
import org.jspace.ActualField;
import org.jspace.FormalField;
import org.jspace.Space;

import java.util.*;
import java.util.function.Function;
import java.util.stream.Collectors;

public class ChestServer extends StaticEntityServer<Chest> {

    private Random rand = new Random();
    private ConnectionManager connectionManager;
    private Space entitySpace;
    private Servers servers;
    private int clientId;
    private Function<Object[], Chest> convert;
    private GameState gameState;

    public ChestServer(GameState gameState, Space entitySpace, StaticEntityCommon common, Function<Object[], Chest> convert, Servers servers) {
        super(gameState, entitySpace, common, convert);
        this.connectionManager = gameState.connectionManager;
        this.entitySpace = entitySpace;
        this.servers = servers;
        this.clientId = connectionManager.getClientId();
        this.convert = convert;
        this.gameState = gameState;
    }

    public void listenForChestRequests() {
        gameState.gameThreads.startHandledThread("accept coins", () -> {
            while (true) {
                acceptCoins();
            }
        });
    }

    // this part is excluded from the protocol
    private void onChestFull(Chest chest) throws InterruptedException {
        StaticEntityPlacer placer = new StaticEntityPlacer(gameState);

        remove(chest, gameState.connectionManager.getServerId());

        servers.enemyServer.increaseDifficulty();
        servers.coinServer.add(placer.placeCoins(chest.getMaxCoins()));
        add(placer.placeChests(1));
    }

    public void acceptCoins() throws InterruptedException {
        while (true) {
            String postulate = common.receiveAnswer(StaticEntityMessage.WHILE_STATEMENT);
            if (Objects.equals(postulate, StaticEntityMessage.CONTINUE)) {

                int fromClientId = common.receiveClientId(StaticEntityMessage.SEND_CLIENTID_SERVER);
                Object[] receivedChest = receiveEntityRequest(StaticEntityMessage.SEND_ENTITY);
                Chest chestId = convert.apply(receivedChest);
                Optional<Chest> chest = entities.stream().filter(c -> Objects.equals(c, chestId)).findFirst();

                if (chest.isPresent() && chest.get().getAmountOfCoins() + 1 <= chest.get().getMaxCoins()) {
                    common.sendAnswer(StaticEntityMessage.IF_STATEMENT, StaticEntityMessage.THEN, fromClientId);

                    List<Integer> clientIds = common.getClientIds();
                    int witnessClient = pickRandomClient(fromClientId, clientIds);
                    common.sendClientId(StaticEntityMessage.SEND_CLIENTID_WITNESS, fromClientId, witnessClient);
                    Boolean isVerified = receiveVerification(StaticEntityMessage.VERIFIED);

                    if (isVerified) {
                        common.sendAnswer(StaticEntityMessage.IF_STATEMENT, StaticEntityMessage.THEN, fromClientId);

                        sendNotification(StaticEntityMessage.ACCEPT_ENTITY, fromClientId);
                        int coin = receiveCoin(StaticEntityMessage.SEND_ENTITY);
                        chest.get().setAmountOfCoins(chest.get().getAmountOfCoins() + coin);
                        sendUpdateChest(StaticEntityMessage.UPDATE_ENTITY, chest.get(), clientIds);

                        if(chest.get().getAmountOfCoins() == chest.get().getMaxCoins()) {
                            onChestFull(chest.get());
                        }
                    } else {
                        common.sendAnswer(StaticEntityMessage.IF_STATEMENT, StaticEntityMessage.ELSE, fromClientId);
                        sendNotification(StaticEntityMessage.DENY_ENTITY, fromClientId);
                        break;
                    }
                } else {
                    common.sendAnswer(StaticEntityMessage.IF_STATEMENT, StaticEntityMessage.ELSE, fromClientId);
                    break;
                }
            } else break;
        }
    }

    private Boolean receiveVerification(String answerMarker) throws InterruptedException {
        Object[] answer = entitySpace.get(new ActualField(answerMarker),
                new FormalField(Boolean.class),
                new ActualField(clientId));
        return (boolean) answer[1];
    }

    private int receiveCoin(String marker) throws InterruptedException {
        Object[] receivedCoin = entitySpace.get(new ActualField(marker),
                                                new FormalField(Integer.class),
                                                new ActualField(clientId));
        return (int) receivedCoin[1];
    }

    private void sendUpdateChest(String notification, Chest chest, List<Integer> clientIds) throws InterruptedException {
        for (int clientId : clientIds) {
            entitySpace.put(notification, chest.getX(), chest.getY(), chest.getAmountOfCoins(), clientId);
        }
    }

    private int pickRandomClient(int clientToCheck, List<Integer> clientIds) {
        List<Integer> otherClients = clientIds.stream()
                .filter(c -> c != clientToCheck)
                .collect(Collectors.toList());
        return otherClients.size() > 0 ? otherClients.get(rand.nextInt(otherClients.size())) : clientToCheck;
    }

}
