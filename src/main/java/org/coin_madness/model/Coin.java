package org.coin_madness.model;

public class Coin extends StaticEntity implements CollidesWithPlayer {

    private CoinClient coinClient;
    public Coin(int x, int y, CoinClient coinClient) {
        super(x, y);
        this.coinClient = coinClient;
    }

    @Override
    public void onPlayerColission(Player player) {
        if(player.getAmountOfCoins() < Player.COIN_LIMIT) {
            coinClient.request(this, () -> player.setAmountOfCoins(player.getAmountOfCoins() + 1), () -> {});
        }
    }
}
