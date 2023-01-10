package org.coin_madness.helpers;

import javafx.scene.image.Image;
import javafx.scene.paint.ImagePattern;

public class ImageLibrary {

    // ----- Maze -----
    public Image ground = new Image("Grass.png");
    public Image wall = new Image("Brick.png");
    public Image coin = new Image("Coin.png");
    public Image chest = new Image("Chest.png");
    public Image traphole = new Image("Traphole.png");

    // ----- Player -----
    public Image idleRight = new Image("Idle_Right_Anim.png");
    public Image walkRight = new Image("Walk_Right_Anim.png");
    public Image[] playerRightAnim = {idleRight, walkRight};

    public Image idleLeft = new Image("Idle_Left_Anim.png");
    public Image walkLeft = new Image("Walk_Left_Anim.png");
    public Image[] playerLeftAnim = {idleLeft, walkLeft};

    public Image idleUp = new Image("Idle_Up_Anim.png");
    public Image walkUp = new Image("Walk_Up_Anim.png");
    public Image[] playerUpAnim = {idleUp, walkUp};

    public Image idleDown = new Image("Idle_Down_Anim.png");
    public Image walkDown = new Image("Walk_Down_Anim.png");
    public Image[] playerDownAnim = {idleDown, walkDown};

}
