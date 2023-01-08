package org.coin_madness;

import javafx.scene.image.Image;
import javafx.scene.paint.ImagePattern;

public class ImageLibrary {

    // ----- Maze -----
    Image grassImage = new Image("Grass.png");
    Image brickImage = new Image("Brick.png");

    ImagePattern ground = new ImagePattern(grassImage);
    ImagePattern wall = new ImagePattern(brickImage);

    // ----- Player -----
    ImagePattern idleRight = new ImagePattern(new Image("Idle_Right_Anim.png"));
    ImagePattern walkRight = new ImagePattern(new Image("Walk_Right_Anim.png"));
    ImagePattern[] playerRightAnim = {idleRight, walkRight};

    ImagePattern idleLeft = new ImagePattern(new Image("Idle_Left_Anim.png"));
    ImagePattern walkLeft = new ImagePattern(new Image("Walk_Left_Anim.png"));
    ImagePattern[] playerLeftAnim = {idleLeft, walkLeft};

    ImagePattern idleUp = new ImagePattern(new Image("Idle_Up_Anim.png"));
    ImagePattern walkUp = new ImagePattern(new Image("Walk_Up_Anim.png"));
    ImagePattern[] playerUpAnim = {idleUp, walkUp};

    ImagePattern idleDown = new ImagePattern(new Image("Idle_Down_Anim.png"));
    ImagePattern walkDown = new ImagePattern(new Image("Walk_Down_Anim.png"));
    ImagePattern[] playerDownAnim = {idleDown, walkDown};

}
