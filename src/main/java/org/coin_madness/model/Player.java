package org.coin_madness.model;

public class Player extends MovableEntity {

    private int amountOfCoins = 0;
    private StaticEntityClient<Coin> coinClient;
    private StaticEntityClient<Chest> chestClient;
    private StaticEntityClient<Traphole> trapholeClient;
    private boolean localPlayer;
    public Player(int id, int x, int y, boolean localPlayer, StaticEntityClient<Coin> coinClient, StaticEntityClient<Chest> chestClient, StaticEntityClient<Traphole> trapholeClient) {
        super(id, x, y);
        this.coinClient = coinClient;
        this.chestClient = chestClient;
        this.trapholeClient = trapholeClient;
    }

    // canMoveto checks for if the new position can be moved to { i.e. NOT a wall for now}
    public boolean canMoveto(Field[][] fields, EntityMovement movement){
        //Checking for walls
        return !fields[movement.getNewX()][movement.getNewY()].isWall();
    }

    public int getAmountOfCoins() {
        return amountOfCoins;
    }

    public void setAmountOfCoins(int amountOfCoins) {
        this.amountOfCoins = amountOfCoins;
    }

    public StaticEntityClient<Coin> getCoinClient() {
        return coinClient;
    }

    public StaticEntityClient<Chest> getChestClient() {
        return chestClient;
    }

    public StaticEntityClient<Traphole> getTrapholeClient() {
        return trapholeClient;
    }

    public boolean isLocalPlayer() {
        return localPlayer;
    }
}
