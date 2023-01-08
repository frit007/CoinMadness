package org.coin_madness;

import javafx.scene.layout.StackPane;
import javafx.scene.shape.Rectangle;
import org.coin_madness.model.Field;

public class MazeFieldView extends StackPane {

    private ImageLibrary graphics;
    private Field field;
    private Rectangle background;

    public MazeFieldView(Field field, ImageLibrary graphics) {
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
