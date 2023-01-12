package org.coin_madness.model;

public class Player extends MovableEntity {

    private int amountOfCoins = 0;
    private boolean localPlayer;

    public Player(int id, int x, int y, boolean localPlayer) {
        super(id, x, y);
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
    
    public boolean isLocalPlayer() {
        return localPlayer;
    }
}
