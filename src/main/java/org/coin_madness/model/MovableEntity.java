package org.coin_madness.model;

abstract class MovableEntity extends Entity {

    String id;

    public MovableEntity(String id, int x, int y) {
        super(x, y);
        this.id = id;
    }

    abstract void move(EntityMovement movement, Field[][] map);

}
