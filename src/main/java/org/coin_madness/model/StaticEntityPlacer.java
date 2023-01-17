package org.coin_madness.model;

import java.util.*;
import java.util.stream.Collectors;

public class StaticEntityPlacer {

    private Random rand = new Random();
    private GameState gameState;
    private List<Field> remainingFields;

    public StaticEntityPlacer(GameState gameState) {
        this.gameState = gameState;
        remainingFields = Arrays.asList(gameState.map)
                                .stream()
                                .flatMap(Arrays::stream)
                                .filter(f -> !f.isWall())
                                .filter(f -> {
                                    Optional<Player> player = gameState.allPlayers()
                                                                       .stream()
                                                                       .filter(p -> p.getX() == f.getX()
                                                                                    && p.getY() == f.getY())
                                                                       .findFirst();
                                    return !player.isPresent();
                                })
                               .collect(Collectors.toList());
    }
    //TODO: trap players
    //TODO: create new static entities after

    public List<Coin> placeCoins(int amountOfCoins) {
        List<Coin> coins = new ArrayList<>();
        while(amountOfCoins > 0 && remainingFields.size() > 0) {
            int pos = rand.nextInt(remainingFields.size());
            Field field = remainingFields.remove(pos);
            Coin coin = new Coin(field.getX(), field.getY(), gameState.coinClient);
            coins.add(coin);
            amountOfCoins--;
        }
        return coins;
    }

    public List<Chest> placeChests(int amountOfChests) {
        ArrayList<Chest> chests = new ArrayList<>();
        while (amountOfChests > 0 && remainingFields.size() > 0) {
            int pos = rand.nextInt(remainingFields.size());
            Field field = remainingFields.remove(pos);
            Chest chest = new Chest(field.getX(), field.getY(), gameState.chestClient);
            chests.add(chest);
            amountOfChests--;
        }
        return chests;
    }

    public List<Traphole> placeTrapholes(int amountOfTrapholes) {
        ArrayList<Traphole> trapholes = new ArrayList<>();
        while(amountOfTrapholes > 0 && remainingFields.size() > 0) {
            int pos = rand.nextInt(remainingFields.size());
            Field field = remainingFields.remove(pos);
            Traphole traphole = new Traphole(field.getX(), field.getY(), gameState);
            trapholes.add(traphole);
            amountOfTrapholes--;
        }
        return trapholes;
    }

}
