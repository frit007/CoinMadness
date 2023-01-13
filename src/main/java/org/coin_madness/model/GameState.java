package org.coin_madness.model;

import org.coin_madness.helpers.ConnectionManager;
import org.coin_madness.helpers.ScopedThreads;

import java.util.HashMap;
import java.util.Map;

public class GameState {
    public ConnectionManager connectionManager;
    public final Map<Integer, Player> networkedPlayers = new HashMap<>();

    public Field[][] map;
    public CoinClient coinClient;

    public StaticEntityClient<Chest> chestClient;

    public StaticEntityClient<Traphole> trapholeClient;
    public final ScopedThreads gameThreads = new ScopedThreads(() -> {});

}
