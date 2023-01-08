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

        playerControl =  keyEvent -> {
            switch (keyEvent.getCode()) {
                case UP:
                    scene.removeEventFilter(KeyEvent.KEY_PRESSED, playerControl);
                    player.setY(player.getY() - 1);
                    playerComponent.walkAnim(0, -this.tileSize, graphics.playerUpAnim, () -> {
                        playerComponent.setY(playerComponent.getY() + playerComponent.getTranslateY());
                        playerComponent.setTranslateY(0);
                        scene.addEventFilter(KeyEvent.KEY_PRESSED, playerControl);
                    });
                    break;
                case DOWN:
                    scene.removeEventFilter(KeyEvent.KEY_PRESSED, playerControl);
                    player.setY(player.getY() + 1);
                    playerComponent.walkAnim(0, this.tileSize, graphics.playerDownAnim, () -> {
                        playerComponent.setY(playerComponent.getY() + playerComponent.getTranslateY());
                        playerComponent.setTranslateY(0);
                        scene.addEventFilter(KeyEvent.KEY_PRESSED, playerControl);
                    });
                    break;
                case LEFT:
                    scene.removeEventFilter(KeyEvent.KEY_PRESSED, playerControl);
                    player.setX(player.getX() - 1);
                    playerComponent.walkAnim(-this.tileSize,0, graphics.playerLeftAnim, () -> {
                        playerComponent.setX(playerComponent.getX() + playerComponent.getTranslateX());
                        playerComponent.setTranslateX(0);
                        scene.addEventFilter(KeyEvent.KEY_PRESSED, playerControl);
                    });
                    break;
                case RIGHT:
                    scene.removeEventFilter(KeyEvent.KEY_PRESSED, playerControl);
                    player.setX(player.getX() + 1);
                    playerComponent.walkAnim(this.tileSize,0, graphics.playerRightAnim, () -> {
                        playerComponent.setX(playerComponent.getX() + playerComponent.getTranslateX());
                        playerComponent.setTranslateX(0);
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
