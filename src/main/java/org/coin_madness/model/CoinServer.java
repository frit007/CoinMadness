package org.coin_madness.model;

import org.coin_madness.helpers.ConnectionManager;
import org.coin_madness.helpers.ScopedThreads;
import org.coin_madness.messages.StaticEntityMessage;
import org.jspace.Space;

import java.util.List;
import java.util.function.Function;

public class CoinServer extends StaticEntityServer<Coin> {

    private Function<Object[], Coin> convert;

    public CoinServer(ConnectionManager connectionManager, Space entitySpace, ScopedThreads staticEntityThreads, Function<Object[], Coin> convert) {
        super(connectionManager, entitySpace, staticEntityThreads, convert);
        this.convert = convert;
    }

    public void listenForCoinRequests(List<Coin> entities) {
        staticEntityThreads.startHandledThread(() -> {
            while (true) {
                checkRequest(entities);
            }
        });
    }

    public void checkRequest(List<Coin> entities) {
        try {
            Object[] receivedEntity =  receiveEntityRequest(StaticEntityMessage.REQUEST_ENTITY);
            Coin coin = convert.apply(receivedEntity);
            int clientId = (int) receivedEntity[3];
            if (entities.contains(coin)) {
                entities.remove(coin);
                sendAnswer(StaticEntityMessage.ANSWER_MARKER, StaticEntityMessage.GIVE_ENTITY, clientId);
                remove(coin);
            } else {
                sendAnswer(StaticEntityMessage.ANSWER_MARKER, StaticEntityMessage.DENY_ENTITY, clientId);
            }
        } catch (InterruptedException e) {
            throw new RuntimeException("Could not check static entity request");
        }
    }

}
