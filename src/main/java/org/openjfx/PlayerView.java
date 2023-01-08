package org.openjfx;

import javafx.animation.KeyFrame;
import javafx.animation.Timeline;
import javafx.animation.TranslateTransition;
import javafx.scene.paint.ImagePattern;
import javafx.scene.shape.Rectangle;
import javafx.util.Duration;

public class PlayerView extends Rectangle {

    private Player player;
    private static final int WALK_ANIM_DURATION = 200;
    private TranslateTransition translateTransition;
    private Timeline timeline;
    private int keyCount = 1;

    public PlayerView(Player player, ImageLibrary graphics, double tileSize) {
        this.player = player;
        setSideLength(tileSize);
        setFill(graphics.idleRight);

        timeline = new Timeline();
        timeline.setCycleCount(Timeline.INDEFINITE);
        translateTransition = new TranslateTransition();
    }

    public void setSideLength(double sideLength) {
        setWidth(sideLength);
        setHeight(sideLength);
    }

    public void walkAnim(double moveByX, double moveByY, ImagePattern[] playerAnim, Runnable movement) {
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
            movement.run();
        });

        timeline.playFromStart();
        translateTransition.playFromStart();
    }

}
