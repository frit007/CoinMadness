package org.coin_madness.model;

public class Chest extends StaticEntity implements CollidesWithPlayer {

    private ChestClient chestClient;
    private static final int MAX_COINS = 3;
    private int amountOfCoins = 0;

    public Chest(int x, int y, ChestClient chestClient) {
        super(x, y);
        this.chestClient = chestClient;
    }

    public int getMaxCoins() {
        return MAX_COINS;
    }

    public int getAmountOfCoins() {
        return amountOfCoins;
    }

    public void setAmountOfCoins(int amountOfCoins) {
        this.amountOfCoins = amountOfCoins;
    }

    @Override
    public void onPlayerCollision(Player player) {
        chestClient.placeCoins(this, player);
    }
}
