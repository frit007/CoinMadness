package org.coin_madness.components;

import javafx.animation.KeyFrame;
import javafx.animation.Timeline;
import javafx.animation.TranslateTransition;
import javafx.scene.paint.ImagePattern;
import javafx.scene.shape.Rectangle;
import javafx.util.Duration;
import org.coin_madness.helpers.ImageLibrary;
import org.coin_madness.model.Player;

public class PlayerComponent extends Rectangle {

    private Player player;
    private static final int WALK_ANIM_DURATION = 200;
    private TranslateTransition translateTransition;
    private Timeline timeline;
    private int keyCount = 1;

    public PlayerComponent(Player player, ImageLibrary graphics, double tileSize) {
        this.player = player;
        setTileSize(tileSize);
        setFill(graphics.idleRight);

        setX(player.getX() * tileSize);
        setY(player.getY() * tileSize);

        timeline = new Timeline();
        timeline.setCycleCount(Timeline.INDEFINITE);
        translateTransition = new TranslateTransition();
    }

    public void setTileSize(double tileSize) {
        setWidth(tileSize);
        setHeight(tileSize);
        setX(player.getX() * tileSize);
        setY(player.getY() * tileSize);
    }

    public void walkAnim(double moveByX, double moveByY, ImagePattern[] playerAnim, Runnable callback) {
        timeline.getKeyFrames().clear();

        timeline.getKeyFrames().add(new KeyFrame(Duration.millis(WALK_ANIM_DURATION / player.getMovementSpeed()), actionEvent -> {
            setFill(playerAnim[keyCount]);
            keyCount = (keyCount + 1) % playerAnim.length;
        }));

        translateTransition.setNode(this);
        translateTransition.setDuration(Duration.millis(1000 / player.getMovementSpeed()));
        translateTransition.setByX(moveByX);
        translateTransition.setByY(moveByY);
        translateTransition.setOnFinished(actionEvent -> {
            timeline.stop();
            setFill(playerAnim[0]);
            keyCount = 1;
            // TODO there might be a race condition here if the tile size is set at the same time
            setY(getY() + getTranslateY());
            setX(getX() + getTranslateX());
            setTranslateX(0);
            setTranslateY(0);
            callback.run();
        });

        timeline.playFromStart();
        translateTransition.playFromStart();
    }

}
