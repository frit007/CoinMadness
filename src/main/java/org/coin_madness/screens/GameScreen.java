package org.coin_madness.screens;

import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Group;
import javafx.scene.Scene;
import javafx.scene.control.ScrollPane;
import javafx.scene.layout.*;
import javafx.scene.paint.Color;
import javafx.stage.Stage;
import org.coin_madness.components.MazeFieldComponent;
import org.coin_madness.components.PlayerComponent;
import org.coin_madness.controller.GameController;
import org.coin_madness.helpers.ConnectionManager;
import org.coin_madness.helpers.ImageLibrary;
import org.coin_madness.model.Field;
import org.coin_madness.model.Player;

import java.util.ArrayList;

public class GameScreen extends Group {

    public static final Color BACKGROUND = Color.GRAY;
    private ScrollPane scrollPane;
    private GridPane mapView;
    private double tileSize;
    ArrayList<MazeFieldComponent> views = new ArrayList<>();
    private GameController gameController;

    public GameScreen(Stage stage, Scene scene, Player player, Field[][] map, ImageLibrary graphics, ConnectionManager connectionManager) {

        PlayerComponent playerComponent = new PlayerComponent(player, graphics, tileSize);
        gameController = new GameController(player, playerComponent, tileSize, scene, graphics, connectionManager, this);

        mapView = new GridPane();
        mapView.setAlignment(Pos.CENTER);
        mapView.setSnapToPixel(false);
        mapView.setBackground(new Background(new BackgroundFill(BACKGROUND, CornerRadii.EMPTY, Insets.EMPTY)));
        for (Field[] row : map) {
            for (Field field : row) {
                MazeFieldComponent fieldView = new MazeFieldComponent(field, graphics);
                fieldView.setSideLength(tileSize);
                fieldView.updateView();
                views.add(fieldView);
                mapView.add(fieldView, field.getX(), field.getY());
            }
        }

        scrollPane = new ScrollPane(mapView);
        scrollPane.getStyleClass().add("edge-to-edge");
        scrollPane.setFitToWidth(true);
        scrollPane.setFitToHeight(true);
        scrollPane.setSnapToPixel(false);
        scrollPane.setHbarPolicy(ScrollPane.ScrollBarPolicy.NEVER);
        scrollPane.setVbarPolicy(ScrollPane.ScrollBarPolicy.NEVER);

        getChildren().add(scrollPane);
        getChildren().add(playerComponent);
        resize(scene.getHeight(), map.length, player, playerComponent);

        stage.heightProperty().addListener((obs, oldVal, newVal) -> {
            resize(scene.getHeight(), map.length, player, playerComponent);
            stage.setWidth((tileSize + 1) * map[0].length);
        });

    }

    private void resize(Double sceneHeight, int mazeRows, Player player, PlayerComponent playerComponent) {
        tileSize = Math.floor(sceneHeight / mazeRows);
        for (MazeFieldComponent view : views) {
            view.setSideLength(tileSize);
        }
        playerComponent.setTileSize(tileSize);
        gameController.setTileSize(tileSize);

        // TODO I just removed this because I'm not sure what the point is (I moved some of the functionality into playerComponent)
        /*
            double origin = tileSize / 2 - playerComponent.getWidth() / 2;
            double cellWidth = mapView.getCellBounds(0,0).getWidth();
            double cellHeight = mapView.getCellBounds(0,0).getHeight();
            playerComponent.setX(origin + player.getX() * cellWidth);
            playerComponent.setY(origin + player.getY() * cellHeight);
        */
    }

}
