package org.coin_madness.model;

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

    public CoinClient(ConnectionManager connectionManager, Space entitySpace,
                      ScopedThreads staticEntityThreads, Field[][] map, Function<Object[], Coin> convert) {
        super(connectionManager, entitySpace, staticEntityThreads, map, convert);
        this.convert = convert;
    }

    public void request(Coin coin, Action given, Action denied) {
        try {
            super.sendEntityRequest(StaticEntityMessage.REQUEST_ENTITY, coin);
            String answer = super.receiveAnswer(StaticEntityMessage.ANSWER_CLIENT);
            if (Objects.equals(answer, StaticEntityMessage.GIVE_ENTITY)) {
                given.handle();
            } else {
                denied.handle();
            }
        } catch (InterruptedException e) {
            throw new RuntimeException("Could not request static entity");
        }
    }

    public void remove(HashMap<Integer, Player> networkPlayers) {
        try {
            Object[] receivedEntity = super.receiveEntityNotification(StaticEntityMessage.REMOVE_ENTITY);
            Coin coin = convert.apply(receivedEntity);
            int clientId = (int) receivedEntity[2];
            removeEntity(coin, clientId, networkPlayers);
        } catch (InterruptedException e) {
            throw new RuntimeException("Unable to remove coin");
        }
    }

    private void removeEntity(Coin coin, int clientId, HashMap<Integer, Player> networkPlayers) {
        super.removeEntity(coin);
        if(networkPlayers.containsKey(clientId)) {
            Player net = networkPlayers.get(clientId);
            net.setAmountOfCoins(net.getAmountOfCoins() + 1);
        }
    }

}
