package org.coin_madness.components;

import javafx.scene.layout.StackPane;
import javafx.scene.shape.Rectangle;
import org.coin_madness.helpers.ImageLibrary;
import org.coin_madness.model.Field;

public class MazeFieldComponent extends StackPane {

    private ImageLibrary graphics;
    private Field field;
    private Rectangle background;

    public MazeFieldComponent(Field field, ImageLibrary graphics) {
        this.graphics = graphics;
        this.field = field;
        this.background = new Rectangle();
        getChildren().add(background);
    }

    public void updateView() {
        if (field.isWall()) {
            background.setFill(graphics.wall);
        } else {
            background.setFill(graphics.ground);
        }
    }

    public void setSideLength(double sideLength) {
        background.setWidth(sideLength);
        background.setHeight(sideLength);
    }

    public Field getField() {
        return field;
    }
}
