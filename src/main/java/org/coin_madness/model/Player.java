package org.coin_madness.model;

public class Player extends MovableEntity {
    private boolean isAlive = true;
    public static final int COIN_LIMIT = 4;
    private int amountOfCoins = 0;
    private final boolean localPlayer;

    public boolean getPlayerAlive(){
        return this.isAlive;
    }
    public void kill(){
        this.isAlive = false;
        this.sendUpdates();
    }

    public Player(int id, int x, int y, boolean localPlayer) {
        super(id, x, y);
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
}
