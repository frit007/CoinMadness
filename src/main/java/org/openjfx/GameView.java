package org.openjfx;

import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Group;
import javafx.scene.Scene;
import javafx.scene.control.ScrollPane;
import javafx.scene.layout.*;
import javafx.scene.paint.Color;
import javafx.stage.Stage;

import java.util.ArrayList;

public class GameView extends Group {

    public static final Color BACKGROUND = Color.GRAY;
    private ScrollPane scrollPane;
    private GridPane mapView;
    private double tileSize;
    ArrayList<MazeFieldView> views = new ArrayList<>();
    private GameController gameController;

    public GameView(Stage stage, Scene scene, Player player, Field[][] map, ImageLibrary graphics) {

        PlayerView playerView = new PlayerView(player, graphics, tileSize);
        gameController = new GameController(player, playerView, tileSize, scene, graphics);

        mapView = new GridPane();
        mapView.setAlignment(Pos.CENTER);
        mapView.setSnapToPixel(false);
        mapView.setBackground(new Background(new BackgroundFill(BACKGROUND, CornerRadii.EMPTY, Insets.EMPTY)));
        for (Field[] row : map) {
            for (Field field : row) {
                MazeFieldView fieldView = new MazeFieldView(field, graphics);
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
        getChildren().add(playerView);

        stage.heightProperty().addListener((obs, oldVal, newVal) -> {
            resize(scene.getHeight(), map.length, player, playerView);
            stage.setWidth((tileSize + 1) * map[0].length);
        });

    }

    private void resize(Double sceneHeight, int mazeRows, Player player, PlayerView playerView) {
        tileSize = Math.floor(sceneHeight / mazeRows);
        for (MazeFieldView view : views) {
            view.setSideLength(tileSize);
        }
        playerView.setSideLength(tileSize);
        gameController.setTileSize(tileSize);

        double origin = tileSize / 2 - playerView.getWidth() / 2;
        double cellWidth = mapView.getCellBounds(0,0).getWidth();
        double cellHeight = mapView.getCellBounds(0,0).getHeight();
        playerView.setX(origin + player.getX() * cellWidth);
        playerView.setY(origin + player.getY() * cellHeight);
    }

}
