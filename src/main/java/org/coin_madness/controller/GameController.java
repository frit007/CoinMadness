package org.coin_madness.controller;

import javafx.event.EventHandler;
import javafx.scene.Scene;
import javafx.scene.input.KeyEvent;
import org.coin_madness.helpers.Action;
import org.coin_madness.model.EntityMovement;
import org.coin_madness.model.Field;
import org.coin_madness.model.Player;

public class GameController {

    private EventHandler<KeyEvent> playerControl;

    public GameController(Player player, Scene scene, Field[][] map) {
        playerControl =  keyEvent -> {
            //scene.removeEventFilter(KeyEvent.KEY_PRESSED, playerControl);
            //Action reAddEventFilter = () -> scene.addEventFilter(KeyEvent.KEY_PRESSED, playerControl);
            //TODO: keypress queue
            switch (keyEvent.getCode()) {
                case UP:
                    player.move(new EntityMovement(player, 0, -1), map);
                    break;
                case DOWN:
                    player.move(new EntityMovement(player, 0, 1), map);
                    break;
                case LEFT:
                    player.move(new EntityMovement(player, -1, 0), map);
                    break;
                case RIGHT:
                    player.move(new EntityMovement(player, 1, 0), map);
                    break;
            }
        };
        scene.addEventFilter(KeyEvent.KEY_PRESSED, playerControl);
    }
}
