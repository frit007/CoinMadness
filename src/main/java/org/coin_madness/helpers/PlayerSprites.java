package org.coin_madness.helpers;

import javafx.scene.image.Image;

import java.util.ArrayList;
import java.util.List;

public class PlayerSprites implements EntitySprites {

    private List<Image> upMovement;
    private List<Image> rightMovement;
    private List<Image> leftMovement;
    private List<Image> downMovement;

    public List<Image> upAnimation() {
        return upMovement;
    }

    public List<Image> rightAnimation() {
        return rightMovement;
    }
    public List<Image> leftAnimation() {
        return leftMovement;
    }
    public List<Image> downAnimation() {
        return downMovement;
    }

    public Image getDownIdle() {
        return downMovement.get(1);
    }

    private String getRootPath() {
        return System.getProperty("user.dir");
    }

    private List<Image> loadMovementImages(String folder) {
        List<Image> images = new ArrayList<>();
        for (int i = 1; i < 4; i++) {
            String path = "file:" + getRootPath() + "/"+"src/main/resources/" + folder + i + ".png";
            try {
                Image image = new Image(path);
                images.add(image);
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
