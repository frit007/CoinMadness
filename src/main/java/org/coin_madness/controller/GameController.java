package org.coin_madness.controller;

import javafx.animation.AnimationTimer;
import javafx.event.EventHandler;
import javafx.scene.Scene;
import javafx.scene.input.KeyEvent;
import org.coin_madness.helpers.ConnectionManager;
import org.coin_madness.model.EntityMovement;
import org.coin_madness.model.Field;
import org.coin_madness.model.Player;
import org.jspace.ActualField;
import org.jspace.FormalField;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class GameController {

    private EventHandler<KeyEvent> playerControl;

    private ConnectionManager connectionManager;
    private int controlledPlayerID;
    private Map<Integer, Player> networkedPlayers;

    public GameController(Player player, Scene scene, Field[][] map, ConnectionManager connectionManager) {
        this.connectionManager = connectionManager;
        this.controlledPlayerID = player.getId();

        // post the initial location
        try {
            connectionManager.getPositionsSpace().put(player.getId(), player.getX(), player.getY());
        } catch (InterruptedException e) {
            e.printStackTrace();
        }

        networkedPlayers = new HashMap<>();

        playerControl =  keyEvent -> {
            EntityMovement movement;
            boolean moved = false;

            switch (keyEvent.getCode()) {
                case UP:
                    movement = new EntityMovement(player, 0, -1);
                    break;
                case DOWN:
                    movement = new EntityMovement(player, 0, 1);
                    break;
                case LEFT:
                    movement = new EntityMovement(player, -1, 0);
                    break;
                case RIGHT:
                    movement = new EntityMovement(player, 1, 0);
                    break;
                default:
                    movement = null;
            }

                if (movement != null) {
                    // Constraints the movement further, checks of the moving to tile is a wall
                    // arguments{ Field[][]         ,int    ,int   }
                    moved = player.canMoveto(map, movement);
                }
                if (moved){
                    player.move(movement, map);
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
                            Player net = networkedPlayers.get(rID);
                            int deltaX = rX - net.getX();
                            int deltaY = rY - net.getY();
                            EntityMovement movement = new EntityMovement(net, deltaX, deltaY);
                            net.move(movement, map);
                        } else {
                            Player p = new Player(rID, rX, rY);
                            map[p.getX()][p.getY()].addEntity(p);
                            networkedPlayers.put(rID, p);
                        }
                    }

                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }
        }.start();

    }
}
