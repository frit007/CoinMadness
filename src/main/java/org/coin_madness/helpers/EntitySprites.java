package org.coin_madness.helpers;

import javafx.scene.image.Image;

import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;

public class PlayerSprites {

    public List<Image> upMovement;
    public List<Image> rightMovement;
    public List<Image> leftMovement;
    public List<Image> downMovement;

    public Image getRightIdle() {
        return rightMovement.get(1);
    }
    public Image getUpIdle() {
        return upMovement.get(1);
    }
    public Image getDownIdle() {
        return downMovement.get(1);
    }
    public Image getLeftIdle() {
        return leftMovement.get(1);
    }

    private List<Image> loadMovementImages(String folder) {
        List<Image> images = new ArrayList<>();
        for (int i = 1; i < 4; i++) {
            String path = "file:" + folder + i + ".png";
            try {
                images.add(new Image(path));
            } catch (Exception e) {
                e.printStackTrace();
                System.out.println("cannot load: " + path);
            }
        }
        return images;
    }

    public PlayerSprites(String basePath) {
        upMovement = loadMovementImages(basePath+"UP_Movement/");
        leftMovement = loadMovementImages(basePath+"LEFT_Movement/");
        rightMovement = loadMovementImages(basePath+"RIGHT_Movement/");
        downMovement = loadMovementImages(basePath+"DOWN_Movement/");
    }
}
