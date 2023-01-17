package org.coin_madness.model;

import org.coin_madness.helpers.Action;

import java.util.ArrayList;
import java.util.List;

public class Field {

    int x;
    int y;
    boolean isWall;
    List<Entity> entities = new ArrayList<>();
    private Action onChange;

    public Field(int x, int y) {
        this.x = x;
        this.y = y;
    }

    public void setOnChange(Action onChange) {
        this.onChange = onChange;
    }

    public void addEntity(Entity entity) {
        entities.add(entity);
        entity.addOnUpdate(this::sendUpdated);
        if(entity instanceof Player) { ///
            Player localPlayer = (Player) entity;
            if(localPlayer.isLocalPlayer()) {
                playerCollisions(localPlayer);
            }
        }
        if (entity instanceof Enemy) {
            for (Entity e : entities) {
                if (e instanceof Player) {
                    Player player = (Player) e;
                    if(player.isLocalPlayer()) {
                        playerCollisions(player);
                    }
                }
            }
        }
        sendUpdated();
    }

    public void removeEntity(Entity entity) {
        entities.remove(entity);
        entity.removeOnUpdated(this::sendUpdated);
        sendUpdated();
    }

    public void playerCollisions(Player player) {
        // copy the player array since onPlayerCollision might update it
        for (Entity entity : new ArrayList<>(entities)) {
            if (entity instanceof CollidesWithPlayer) {
                ((CollidesWithPlayer) entity).onPlayerCollision(player);
            }
        }
    }

    public boolean isWall() {
        return isWall;
    }

    public void setWall(boolean wall) {
        isWall = wall;
    }

    public int getX() {
        return x;
    }

    public int getY() {
        return y;
    }

    public List<Entity> getEntities() {
        return entities;
    }

    public void sendUpdated() {
        if(onChange != null) {
            onChange.handle();
        }
    }
}
