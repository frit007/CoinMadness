package org.coin_madness.helpers;

import javafx.scene.image.Image;

import java.util.ArrayList;
import java.util.List;

public class EnemySprites implements EntitySprites{
    private Image enemyImage;
    private List<Image> animation;

    public EnemySprites() {
        enemyImage = new Image("Ghost.png");
        animation = new ArrayList<>();
        animation.add(enemyImage);
    }

    public Image getDownIdle() {
        return enemyImage;
    }

    public List<Image> upAnimation() {
        return animation;
    }
    public List<Image> rightAnimation() {
        return animation;
    }
    public List<Image> leftAnimation() {
        return animation;
    }
    public List<Image> downAnimation() {
        return animation;
    }

}
