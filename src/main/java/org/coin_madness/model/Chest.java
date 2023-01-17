package org.coin_madness.model;

import org.coin_madness.helpers.Action;

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
        System.out.println("add pending???");
        if(pendingCoinAnimations == 0 && isFull()) {
            System.out.println("One?");
            onPendingCoinAnimationDone.run();
        } else {
            System.out.println("Two?");
            this.onPendingCoinAnimationDone = onPendingCoinAnimationDone;
            System.out.println("(after 2)has pending " + ((onPendingCoinAnimationDone != null ? "not null" : "null" )));
        }
        System.out.println(this);
    }

    @Override
    public String toString() {
        return "Chest{" +
                "chestClient=" + chestClient +
                ", amountOfCoins=" + amountOfCoins +
                ", pendingCoinAnimations=" + pendingCoinAnimations +
                ", onPendingCoinAnimationDone=" + onPendingCoinAnimationDone +
                ", x=" + x +
                ", y=" + y +
                '}';
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
            System.out.println("close");
            System.out.println(this);
            System.out.println("has pending " + ((onPendingCoinAnimationDone != null ? "not null" : "null" )));
            if(pendingCoinAnimations == 0 && isFull() && onPendingCoinAnimationDone != null) {
                onPendingCoinAnimationDone.run();
            }

            return true;
        }
        return false;
    }
}
