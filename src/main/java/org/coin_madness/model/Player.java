package org.coin_madness.model;

import org.coin_madness.helpers.TimeHelper;

public class Player extends MovableEntity {

    private float movementSpeed = 3;
    private EntityMovement entityMovement;

    public Player(int id, int x, int y) {
        super(id, x, y);
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

    public EntityMovement getEntityMovement() {
        return entityMovement;
    }

    public boolean isMoving() {
        return !(this.entityMovement == null || this.entityMovement.getFinishMovementAt() < TimeHelper.getNowInMillis());
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
    public boolean canMoveto(Field[][] fields, EntityMovement movement){
        //Checking for walls
        return !fields[movement.getNewX()][movement.getNewY()].isWall();
    }

}
