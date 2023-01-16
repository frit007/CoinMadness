package org.coin_madness.model;

import javafx.application.Platform;
import org.jspace.ActualField;
import org.jspace.FormalField;

import java.util.function.Consumer;

public class DeathClient {

    private final Consumer<GameState> goToEndScreen;
    private GameState gameState;

    public DeathClient(GameState gameState, Consumer<GameState> goToEndScreen) {
        this.gameState = gameState;
        this.goToEndScreen = goToEndScreen;

        gameState.gameThreads.startHandledThread("Listen for deaths", () -> {
            while(true) {
                Object[] deathMessage = gameState.connectionManager.getDeathSpace().get(
                        new ActualField(gameState.connectionManager.getClientId()),
                        new FormalField(Integer.class)
                );
                int killedClientId = (int) deathMessage[1];
                Platform.runLater(() -> {
                    Player player = gameState.networkedPlayers.get(killedClientId);
                    player.kill();
                    gameState.map[player.getX()][player.getY()].sendUpdated();
                    System.out.println("Player " + player.getId() + " has died");
                    checkForGameOver();
                });
            }
        });
    }

    private void checkForGameOver() {
        if(everybodyIsDead()) {
            goToEndScreen.accept(gameState);
        }
    }
    
    private boolean everybodyIsDead() {
        for (Player player : gameState.allPlayers())
            if(player.isAlive())
                return false;

        return true;
    }

    public void sendDeath() {
        for(var otherPlayer : gameState.networkedPlayers.values()) {
            try {
                gameState.connectionManager.getDeathSpace().put(otherPlayer.getId(), gameState.connectionManager.getClientId());
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
        checkForGameOver();
    }

}
