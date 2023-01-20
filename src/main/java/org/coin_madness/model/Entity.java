package org.coin_madness.model;

import javafx.application.Platform;

import java.util.ArrayList;
import java.util.List;

public class Entity {

    protected int x;
    protected int y;

    private List<Runnable> onUpdate = new ArrayList<>();

    public Entity(int x, int y) {
        this.x = x;
        this.y = y;
    }

    public synchronized void addOnUpdate(Runnable onUpdate) {
        this.onUpdate.add(onUpdate);
    }
    protected synchronized void sendUpdates() {
        for (Runnable runnable: onUpdate) {
            Platform.runLater(runnable);
        }
    }

    public synchronized void setX(int x) {
        this.x = x;
    }

    public synchronized void setY(int y) {
        this.y = y;
    }

    public int getX() {
        return x;
    }

    public int getY() {
        return y;
    }

    public synchronized void removeOnUpdated(Runnable sendUpdated) {
        onUpdate.remove(sendUpdated);
    }
}
