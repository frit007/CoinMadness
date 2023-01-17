package org.coin_madness.model;

import org.coin_madness.helpers.EnemySprites;
import org.coin_madness.helpers.ImageLibrary;

public class Enemy extends MovableEntity implements CollidesWithPlayer {

    public int visibleToClientId;
    public GameState gameState;

    public Enemy(int id, int visibleToClientId, int x, int y, GameState gameState) {
        super(id, ImageLibrary.ENEMY_SPRITE_ID, x, y);
        this.visibleToClientId = visibleToClientId;
        this.gameState = gameState;
    }

    public int getVisibleToClientId() {
        return visibleToClientId;
    }


    @Override
    public void onPlayerCollision(Player player) {
        if(!player.isAlive()) {
            return;
        }
        player.kill();
        gameState.deathClient.sendDeath();
        gameState.map[x][y].sendUpdated();
        System.out.println("Hit Enemy!");
    }

}
