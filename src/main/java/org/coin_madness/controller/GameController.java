package org.coin_madness;

import javafx.event.EventHandler;
import javafx.scene.Scene;
import javafx.scene.input.KeyEvent;
import org.coin_madness.model.Player;

public class GameController {

    private EventHandler<KeyEvent> playerControl;
    private double tileSize;

    public GameController(Player player, PlayerView playerView, double tileSize, Scene scene, ImageLibrary graphics) {

        playerControl =  keyEvent -> {
            switch (keyEvent.getCode()) {
                case UP:
                    scene.removeEventFilter(KeyEvent.KEY_PRESSED, playerControl);
                    player.setY(player.getY() - 1);
                    playerView.walkAnim(0, -this.tileSize, graphics.playerUpAnim, () -> {
                        playerView.setY(playerView.getY() + playerView.getTranslateY());
                        playerView.setTranslateY(0);
                        scene.addEventFilter(KeyEvent.KEY_PRESSED, playerControl);
                    });
                    break;
                case DOWN:
                    scene.removeEventFilter(KeyEvent.KEY_PRESSED, playerControl);
                    player.setY(player.getY() + 1);
                    playerView.walkAnim(0, this.tileSize, graphics.playerDownAnim, () -> {
                        playerView.setY(playerView.getY() + playerView.getTranslateY());
                        playerView.setTranslateY(0);
                        scene.addEventFilter(KeyEvent.KEY_PRESSED, playerControl);
                    });
                    break;
                case LEFT:
                    scene.removeEventFilter(KeyEvent.KEY_PRESSED, playerControl);
                    player.setX(player.getX() - 1);
                    playerView.walkAnim(-this.tileSize,0, graphics.playerLeftAnim, () -> {
                        playerView.setX(playerView.getX() + playerView.getTranslateX());
                        playerView.setTranslateX(0);
                        scene.addEventFilter(KeyEvent.KEY_PRESSED, playerControl);
                    });
                    break;
                case RIGHT:
                    scene.removeEventFilter(KeyEvent.KEY_PRESSED, playerControl);
                    player.setX(player.getX() + 1);
                    playerView.walkAnim(this.tileSize,0, graphics.playerRightAnim, () -> {
                        playerView.setX(playerView.getX() + playerView.getTranslateX());
                        playerView.setTranslateX(0);
                        scene.addEventFilter(KeyEvent.KEY_PRESSED, playerControl);
                    });
                    break;
            }
        };
        scene.addEventFilter(KeyEvent.KEY_PRESSED, playerControl);
    }

    public void setTileSize(double tileSize) {
        this.tileSize = tileSize;
    }
}
