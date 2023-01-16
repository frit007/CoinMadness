package org.coin_madness.screens;

import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.image.ImageView;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Color;
import javafx.scene.text.Font;
import org.coin_madness.helpers.ImageLibrary;
import org.coin_madness.model.GameState;
import org.coin_madness.model.Player;

import java.util.function.Consumer;

public class EndScreen extends VBox {
    private final ImageLibrary graphics;
    GameState gameState;
    public EndScreen(GameState gameState, ImageLibrary graphics, Consumer<String> goToMainScreen) {
        this.gameState = gameState;
        this.graphics = graphics;

        setAlignment(Pos.CENTER);

        int score = 1337;
        Label scoreLabel = new Label("Score: " + score);
        scoreLabel.setTextFill(Color.BLACK);
        scoreLabel.setStyle("-fx-font: 40px arial;");
        scoreLabel.setPadding(new Insets(20,60,20,60));

        HBox playerScoreLabelBox = new HBox();
        playerScoreLabelBox.setAlignment(Pos.CENTER);
        ImageView localPlayerIcon = new ImageView();
        int spriteId = gameState.localPlayer.getSpriteId();
        localPlayerIcon.setImage(graphics.getSprites(spriteId).getDownIdle()); //TODO - Change for each local player
        localPlayerIcon.setFitHeight(60); localPlayerIcon.setFitWidth(60);
        playerScoreLabelBox.getChildren().addAll(localPlayerIcon, scoreLabel);


        this.getChildren().add(playerScoreLabelBox);

        for (Player player : gameState.allPlayers()) {
            getChildren().add(addPlayerScore(player));
        }

        Button continueButton = createButton("Continue");
        getChildren().add(continueButton);
        continueButton.setOnMouseClicked(mouse -> {
            goToMainScreen.accept(null);
        });
    }

    private HBox addPlayerScore(Player player) {
        HBox playerBox = new HBox();
        playerBox.setAlignment(Pos.CENTER);
        ImageView playerIcon = new ImageView();

        int spriteId = player.getSpriteId();
        playerIcon.setImage(graphics.getSprites(spriteId).getDownIdle());
        playerIcon.setFitWidth(30);
        playerIcon.setFitHeight(30);

        Label playerScore = new Label();
        //TODO - Get player score and set different icons for the different players
        playerScore.setText(player.getAmountOfCoins() + " coins");
        playerScore.setFont(new Font("Arial",25));


        playerBox.getChildren().addAll(playerIcon, playerScore);

        return playerBox;
    }


    private Button createButton(String text) {
        Button button = new Button(text);
        button.setPadding(new Insets(15,15,15,15));
        button.setStyle("-fx-font-size: 20px;-fx-background-color: #4447fe; ");
        button.setTextFill(Color.WHITE);

        return button;
    }


}
