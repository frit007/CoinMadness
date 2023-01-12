package org.coin_madness.model;

import org.coin_madness.helpers.Action;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
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
        if(onChange != null) {
            onChange.handle();
        }
        if(entity instanceof Player) {
            playerCollisions((Player) entity);
        }
    }

    public void removeEntity(Entity entity) {
        entities.remove(entity);
        if(onChange != null) {
            onChange.handle();
        }
    }

    public void playerCollisions(Player player) {
        for (Entity entity : entities) {
            if (entity instanceof CollidesWithPlayer) {
                ((CollidesWithPlayer) entity).onPlayerColission(player);
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

}
