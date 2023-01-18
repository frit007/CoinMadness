package org.coin_madness.helpers;

import javafx.scene.image.Image;
import org.coin_madness.model.EntityMovement;

public class ImageLibrary {

    // ----- Maze -----
    public Image ground = new Image("Grass.png");
    public Image wall = new Image("Brick.png");
    public Image coin = new Image("Coin.png");
    public Image traphole = new Image("Traphole.png");
    public Image tombstone = new Image("tombstone.png");

    // ----- Chests -----
    public Image woodChestClosed = new Image("WoodChest_Closed.png");
    public Image woodChestOpened = new Image("WoodChest_Opened.png");
    public Image silverChestClosed = new Image("SilverChest_Closed.png");
    public Image silverChestOpened = new Image("SilverChest_Opened.png");
    public Image goldChestClosed = new Image("GoldChest_Closed.png");
    public Image goldChestOpened = new Image("GoldChest_Opened.png");

    public Image[] woodChest = {woodChestClosed, woodChestOpened};
    public Image[] silverChest = {silverChestClosed, silverChestOpened};
    public Image[] goldChest = {goldChestClosed, goldChestOpened};

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
