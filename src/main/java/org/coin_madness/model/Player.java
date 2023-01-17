package org.coin_madness.model;

public class Player extends MovableEntity {
    public static final int COIN_LIMIT = 4;
    private int amountOfCoins = 0;
    private final boolean localPlayer;
    private int modelId;
    private int score = 0;

    public Player(int id, int x, int y, int spriteId, boolean localPlayer) {
        super(id, spriteId, x, y);
        this.spriteId = spriteId;
        this.localPlayer = localPlayer;
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
        sendUpdates();
    }
    
    public boolean isLocalPlayer() {
        return localPlayer;
    }

    public int getScore() {
        return score;
    }

    // display includes a bonus for the amount of coins
    public int getDisplayScore() {
        return score + getAmountOfCoins() * 5;
    }

    public void setScore(int score) {
        this.score = score;
        sendUpdates();
    }
}
