package org.coin_madness.model;

public class Chest extends StaticEntity implements CollidesWithPlayer {

    public Chest(int x, int y) {
        super(x, y);
    }

    @Override
    public void onPlayerColission(Player player) {
        System.out.println("Hit Chest!");
    }
}
