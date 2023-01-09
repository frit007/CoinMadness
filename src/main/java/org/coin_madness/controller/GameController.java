package org.coin_madness.controller;

import javafx.event.EventHandler;
import javafx.scene.Scene;
import javafx.scene.input.KeyEvent;
import org.coin_madness.components.PlayerComponent;
import org.coin_madness.helpers.ImageLibrary;
import org.coin_madness.model.Player;

public class GameController {

    private EventHandler<KeyEvent> playerControl;
    private double tileSize;

    public GameController(Player player, PlayerComponent playerComponent, double tileSize, Scene scene, ImageLibrary graphics) {

        this.tileSize = tileSize;

        playerControl =  keyEvent -> {
            scene.removeEventFilter(KeyEvent.KEY_PRESSED, playerControl);
            Runnable reAddEventFilter = () -> {
                scene.addEventFilter(KeyEvent.KEY_PRESSED, playerControl);
            };
            switch (keyEvent.getCode()) {
                case UP:
                    player.setY(player.getY() - 1);
                    playerComponent.walkAnim(0, -this.tileSize, graphics.playerUpAnim, reAddEventFilter);
                    break;
                case DOWN:
                    player.setY(player.getY() + 1);
                    playerComponent.walkAnim(0, this.tileSize, graphics.playerDownAnim, reAddEventFilter);
                    break;
                case LEFT:
                    player.setX(player.getX() - 1);
                    playerComponent.walkAnim(-this.tileSize,0, graphics.playerLeftAnim, reAddEventFilter);
                    break;
                case RIGHT:
                    player.setX(player.getX() + 1);
                    playerComponent.walkAnim(this.tileSize,0, graphics.playerRightAnim, reAddEventFilter);
                    break;
                default:
                    scene.addEventFilter(KeyEvent.KEY_PRESSED, playerControl);
            }
        };
        scene.addEventFilter(KeyEvent.KEY_PRESSED, playerControl);
    }

    public void setTileSize(double tileSize) {
        this.tileSize = tileSize;
    }
}
