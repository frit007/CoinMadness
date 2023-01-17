package org.coin_madness.model;

public class Traphole extends StaticEntity implements CollidesWithPlayer {
    GameState gameState;

    public Traphole(int x, int y, GameState gameState) {
        super(x, y);
        this.gameState = gameState;
    }

    @Override
    public void onPlayerCollision(Player player) {
        if(!player.isAlive()) {
            return;
        }
        player.kill();
        gameState.deathClient.sendDeath();
        gameState.map[x][y].sendUpdated();
        System.out.println("Hit Traphole!");
    }

}
