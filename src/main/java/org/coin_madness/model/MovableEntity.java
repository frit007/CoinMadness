package org.coin_madness.model;

import org.coin_madness.helpers.TimeHelper;

public class MovableEntity extends Entity {

    int id;
    private float movementSpeed = 3;
    private EntityMovement entityMovement;

    public MovableEntity(int id, int x, int y) {
        super(x, y);
        this.id = id;
    }

    public void move(EntityMovement entityMovement, Field[][] map) {
        if(entityMovement.getNewX() == x && entityMovement.getNewY() == y) {
            return;
        }
        if(this.entityMovement == null || this.entityMovement.getFinishMovementAt() < TimeHelper.getNowInMillis()) {
            this.entityMovement = entityMovement;
        }
        map[entityMovement.oldX][entityMovement.oldY].removeEntity(this);
        map[entityMovement.newX][entityMovement.newY].addEntity(this);
        x = entityMovement.newX;
        y = entityMovement.newY;
    }

    public int getId() {
        return id;
    }

    public EntityMovement getEntityMovement() {
        return entityMovement;
    }

    public float getMovementSpeed() {
        return movementSpeed;
    }

    public void setMovementSpeed(float movementSpeed) {
        this.movementSpeed = movementSpeed;
    }

}
