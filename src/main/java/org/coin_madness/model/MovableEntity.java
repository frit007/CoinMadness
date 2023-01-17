package org.coin_madness.model;

import org.coin_madness.helpers.TimeHelper;

import java.util.ArrayList;
import java.util.List;

public class MovableEntity extends Entity {

    int id;
    private boolean alive = true;
    protected int spriteId;
    private float movementSpeed = 3;
    private EntityMovement entityMovement;


    public boolean isAlive() {
        return this.alive;
    }

    public void kill() {
        this.alive = false;
        this.sendUpdates();
    }

    public MovableEntity(int id, int spriteId, int x, int y) {
        super(x, y);
        this.id = id;
        this.spriteId = spriteId;
    }

    public int getSpriteId() {
        return spriteId;
    }

    /**
     * This should only be called when the player is not already moving.
     */
    public void move(EntityMovement entityMovement, Field[][] map) {
        if(entityMovement.getNewX() == x && entityMovement.getNewY() == y) {
            return;
        }
        this.entityMovement = entityMovement;
        map[entityMovement.oldX][entityMovement.oldY].removeEntity(this);
        map[entityMovement.newX][entityMovement.newY].addEntity(this);
        x = entityMovement.newX;
        y = entityMovement.newY;
    }

    public boolean isMoving() {
        return !(this.entityMovement == null || this.entityMovement.getFinishMovementAt() <= TimeHelper.getNowInMillis());
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
