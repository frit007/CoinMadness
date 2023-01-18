package org.coin_madness.model;

import org.coin_madness.messages.StaticEntityMessage;
import org.jspace.Space;

import java.util.function.Function;

public class CoinServer extends StaticEntityServer<Coin> {

    private Function<Object[], Coin> convert;
    private GameState gameState;

    public CoinServer(GameState gameState, Space entitySpace, Function<Object[], Coin> convert) {
        super(gameState, entitySpace, convert);
        this.convert = convert;
        this.gameState = gameState;
    }

    public void listenForCoinRequests() {
        gameState.gameThreads.startHandledThread("check coin requests", () -> {
            while (true) {
                checkRequest();
            }
        });
    }

    public void checkRequest() {
        try {
            Object[] receivedEntity =  receiveEntityRequest(StaticEntityMessage.REQUEST_ENTITY);
            Coin coin = convert.apply(receivedEntity);
            int clientId = (int) receivedEntity[3]; //more efficient by using server id?
            if (entities.contains(coin)) {
                entities.remove(coin);
                sendAnswer(StaticEntityMessage.ANSWER_MARKER, StaticEntityMessage.GIVE_ENTITY, clientId);
                remove(coin, clientId);
            } else {
                sendAnswer(StaticEntityMessage.ANSWER_MARKER, StaticEntityMessage.DENY_ENTITY, clientId);
            }
        } catch (InterruptedException e) {
            throw new RuntimeException("Could not check static entity request");
        }
    }

}
