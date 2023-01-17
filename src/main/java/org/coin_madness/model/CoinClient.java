package org.coin_madness.model;

import javafx.application.Platform;
import org.coin_madness.helpers.Action;
import org.coin_madness.helpers.ConnectionManager;
import org.coin_madness.helpers.ScopedThreads;
import org.coin_madness.messages.StaticEntityMessage;
import org.jspace.Space;

import java.util.HashMap;
import java.util.Objects;
import java.util.function.Function;

public class CoinClient extends StaticEntityClient<Coin> {

    private Function<Object[], Coin> convert;

    public CoinClient(Space entitySpace, GameState gameState, Function<Object[], Coin> convert) {
        super(entitySpace, gameState, convert);
        this.convert = convert;
    }

    public void request(Coin coin, Action given, Action denied) {
        try {
            sendEntityRequest(StaticEntityMessage.REQUEST_ENTITY, coin);
            String answer = receiveAnswer(StaticEntityMessage.ANSWER_MARKER);
            if (Objects.equals(answer, StaticEntityMessage.GIVE_ENTITY)) {
                given.handle();
            } else {
                denied.handle();
            }
        } catch (InterruptedException e) {
            throw new RuntimeException("Could not request static entity");
        }
    }

    public void remove() throws InterruptedException {
        Object[] receivedEntity = super.receiveEntityNotification(StaticEntityMessage.REMOVE_ENTITY);
        Coin coin = convert.apply(receivedEntity);
        int clientId = (int) receivedEntity[3];
        removeEntity(coin, clientId);
    }

    private void removeEntity(Coin coin, int clientId) {
        removeEntity(coin);
        if(gameState.networkedPlayers.containsKey(clientId)) {
            Platform.runLater(() -> {
                Player net = gameState.networkedPlayers.get(clientId);
                net.setAmountOfCoins(net.getAmountOfCoins() + 1);
            });
        }
    }

}
