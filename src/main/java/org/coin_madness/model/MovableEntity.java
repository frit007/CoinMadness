package org.coin_madness.model;

abstract class MovableEntity extends Entity {

    int id;

    public MovableEntity(int id, int x, int y) {
        super(x, y);
        this.id = id;
    }

    abstract void move(EntityMovement movement, Field[][] map);

}
