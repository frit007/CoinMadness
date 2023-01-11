package org.coin_madness.model;

import org.coin_madness.helpers.TimeHelper;

public class EntityMovement {
    private long startedMovementAt = 0;
    private long finishMovementAt = 0;
    int newX;
    int newY;
    int oldX;
    int oldY;
    int deltaX;
    int deltaY;
    private Runnable completionHandler;

    public EntityMovement(Player player, int deltaX, int deltaY, Runnable completionHandler) {
        oldX = player.getX();
        oldY = player.getY();
        newX = oldX + deltaX;
        newY = oldY + deltaY;
        this.deltaX = deltaX;
        this.deltaY = deltaY;
        startedMovementAt = TimeHelper.getNowInMillis();
        finishMovementAt = startedMovementAt + (long)(1000 / player.getMovementSpeed());
        this.completionHandler = completionHandler;
    }

    public EntityMovement(Player player, Direction dir, Runnable completionHandler) {
        this(
                player,
                dir == Direction.RIGHT ? 1 : (dir == Direction.LEFT ? -1 : 0),
                dir == Direction.DOWN ? 1 : (dir == Direction.UP ? -1 : 0),
                completionHandler
                );
    }

    public long getStartedMovementAt() {
        return startedMovementAt;
    }

    public long getFinishMovementAt() {
        return finishMovementAt;
    }

    public int getNewX() {
        return newX;
    }

    public int getNewY() {
        return newY;
    }

    public int getOldX() {
        return oldX;
    }

    public int getOldY() {
        return oldY;
    }

    public int getDeltaX() {
        return deltaX;
    }

    public int getDeltaY() {
        return deltaY;
    }

    public long getDuration() {
        return finishMovementAt - startedMovementAt;
    }

    public void finish() {
        completionHandler.run();
    }
    
}
