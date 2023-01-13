package org.coin_madness.model;

import java.util.*;
import java.util.stream.Collectors;

public class StaticEntityPlacer {

    private Random rand = new Random();

    public StaticEntityPlacer() {}
    //TODO: Make sure they are not in the same place
    //TODO: Make a better system

    public List<Coin> placeCoins(Field[][] map, int amountOfCoins) {
        List<Field> remainingFields = Arrays.asList(map).stream().flatMap(Arrays::stream)
                                                                 .filter(f -> !f.isWall())
                                                                 .collect(Collectors.toList());
        List<Coin> coins = new ArrayList<>();
        while(amountOfCoins > 0 && remainingFields.size() > 0) {
            int pos = rand.nextInt(remainingFields.size());
            Field field = remainingFields.remove(pos);
            Coin coin = new Coin(field.getX(), field.getY(), null);
            coins.add(coin);
            amountOfCoins--;
        }
        return coins;
    }

    public List<Chest> placeChests(Field[][] map) {
        List<Field> remainingFields = Arrays.asList(map).stream().flatMap(Arrays::stream)
                                                        .filter(f -> isCorner(f))
                                                        .collect(Collectors.toList());
        ArrayList<Chest> chests = new ArrayList<>();
        while(remainingFields.size() > 0) {
            Field field = remainingFields.remove(0);
            Chest chest = new Chest(field.getX(), field.getY(), null);
            chests.add(chest);
        }
        return chests;
    }

    // Depends on the static layout of map.csv
    private boolean isCorner(Field f) {
        return (f.getX() == 1 || f.getX() == 30)
                && (f.getY() == 1 || f.getY() == 30);
    }

    public List<Traphole> placeTrapholes(GameState gameState, int amountOfTrapholes) {
        List<Field> remainingFields = Arrays.asList(gameState.map).stream().flatMap(Arrays::stream)
                                                                 .filter(f -> !f.isWall())
                                                                 .collect(Collectors.toList());
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
