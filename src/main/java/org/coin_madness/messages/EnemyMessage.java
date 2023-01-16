package org.coin_madness.messages;

public class EnemyMessage {
    // (CREATE_ENEMY, int clientId, int enemyId, int visibleToClientId, int x, int y)
    // create a new enemy
    // use clientId so client can await messages targeted at them
    public static final String CREATE_ENEMY = "create_enemy";

    // (MOVE_ENEMY, int clientId, int enemyId, int oldX, int oldY, int deltaX, int deltaY, float movementSpeed)
    // move an enemy
    // use clientId so client can await messages targeted at them
    public static final String MOVE_ENEMY = "move_enemy";
}
