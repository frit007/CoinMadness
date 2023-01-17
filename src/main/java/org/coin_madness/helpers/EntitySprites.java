package org.coin_madness.helpers;

import javafx.scene.image.Image;

import java.util.List;

public interface EntitySprites {

    public Image getDownIdle();

    public List<Image> upAnimation();
    public List<Image> rightAnimation();
    public List<Image> leftAnimation();
    public List<Image> downAnimation();

}
