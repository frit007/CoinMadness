package org.coin_madness.model;

import org.coin_madness.screens.GameScreen;

public class Player {

    private int id;
    private int x;
    private int y;
    private float movementSpeed = 3;

    public Player(int id, int x, int y) {
        this.id = id;
        this.x = x;
        this.y = y;
    }

    public int getId() {
        return id;
    }

    public int getX() {
        return x;
    }

    public void setX(int x) {
        this.x = x;
    }

    public int getY() {
        return y;
    }

    public void setY(int y) {
        this.y = y;
    }

    public float getMovementSpeed() {
        return movementSpeed;
    }

    public void setMovementSpeed(float movementSpeed) {
        this.movementSpeed = movementSpeed;
    }


        // canMoveto checks for if the new position can be moved to { i.e. NOT a wall for now}
    public boolean canMoveto(Field[][] fields, int deltaX, int deltaY ){
        int newPositionX  = this.getX() + deltaX;
        int newPositionY  = this.getY() + deltaY;
        //Checking for walls
        if(fields[newPositionY][newPositionX].isWall()){
            return false;
        }else {
            return true;
        }
    }

}
