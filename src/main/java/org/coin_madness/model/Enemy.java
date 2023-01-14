package org.coin_madness.model;

import org.coin_madness.helpers.EnemySprites;
import org.coin_madness.helpers.ImageLibrary;

public class Enemy extends MovableEntity {

    public int visibleToClientId;

    public Enemy(int id, int visibleToClientId, int x, int y) {
        super(id, ImageLibrary.ENEMY_SPRITE_ID, x, y);
        this.visibleToClientId = visibleToClientId;
    }

    public int getVisibleToClientId() {
        return visibleToClientId;
    }

}
