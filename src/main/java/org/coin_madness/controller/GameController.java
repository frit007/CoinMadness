package org.coin_madness.controller;

import javafx.animation.AnimationTimer;
import javafx.event.EventHandler;
import javafx.scene.Scene;
import javafx.scene.input.KeyEvent;
import org.coin_madness.components.PlayerComponent;
import org.coin_madness.helpers.ConnectionManager;
import org.coin_madness.helpers.ImageLibrary;
import org.coin_madness.model.Player;
import org.coin_madness.screens.GameScreen;
import org.jspace.ActualField;
import org.jspace.FormalField;
import org.jspace.TemplateField;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class GameController {

    private EventHandler<KeyEvent> playerControl;
    private double tileSize;
    private ConnectionManager connectionManager;
    private Integer controlledPlayerID;
    private Map<Integer, PlayerComponent> networkedPlayers;
    private GameScreen gameScreen;

    public GameController(Player player, PlayerComponent playerComponent, double tileSize, Scene scene, ImageLibrary graphics, ConnectionManager connectionManager, GameScreen gameScreen) {

        this.tileSize = tileSize;
        this.connectionManager = connectionManager;
        this.controlledPlayerID = player.getId();
        this.gameScreen = gameScreen;

        // post the initial location
        try {
            connectionManager.getPositionsSpace().put(player.getId(), player.getX(), player.getY());
        } catch (InterruptedException e) {
            e.printStackTrace();
        }

        networkedPlayers = new HashMap<>();

        playerControl =  keyEvent -> {
            scene.removeEventFilter(KeyEvent.KEY_PRESSED, playerControl);
            Runnable reAddEventFilter = () -> {
                scene.addEventFilter(KeyEvent.KEY_PRESSED, playerControl);
            };
            boolean moved = true;
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
                    moved = false;
            }
            try {
                if (moved) {
                    connectionManager.getPositionsSpace().getp(new ActualField(player.getId()), new FormalField(Integer.class), new FormalField(Integer.class));
                    connectionManager.getPositionsSpace().put(player.getId(), player.getX(), player.getY());
                }
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        };
        scene.addEventFilter(KeyEvent.KEY_PRESSED, playerControl);

        // TODO There is probably a better way to do a game loop like this
        new AnimationTimer() {
            public void handle(long currentNanoTime) {

                try {
                    List<Object[]> results = connectionManager.getPositionsSpace().queryAll(
                            new FormalField(Integer.class),
                            new FormalField(Integer.class),
                            new FormalField(Integer.class)
                    );

                    for (Object[] result : results) {

                        Integer rID = (Integer) result[0];
                        Integer rX = (Integer) result[1];
                        Integer rY = (Integer) result[2];

                        if (rID.equals(controlledPlayerID)) continue;

                        if (networkedPlayers.containsKey(rID)) {
                            networkedPlayers.get(rID).moveTo(rX, rY);
                        } else {
                            PlayerComponent p = new PlayerComponent(new Player(rID, rX, rY), graphics, GameController.this.tileSize);
                            gameScreen.getChildren().add(p);
                            networkedPlayers.put(rID, p);
                        }

                    }

                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }
        }.start();

    }

    public void setTileSize(double tileSize) {
        this.tileSize = tileSize;
        // TODO refactor so that this resizes ALL the PlayerComponents
    }
}
