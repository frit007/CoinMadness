package org.coin_madness.components;

import javafx.scene.image.ImageView;
import org.coin_madness.helpers.ImageLibrary;
import org.coin_madness.model.Traphole;

public class TrapholeDrawer implements Drawer<Traphole> {
    private ImageLibrary graphics;

    public TrapholeDrawer(ImageLibrary graphics) {
        this.graphics = graphics;
    }

    @Override
    public void draw(Traphole entity, ImageView view) {
        view.setImage(graphics.traphole);
    }
}
