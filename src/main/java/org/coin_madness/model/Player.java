package org.coin_madness.model;

public class Player extends MovableEntity {
    private boolean alive = true;
    public static final int COIN_LIMIT = 4;
    private int amountOfCoins = 0;
    private final boolean localPlayer;
    private int modelId;
    private int score = 0;

    public Player(int id, int x, int y, int modelId, boolean localPlayer) {
        super(id, x, y);
        this.modelId = modelId;
        this.localPlayer = localPlayer;
    }

    public boolean isAlive(){
        return this.alive;
    }
    public void kill(){
        this.alive = false;
        this.sendUpdates();
    }
    public int getModelId() {
        return modelId;
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

    public void setScore(int score) {
        this.score = score;
    }
}
