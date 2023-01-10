package org.coin_madness.components;

import javafx.animation.Animation;
import javafx.animation.KeyFrame;
import javafx.animation.Timeline;
import javafx.animation.TranslateTransition;
import javafx.geometry.Bounds;
import javafx.scene.Group;
import javafx.scene.Scene;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.BorderPane;
import javafx.util.Duration;
import org.coin_madness.helpers.ImageLibrary;
import org.coin_madness.model.EntityMovement;
import org.coin_madness.model.Player;

import java.util.HashMap;

public class PlayerDrawer implements Drawer<Player> {
    private String[] playerIds;
    private ImageLibrary graphics;
    private HashMap<String, AnimationState> playerAnimations = new HashMap<>();
    Group container;
    public PlayerDrawer(String[] playerIds, ImageLibrary graphics, Group container) {
        this.graphics = graphics;
        this.container = container;

        for (String id : playerIds) {
            Timeline timeline = new Timeline();
            TranslateTransition translateTransition = new TranslateTransition();
            timeline.setCycleCount(Animation.INDEFINITE);
            AnimationState animationState = new AnimationState(timeline, translateTransition);
            playerAnimations.put(id, animationState);
        }
    }

    @Override
    public void draw(Player player, ImageView view) {
        EntityMovement movement = player.getEntityMovement();
        if(movement == null) {
            view.setImage(graphics.idleDown);
            return;
        }

        AnimationState animationState = playerAnimations.get(player.getId());

        if(animationState.currentMovement != movement) {
            animationState.playAnim(findAnimation(movement), movement, view);
        }
    }

    public Image[] findAnimation(EntityMovement movement) {
        if(movement.getDeltaX() > 0) {
            return graphics.playerRightAnim;
        } else if(movement.getDeltaX() < 0) {
            return graphics.playerLeftAnim;
        } else if (movement.getDeltaY() > 0) {
            return graphics.playerDownAnim;
        } else {
            return graphics.playerUpAnim;
        }
    }

    public class AnimationState {
        private Timeline timeline;
        private int keyCount;
        private TranslateTransition translateTransition;
        private EntityMovement currentMovement = null;
        private ImageView imageView;
        private static final int FRAMES = 4;

        public AnimationState(Timeline timeline, TranslateTransition translateTransition) {
            this.timeline = timeline;
            this.translateTransition = translateTransition;
        }

        public void playAnim(Image[] playerAnim, EntityMovement movement, ImageView view) {
            if(imageView == null) {
                imageView = new ImageView();
                container.getChildren().add(imageView);
            }
            timeline.getKeyFrames().clear();
            timeline.getKeyFrames().add(new KeyFrame(Duration.millis(movement.getDuration() / FRAMES), actionEvent -> {
                imageView.setImage(playerAnim[keyCount]);
                keyCount = (keyCount + 1) % playerAnim.length;
            }));

            translateTransition.setNode(imageView);
            translateTransition.setDuration(Duration.millis(movement.getDuration()));

            Bounds parentBounds = container.localToScene(container.getBoundsInLocal());
            Bounds viewBounds = view.localToScene(view.getBoundsInLocal());

            imageView.setFitWidth(view.getFitWidth());
            imageView.setFitHeight(view.getFitHeight());

            double viewPosX = viewBounds.getMinX() - parentBounds.getMinX();
            double viewPosY = viewBounds.getMinY() - parentBounds.getMinY();

            translateTransition.setFromX(-movement.getDeltaX() * view.getFitWidth() + viewPosX);
            translateTransition.setFromY(-movement.getDeltaY() * view.getFitWidth() + viewPosY);

            translateTransition.setByX(movement.getDeltaX() * view.getFitWidth());
            translateTransition.setByY(movement.getDeltaY() * view.getFitWidth());
            imageView.setImage(playerAnim[0]);
            imageView.setVisible(true);

            translateTransition.setOnFinished(actionEvent -> {
                timeline.stop();
                view.setImage(playerAnim[0]);
                keyCount = 1;
                imageView.setVisible(false);
            });

            timeline.playFromStart();
            translateTransition.playFromStart();
        }
    }

}
