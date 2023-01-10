package org.coin_madness.components;

import javafx.scene.image.ImageView;
import org.coin_madness.model.Entity;

public interface Drawer <T extends Entity> {

    void draw(T entity, ImageView view);
}
