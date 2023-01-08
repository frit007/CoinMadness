package org.coin_madness.helpers;

import javafx.scene.image.Image;
import javafx.scene.paint.ImagePattern;

public class ImageLibrary {

    // ----- Maze -----
    Image grassImage = new Image("Grass.png");
    Image brickImage = new Image("Brick.png");

    public ImagePattern ground = new ImagePattern(grassImage);
    public ImagePattern wall = new ImagePattern(brickImage);

    // ----- Player -----
    public ImagePattern idleRight = new ImagePattern(new Image("Idle_Right_Anim.png"));
    public ImagePattern walkRight = new ImagePattern(new Image("Walk_Right_Anim.png"));
    public ImagePattern[] playerRightAnim = {idleRight, walkRight};

    public ImagePattern idleLeft = new ImagePattern(new Image("Idle_Left_Anim.png"));
    public ImagePattern walkLeft = new ImagePattern(new Image("Walk_Left_Anim.png"));
    public ImagePattern[] playerLeftAnim = {idleLeft, walkLeft};

    public ImagePattern idleUp = new ImagePattern(new Image("Idle_Up_Anim.png"));
    public ImagePattern walkUp = new ImagePattern(new Image("Walk_Up_Anim.png"));
    public ImagePattern[] playerUpAnim = {idleUp, walkUp};

    public ImagePattern idleDown = new ImagePattern(new Image("Idle_Down_Anim.png"));
    public ImagePattern walkDown = new ImagePattern(new Image("Walk_Down_Anim.png"));
    public ImagePattern[] playerDownAnim = {idleDown, walkDown};

}
