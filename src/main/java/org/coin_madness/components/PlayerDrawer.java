package org.coin_madness.components;

import javafx.animation.*;
import javafx.geometry.Bounds;
import javafx.scene.Group;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.util.Duration;
import org.coin_madness.helpers.EntitySprites;
import org.coin_madness.helpers.PlayerSprites;
import org.coin_madness.helpers.ImageLibrary;
import org.coin_madness.model.EntityMovement;
import org.coin_madness.model.MovableEntity;
import org.coin_madness.model.Player;

import java.util.HashMap;
import java.util.List;

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
    public void draw(MovableEntity entity, ImageView view) {
        EntityMovement movement = entity.getEntityMovement();
        int spriteId = entity.getSpriteId();
        EntitySprites sprites = graphics.getSprites(spriteId);


        if(!entity.isAlive()) {
            view.setImage(graphics.tombstone);
            AnimationState removeAnimation = getAnimation(entity.getId());
            // stop the animation, since the player is now dead
            removeAnimation.translateTransition.stop();
            removeAnimation.timeline.stop();
            removeAnimation.imageView.setImage(null);
            return;
        }

        //Handling
        if(movement == null) {
            view.setImage(sprites.getDownIdle());
            return;
        }
        AnimationState animationState = getAnimation(entity.getId());

        if(!movement.equals(animationState.currentMovement)) {
            animationState.playAnim(findAnimation(movement, sprites), movement, view);
        }

        if(animationState.currentMovement != null) {
            view.setImage(null);
        }
    }

    private List<Image> findAnimation(EntityMovement movement, EntitySprites sprites) {
        if(movement.getDeltaX() > 0) {
            return sprites.rightAnimation();
        } else if(movement.getDeltaX() < 0) {
            return sprites.leftAnimation();
        } else if (movement.getDeltaY() > 0) {
            return sprites.downAnimation();
        } else {
            return sprites.upAnimation();
        }
    }

    private class AnimationState {
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

        public void playAnim(List<Image> playerAnim, EntityMovement movement, ImageView view) {
            this.currentMovement = movement;
            timeline.getKeyFrames().clear();
            timeline.getKeyFrames().add(new KeyFrame(Duration.millis(movement.getDuration() / FRAMES), actionEvent -> {
                imageView.setImage(playerAnim.get(keyCount));
                keyCount = (keyCount + 1) % playerAnim.size();
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
            imageView.setImage(playerAnim.get(0));
            imageView.setVisible(true);

            translateTransition.setOnFinished(actionEvent -> {
                timeline.stop();
                view.setImage(playerAnim.get(0));
                keyCount = 0;
                imageView.setVisible(false);
                movement.finish();
                this.currentMovement = null;
            });

            translateTransition.setInterpolator(Interpolator.LINEAR);

            timeline.playFromStart();
            translateTransition.playFromStart();
        }
    }

}
