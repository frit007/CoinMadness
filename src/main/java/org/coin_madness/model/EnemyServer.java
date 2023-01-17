package org.coin_madness.model;

import org.coin_madness.messages.EnemyMessage;
import org.jspace.Space;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Random;
import java.util.stream.Collectors;

public class EnemyServer {

    float movementSpeed = 0.5f;
    private Random rand = new Random();

    // move ghosts
    //  - score -> ghost movement
    GameState gameState;
    Space enemySpace;
    public EnemyServer(GameState gameState) {
        this.gameState = gameState;
        enemySpace = gameState.connectionManager.getEnemySpace();
    }


    public void createEnemies() {

        List<Field> remainingFields = Arrays.asList(gameState.map).stream().flatMap(Arrays::stream)
                .filter(f -> !f.isWall())
                .collect(Collectors.toList());

        // create enemies
        int id = 0;
        for (Player player: gameState.allPlayers()) {
            int pos = rand.nextInt(remainingFields.size());
            Field field = remainingFields.remove(pos);

            Enemy enemy = new Enemy(id++, player.getId(), field.getX(), field.getY(), gameState);

            gameState.enemies.put(enemy.getId(), enemy);

            try {

                for (Player specificPlayer: gameState.allPlayers()) {
                    enemySpace.put(
                            EnemyMessage.CREATE_ENEMY,
                            specificPlayer.getId(),
                            enemy.id,
                            enemy.getVisibleToClientId(),
                            enemy.getX(),
                            enemy.getY());
                }

            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }

        this.enemyMovement();
    }

    private void enemyMovement() {
        gameState.gameThreads.startHandledThread("Server generates enemy movement",() -> {
            Random rand = new Random();
            while(true){
                long sleepDuration = 1000;


                for (Enemy enemy : gameState.enemies.values()) {
                    ArrayList<EntityMovement> possibleMoves = new ArrayList<>(0);

                    enemy.setMovementSpeed(movementSpeed);
                    possibleMoves.add(new EntityMovement(enemy, 1, 0, () -> {}));
                    possibleMoves.add(new EntityMovement(enemy, -1, 0, () -> {}));
                    possibleMoves.add(new EntityMovement(enemy, 0, 1, () -> {}));
                    possibleMoves.add(new EntityMovement(enemy, 0, -1, () -> {}));

                    List<EntityMovement> validMoves = possibleMoves
                            .stream()
                            .filter(move -> !gameState.map[move.getNewX()][move.getNewY()].isWall)
                            .collect(Collectors.toList());


                    EntityMovement nextMove =  validMoves.get(rand.nextInt(validMoves.size()));

                    for (Player player: gameState.allPlayers()) {
                        enemySpace.put(EnemyMessage.MOVE_ENEMY,
                                player.getId(),
                                enemy.getId(),
                                nextMove.oldX,
                                nextMove.oldY,
                                nextMove.deltaX,
                                nextMove.deltaY,
                                enemy.getMovementSpeed());
                    }
                    sleepDuration = nextMove.getDuration();
                }
                Thread.sleep(sleepDuration);

            }
        });
    }
}
