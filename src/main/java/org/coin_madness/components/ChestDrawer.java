package org.coin_madness.components;

import javafx.animation.Animation;
import javafx.animation.Interpolator;
import javafx.animation.Timeline;
import javafx.animation.TranslateTransition;
import javafx.geometry.Bounds;
import javafx.scene.Group;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.util.Duration;
import org.coin_madness.helpers.ImageLibrary;
import org.coin_madness.model.Chest;

import java.util.HashMap;

public class ChestDrawer implements Drawer<Chest> {
    private ImageLibrary graphics;
    Group container;
    private HashMap<String, AnimationState> chestAnimations = new HashMap<>();
    private static final int DURATION = 500;
    private static final int OFFSET = 5;

    public ChestDrawer(ImageLibrary graphics, Group container) {
        this.graphics = graphics;
        this.container = container;
    }

    public AnimationState getAnimation(String id) {
        if(!chestAnimations.containsKey(id)) {
            TranslateTransition translateTransition = new TranslateTransition();
            ImageView imageView = new ImageView();
            chestAnimations.put(id, new AnimationState(translateTransition, imageView));
        }
        return chestAnimations.get(id);
    }

//    @Override
//    public void draw(Chest entity, ImageView view) {
//        view.setImage(graphics.chest);
//    }

    @Override
    public void draw(Chest chest, ImageView view) {
        view.setImage(graphics.chest);
        AnimationState animationState = getAnimation(chest.getX() + ":" + chest.getY()); //enough?

        if(chest.hasPendingAnimation() && !animationState.isRunningAnimation()) {
            animationState.playAnim(chest, graphics.coin, view);
        }

    }

    private class AnimationState {
        private TranslateTransition translateTransition;
        private ImageView imageView;
        private Chest animationTarget;

        public boolean isRunningAnimation() {
            return animationTarget != null;
        }

        public AnimationState(TranslateTransition translateTransition, ImageView imageView) {
            this.imageView = imageView;
            this.translateTransition = translateTransition;
            imageView.setVisible(false);
            container.getChildren().add(imageView);
        }

        public void playAnim(Chest chest, Image coin, ImageView view) {
            animationTarget = chest;
            translateTransition.setNode(imageView);
            translateTransition.setDuration(Duration.millis(DURATION));

            Bounds parentBounds = container.localToScene(container.getBoundsInLocal());
            Bounds viewBounds = view.localToScene(view.getBoundsInLocal());

            imageView.setFitWidth(view.getFitWidth() / 2);
            imageView.setFitHeight(view.getFitHeight() / 2);

            double viewPosX = viewBounds.getMinX() - parentBounds.getMinX();
            double viewPosY = viewBounds.getMinY() - parentBounds.getMinY();

            translateTransition.setFromX(viewPosX + view.getFitWidth() / 2 - imageView.getFitWidth() / 2);
            translateTransition.setFromY(viewPosY - OFFSET);
            translateTransition.setToY(viewPosY + OFFSET);
            imageView.setImage(coin);
            imageView.setVisible(true);

            translateTransition.setOnFinished(actionEvent -> {
                imageView.setVisible(false);

                chest.takePendingAnimation();

                animationTarget = null;

                if(chest.hasPendingAnimation()) {
                    this.playAnim(chest, graphics.coin, view);
                }
            });

            translateTransition.setInterpolator(Interpolator.LINEAR);
            translateTransition.playFromStart();
        }

    }

}
