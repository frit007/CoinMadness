package org.coin_madness.controller;

import javafx.animation.AnimationTimer;
import javafx.event.EventHandler;
import javafx.scene.Scene;
import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyEvent;
import org.coin_madness.helpers.ConnectionManager;
import org.coin_madness.model.Direction;
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
    private Direction currentDirection;
    private Direction nextDirection;
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
            Direction dir = Direction.fromKeyCode(keyEvent.getCode());
            if (dir != null) nextDirection = dir;
            if (currentDirection == null) currentDirection = dir;
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

                    // TODO Smooth things out here. There should be some method so that the player doesn't arrive
                    //  somewhere, stop, and then continue moving again
                    if (currentDirection != null && !player.isMoving()) {

                        EntityMovement preferredMovement = new EntityMovement(player, nextDirection);
                        EntityMovement momentumMovement = new EntityMovement(player, currentDirection);
                        EntityMovement chosenMovement = null;
                        if (player.canMoveto(map, preferredMovement)) {
                            chosenMovement = preferredMovement;
                            currentDirection = nextDirection;
                        } else if (player.canMoveto(map, momentumMovement)) {
                            chosenMovement = momentumMovement;
                        }

                        if (chosenMovement != null) {
                            player.move(chosenMovement, map);
                            // remove last position and replace with new position
                            connectionManager.getPositionsSpace().getp(
                                    new ActualField(player.getId()),
                                    new FormalField(Integer.class),
                                    new FormalField(Integer.class)
                            );
                            connectionManager.getPositionsSpace().put(player.getId(), player.getX(), player.getY());
                        }
                    }

                } catch (InterruptedException e) {
                    e.printStackTrace();
                }

            }
        }.start();

    }
}
