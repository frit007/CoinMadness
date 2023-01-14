package org.coin_madness.model;

import javafx.application.Platform;
import org.coin_madness.messages.EnemyMessage;
import org.jspace.ActualField;
import org.jspace.FormalField;
import org.jspace.Space;

public class EnemyClient {

    private GameState gameState;
    private Space enemySpace;
    public EnemyClient(GameState gameState) {
        this.gameState = gameState;
        this.enemySpace = gameState.connectionManager.getEnemySpace();
    }

    public void listenForNewEnemies() {
        gameState.gameThreads.startHandledThread(() -> {
            while(true) {
                Object[] newEnemy = enemySpace.get(
                        new ActualField(EnemyMessage.CREATE_ENEMY),
                        new ActualField(gameState.connectionManager.getClientId()),
                        new FormalField(Integer.class),
                        new FormalField(Integer.class),
                        new FormalField(Integer.class),
                        new FormalField(Integer.class)
                );
                int enemyId = (int)newEnemy[2];
                int visibleToClientId = (int)newEnemy[3];
                int x = (int)newEnemy[4];
                int y = (int)newEnemy[5];

                Platform.runLater(() -> {
                    if(!gameState.enemies.containsKey(enemyId)) {
                        Enemy enemy = new Enemy(enemyId, visibleToClientId, x, y);
                        gameState.enemies.put(enemyId, enemy);
                        gameState.map[x][y].addEntity(enemy);
                    }
                });
            }
        });
    }

    public void listenForEnemyMovement() {
        gameState.gameThreads.startHandledThread(() -> {
            while(true) {
                Object[] enemyMovement = enemySpace.get(
                        new ActualField(EnemyMessage.MOVE_ENEMY),
                        new ActualField(gameState.connectionManager.getClientId()),
                        new FormalField(Integer.class), // enemyId
                        new FormalField(Integer.class), // oldX
                        new FormalField(Integer.class), // oldY
                        new FormalField(Integer.class), // deltaX
                        new FormalField(Integer.class), // deltaY
                        new FormalField(Float.class) // movementSpeed
                );
                int enemyId = (int)enemyMovement[2];
                int oldX = (int)enemyMovement[3];
                int oldY = (int)enemyMovement[4];
                int deltaX = (int)enemyMovement[5];
                int deltaY = (int)enemyMovement[6];
                float movementSpeed = (float)enemyMovement[7];

                Platform.runLater(() -> {
                    Enemy enemy = gameState.enemies.get(enemyId);
                    if(enemy != null) {
                        enemy.setMovementSpeed(movementSpeed);
                        enemy.setX(oldX);
                        enemy.setY(oldY);
                        enemy.move(new EntityMovement(enemy, deltaX, deltaY, () -> {}), gameState.map);
                    }
                });

            }
        });
    }

    public void listenForChanges() {
        listenForNewEnemies();
        listenForEnemyMovement();
    }
}
