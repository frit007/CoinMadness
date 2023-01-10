package org.coin_madness.components;

import javafx.scene.image.ImageView;
import org.coin_madness.helpers.ImageLibrary;
import org.coin_madness.model.Chest;

public class ChestDrawer implements Drawer<Chest> {
    private ImageLibrary graphics;

    public ChestDrawer(ImageLibrary graphics) {
        this.graphics = graphics;
    }

    @Override
    public void draw(Chest entity, ImageView view) {
        view.setImage(graphics.chest);
    }
}
