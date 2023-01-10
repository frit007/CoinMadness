package org.coin_madness.components;

import javafx.scene.image.ImageView;
import org.coin_madness.helpers.ImageLibrary;
import org.coin_madness.model.Coin;

public class CoinDrawer implements Drawer<Coin> {
    private ImageLibrary graphics;
    public CoinDrawer(ImageLibrary graphics) {
        this.graphics = graphics;
    }

    @Override
    public void draw(Coin entity, ImageView view) {
        view.setImage(graphics.coin);
    }
}