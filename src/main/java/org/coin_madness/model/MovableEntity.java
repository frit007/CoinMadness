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

    public synchronized void kill() {
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
    public synchronized void move(EntityMovement entityMovement, Field[][] map) {
        if(entityMovement.getNewX() == x && entityMovement.getNewY() == y) {
            return;
        }
        this.entityMovement = entityMovement;
        if(!map[entityMovement.oldX][entityMovement.oldY].removeEntity(this)) {
            // if we fail to remove an entity from the map, we likely missed an update
            // and the entity is somewhere else on the map, so remove them from whatever square the entity is on
            forceRemoveFromMap(map);
        }
        map[entityMovement.newX][entityMovement.newY].addEntity(this);
        x = entityMovement.newX;
        y = entityMovement.newY;
    }

    private synchronized boolean forceRemoveFromMap(Field[][] map) {
        for(Field[] rows : map)
            for (Field field : rows)
                if (field.removeEntity(this))
                    return true;
        return false;
    }

    public boolean isMoving() {
        // Allow starting the next movement slightly earlier, to make movement feel smoother
        // This also reduces bugs involving the player stopping without any reason
        int CHEAT_WINDOW = 10;
        return !(this.entityMovement == null || this.entityMovement.getFinishMovementAt() - CHEAT_WINDOW <= TimeHelper.getNowInMillis());
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

    public boolean isVisible(GameState gameState) {
        return true;
    }
}
