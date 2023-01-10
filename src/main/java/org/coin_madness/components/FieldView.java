package org.coin_madness.components;

import javafx.scene.image.ImageView;
import javafx.scene.layout.StackPane;
import org.coin_madness.helpers.ImageLibrary;
import org.coin_madness.model.*;

import java.util.HashMap;

public class FieldView extends StackPane {

    private ImageLibrary graphics;
    private Field field;
    private ImageView background;
    private ImageView staticEntityView;
    private ImageView movableEntityView;
    private HashMap<Class, Drawer> drawerMap;

    public FieldView(Field field, ImageLibrary graphics, HashMap<Class, Drawer> drawerMap) {
        this.graphics = graphics;
        this.field = field;
        this.drawerMap = drawerMap;

        background = new ImageView();
        staticEntityView = new ImageView();
        movableEntityView = new ImageView();

        getChildren().add(background);
        getChildren().add(staticEntityView);
        getChildren().add(movableEntityView);

        field.setOnChange(this::updateView);
    }

    public void updateView() {
        if (field.isWall()) {
            background.setImage(graphics.wall);
        } else {
            background.setImage(graphics.ground);
        }


        //TODO: clean up
        boolean hasStaticEntity = false;
        boolean hasMoveableEntity = false;

        for (Entity entity : field.getEntities()) {
            if(entity != null && drawerMap.containsKey(entity.getClass())) {
                if(entity instanceof StaticEntity) {
                    drawerMap.get(entity.getClass()).draw(entity, staticEntityView);
                    hasStaticEntity = true;
                } else {
                    drawerMap.get(entity.getClass()).draw(entity, movableEntityView);
                    hasMoveableEntity = true;
                }
            }
        }

        if(!hasStaticEntity) {
            staticEntityView.setImage(null);
        }

        if(!hasMoveableEntity) {
            movableEntityView.setImage(null);
        }
    }

    public void setSideLength(double sideLength) {
        background.setFitWidth(sideLength);
        background.setFitHeight(sideLength);
        staticEntityView.setFitHeight(sideLength);
        staticEntityView.setFitWidth(sideLength);
        movableEntityView.setFitHeight(sideLength);
        movableEntityView.setFitWidth(sideLength);
    }

    public Field getField() {
        return field;
    }

}
