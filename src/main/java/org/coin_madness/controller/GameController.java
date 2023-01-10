package org.coin_madness.controller;

import javafx.animation.AnimationTimer;
import javafx.event.EventHandler;
import javafx.scene.Scene;
import javafx.scene.input.KeyEvent;
import javafx.scene.paint.ImagePattern;
import org.coin_madness.components.PlayerComponent;
import org.coin_madness.helpers.ConnectionManager;
import org.coin_madness.helpers.ImageLibrary;
import org.coin_madness.model.Field;
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

            Runnable reAddEventFilter = () -> {
                scene.addEventFilter(KeyEvent.KEY_PRESSED, playerControl);
            };
            boolean moved = true;

            int deltaX = 0;
            int deltaY = 0;
            ImagePattern[] animation = new ImagePattern[0];

            switch (keyEvent.getCode()) {
                case UP:
                    deltaY = - 1;
                    animation = graphics.playerUpAnim;
                    break;
                case DOWN:
                    deltaY = 1;
                    animation = graphics.playerDownAnim;
                    break;
                case LEFT:
                    deltaX = -1;
                    animation = graphics.playerLeftAnim;
                    break;
                case RIGHT:
                    deltaX = 1;
                    animation = graphics.playerRightAnim;
                    break;
                default:
                    moved = false;
            }
            if (moved){
                    // Constraints the movement further, checks of the moving to tile is a wall
                            // arguments{ Field[][]         ,int    ,int   }
                moved = player.canMoveto(gameScreen.getMap(), deltaX, deltaY);
            }
            if (moved){
                player.setX(player.getX() + deltaX);
                player.setY(player.getY() + deltaY);
                scene.removeEventFilter(KeyEvent.KEY_PRESSED, playerControl);
                playerComponent.walkAnim(deltaX*this.tileSize , deltaY*this.tileSize, animation , reAddEventFilter);
            }




            try {
                if (moved) { // remove last pos --> replace with new pos
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
