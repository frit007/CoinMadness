package org.coin_madness.helpers;

import javafx.scene.image.Image;
import org.coin_madness.model.EntityMovement;

public class ImageLibrary {

    // ----- Maze -----
    public Image ground = new Image("Grass.png");
    public Image wall = new Image("Brick_OLD.png");
    public Image coin = new Image("Coin.png");
    public Image chest = new Image("Chest.png");
    public Image traphole = new Image("Traphole.png");

    // ----- Player -----
//    public Image idleRight = new Image("Idle_Right_Anim.png");
//    public Image walkRight = new Image("Walk_Right_Anim.png");
//    public Image[] playerRightAnim = {idleRight, walkRight};
//
//    public Image idleLeft = new Image("Idle_Left_Anim.png");
//    public Image walkLeft = new Image("Walk_Left_Anim.png");
//    public Image[] playerLeftAnim = {idleLeft, walkLeft};
//
//    public Image idleUp = new Image("Idle_Up_Anim.png");
//    public Image walkUp = new Image("Walk_Up_Anim.png");
//    public Image[] playerUpAnim = {idleUp, walkUp};
//
//    public Image idleDown = new Image("Idle_Down_Anim.png");
//    public Image walkDown = new Image("Walk_Down_Anim.png");
//    public Image[] playerDownAnim = {idleDown, walkDown};

    public Image tombstone = new Image("tombstone.png");

    // ---- Player UI ----
    public Image coinSlot = new Image("Coin_Slot.png");

    public static final int ENEMY_SPRITE_ID = 4;

    private EntitySprites[] sprites = new EntitySprites[] {
            new PlayerSprites("Player1/"),
            new PlayerSprites("Player2/"),
            new PlayerSprites("Player3/"),
            new PlayerSprites("Player4/"),
            new EnemySprites(),
    };

    public EntitySprites getSprites(int spriteId) {
        return sprites[spriteId];
    }

}
