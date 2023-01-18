package org.coin_madness.controller;

import javafx.animation.AnimationTimer;
import javafx.application.Platform;
import javafx.event.EventHandler;
import javafx.scene.Scene;
import javafx.scene.input.KeyEvent;
import org.coin_madness.components.GameStatusBar;
import org.coin_madness.helpers.ConnectionManager;
import org.coin_madness.model.*;
import org.jspace.ActualField;
import org.jspace.FormalField;

import java.util.List;

public class GameController {

    private EventHandler<KeyEvent> playerControl;

    private ConnectionManager connectionManager;
    private int controlledPlayerID;
    private Direction currentDirection;
    private Direction nextDirection;
    private Player player;
    private Field[][] map;
    private GameState gameState;

    public GameController(Scene scene, ConnectionManager connectionManager, GameState gameState) {
        this.connectionManager = connectionManager;
        player = gameState.localPlayer;
        this.controlledPlayerID = player.getId();
        this.map = gameState.map;
        this.gameState = gameState;

        // post the initial location, and take the field lock
        try {
            connectionManager.getPositionsSpace().put(player.getId(), player.getX(), player.getY());
            connectionManager.getFieldLocksSpace().get(
                    new ActualField(player.getX()),
                    new ActualField(player.getY())
            );
        } catch (InterruptedException e) {
            e.printStackTrace();
        }

        playerControl =  keyEvent -> {
            Direction dir = Direction.fromKeyCode(keyEvent.getCode());
            if (dir != null) {
                nextDirection = dir;
                if (currentDirection == null) currentDirection = dir;
                updateMovement();
            }
        };
        
        scene.addEventFilter(KeyEvent.KEY_PRESSED, playerControl);

        gameState.gameThreads.startHandledThread("Player movement", () -> {
            while(true) {
                List<Object[]> results = connectionManager.getPositionsSpace().queryAll(
                        new FormalField(Integer.class),
                        new FormalField(Integer.class),
                        new FormalField(Integer.class)
                );
                Platform.runLater(() -> {
                    for (Object[] result : results) {

                        Integer rID = (Integer) result[0];
                        Integer rX = (Integer) result[1];
                        Integer rY = (Integer) result[2];

                        if (rID.equals(controlledPlayerID)) continue;

                        if (gameState.networkedPlayers.containsKey(rID)) {
                            Player net = gameState.networkedPlayers.get(rID);
                            int deltaX = rX - net.getX();
                            int deltaY = rY - net.getY();
                            EntityMovement movement = new EntityMovement(net, deltaX, deltaY, () -> {});
                            net.move(movement, map);
                        } else {
                            // unknown player, all known players have been created at start up.
                        }
                    }
                });
                Thread.sleep(20);
            }
        });

    }

    /**
     * If the player is not already moving, move it in the correct direction
     * according to current and next direction.
     */
    public void updateMovement() {
        try {
            if (currentDirection != null && !player.isMoving() && player.isAlive()) {

                // Select the correct movement based on current and next direction
                EntityMovement preferredMovement = new EntityMovement(player, nextDirection, this::updateMovement);
                EntityMovement momentumMovement = new EntityMovement(player, currentDirection, this::updateMovement);
                EntityMovement chosenMovement = null;
                if (player.canMoveto(map, preferredMovement)) {
                    chosenMovement = preferredMovement;
                    currentDirection = nextDirection;
                } else if (player.canMoveto(map, momentumMovement)) {
                    chosenMovement = momentumMovement;
                }

                if (chosenMovement != null) {

                    Object[] lock = connectionManager.getFieldLocksSpace().getp(
                            new ActualField(chosenMovement.getNewX()),
                            new ActualField(chosenMovement.getNewY())
                    );
                    if (lock == null) return; // If the position is blocked

                    // Actually make the movement
                    player.move(chosenMovement, map);

                    // Notify the other players about the movement
                    connectionManager.getPositionsSpace().getp(
                            new ActualField(player.getId()),
                            new FormalField(Integer.class),
                            new FormalField(Integer.class)
                    );
                    connectionManager.getPositionsSpace().put(player.getId(), player.getX(), player.getY());

                    // release the old lock
                    connectionManager.getFieldLocksSpace().put(chosenMovement.getOldX(), chosenMovement.getOldY());

                }
            }
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }

}
