package org.coin_madness.components;

import javafx.fxml.FXMLLoader;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.Label;
import javafx.scene.image.ImageView;
import javafx.scene.layout.*;
import javafx.scene.paint.Color;
import org.coin_madness.helpers.ImageLibrary;
import org.coin_madness.model.Player;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

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
                updatePlayerUI();
            } catch (IOException e) {
                e.printStackTrace();
            }
            playerImage.setPreserveRatio(true);
            playerImage.setFitHeight(25);

            int spriteId = player.getSpriteId();
            playerImage.setImage(graphics.getSprites(spriteId).getDownIdle());


            int index = 0;
            while (index < playerUIs.size()) {
                if(player.getId() < playerUIs.get(index).player.getId()) {
                    break;
                }
                index++;
            }
            // make sure the players are drawn in the same order on everyones screen
            playerUIs.add(index, this);
            root.getChildren().add(index + 1, playerMenu);

            player.addOnUpdate(this::updatePlayerUI);

        }

        private void updatePlayerUI() {
            coins.getChildren().clear();
            if(!player.isAlive()){
                playerImage.setImage(graphics.tombstone);
            }
            int coinLimit = Player.COIN_LIMIT;
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
    List<PlayerUI> playerUIs = new ArrayList<>();

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
