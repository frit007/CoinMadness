package org.coin_madness.model;

public class Coin extends StaticEntity implements CollidesWithPlayer {

    private StaticEntityClient<Coin> coinClient;
    public Coin(int x, int y, StaticEntityClient<Coin> coinClient) {
        super(x, y);
        this.coinClient = coinClient;
    }

    @Override
    public void onPlayerColission(Player player) {
        coinClient.request(this, () -> player.setAmountOfCoins(player.getAmountOfCoins() + 1), () -> {});
    }
}
