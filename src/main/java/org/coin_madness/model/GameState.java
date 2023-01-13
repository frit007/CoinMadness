package org.coin_madness.model;

import org.coin_madness.helpers.ConnectionManager;
import org.coin_madness.helpers.ScopedThreads;

import java.util.*;

public class GameState {
    public ConnectionManager connectionManager;

    public final Map<Integer, Player> networkedPlayers = new HashMap<>();

    public Field[][] map;

    public CoinClient coinClient;

    public StaticEntityClient<Chest> chestClient;

    public StaticEntityClient<Traphole> trapholeClient;

    public DeathClient deathClient;

    public Player localPlayer;

    public List<Player> allPlayers() {
        List<Player> players = new ArrayList<>();
        players.add(localPlayer);
        players.addAll(networkedPlayers.values());
        return players;
    }

    public final ScopedThreads gameThreads = new ScopedThreads(() -> {});

}
