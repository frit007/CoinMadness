package org.coin_madness.model;

public class Traphole extends StaticEntity implements CollidesWithPlayer {

    public Traphole(int x, int y) {
        super(x, y);
    }

    @Override
    public void onPlayerColission(Player player) {
        System.out.println("Hit Traphole!");
    }

}
