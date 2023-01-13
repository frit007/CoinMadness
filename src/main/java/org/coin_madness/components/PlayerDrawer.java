package org.coin_madness.components;

import javafx.animation.*;
import javafx.geometry.Bounds;
import javafx.scene.Group;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.util.Duration;
import org.coin_madness.helpers.ImageLibrary;
import org.coin_madness.model.EntityMovement;
import org.coin_madness.model.MovableEntity;

import java.util.HashMap;

public class PlayerDrawer implements Drawer<MovableEntity> {
    private ImageLibrary graphics;
    private HashMap<Integer, AnimationState> playerAnimations = new HashMap<>();
    Group container;
    public PlayerDrawer(ImageLibrary graphics, Group container) {
        this.graphics = graphics;
        this.container = container;
    }

    public AnimationState getAnimation(int id) {
        if(!playerAnimations.containsKey(id)) {
            Timeline timeline = new Timeline();
            timeline.setCycleCount(Animation.INDEFINITE);
            TranslateTransition translateTransition = new TranslateTransition();
            ImageView imageView = new ImageView();
            playerAnimations.put(id, new AnimationState(timeline, translateTransition, imageView));
        }
        return playerAnimations.get(id);
    }

    @Override
    //TODO: different color for networkPlayers? enemies?
    public void draw(MovableEntity player, ImageView view) {
        EntityMovement movement = player.getEntityMovement();
        if(movement == null) {
            view.setImage(graphics.idleDown);
            return;
        }

        AnimationState animationState = getAnimation(player.getId());

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

        public AnimationState(Timeline timeline, TranslateTransition translateTransition, ImageView imageView) {
            this.imageView = imageView;
            this.timeline = timeline;
            this.translateTransition = translateTransition;
            imageView.setVisible(false);
            container.getChildren().add(imageView);
        }

        public void playAnim(Image[] playerAnim, EntityMovement movement, ImageView view) {
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
                movement.finish();
            });

            translateTransition.setInterpolator(Interpolator.LINEAR);

            timeline.playFromStart();
            translateTransition.playFromStart();
        }
    }

}
