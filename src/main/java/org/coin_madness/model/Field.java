package org.coin_madness.model;

public class Field {

    int id;
    boolean isWall;
    boolean hasCoin;
    int x;
    int y;

    public Field(int id, int x, int y) {
        this.id = id;
        this.x = x;
        this.y = y;
    }

    public int getId() {
        return id;
    }

    public boolean isWall() {
        return isWall;
    }

    public void setWall(boolean wall) {
        isWall = wall;
    }

    public boolean hasCoin() {
        return hasCoin;
    }

    public void setHasCoin(boolean hasCoin) {
        this.hasCoin = hasCoin;
    }

    public int getX() {
        return x;
    }

    public int getY() {
        return y;
    }

}
