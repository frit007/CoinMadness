package org.coin_madness.model;

public class Coin extends StaticEntity implements CollidesWithPlayer {

    public Coin(int x, int y) {
        super(x, y);
    }

    @Override
    public void onPlayerColission(Player player) {
        player.getCoinClient().request(this, () -> player.setAmountOfCoins(player.getAmountOfCoins() + 1), () -> {});
    }
}
