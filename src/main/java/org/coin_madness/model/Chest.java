package org.coin_madness.model;

public class Chest extends StaticEntity implements CollidesWithPlayer {

    private ChestClient chestClient;
    private static final int MAX_COINS = 3;
    private int amountOfCoins = 0;
    private int pendingCoinAnimations = 0;
    private Runnable onPendingCoinAnimationDone;
    public Chest(int x, int y, ChestClient chestClient) {
        super(x, y);
        this.chestClient = chestClient;
    }

    public int getMaxCoins() {
        return MAX_COINS;
    }

    public int getAmountOfCoins() {
        return amountOfCoins;
    }

    public void setAmountOfCoins(int amountOfCoins) {
        pendingCoinAnimations += amountOfCoins - this.amountOfCoins;
        this.amountOfCoins = amountOfCoins;
        sendUpdates();
    }

    // used for removing the chest after the animation is done
    public void addOnPendingAnimationDone(Runnable onPendingCoinAnimationDone) {
        if(pendingCoinAnimations == 0 && isFull()) {
            onPendingCoinAnimationDone.run();
        } else {
            this.onPendingCoinAnimationDone = onPendingCoinAnimationDone;
        }
    }

    public int getPendingCoinAnimations() {
        return pendingCoinAnimations;
    }

    @Override
    public void onPlayerCollision(Player player) {
        chestClient.placeCoins(this, player);
    }
    public boolean hasPendingAnimation() {
        return pendingCoinAnimations > 0;
    }

    public boolean isFull() {
        return getAmountOfCoins() == getMaxCoins();
    }
    public boolean takePendingAnimation() {
        if(hasPendingAnimation()) {
            pendingCoinAnimations--;
            if(pendingCoinAnimations == 0 && isFull() && onPendingCoinAnimationDone != null) {
                onPendingCoinAnimationDone.run();
            }

            return true;
        }
        return false;
    }
}
