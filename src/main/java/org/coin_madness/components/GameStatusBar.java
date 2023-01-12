package org.coin_madness.components;

import javafx.fxml.FXMLLoader;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.control.Label;
import javafx.scene.image.ImageView;
import javafx.scene.layout.*;
import javafx.scene.paint.Color;
import org.coin_madness.helpers.ImageLibrary;
import org.coin_madness.model.Player;

import java.io.IOException;

public class GameStatusBar extends HBox {

    private ImageLibrary graphics;

    public HBox root;
    Label score;

    public class PlayerUI {
        Player player;
        HBox playerMenu;

        HBox coins;
        ImageView playerImage;
        public PlayerUI(Player player) {
            this.player = player;
            try {
                playerMenu = FXMLLoader.load(Thread.currentThread().getContextClassLoader().getResource("player_ui.fxml"));
                coins = (HBox) playerMenu.lookup("#coins");
                playerImage = (ImageView) playerMenu.lookup("#player_image");
                updateCoins();
            } catch (IOException e) {
                e.printStackTrace();
            }

            playerImage.setPreserveRatio(true);
            playerImage.setFitHeight(25);

            root.getChildren().add(playerMenu);
            player.addOnUpdate(this::updateCoins);
        }

        private void updateCoins() {
            coins.getChildren().clear();

            int coinLimit = 4; // TODO use a global coin limit to make it easier to change it
            for (int i = 0; i < coinLimit; i++) {
                ImageView view = new ImageView();
                view.setFitWidth(30);
                view.setFitHeight(30);
                if(i < player.getAmountOfCoins()) {
                    view.setImage(graphics.coin);
                } else {
                    view.setImage(graphics.coinSlot);
                }
                coins.getChildren().add(view);
            }
        }

    }

    public GameStatusBar(ImageLibrary graphics) {
        setBackground(new Background(new BackgroundFill(Color.LIGHTGRAY,
                CornerRadii.EMPTY,
                Insets.EMPTY)));
        this.graphics = graphics;
        try {
            root = FXMLLoader.load(Thread.currentThread().getContextClassLoader().getResource("status_bar.fxml"));
            score = (Label) root.lookup("#score");
            // TODO listen for score updates
        } catch (IOException e) {
            e.printStackTrace();
        }

        HBox.setHgrow(root, Priority.ALWAYS);
        HBox.setHgrow(this, Priority.ALWAYS);

        setAlignment(Pos.CENTER);
        getChildren().add(root);
    }

    public void addPlayer(Player player) {
        new PlayerUI(player);
    }
}
